package com.example.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthManager {

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }
        val status = biometricManager.canAuthenticate(authenticators)
        return status == BiometricManager.BIOMETRIC_SUCCESS ||
               status == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun isHardwareAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }
        val status = biometricManager.canAuthenticate(authenticators)
        return status != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
               status != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "BRICK • Connexion Sécurisée",
        subtitle: String = "Empreinte digitale, mot de passe ou schéma du téléphone",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Biometric attempt failed, UI will let user retry or use device credential
            }
        }

        try {
            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            } else {
                promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            }

            val promptInfo = promptInfoBuilder.build()
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // Fallback for devices with specific vendor constraints on DEVICE_CREDENTIAL
            try {
                val fallbackPrompt = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText("Annuler")
                    .build()
                val biometricPrompt = BiometricPrompt(activity, executor, callback)
                biometricPrompt.authenticate(fallbackPrompt)
            } catch (inner: Exception) {
                onError(inner.localizedMessage ?: "Authentification non disponible")
            }
        }
    }
}
