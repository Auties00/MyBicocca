package it.attendance100.mybicocca.domain.model.elearning.quiz

import java.time.Instant

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
