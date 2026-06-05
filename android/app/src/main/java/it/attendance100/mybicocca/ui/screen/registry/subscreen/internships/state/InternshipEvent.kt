package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.state

sealed interface InternshipEvent {
    data class ShowMessage(val message: String) : InternshipEvent
}
