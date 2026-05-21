package it.attendance100.mybicocca.domain.model.elearning.quiz

enum class AttemptState(val raw: String) {
    InProgress("inprogress"),
    Overdue("overdue"),
    Finished("finished"),
    Abandoned("abandoned"),
    Unknown("unknown");

    companion object {
        fun fromRaw(raw: String?): AttemptState =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Unknown
    }
}
