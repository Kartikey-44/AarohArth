package ind.finance.aaroharth.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _profileImagePath = MutableLiveData<String?>()
    val profileImagePath: LiveData<String?> = _profileImagePath

    init {
        _username.value = prefs.getString("username", "")
        _email.value = prefs.getString("email", "")
        _profileImagePath.value = prefs.getString("profile_image_path", null)
    }

    fun updateUsername(newName: String) {
        prefs.edit().putString("username", newName).apply()
        _username.value = newName
    }

    fun updateProfileImage(path: String) {
        prefs.edit().putString("profile_image_path", path).apply()
        _profileImagePath.value = path
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
}