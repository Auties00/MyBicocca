package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.state

// Shared by the procedure sub-screens (cambio percorso, proroga). One-shot, never
// replayed across rotation — surfaced as a snackbar.
sealed interface ProcedureEvent {
    data class ShowMessage(val message: String) : ProcedureEvent
}
