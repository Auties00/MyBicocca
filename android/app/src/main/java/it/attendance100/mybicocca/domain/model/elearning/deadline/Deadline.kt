package it.attendance100.mybicocca.domain.model.elearning.deadline

import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import java.time.Instant

sealed interface Deadline {
    val courseId: CourseId
    val title: String
    val dueAt: Instant

    data class Assignment(
        val id: AssignmentId,
        override val courseId: CourseId,
        override val title: String,
        override val dueAt: Instant,
    ) : Deadline

    data class Quiz(
        val id: QuizId,
        override val courseId: CourseId,
        override val title: String,
        override val dueAt: Instant,
    ) : Deadline
}
