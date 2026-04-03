package ind.finance.aaroharth.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    init {
        _username.value = prefs.getString("username", "")
        _email.value = prefs.getString("email", auth.currentUser?.email ?: "")
        _profileImageUrl.value = prefs.getString("profile_image_url", null)
    }

    fun loadProfileFromCloud() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val nameFromCloud = doc.getString("username")
                    val imageUrl = doc.getString("profileImageUrl")
                    
                    if (!nameFromCloud.isNullOrEmpty()) {
                        prefs.edit().putString("username", nameFromCloud).apply()
                        _username.value = nameFromCloud
                    }
                    if (imageUrl != null) {
                        prefs.edit().putString("profile_image_url", imageUrl).apply()
                        _profileImageUrl.value = imageUrl
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun updateUsername(newName: String) {
        prefs.edit().putString("username", newName).apply()
        _username.value = newName
        syncProfileToCloud()
    }

    fun updateProfileImage(uri: Uri) {
        val url = uri.toString()
        prefs.edit().putString("profile_image_url", url).apply()
        _profileImageUrl.value = url
        syncProfileToCloud()
    }

    fun backupProfileNow() {
        syncProfileToCloud()
    }

    private fun syncProfileToCloud() {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "username" to (_username.value ?: ""),
            "email" to (_email.value ?: ""),
            "profileImageUrl" to (_profileImageUrl.value ?: ""),
            "lastUpdated" to System.currentTimeMillis()
        )
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).set(data).await()
            } catch (e: Exception) { }
        }
    }

    fun logout() {
        auth.signOut()
        prefs.edit().clear().apply()
    }
}
