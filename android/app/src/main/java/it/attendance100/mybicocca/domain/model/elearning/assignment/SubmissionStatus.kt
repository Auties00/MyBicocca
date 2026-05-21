package it.attendance100.mybicocca.domain.model.elearning.assignment

import java.time.Instant

sealed interface SubmissionStatus {
    data object NotSubmitted : SubmissionStatus
    data class Draft(val savedAt: Instant?) : SubmissionStatus
    data class Submitted(
        val submittedAt: Instant?,
        val files: List<Assignment.AttachmentRef>,
        val onlineText: String?,
    ) : SubmissionStatus
    data class Graded(
        val submittedAt: Instant?,
        val grade: Double?,
        val maxGrade: Double?,
        val feedback: String?,
    ) : SubmissionStatus
}
