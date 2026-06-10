package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

/**
 * One-shot outcome of a booking attempt, consumed exactly once by the hosting sheet
 * (Channel-backed, never replayed across rotation).
 */
sealed interface BookingSheetEvent {
    data object BookedSuccessfully : BookingSheetEvent
    data class BookingFailed(val cause: Throwable) : BookingSheetEvent
}
