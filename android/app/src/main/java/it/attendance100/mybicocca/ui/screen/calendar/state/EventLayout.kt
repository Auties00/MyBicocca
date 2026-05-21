package it.attendance100.mybicocca.ui.screen.calendar.state

import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent

const val TIMELINE_WINDOW_START_MINUTE = 8 * 60
const val TIMELINE_WINDOW_END_MINUTE = 22 * 60

fun layoutDay(
    events: List<CalendarEvent>,
    window: IntRange = TIMELINE_WINDOW_START_MINUTE until TIMELINE_WINDOW_END_MINUTE,
): DayLayout {
    if (events.isEmpty()) {
        return DayLayout(emptyList(), maxLane = 0, startMinute = window.first, endMinute = window.last)
    }

    // Pre-compute minute ranges and sort by start.
    val sorted = events
        .map { it to it.minuteRange() }
        .sortedWith(compareBy({ it.second.first }, { it.second.second }))

    // Lane assignment: laneEndMinute[i] = the highest end-minute placed on lane i so far.
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

    // Cluster: union-find by overlap.
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
            // sorted by start: if j starts after i ends, no further j overlaps i either
            if (placements[j].startMin >= placements[i].endMin) break
            union(i, j)
        }
    }

    // For each cluster compute the max lane used (cluster width).
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
        startMinute = window.first,
        endMinute = window.last,
    )
}

private data class LaneInfo(
    val event: CalendarEvent,
    val startMin: Int,
    val endMin: Int,
    val lane: Int,
)

private fun CalendarEvent.minuteRange(): Pair<Int, Int> {
    val s = start.hour * 60 + start.minute
    val e = (end.hour * 60 + end.minute).coerceAtLeast(s + 1)
    return s to e
}
