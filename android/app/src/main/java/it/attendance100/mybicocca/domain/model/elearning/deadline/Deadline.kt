package it.attendance100.mybicocca.domain.model.elearning.deadline

import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import java.time.Instant

/**
 * An upcoming due date harvested from the Moodle calendar web service. The calendar
 * exports each event with the course-module id in its `instance` field; ids here are
 * already translated to the real module instance ids via batched assignment/quiz
 * lookups, so they open the matching detail screen directly. Shown on course cards
 * and merged into the calendar tab.
 *
 * @property courseId The course the deadline belongs to.
 * @property title Event title as authored on Moodle.
 * @property dueAt When the activity is due.
 */
sealed interface Deadline {
    val courseId: CourseId
    val title: String
    val dueAt: Instant

    /**
     * Due date of an assignment ("compito").
     *
     * @property id The mod_assign instance id of the assignment.
     */
    data class Assignment(
        val id: AssignmentId,
        override val courseId: CourseId,
        override val title: String,
        override val dueAt: Instant,
    ) : Deadline

    /**
     * Closing date of a quiz.
     *
     * @property id The mod_quiz instance id of the quiz.
     */
    data class Quiz(
        val id: QuizId,
        override val courseId: CourseId,
        override val title: String,
        override val dueAt: Instant,
    ) : Deadline
}
