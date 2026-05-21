package it.attendance100.mybicocca.domain.model.elearning.quiz

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

data class Quiz(
    val id: QuizId,
    val courseId: CourseId,
    val cmId: Int?,
    val name: String,
    val intro: String?,
    val timeOpen: Instant?,
    val timeClose: Instant?,
    val timeLimitSeconds: Long?,
    val gracePeriodSeconds: Long?,
    val maxAttempts: Int?,
    val passGrade: Double?,
    val sumGrades: Double?,
    val preferredBehaviour: String?,
    val reviewBeforeBitmask: Int?,
    val reviewAfterBitmask: Int?,
)
