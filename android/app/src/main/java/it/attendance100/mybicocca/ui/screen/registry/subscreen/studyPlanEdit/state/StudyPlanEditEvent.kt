package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state

sealed interface StudyPlanEditEvent {
    data object Submitted : StudyPlanEditEvent
}
