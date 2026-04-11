package ind.finance.aaroharth.authFragments

import android.app.Dialog
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import ind.finance.aaroharth.MainActivity
import ind.finance.aaroharth.R
import ind.finance.aaroharth.databinding.ActivitySignInBinding
import ind.finance.aaroharth.databinding.DialogScreenBinding
import ind.finance.aaroharth.viewmodels.AuthViewModel

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels()
    private var loadingDialog: Dialog? = null

    companion object {
        const val RC_GOOGLE_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBlurIfNeeded()
        setupUi()
        observeViewModel()
    }

    private fun setupUi() {
        binding.googleSignInButton.setOnClickListener {
            startGoogleSignIn()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailEntryField.text.toString().trim()
            val password = binding.passwordEntryField.text.toString().trim()
            viewModel.signIn(email, password)
        }

        binding.signinSignupButton.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        val text = "Don't Have An Account? Sign Up"
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.signin)),
            23, 30,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.signinSignupButton.text = spannable
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> showLoading("Signing In")
                is AuthViewModel.AuthState.Success -> {
                    loadingDialog?.dismiss()
                    showDialog("Success.json", state.message)
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }, 2000)
                }
                is AuthViewModel.AuthState.Restoring -> {
                    showLoading(state.message)
                }
                is AuthViewModel.AuthState.Error -> {
                    loadingDialog?.dismiss()
                    showDialog(state.animation, state.message)
                }
                else -> loadingDialog?.dismiss()
            }
        }
    }

    private fun startGoogleSignIn() {
        if (!viewModel.isInternetAvailable()) {
            showDialog("nointernet.json", "No internet connection")
            return
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            viewModel.firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            showDialog("Failed.json", "Google sign-in failed: ${e.statusCode}")
        }
    }

    private fun applyBlurIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.bgImage.setRenderEffect(
                RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            )
        }
    }

    private fun showLoading(message: String) {
        if (loadingDialog == null) {
            loadingDialog = Dialog(this)
            val dialogBinding = DialogScreenBinding.inflate(layoutInflater)
            loadingDialog?.setContentView(dialogBinding.root)
            loadingDialog?.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
            dialogBinding.dialogLottie.setAnimation("loading.json")
            dialogBinding.dialogLottie.playAnimation()
            dialogBinding.message.text = message
        } else {
            val messageView = loadingDialog?.findViewById<android.widget.TextView>(R.id.message)
            messageView?.text = message
        }
        loadingDialog?.show()
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
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 2000000)
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        super.onDestroy()
    }
}