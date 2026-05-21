package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state

sealed interface AssignmentDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : AssignmentDetailOneShotEvent
    data class OpenFile(val url: String, val fileName: String?) : AssignmentDetailOneShotEvent
}
