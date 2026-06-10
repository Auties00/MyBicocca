package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * A single question inside a quiz attempt page, carried as the raw HTML Moodle's mod_quiz
 * attempt web services render; the quiz detail sheet parses it into native input controls.
 *
 * @property slot 1-based position of the question within the attempt.
 * @property type Raw Moodle question-type key (e.g. multichoice, truefalse, multianswer).
 * @property pageIndex 0-based index of the page the question sits on.
 * @property html Question formulation HTML, including the hidden form inputs whose values must
 * accompany every answer save.
 * @property state Raw Moodle question state (e.g. todo, complete, gradedright), when reported.
 * @property mark Mark earned, when graded.
 * @property maxMark Maximum mark for the question, when reported.
 * @property flagged Whether the student flagged the question.
 * @property sequenceCheck Anti-replay counter for the slot; it must round-trip exactly as
 * received when saving or submitting answers, or Moodle rejects the data as out-of-sequence.
 */
data class AttemptQuestion(
    val slot: Int,
    val type: String,
    val pageIndex: Int,
    val html: String,
    val state: String?,
    val mark: Double?,
    val maxMark: Double?,
    val flagged: Boolean,
    val sequenceCheck: String?,
)
