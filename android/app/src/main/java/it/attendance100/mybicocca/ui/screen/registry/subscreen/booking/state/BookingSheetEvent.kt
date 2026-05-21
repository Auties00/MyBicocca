package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

sealed interface BookingSheetEvent {
    data object BookedSuccessfully : BookingSheetEvent
    data class BookingFailed(val cause: Throwable) : BookingSheetEvent
}
