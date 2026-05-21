package it.attendance100.mybicocca.domain.model.elearning.quiz

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
