package it.attendance100.mybicocca.ui.screen.lock

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Whether the device can perform a biometric unlock right now. */
enum class BiometricCapability {
    /** Hardware present and at least one biometric enrolled. */
    Available,

    /** Hardware present but nothing enrolled. */
    NoneEnrolled,

    /** No hardware, disabled or unusable. */
    Unavailable,
}

private const val ALLOWED_AUTHENTICATORS = BIOMETRIC_WEAK

fun biometricCapability(context: Context): BiometricCapability =
    when (BiometricManager.from(context).canAuthenticate(ALLOWED_AUTHENTICATORS)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.Available
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NoneEnrolled
        else -> BiometricCapability.Unavailable
    }

/**
 * Biometric capability that re-queries on every ON_RESUME, so enrolling or removing a biometric in
 * system settings and returning to the app is reflected without a process restart.
 */
@Composable
fun rememberBiometricCapability(): BiometricCapability {
    val context = LocalContext.current
    var capability by remember { mutableStateOf(biometricCapability(context)) }
    LifecycleResumeEffect(Unit) {
        capability = biometricCapability(context)
        onPauseOrDispose { }
    }
    return capability
}

/**
 * Shows the system biometric sheet.
 *
 * - [onSuccess] — authentication passed.
 * - [onError]   — unrecoverable: the user tapped the negative button, cancelled, or the sensor locked out. -> password fallback.
 * - [onFailed]  — a single rejected-but-retryable attempt. The sheet stays up -> ignored, retry.
 */
fun promptBiometric(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    negativeButton: String,
    onSuccess: () -> Unit,
    onError: (errorCode: Int, message: CharSequence) -> Unit,
    onFailed: () -> Unit = {},
) {
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                onSuccess()

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
                onError(errorCode, errString)

            override fun onAuthenticationFailed() = onFailed()
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButton)
        .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        .build()
    prompt.authenticate(info)
}

tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
