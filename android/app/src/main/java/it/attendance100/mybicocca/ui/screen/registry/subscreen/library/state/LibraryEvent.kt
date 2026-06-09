package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

sealed interface LibraryEvent {
    data object ReservationCancelled : LibraryEvent
    data class CancelFailed(val cause: Throwable) : LibraryEvent
    data class BookingFailed(val cause: Throwable) : LibraryEvent
    // Login (email-validation) outcomes.
    data class LoginEmailSent(val email: String) : LibraryEvent
    data class LoginRequestFailed(val cause: Throwable) : LibraryEvent
    data object LoginNotYetValidated : LibraryEvent
    data object LoggedIn : LibraryEvent
    data class LoginFailed(val cause: Throwable) : LibraryEvent
    data class SyncFailed(val cause: Throwable) : LibraryEvent
    // On-site presence validation outcomes.
    data object PresenceVerified : LibraryEvent
    data object PresenceInvalidCode : LibraryEvent
    data class PresenceFailed(val cause: Throwable) : LibraryEvent
}
