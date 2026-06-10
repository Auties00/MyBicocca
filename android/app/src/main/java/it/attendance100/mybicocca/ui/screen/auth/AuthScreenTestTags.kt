package it.attendance100.mybicocca.ui.screen.auth

/**
 * Stable `testTag` identifiers for [AuthScreen] / [AuthScreenSheetContent] and the shared
 * sign-in form body, referenced by both the screen and its UI tests so a user-visible copy
 * change never breaks a test and a tag rename is a compile error rather than a silently missed
 * node. The screen applies them with `Modifier.testTag`; tests anchor on the constants instead
 * of on Italian text.
 */
object AuthScreenTestTags {
    const val ROOT = "auth:root"
    const val USERNAME_FIELD = "auth:usernameField"
    const val PASSWORD_FIELD = "auth:passwordField"
    const val SUBMIT_BUTTON = "auth:submitButton"
    const val SPID_BUTTON = "auth:spidButton"
    const val CIE_BUTTON = "auth:cieButton"
    const val CANCEL_BUTTON = "auth:cancelButton"
}
