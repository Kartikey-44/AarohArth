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

        applyBlurIfNeeded()
        setupUi()
    }

    private fun setupUi() {

        binding.googleSignUpButton.setOnClickListener {
            if (isInternetAvailable(this)) startGoogleSignIn()
            else showDialog("nointernet.json", "No internet connection")
        }

        binding.signUpSignInButton.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.emailEntryField.text.toString().trim()
            val password = binding.passwordEntryField.text.toString().trim()

            if (isInternetAvailable(this)) checkCredentials(email, password)
            else showDialog("nointernet.json", "No internet connection")
        }

        val text = "Already Have An Account? Sign In"
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.signin)),
            25, 32,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.signUpSignInButton.text = spannable
    }

    // ---------------- GOOGLE SIGN-IN ----------------

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                showDialog("Failed.json", "Google sign-in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result!!
                    val user = result.user!!

                    if (result.additionalUserInfo?.isNewUser == true) {
                        saveGoogleUserToDatabase(user)
                    }

                    onAuthSuccess()
                } else {
                    handleGoogleError(task.exception)
                }
            }
    }

    private fun saveGoogleUserToDatabase(user: FirebaseUser) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(user.uid).setValue(
            user_detail(user.email ?: "", "GOOGLE_AUTH")
        )
    }

    private fun handleGoogleError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            showDialog("Failed.json", exception.message ?: "Auth error")
        } else {
            showDialog("Failed.json", "Google authentication failed")
        }
    }

    // ---------------- EMAIL SIGN-UP ----------------

    private fun checkCredentials(email: String, password: String) {
        when {
            email.isEmpty() -> showDialog("DangerIcon.json", "Email cannot be empty")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                showDialog("DangerIcon.json", "Invalid email address")
            password.length < 6 ->
                showDialog("DangerIcon.json", "Password must be at least 6 characters")
            else -> signUp(email, password)
        }
    }

    private fun signUp(email: String, password: String) {
        showLoading("Signing up...")

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    database.child(uid).setValue(user_detail(email, password))
                        .addOnSuccessListener { onAuthSuccess() }
                        .addOnFailureListener {
                            showDialog("Failed.json", "Database error")
                        }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        showDialog("DangerIcon.json", "User already exists. Sign in.")
                    } else {
                        showDialog("Failed.json", task.exception?.message ?: "Signup failed")
                    }
                }
            }
    }

    // ---------------- SUCCESS HANDLER ----------------

    private fun onAuthSuccess() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .apply()

        showDialog("Success.json", "Signed in successfully")

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    // ---------------- UTILS ----------------

    private fun applyBlurIfNeeded() {
        if (!isRunningTest() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.bgImage.setRenderEffect(
                RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            )
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
        dialogBinding.dialogLottie.setAnimation("loading.json")
        dialogBinding.dialogLottie.playAnimation()
        dialogBinding.message.text = message
        loadingDialog.show()
    }

    private fun showDialog(animation: String, message: String) {
        val dialog = Dialog(this)
        val db = DialogScreenBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        db.dialogLottie.setAnimation(animation)
        db.dialogLottie.playAnimation()
        db.message.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 2000)
    }

    private fun isRunningTest(): Boolean =
        try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
}
