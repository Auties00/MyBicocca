package it.attendance100.mybicocca.domain.model.elearning.quiz

data class AttemptReview(
    val attemptId: AttemptId,
    val pages: List<AttemptPage>,
    val sumGrades: Double?,
    val maxGrade: Double?,
    val gradeFormatted: String?,
    val feedback: String?,
)
