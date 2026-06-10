package it.attendance100.mybicocca.domain.model.calendar

/**
 * Stable identity of a [CalendarEvent], unique across all sources.
 *
 * @property value Encoded as `"<source code>_<native id>"`, namespacing each source's native
 *   identifier so ids coming from different upstreams cannot collide.
 */
@JvmInline
value class CalendarEventId(val value: String) {
    companion object {
        /** Builds the namespaced id from a source tag and that source's native identifier. */
        fun of(source: EventSource, nativeId: String) =
            CalendarEventId("${source.code}_$nativeId")
    }
}
