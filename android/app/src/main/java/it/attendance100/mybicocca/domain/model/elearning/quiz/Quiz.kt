package it.attendance100.mybicocca.domain.model.elearning.quiz

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

/**
 * A quiz of an e-learning course, as listed by Moodle's mod_quiz_get_quizzes_by_courses web
 * service. At this university quizzes are typically unlimited-attempt self-assessments with no
 * deadlines, so resuming or starting an attempt is the student's primary action. Listed among
 * the course detail's activities and shown in full in the quiz detail sheet.
 *
 * @property id Moodle quiz instance id.
 * @property courseId Course the quiz belongs to.
 * @property cmId Course-module id of the activity, when known; used for deep links.
 * @property name Quiz title as set by the teacher.
 * @property intro Description HTML, when set.
 * @property timeOpen When the quiz opens for attempts; null when always open.
 * @property timeClose When the quiz stops accepting attempts; null when it never closes.
 * @property timeLimitSeconds Per-attempt time limit; null when untimed.
 * @property gracePeriodSeconds Extra time to submit after the limit expires, when configured.
 * @property maxAttempts Maximum number of attempts, when limited.
 * @property passGrade Grade needed to pass, when known.
 * @property sumGrades Total of the question marks available, when reported.
 * @property maxGrade Maximum grade of the quiz on its displayed scale (Moodle rescales the
 * question marks onto this ceiling); the denominator results render against.
 * @property preferredBehaviour Raw Moodle question-behaviour key (e.g. deferredfeedback).
 * @property reviewBeforeBitmask Raw Moodle review-options bitmask in force before the quiz
 * closes, when known.
 * @property reviewAfterBitmask Raw Moodle review-options bitmask in force after the quiz
 * closes, when known.
 */
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
    val maxGrade: Double?,
    val preferredBehaviour: String?,
    val reviewBeforeBitmask: Int?,
    val reviewAfterBitmask: Int?,
)
