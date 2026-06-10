package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

/**
 * Stable `testTag` identifiers for the booking sub-flow pages hosted by the Appelli sheet
 * ([ExamCalendarPage], [CallPage], [ConfirmPage]), referenced by both the screen and its UI
 * tests so a user-visible copy change never breaks a test and a tag rename is a compile error
 * rather than a silently missed node. The pages apply them with `Modifier.testTag`; tests anchor
 * on the constants instead of on Italian text.
 *
 * The bookable-calendar state markers ([CALENDAR_LOADING], [CALENDAR_ERROR], [CALENDAR_EMPTY],
 * [CALENDAR_CONTENT]) are mutually exclusive per the call list's
 * [it.attendance100.mybicocca.core.state.Loadable]/[it.attendance100.mybicocca.core.state.SyncStatus]
 * pair. Date cells are keyed by their exam-call id via [call].
 */
object BookingTestTags {
    const val CALENDAR_ROOT = "booking:calendar:root"
    const val CALENDAR_LOADING = "booking:calendar:loading"
    const val CALENDAR_ERROR = "booking:calendar:error"
    const val CALENDAR_EMPTY = "booking:calendar:empty"
    const val CALENDAR_CONTENT = "booking:calendar:content"

    const val CALL_BOOK_BUTTON = "booking:call:bookButton"

    const val CONFIRM_NOTE_FIELD = "booking:confirm:noteField"
    const val CONFIRM_BUTTON = "booking:confirm:confirmButton"

    fun call(examCallId: Long?): String = "booking:call:${examCallId ?: "unknown"}"
}
