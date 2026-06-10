package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * Identifier of one of the student's attempts at a quiz, as assigned by Moodle's mod_quiz web
 * services.
 *
 * @property value Positive Moodle quiz-attempt id.
 */
@JvmInline
value class AttemptId(val value: Int)
