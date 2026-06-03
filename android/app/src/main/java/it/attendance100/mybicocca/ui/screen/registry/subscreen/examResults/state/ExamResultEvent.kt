package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state

sealed interface ExamResultEvent {
    data object AcceptSucceeded : ExamResultEvent
    data object RejectSucceeded : ExamResultEvent
    data class Failed(val cause: Throwable) : ExamResultEvent
}

sealed interface ExamResultActionState {
    data object Idle : ExamResultActionState
    // applicationListId of the result whose accept/reject is in flight.
    data class InProgress(val applicationListId: Long) : ExamResultActionState
}
