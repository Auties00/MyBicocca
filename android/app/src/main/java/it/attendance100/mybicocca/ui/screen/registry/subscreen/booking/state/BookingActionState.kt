package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

/** Whether the booking submission is in flight; guards the confirm action against re-entry. */
sealed interface BookingActionState {
    data object Idle : BookingActionState
    data object InProgress : BookingActionState
}
