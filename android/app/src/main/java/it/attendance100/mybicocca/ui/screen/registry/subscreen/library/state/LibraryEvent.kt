package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

/**
 * One-shot outcomes of Biblioteca operations — cancellation, booking, login (email validation),
 * sync, and on-site presence verification — rendered by the sheet as result pages. The login
 * results handled in-content by the login page ("link not yet opened", "logged in") are modeled
 * by [LibraryLoginFeedback] rather than as events.
 */
sealed interface LibraryEvent {
    data object ReservationCancelled : LibraryEvent
    data class CancelFailed(val cause: Throwable) : LibraryEvent
    data class BookingFailed(val cause: Throwable) : LibraryEvent

    /**
     * The validation link went out to [email]. Surfaces no result page: the login page advances
     * to its check-your-email state through the login phase stream.
     */
    data class LoginEmailSent(val email: String) : LibraryEvent
    data class LoginRequestFailed(val cause: Throwable) : LibraryEvent
    data class LoginFailed(val cause: Throwable) : LibraryEvent
    data class SyncFailed(val cause: Throwable) : LibraryEvent
    data object PresenceVerified : LibraryEvent
    data object PresenceInvalidCode : LibraryEvent
    data class PresenceFailed(val cause: Throwable) : LibraryEvent
}
