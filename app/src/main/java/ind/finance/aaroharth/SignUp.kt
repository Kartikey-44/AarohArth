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
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ind.finance.aaroharth.databinding.ActivitySignUpBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: DatabaseReference
    private lateinit var loadingDialog: Dialog
    private lateinit var dialogBinding: DialogScreenBinding

    companion object {
        const val RC_GOOGLE_SIGN_IN = 1001
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isRunningTest() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blur = RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            binding.bgImage.setRenderEffect(blur)
        }


        binding.googleSignUpButton.setOnClickListener {
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

        binding.signUpSignInButton.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEntryField.text.toString().trim()
            val password = binding.passwordEntryField.text.toString().trim()

            if (isInternetAvailable(this)) {
                checkCredentials(email, password)
            } else {
                dialogWarning("No Internet Connection")
            }
        }
    }

    // ---------------- GOOGLE SIGN-IN RESULT ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                dialogFailed("Google sign-in failed")
            }
        }
    }

    // ---------------- FIXED GOOGLE AUTH ----------------
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val authResult = task.result
                    val firebaseUser = authResult.user!!
                    val isNewUser =
                        authResult.additionalUserInfo?.isNewUser == true

                    if (isNewUser) {
                        saveGoogleUserToDatabase(firebaseUser)
                    }

                    dialogSuccess("Google Sign-In Successful")
                    getSharedPreferences("app_prefs",MODE_PRIVATE).edit().putBoolean("firstopen",false).apply()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }, 2500)

                } else {
                    handleGoogleError(task.exception)
                }
            }
    }

    private fun saveGoogleUserToDatabase(user: FirebaseUser) {
        database = FirebaseDatabase.getInstance().getReference("Users")

        val userData = user_detail(
            email = user.email ?: "",
            password = "GOOGLE_AUTH"
        )

        database.child(user.uid).setValue(userData)
    }

    private fun handleGoogleError(exception: Exception?) {
        exception?.printStackTrace()

        if (exception is FirebaseAuthException) {
            dialogFailed(
                "Code: ${exception.errorCode}\n${exception.message}"
            )
        } else {
            dialogFailed(exception?.toString() ?: "Unknown Google auth error")
        }
    }


    // ---------------- EMAIL SIGNUP (UNCHANGED) ----------------
    private fun checkCredentials(email: String, password: String) {
        when {
            email.isEmpty() ->
                dialogWarning("Email cannot be empty")

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                dialogWarning("Invalid email address")

            password.isEmpty() ->
                dialogWarning("Password cannot be empty")

            password.length < 6 ->
                dialogWarning("Password must be at least 6 characters")

            else ->
                signUp(email, password)
        }
    }

    private fun signUp(email: String, password: String) {
        showLoading("Signing Up")

        Idling.bridge.increment()

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        database = FirebaseDatabase.getInstance().getReference("Users")

                        val user = user_detail(email, password)

                        database.child(uid).setValue(user)
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                dialogSuccess("Signed Up Successfully")
                                getSharedPreferences("app_prefs",MODE_PRIVATE)
                                    .edit().putBoolean("firstopen",false).apply()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }, 2500)
                            }
                            .addOnFailureListener {
                                loadingDialog.dismiss()
                                dialogFailed("Database error")
                            }

                    } else {
                        loadingDialog.dismiss()
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            dialogWarning("User already exists\nPlease Sign In")
                        } else {
                            dialogFailed(task.exception?.message ?: "Signup failed")
                        }
                    }
                } finally {
                    // 🔥 GUARANTEED to run in ALL cases
                    Idling.bridge.decrement()
                }
            }
    }


    // ---------------- UTILS ----------------
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
    private fun nointernet(message: String)=showDialog("nointernet.json",message)
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
