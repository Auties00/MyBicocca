package it.attendance100.mybicocca.domain.model.elearning.assignment

import java.time.Instant

/**
 * The student's hand-in state for an assignment, derived from Moodle's
 * mod_assign_get_submission_status web service. Drives the status badge and the available
 * actions (edit, finalize, remove) in the assignment detail sheet.
 */
sealed interface SubmissionStatus {
    /** Nothing handed in yet; the student can create a new submission. */
    data object NotSubmitted : SubmissionStatus

    /**
     * A submission exists but has not been finalized for grading; the student can still edit it.
     *
     * @property savedAt When the draft was last modified, when Moodle reports it.
     */
    data class Draft(val savedAt: Instant?) : SubmissionStatus

    /**
     * The submission has been finalized and is waiting to be graded.
     *
     * @property submittedAt When the submission was handed in, when Moodle reports it.
     * @property files Files attached to the submission.
     * @property onlineText Online-text content of the submission, when that submission type is used.
     */
    data class Submitted(
        val submittedAt: Instant?,
        val files: List<Assignment.AttachmentRef>,
        val onlineText: String?,
    ) : SubmissionStatus

    /**
     * The submission has been graded by the teacher.
     *
     * @property submittedAt When the submission was handed in, when Moodle reports it.
     * @property grade Awarded grade, when published.
     * @property maxGrade Maximum awardable grade, when known.
     * @property feedback Teacher's feedback, when present; may contain HTML.
     */
    data class Graded(
        val submittedAt: Instant?,
        val grade: Double?,
        val maxGrade: Double?,
        val feedback: String?,
    ) : SubmissionStatus
}
