package ind.finance.aaroharth.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import ind.finance.aaroharth.data.model.user_detail

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val animation: String, val message: String) : AuthState()
    }

    fun isInternetAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("DangerIcon.json", "Email/Password cannot be empty")
            return
        }

        if (!isInternetAvailable()) {
            _authState.value = AuthState.Error("nointernet.json", "No internet connection")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()
                    _authState.value = AuthState.Success("Sign In Successful")
                } else {
                    _authState.value = AuthState.Error("Failed.json", task.exception?.message ?: "Sign in failed")
                }
            }
    }

    fun signUp(email: String, password: String) {
        if (email.isEmpty() || password.length < 6) {
            _authState.value = AuthState.Error("DangerIcon.json", "Invalid Email or Password (min 6 chars)")
            return
        }

        if (!isInternetAvailable()) {
            _authState.value = AuthState.Error("nointernet.json", "No internet connection")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    database.child(user.uid).setValue(user_detail(email, password))
                        .addOnSuccessListener {
                            prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()
                            _authState.value = AuthState.Success("Signed up successfully")
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error("Failed.json", "Database error")
                        }
                } else {
                    _authState.value = AuthState.Error("Failed.json", task.exception?.message ?: "Signup failed")
                }
            }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result!!
                    val user = result.user!!
                    val email = user.email ?: ""
                    prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()

                    if (result.additionalUserInfo?.isNewUser == true) {
                        database.child(user.uid).setValue(user_detail(email, "GOOGLE_AUTH"))
                    }
                    _authState.value = AuthState.Success("Sign In Successful")
                } else {
                    _authState.value = AuthState.Error("Failed.json", "Google authentication failed")
                }
            }
    }
}