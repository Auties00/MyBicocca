package it.attendance100.mybicocca.ui.screen.calendar.state

/**
 * Single-delivery notifications from the calendar ViewModel to the screen, delivered
 * through a channel-backed flow so each fires exactly once and never replays across
 * configuration changes.
 */
sealed interface CalendarOneShotEvent {
    /** A month refresh threw; carries the failure for user-facing messaging. */
    data class RefreshFailed(val cause: Throwable) : CalendarOneShotEvent

    /** Calendar sources rejected the session and a fresh sign-in is needed. */
    data object RequireSignIn : CalendarOneShotEvent
}
