package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state

sealed interface BookedEvent {
    data object CancellationSucceeded : BookedEvent
    data class CancellationFailed(val cause: Throwable) : BookedEvent
    class OpenPdf(val bytes: ByteArray, val fileName: String) : BookedEvent
    data class ShowMessage(val message: String) : BookedEvent
}

sealed interface CancelActionState {
    data object Idle : CancelActionState
    data class InProgress(val key: String) : CancelActionState
}

// The two downloadable booking documents.
enum class ExamDocument { BookingSlip, PresenceCertificate }

// Tracks which document (if any) is currently downloading, scoped to a booking so only
// the active button shows a spinner.
sealed interface DocDownloadState {
    data object Idle : DocDownloadState
    data class InProgress(val bookingKey: String, val document: ExamDocument) : DocDownloadState
}
