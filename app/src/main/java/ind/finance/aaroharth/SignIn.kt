package ind.finance.aaroharth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ind.finance.aaroharth.databinding.ActivitySignInBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding

class SignIn : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loadingDialog: Dialog
    private lateinit var dialogBinding: DialogScreenBinding

    companion object {
        const val RC_GOOGLE_SIGN_IN = 1001
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isRunningTest() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bgImage.setRenderEffect(blur)
        }





        // ---------- GOOGLE SIGN-IN BUTTON ----------
        binding.googleSignInButton.setOnClickListener {
            if(isInternetAvailable(this)){
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                googleSignInClient = GoogleSignIn.getClient(this, gso)
                startActivityForResult(
                    googleSignInClient.signInIntent,
                    RC_GOOGLE_SIGN_IN
                )
            }
            else{
                nointernetavailble("No Connection Availble")
            }
        }
        binding.btnSignIn.setOnClickListener {
            val email=binding.emailEntryField.text.toString().trim()
            val password=binding.passwordEntryField.text.toString().trim()
           if(isInternetAvailable(this)){
               if(email.isEmpty()){
                   dialogWarning("Email Cannot Be Empty")
               }
               else if(password.isEmpty()){
                   dialogWarning("Password Cannot Be Empty")
               }
               else{
                   signIn(email,password)
               }
            }
            else{
                nointernetavailble("No Connection Availble")

           }
        }

        // ---------- GO TO SIGN UP ----------
        binding.signinSignupButton.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
        val text= "Don't Have An Account? Sign Up"
        val spannable= SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.signin)),
            23,30,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.signinSignupButton.text=spannable
    }

    private fun signIn(email: String, password: String) {
        showLoading("Signing In")

        Idling.bridge.increment()

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        loadingDialog.dismiss()
                        dialogFailed("User not found")
                        Idling.bridge.decrement()   // ✅ EXIT PATH
                        return@addOnCompleteListener
                    }

                    database = FirebaseDatabase.getInstance().getReference("Users")
                    database.child(uid).get()
                        .addOnSuccessListener {
                            loadingDialog.dismiss()

                            if (it.exists()) {
                                dialogSuccess("Sign In Successful \n Welcome")
                                getSharedPreferences("app_prefs",MODE_PRIVATE).edit().putBoolean("firstopen",false).apply()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this, MainActivity::class.java))
                                }, 2500)
                            } else {
                                dialogFailed("User Data Not Found")
                            }

                            Idling.bridge.decrement()   // ✅ FINAL SUCCESS
                        }
                        .addOnFailureListener {
                            loadingDialog.dismiss()
                            dialogFailed("Unable To Fetch User Data")
                            Idling.bridge.decrement()   // ✅ DB FAILURE
                        }

                } else {
                    loadingDialog.dismiss()
                    dialogFailed("Some Error Occurred")
                    Idling.bridge.decrement()           // ✅ AUTH FAILURE
                }
            }
    }

    // ---------- GOOGLE RESULT ----------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: Exception) {
                // optional: show dialog/toast
            }
        }
    }

    // ---------- FIREBASE AUTH ----------
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialogSuccess("Sign In Successfull \n Welcome")
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            startActivity(Intent(this, MainActivity::class.java))
                        },2500
                    )
                    finish()
                } else {
                    // optional: show error dialog
                }
            }
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showLoading(message: String) {
        loadingDialog = Dialog(this)
        dialogBinding = DialogScreenBinding.inflate(layoutInflater)
        loadingDialog.setContentView(dialogBinding.root)
        loadingDialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        setupLottie(dialogBinding.dialogLottie, "loading.json")
        dialogBinding.message.text = message
        loadingDialog.show()
    }

    private fun dialogWarning(message: String) = showDialog("DangerIcon.json", message)
    private fun dialogFailed(message: String) = showDialog("Failed.json", message)
    private fun dialogSuccess(message: String) = showDialog("Success.json", message)
    private fun nointernetavailble(message: String)=showDialog("nointernet.json",message)

    private fun showDialog(animation: String, message: String) {

        val dialog = Dialog(this)
        val db = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        setupLottie(db.dialogLottie, animation)
        db.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 2500)
    }
    private fun nointernet(message:String)=showDialog("nointernet.json",message)
    fun isRunningTest(): Boolean {
        return try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    private fun setupLottie(lottie: com.airbnb.lottie.LottieAnimationView, animation: String) {
        if (isRunningTest()) {
            lottie.cancelAnimation()
            lottie.progress = 1f
            lottie.visibility = android.view.View.GONE
        } else {
            lottie.setAnimation(animation)
            lottie.repeatCount = 0
            lottie.playAnimation()
        }
    }


}
