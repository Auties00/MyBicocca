package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state

sealed interface AppointmentsEvent {
    data object ReservationCancelled : AppointmentsEvent
    data class CancelFailed(val cause: Throwable) : AppointmentsEvent
    data class PdfReady(val fileName: String, val bytes: ByteArray) : AppointmentsEvent
    data class PdfFailed(val cause: Throwable) : AppointmentsEvent
    data class BookingFailed(val cause: Throwable) : AppointmentsEvent
}
