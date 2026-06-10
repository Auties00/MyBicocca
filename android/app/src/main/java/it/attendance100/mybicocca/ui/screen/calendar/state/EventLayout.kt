package it.attendance100.mybicocca.ui.screen.calendar.state

import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent

const val TIMELINE_WINDOW_START_MINUTE = 8 * 60
const val TIMELINE_WINDOW_END_MINUTE = 22 * 60

/**
 * Minimum placement height of a timeline block, in minutes: anything shorter renders too
 * small to read or tap. Point events (deadlines) and very short slots are padded up to
 * this for placement only — the card still shows the real times.
 */
private const val MIN_BLOCK_MINUTES = 24

/**
 * Arranges one day's events into lanes for a timeline column.
 *
 * Events are sorted by start and assigned greedily to the first lane free at their start
 * minute (each lane tracks the highest end minute placed on it). Overlapping events are
 * then grouped into clusters with a union-find pass — the start-sorted order lets overlap
 * detection stop at the first event starting after the current one ends — and every
 * member of a cluster receives an equal fraction of the column width, so a pair shares
 * halves while an isolated event spans the full column.
 *
 * The vertical window starts from [window] — exact minutes, `first` the top and `last` the
 * bottom of the grid; the 08:00–22:00 default covers lessons — and grows, snapped to whole
 * hours, to whatever the day actually holds: deadlines sit at 23:59 and appointments can
 * fall outside the default span. Callers sharing one hour gutter across several days pass
 * the pre-fitted [timelineWindowFor] window so every column agrees on the scale.
 */
fun layoutDay(
    events: List<CalendarEvent>,
    window: IntRange = TIMELINE_WINDOW_START_MINUTE..TIMELINE_WINDOW_END_MINUTE,
): DayLayout {
    if (events.isEmpty()) {
        return DayLayout(emptyList(), maxLane = 0, startMinute = window.first, endMinute = window.last)
    }

    val sorted = events
        .map { it to it.minuteRange() }
        .sortedWith(compareBy({ it.second.first }, { it.second.second }))

    val windowStart = minOf(window.first, sorted.minOf { it.second.first } / 60 * 60)
    val windowEnd = maxOf(window.last, ((sorted.maxOf { it.second.second } + 59) / 60 * 60).coerceAtMost(24 * 60))

    val laneEndMinute = IntArray(events.size) { Int.MIN_VALUE }
    val placements = ArrayList<LaneInfo>(events.size)
    var maxLaneUsed = -1

    for ((event, range) in sorted) {
        val (start, end) = range
        var picked = -1
        for (i in 0..maxLaneUsed + 1) {
            if (i > laneEndMinute.lastIndex) break
            if (laneEndMinute[i] <= start) {
                picked = i
                break
            }
        }
        if (picked == -1) {
            picked = maxLaneUsed + 1
        }
        if (picked > maxLaneUsed) maxLaneUsed = picked
        laneEndMinute[picked] = end
        placements += LaneInfo(event, start, end, picked)
    }

    val parent = IntArray(placements.size) { it }
    fun find(i: Int): Int {
        var x = i
        while (parent[x] != x) {
            parent[x] = parent[parent[x]]
            x = parent[x]
        }
        return x
    }
    fun union(a: Int, b: Int) {
        val ra = find(a); val rb = find(b)
        if (ra != rb) parent[ra] = rb
    }
    for (i in placements.indices) {
        for (j in (i + 1) until placements.size) {
            if (placements[j].startMin >= placements[i].endMin) break
            union(i, j)
        }
    }

    val clusterMaxLane = HashMap<Int, Int>()
    val clusterId = HashMap<Int, Int>()
    var nextClusterId = 0
    placements.forEachIndexed { idx, info ->
        val root = find(idx)
        val current = clusterMaxLane[root] ?: -1
        clusterMaxLane[root] = maxOf(current, info.lane)
        if (root !in clusterId) {
            clusterId[root] = nextClusterId++
        }
    }

    val items = placements.mapIndexed { idx, info ->
        val root = find(idx)
        val lanesInCluster = (clusterMaxLane[root] ?: info.lane) + 1
        val width = 1f / lanesInCluster
        LaidOutEvent(
            event = info.event,
            laneStart = info.lane * width,
            laneWidth = width,
            lane = info.lane,
            cluster = clusterId.getValue(root),
            startMinute = info.startMin,
            endMinute = info.endMin,
        )
    }

    return DayLayout(
        items = items,
        maxLane = maxLaneUsed.coerceAtLeast(0),
        startMinute = windowStart,
        endMinute = windowEnd,
    )
}

/**
 * Shared vertical window for every timeline surface currently on screen: the default
 * 08:00–22:00 span grown, snapped to whole hours, to cover every loaded event. Day pages
 * sit side by side in a pager and share one hour gutter, so the window must not vary per
 * day — it is computed once over everything loaded, which can extend the span for days far
 * from the visible one; a stable frame is worth the occasional extra hour of grid. Bounds
 * are exact minutes: `first` is the top of the grid, `last` the bottom.
 */
fun timelineWindowFor(eventsByDay: Collection<List<CalendarEvent>>): IntRange {
    var start = TIMELINE_WINDOW_START_MINUTE
    var end = TIMELINE_WINDOW_END_MINUTE
    eventsByDay.forEach { events ->
        events.forEach { event ->
            val (eventStart, eventEnd) = event.minuteRange()
            if (eventStart < start) start = eventStart / 60 * 60
            if (eventEnd > end) end = ((eventEnd + 59) / 60 * 60).coerceAtMost(24 * 60)
        }
    }
    return start..end
}

private data class LaneInfo(
    val event: CalendarEvent,
    val startMin: Int,
    val endMin: Int,
    val lane: Int,
)

/**
 * Placement minutes for the event, padded to [MIN_BLOCK_MINUTES]. A block whose padded
 * end hits midnight (a 23:59 deadline) grows backwards from the end, preserving the
 * minimum readable height.
 */
private fun CalendarEvent.minuteRange(): Pair<Int, Int> {
    val s0 = start.hour * 60 + start.minute
    val e = (end.hour * 60 + end.minute)
        .coerceAtLeast(s0 + MIN_BLOCK_MINUTES)
        .coerceAtMost(24 * 60)
    val s = minOf(s0, e - MIN_BLOCK_MINUTES).coerceAtLeast(0)
    return s to e
}
