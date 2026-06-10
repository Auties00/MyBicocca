package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state

/** One-shot outcomes of the accept/reject actions on a published esito. */
sealed interface ExamResultEvent {
    data object AcceptSucceeded : ExamResultEvent
    data object RejectSucceeded : ExamResultEvent
    data class Failed(val cause: Throwable) : ExamResultEvent
}

/**
 * Whether an accept/reject is in flight, keyed by the result's applicationListId so only
 * that outcome's action pair shows a spinner.
 */
sealed interface ExamResultActionState {
    data object Idle : ExamResultActionState
    data class InProgress(val applicationListId: Long) : ExamResultActionState
}
