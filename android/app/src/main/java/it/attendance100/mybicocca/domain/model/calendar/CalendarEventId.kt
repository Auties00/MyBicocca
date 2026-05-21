package it.attendance100.mybicocca.domain.model.calendar

@JvmInline
value class CalendarEventId(val value: String) {
    companion object {
        fun of(source: EventSource, nativeId: String) =
            CalendarEventId("${source.code}_$nativeId")
    }
}
