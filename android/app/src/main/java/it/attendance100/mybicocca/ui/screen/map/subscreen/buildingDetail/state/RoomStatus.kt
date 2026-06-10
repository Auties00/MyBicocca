package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail.state

import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import java.time.LocalDateTime
import java.time.LocalTime

/** Live availability of a room derived from today's occupation slots. */
sealed interface RoomStatus {
    /** A null [until] means free for the rest of the day. */
    data class Free(val until: LocalTime?) : RoomStatus

    data class Busy(val title: String, val kind: String?, val until: LocalTime) : RoomStatus

    /** The schedule fetch failed: availability can't be claimed either way. */
    data object Unknown : RoomStatus
}

/**
 * Derives the room's status at [now] from today's [entries] (null entries mean the schedule
 * fetch failed). Back-to-back slots are chained so "occupata fino alle" reports the real end
 * of the busy block, not just the current event's end.
 */
fun roomStatus(entries: List<RoomScheduleEntry>?, now: LocalDateTime): RoomStatus {
    if (entries == null) return RoomStatus.Unknown
    val sorted = entries.sortedBy { it.start }
    val current = sorted.firstOrNull { !now.isBefore(it.start) && now.isBefore(it.end) }
    if (current == null) {
        val next = sorted.firstOrNull { it.start.isAfter(now) }
        return RoomStatus.Free(until = next?.start?.toLocalTime())
    }
    var end = current.end
    while (true) {
        val chained = sorted.firstOrNull { !it.start.isAfter(end) && it.end.isAfter(end) } ?: break
        end = chained.end
    }
    return RoomStatus.Busy(title = current.title, kind = current.kind, until = end.toLocalTime())
}
