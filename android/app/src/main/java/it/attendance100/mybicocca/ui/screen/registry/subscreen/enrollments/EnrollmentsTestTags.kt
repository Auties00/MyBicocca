package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments

/**
 * Stable `testTag` identifiers for the Iscrizioni sheet ([EnrollmentsTimelinePage]), referenced by
 * both the screen and its UI tests so a user-visible copy change never breaks a test and a tag
 * rename is a compile error rather than a silently missed node. The screen applies them with
 * `Modifier.testTag` at the call site; tests anchor on the constants instead of on Italian text.
 *
 * [ROW_PREFIX] is concatenated with an enrollment id to address a specific academic-year row.
 * [RENEW_BUTTON] tags the pinned renewal footer, the page's primary action.
 */
object EnrollmentsTestTags {
    const val ROOT = "enrollments:root"
    const val STATE_LOADING = "enrollments:state:loading"
    const val STATE_ERROR = "enrollments:state:error"
    const val STATE_EMPTY = "enrollments:state:empty"
    const val STATE_CONTENT = "enrollments:state:content"
    const val RENEW_BUTTON = "enrollments:renewButton"
    const val ROW_PREFIX = "enrollments:row:"

    fun row(id: Long): String = "$ROW_PREFIX$id"
}
