package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state

/** One-shot request from the ViewModel to the Presenze UI. */
sealed interface AttendanceEvent {
    /** Jump into the rileva marking flow; emitted when a presence QR scanned outside the app arrives. */
    data object OpenRilevaSheet : AttendanceEvent
}
