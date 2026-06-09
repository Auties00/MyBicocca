package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state

sealed interface AttendanceEvent {
    data object OpenRilevaSheet : AttendanceEvent
}
