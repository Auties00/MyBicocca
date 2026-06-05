package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state

// One-shot effects from the graduation flow: every successful mutation re-pulls the hub,
// so the screen only needs a snackbar message and which sheet (if any) to dismiss.
sealed interface DegreeAwardEvent {
    data class ShowMessage(val message: String) : DegreeAwardEvent
    data class ShowError(val message: String) : DegreeAwardEvent
    data object ApplicationSubmitted : DegreeAwardEvent
    data object ThesisSubmitted : DegreeAwardEvent
    data object SupervisorsAssigned : DegreeAwardEvent
    data object DiscussionModeSet : DegreeAwardEvent
    data object ApplicationCancelled : DegreeAwardEvent
    data class OpenUrl(val url: String) : DegreeAwardEvent
}
