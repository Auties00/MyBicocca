package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state

sealed interface BookedEvent {
    data object CancellationSucceeded : BookedEvent
    data class CancellationFailed(val cause: Throwable) : BookedEvent
}

sealed interface CancelActionState {
    data object Idle : CancelActionState
    data class InProgress(val key: String) : CancelActionState
}
