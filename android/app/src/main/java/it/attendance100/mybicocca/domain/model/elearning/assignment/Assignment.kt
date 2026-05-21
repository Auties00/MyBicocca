package it.attendance100.mybicocca.domain.model.elearning.assignment

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

data class Assignment(
    val id: AssignmentId,
    val courseId: CourseId,
    val cmId: Int?,
    val name: String,
    val intro: String?,
    val introFiles: List<AttachmentRef>,
    val dueDate: Instant?,
    val allowSubmissionsFrom: Instant?,
    val cutoffDate: Instant?,
    val gradingDueDate: Instant?,
    val maxAttempts: Int?,
    val allowedExtensions: List<String>,
    val allowDrafts: Boolean,
    val submissionStatus: SubmissionStatus,
) {
    data class AttachmentRef(
        val fileName: String,
        val fileUrl: String?,
        val mimeType: String?,
        val sizeBytes: Long?,
    )
}
