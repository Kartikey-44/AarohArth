package ind.finance.aaroharth.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import ind.finance.aaroharth.data.local.App_Database
import ind.finance.aaroharth.data.model.Account_Info
import ind.finance.aaroharth.data.model.Budget_Info
import ind.finance.aaroharth.data.model.Transaction_Info
import ind.finance.aaroharth.data.model.user_detail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().getReference("Users")
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val database = App_Database.getInstance(application)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val animation: String, val message: String) : AuthState()
        data class Restoring(val message: String) : AuthState()
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
                    val userId = auth.currentUser?.uid ?: ""
                    prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()
                    restoreUserData(userId)
                } else {
                    _authState.value = AuthState.Error("Failed.json",  "Sign in failed wrong Id or Password")
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
                    viewModelScope.launch {
                        try {
                            rtdb.child(user.uid).setValue(user_detail(email, password)).await()
                            
                            val userData = mapOf(
                                "username" to "",
                                "email" to email,
                                "profileImageUrl" to "",
                                "lastUpdated" to System.currentTimeMillis()
                            )
                            firestore.collection("users").document(user.uid).set(userData).await()
                            
                            withContext(Dispatchers.Main) {
                                prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()
                                _authState.value = AuthState.Success("Signed up successfully")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                // Even if firestore/rtdb fails, the user is created in Auth
                                prefs.edit().putString("email", email).putBoolean("is_logged_in", true).apply()
                                _authState.value = AuthState.Success("Signed up (Cloud sync pending)")
                            }
                        }
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
                        val userData = mapOf(
                            "username" to (user.displayName ?: ""),
                            "email" to email,
                            "profileImageUrl" to (user.photoUrl?.toString() ?: ""),
                            "lastUpdated" to System.currentTimeMillis()
                        )
                        
                        viewModelScope.launch {
                            try {
                                firestore.collection("users").document(user.uid).set(userData).await()
                                rtdb.child(user.uid).setValue(user_detail(email, "GOOGLE_AUTH")).await()
                                
                                withContext(Dispatchers.Main) {
                                    prefs.edit().putString("username", user.displayName).apply()
                                    _authState.value = AuthState.Success("Welcome to Aaroh Arth!")
                                }
                            } catch (e: Exception) {
                                _authState.value = AuthState.Success("Welcome! (Cloud init failed)")
                            }
                        }
                    } else {
                        restoreUserData(user.uid)
                    }
                } else {
                    _authState.value = AuthState.Error("Failed.json", "Google authentication failed")
                }
            }
    }

    private fun restoreUserData(userId: String) {
        _authState.value = AuthState.Restoring("Restoring your data from cloud...")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Restore Profile info to Prefs
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    withContext(Dispatchers.Main) {
                        prefs.edit()
                            .putString("username", userDoc.getString("username"))
                            .putString("profile_image_url", userDoc.getString("profileImageUrl"))
                            .apply()
                    }
                }

                // 2. Restore Accounts
                val accounts = firestore.collection("users").document(userId).collection("accounts").get().await()
                if (!accounts.isEmpty) {
                    val accountList = accounts.toObjects(Account_Info::class.java).map { it.copy(isSynced = true) }
                    accountList.forEach { database.accountDao().insertAccount(it) }
                    withContext(Dispatchers.Main) { prefs.edit().putBoolean("has_account", true).apply() }
                }

                // 3. Restore Transactions
                val transactions = firestore.collection("users").document(userId).collection("transactions").get().await()
                if (!transactions.isEmpty) {
                    val transactionList = transactions.toObjects(Transaction_Info::class.java).map { it.copy(isSynced = true) }
                    transactionList.forEach { database.transactionDao().insertTransaction(it) }
                }

                // 4. Restore Budgets
                val budgets = firestore.collection("users").document(userId).collection("budgets").get().await()
                if (!budgets.isEmpty) {
                    val budgetList = budgets.toObjects(Budget_Info::class.java).map { it.copy(isSynced = true) }
                    budgetList.forEach { database.budgetDao().insertInfo(it) }
                }

                withContext(Dispatchers.Main) {
                    _authState.value = AuthState.Success("Data restored successfully!")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _authState.value = AuthState.Success("Sign In Successful (Restore skipped: ${e.message})")
                }
            }
        }
    }
}
