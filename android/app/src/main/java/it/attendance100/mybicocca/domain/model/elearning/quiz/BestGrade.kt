package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * The student's best grade across all attempts at a quiz, from Moodle's
 * mod_quiz_get_user_best_grade web service. Shown as the headline result in the quiz detail
 * sheet.
 *
 * @property quizId Quiz the grade refers to.
 * @property grade Best grade achieved, when the student has a graded attempt.
 * @property maxGrade Grade ceiling to display the result against: the quiz's own maximum
 * grade, copied from the cached quiz row because the best-grade service reports no ceiling.
 */
data class BestGrade(
    val quizId: QuizId,
    val grade: Double?,
    val maxGrade: Double?,
)
