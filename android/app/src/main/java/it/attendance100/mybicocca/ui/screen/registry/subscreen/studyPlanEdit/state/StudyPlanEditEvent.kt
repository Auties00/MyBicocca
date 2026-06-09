package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state

sealed interface StudyPlanEditEvent {
    // message reflects the schema's approval flavour (automatic vs manual approval).
    data class Submitted(val message: String) : StudyPlanEditEvent
}
