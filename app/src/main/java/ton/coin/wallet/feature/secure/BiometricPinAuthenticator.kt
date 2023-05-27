package ton.coin.wallet.feature.secure

import android.content.Context
import android.os.CancellationSignal
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

class BiometricPinAuthenticator(private val context: Context) {

    private var biometricPrompt: BiometricPrompt? = null
    private var cancellationSignal: CancellationSignal? = null

    fun authenticateWithBiometricsOrPin(callback: AuthenticationCallback) {
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
            authenticateWithBiometrics(callback)
        } else {
            authenticateWithPin(callback)
        }
    }

    private fun authenticateWithBiometrics(callback: AuthenticationCallback) {
        val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                callback.onAuthenticationSuccessful()
            }

            override fun onAuthenticationFailed() {
                callback.onAuthenticationFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                callback.onAuthenticationError(errorCode, errString.toString())
            }
        }

        /*biometricPrompt = BiometricPrompt.Builder(context)
            .setTitle("Biometric Authentication")
            .setSubtitle("Place your finger on the sensor")
            .setDescription("Use your fingerprint to authenticate")
            .setNegativeButton("Use PIN", context.mainExecutor,
                { _, _ -> authenticateWithPin(callback) })
            .build()

        cancellationSignal = CancellationSignal()
        biometricPrompt?.authenticate(
            BiometricPrompt.CryptoObject(null), cancellationSignal,
            context.mainExecutor, biometricCallback
        )*/
    }

    private fun authenticateWithPin(callback: AuthenticationCallback) {
        // Show a PIN authentication dialog or activity
        // For simplicity, let's assume the user entered the PIN successfully
        callback.onAuthenticationSuccessful()
    }

    fun cancelAuthentication() {
        cancellationSignal?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
        }
    }

    interface AuthenticationCallback {
        fun onAuthenticationSuccessful()
        fun onAuthenticationFailed()
        fun onAuthenticationError(errorCode: Int, errorMessage: String)
    }
}