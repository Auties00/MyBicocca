package it.attendance100.mybicocca.domain.model.elearning.quiz

// Map of input field name -> value, mirroring how Moodle processes question form submissions.
// (e.g., "q123:1_answer" -> "Paris"). The repository serializes/deserializes against the
// data-api's ElearningAttemptDataItem format.
data class AttemptAnswer(
    val slot: Int,
    val fields: Map<String, String>,
)
