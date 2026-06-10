package it.attendance100.mybicocca.ui.screen.lock

/**
 * Stable `testTag` identifiers for [AppLockScreen], referenced by both the screen and its UI tests
 * so a user-visible copy change never breaks a test and a tag rename is a compile error rather than
 * a silently missed node. The screen applies them with `Modifier.testTag`; tests anchor on the
 * constants instead of on Italian text.
 */
object AppLockTestTags {
    const val ROOT = "lock:root"
    const val USERNAME = "lock:username"
    const val PASSWORD_FIELD = "lock:passwordField"
    const val UNLOCK_BUTTON = "lock:unlockButton"
    const val USE_BIOMETRIC_BUTTON = "lock:useBiometricButton"
    const val USE_PASSWORD_BUTTON = "lock:usePasswordButton"
}
