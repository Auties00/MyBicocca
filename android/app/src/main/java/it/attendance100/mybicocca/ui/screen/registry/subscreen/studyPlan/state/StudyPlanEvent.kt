package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state

sealed interface StudyPlanEvent {
    data class OpenPdf(val bytes: ByteArray, val fileName: String) : StudyPlanEvent
    data class ShowMessage(val message: String) : StudyPlanEvent
}
