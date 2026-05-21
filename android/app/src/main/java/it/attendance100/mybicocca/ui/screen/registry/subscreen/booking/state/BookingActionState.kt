package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

sealed interface BookingActionState {
    data object Idle : BookingActionState
    data object InProgress : BookingActionState
}
