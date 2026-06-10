package it.attendance100.mybicocca.domain.model.elearning.assignment

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

/**
 * An assignment (compito) of an e-learning course, as listed by Moodle's
 * mod_assign_get_assignments web service and enriched with the student's own submission state.
 * Listed among the course detail's activities and shown in full in the assignment detail sheet.
 *
 * @property id Moodle assignment instance id.
 * @property courseId Course the assignment belongs to.
 * @property cmId Course-module id of the activity, when known; used for deep links.
 * @property name Assignment title as set by the teacher.
 * @property intro Description HTML, when set.
 * @property introFiles Files attached to the description.
 * @property dueDate Hand-in deadline, when set.
 * @property allowSubmissionsFrom Earliest time submissions are accepted, when restricted.
 * @property cutoffDate Hard deadline after which Moodle rejects submissions, when set.
 * @property gradingDueDate Date by which teachers are expected to grade, when set.
 * @property maxAttempts Maximum number of submission attempts, when limited.
 * @property allowedExtensions File extensions accepted for upload; empty when unrestricted.
 * @property allowDrafts Whether saves are kept as drafts until the student explicitly
 * finalizes them for grading.
 * @property submissionStatus The student's own hand-in state.
 * @property pageUrl Web page of the activity on the e-learning site, offered as a browser
 * escape hatch for completing a hand-in outside the app; null when the course-module id is
 * unknown.
 */
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
    val pageUrl: String?,
) {
    /**
     * Reference to a file exposed by the assignment — a description attachment or a submitted
     * file. Rendered as attachment tiles in the assignment detail sheet.
     *
     * @property fileName File name as stored on Moodle.
     * @property fileUrl Download URL, when available; webservice pluginfile URLs already carry
     * the access token.
     * @property mimeType MIME type, when reported.
     * @property sizeBytes Size in bytes, when reported.
     */
    data class AttachmentRef(
        val fileName: String,
        val fileUrl: String?,
        val mimeType: String?,
        val sizeBytes: Long?,
    )
}
