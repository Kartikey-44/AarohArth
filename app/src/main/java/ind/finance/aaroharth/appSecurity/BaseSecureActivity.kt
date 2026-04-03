package ind.finance.aaroharth.appSecurity

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

abstract class BaseSecureActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        val now = System.currentTimeMillis()
        val shouldLock =
            now - AppLockManager.lastBackgroundTime > AppLockManager.LOCK_TIMEOUT

        if (shouldLock && AppLockManager.isAppLocked) {
            showAuth()
        }
    }

    private fun showAuth() {
        val executor = ContextCompat.getMainExecutor(this)

        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    AppLockManager.isAppLocked = false
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock AarohArth")
            .setSubtitle("Authenticate to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(promptInfo)
    }
}