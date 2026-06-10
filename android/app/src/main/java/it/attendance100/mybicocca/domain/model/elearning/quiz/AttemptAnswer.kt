package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * The student's answer data for one question slot of a quiz attempt, expressed as the raw form
 * fields Moodle's question engine processes (e.g. "q123:1_answer" -> "Paris"). Field names are
 * the literal input names from the question HTML, including the hidden ones that must
 * accompany every save; the repository serializes the map against the e-learning client's
 * attempt-data item format.
 *
 * @property slot 1-based question position the fields belong to.
 * @property fields Map of input field name to submitted value.
 */
data class AttemptAnswer(
    val slot: Int,
    val fields: Map<String, String>,
)
