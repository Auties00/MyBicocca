package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * Post-submission review of a finished quiz attempt, from Moodle's mod_quiz_get_attempt_review
 * web service: every question page with its graded state plus the overall result. Shown by the
 * quiz detail sheet after an attempt is submitted or when a past attempt is reopened.
 *
 * @property attemptId Attempt the review belongs to.
 * @property pages All question pages of the attempt.
 * @property sumGrades Total of the question marks earned, when reported.
 * @property maxGrade Maximum achievable grade, when reported.
 * @property gradeFormatted Grade as the platform formats it for display, when reported.
 * @property feedback Overall feedback HTML for the achieved grade band, when configured.
 */
data class AttemptReview(
    val attemptId: AttemptId,
    val pages: List<AttemptPage>,
    val sumGrades: Double?,
    val maxGrade: Double?,
    val gradeFormatted: String?,
    val feedback: String?,
)
