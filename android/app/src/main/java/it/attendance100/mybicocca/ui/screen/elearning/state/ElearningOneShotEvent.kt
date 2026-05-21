package it.attendance100.mybicocca.ui.screen.elearning.state

import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId

sealed interface ElearningOneShotEvent {
    data class OpenCourse(val courseId: CourseId) : ElearningOneShotEvent
    data class OpenAssignment(val courseId: CourseId, val assignmentId: AssignmentId) : ElearningOneShotEvent
    data class OpenQuiz(val courseId: CourseId, val quizId: QuizId) : ElearningOneShotEvent
    data object RequireSignIn : ElearningOneShotEvent
}
