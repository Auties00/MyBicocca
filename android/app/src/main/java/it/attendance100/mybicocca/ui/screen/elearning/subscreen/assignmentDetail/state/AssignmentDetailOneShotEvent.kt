package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state

import it.attendance100.mybicocca.core.text.UiText

/**
 * One-shot events of the compito sheet: refresh failures, attachment-open requests, and the
 * submission lifecycle outcomes — [DraftSaved], [SubmissionSent], [SubmissionRemoved],
 * [ActionFailed] — that the sheet presents as an in-sheet result page.
 */
sealed interface AssignmentDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : AssignmentDetailOneShotEvent
    data class OpenFile(val url: String, val fileName: String?) : AssignmentDetailOneShotEvent

    data object DraftSaved : AssignmentDetailOneShotEvent
    data object SubmissionSent : AssignmentDetailOneShotEvent
    data object SubmissionRemoved : AssignmentDetailOneShotEvent
    data class ActionFailed(val title: UiText, val cause: Throwable) : AssignmentDetailOneShotEvent
}
