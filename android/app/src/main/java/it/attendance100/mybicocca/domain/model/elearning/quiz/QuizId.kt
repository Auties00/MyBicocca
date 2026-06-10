package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * Identifier of a quiz on the e-learning platform — Moodle's mod_quiz instance id, distinct
 * from the course-module id (cmid) of the same activity.
 *
 * @property value Positive Moodle quiz instance id.
 */
@JvmInline
value class QuizId(val value: Int)
