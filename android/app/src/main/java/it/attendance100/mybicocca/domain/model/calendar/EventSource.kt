package it.attendance100.mybicocca.domain.model.calendar

enum class EventSource(val code: String) {
    LESSON("lesson"),
    EXAM("exam");

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCode(code: String): EventSource? = byCode[code]
    }
}
