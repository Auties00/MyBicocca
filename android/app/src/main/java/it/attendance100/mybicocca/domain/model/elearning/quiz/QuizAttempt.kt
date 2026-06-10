package it.attendance100.mybicocca.domain.model.elearning.quiz

import java.time.Instant

/**
 * One of the student's attempts at a quiz, as returned by Moodle's mod_quiz_get_user_attempts
 * web service. Listed in the quiz detail sheet, where an in-progress attempt is surfaced as
 * the resume action.
 *
 * @property id Moodle quiz-attempt id.
 * @property quizId Quiz the attempt belongs to.
 * @property userId Moodle user id of the attempt's owner.
 * @property attemptNumber 1-based ordinal of the attempt within the quiz.
 * @property state Lifecycle state of the attempt.
 * @property sumGrades Total of the question marks earned, when graded.
 * @property timeStart When the attempt was started.
 * @property timeFinish When the attempt was submitted, once finished.
 * @property timeModified When the platform last touched the attempt.
 * @property layout Raw Moodle slot-layout string: comma-separated question slots where ",0"
 * entries mark page breaks (e.g. "1,2,0,3,0").
 * @property previewMode Whether this is a teacher preview rather than a real student attempt.
 */
data class QuizAttempt(
    val id: AttemptId,
    val quizId: QuizId,
    val userId: Int,
    val attemptNumber: Int,
    val state: AttemptState,
    val sumGrades: Double?,
    val timeStart: Instant?,
    val timeFinish: Instant?,
    val timeModified: Instant?,
    val layout: String?,
    val previewMode: Boolean,
)
