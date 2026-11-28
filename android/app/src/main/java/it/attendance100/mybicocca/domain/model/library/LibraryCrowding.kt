package it.attendance100.mybicocca.domain.model.library

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Qualitative crowding tier shared between the library-card armchairs and the crowding chart. */
enum class LibraryOccupancyLevel { Low, Moderate, High }

/** Classifies a percentage into low (<40), moderate (40..69), or high (>=70). */
fun occupancyLevelOf(percentage: Int): LibraryOccupancyLevel = when {
    percentage < 40 -> LibraryOccupancyLevel.Low
    percentage < 70 -> LibraryOccupancyLevel.Moderate
    else -> LibraryOccupancyLevel.High
}

/** One raw occupancy forecast range as delivered by Affluences or persisted in history. */
data class LibraryCrowdingSlot(
    val start: LocalTime,
    val end: LocalTime,
    val occupancyPercentage: Int,
)

/**
 * One 30-minute bar bucket for the crowding chart. [occupancyPercentage] is null when no forecast
 * range covers [start], never defaulted to zero.
 */
data class LibraryCrowdingBucket(
    val start: LocalTime,
    val end: LocalTime,
    val occupancyPercentage: Int?,
)

/** Persisted crowding history entry for one library on one weekday. */
data class LibraryWeekdayCrowding(
    val weekday: DayOfWeek,
    val date: LocalDate,
    val slots: List<LibraryCrowdingSlot>,
    val isClosed: Boolean,
)

/** Chart state for a single weekday page in the library crowding pager. */
sealed interface LibraryDayCrowdingState {
    /** Day is known closed from timetable or a stored closed history entry. */
    data object Closed : LibraryDayCrowdingState

    /** No forecast or cached history is available for this weekday yet. */
    data object Unavailable : LibraryDayCrowdingState

    /**
     * Expanded 30-minute buckets ready to plot.
     *
     * @property buckets 30-minute buckets spanning the day's opening window.
     * @property sourceDate Date the forecast belongs to, used for the cached-weekday caption.
     * @property liveBucketIndex Index in [buckets] highlighted with the live marker, or null.
     * @property liveOccupancy Live occupancy value at [liveBucketIndex], when highlighted.
     * @property peakBucket Highest-occupancy non-empty bucket for accessibility summaries.
     */
    data class Data(
        val buckets: List<LibraryCrowdingBucket>,
        val sourceDate: LocalDate?,
        val liveBucketIndex: Int?,
        val liveOccupancy: Int?,
        val peakBucket: LibraryCrowdingBucket?,
    ) : LibraryDayCrowdingState
}

/** One weekday page (Monday through Sunday) of a library's crowding chart. */
data class LibraryWeekdayCrowdingPage(
    val weekday: DayOfWeek,
    val isToday: Boolean,
    val state: LibraryDayCrowdingState,
)

private const val BUCKET_MINUTES = 30
private const val MINUTES_PER_DAY = 24 * 60

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

private fun endMinuteOfDay(start: LocalTime, end: LocalTime): Int {
    val startMin = start.toMinuteOfDay()
    val endMin = end.toMinuteOfDay()
    return if (endMin == 0 && startMin > 0) MINUTES_PER_DAY else endMin
}

private fun minuteOfDayToLocalTime(minute: Int): LocalTime =
    if (minute >= MINUTES_PER_DAY) LocalTime.MIDNIGHT else LocalTime.of(minute / 60, minute % 60)

/**
 * Expands uneven Affluences forecast ranges into uniform 30-minute buckets from [openingTime] to
 * [closingTime] (falling back to the slot bounds when omitted). Each bucket takes the occupancy of
 * the range covering its start (`slot.start <= bucket.start < slot.end`); uncovered buckets keep a
 * null occupancy, never zero.
 */
fun expandToHalfHourBuckets(
    slots: List<LibraryCrowdingSlot>,
    openingTime: LocalTime? = null,
    closingTime: LocalTime? = null,
): List<LibraryCrowdingBucket> {
    val normalizedSlots = slots.mapNotNull { slot ->
        val s = slot.start.toMinuteOfDay()
        val e = endMinuteOfDay(slot.start, slot.end)
        if (e <= s) null else Triple(s, e, slot.occupancyPercentage)
    }

    val startMin = openingTime?.toMinuteOfDay()
        ?: normalizedSlots.minOfOrNull { it.first }
        ?: return emptyList()
    val endMin = when {
        closingTime != null -> endMinuteOfDay(openingTime ?: LocalTime.MIDNIGHT, closingTime)
        else -> normalizedSlots.maxOfOrNull { it.second } ?: return emptyList()
    }
    if (endMin <= startMin) return emptyList()

    val buckets = mutableListOf<LibraryCrowdingBucket>()
    var cursor = startMin
    while (cursor < endMin) {
        val bucketEnd = minOf(cursor + BUCKET_MINUTES, endMin)
        val covering = normalizedSlots.firstOrNull { (s, e, _) -> s <= cursor && cursor < e }
        buckets += LibraryCrowdingBucket(
            start = minuteOfDayToLocalTime(cursor),
            end = minuteOfDayToLocalTime(bucketEnd),
            occupancyPercentage = covering?.third,
        )
        cursor += BUCKET_MINUTES
    }
    return buckets
}

/** Overload accepting a day's opening ranges from the timetable. */
fun expandToHalfHourBuckets(
    slots: List<LibraryCrowdingSlot>,
    openingRanges: List<LibraryOpeningRange>,
): List<LibraryCrowdingBucket> {
    val open = openingRanges.minOfOrNull { it.open }
    val close = openingRanges.maxByOrNull { endMinuteOfDay(it.open, it.close) }?.close
    return expandToHalfHourBuckets(slots, openingTime = open, closingTime = close)
}

/**
 * Builds the seven Monday-to-Sunday crowding chart pages from live status, timetable hours, and
 * cached weekday history.
 */
fun buildWeeklyCrowdingChart(
    today: LocalDate,
    nowTime: LocalTime,
    liveStatus: LibraryLiveStatus?,
    weekHours: LibraryWeekHours?,
    history: Map<DayOfWeek, LibraryWeekdayCrowding>,
): List<LibraryWeekdayCrowdingPage> {
    val hoursByWeekday = weekHours?.days?.associateBy { it.day.dayOfWeek }.orEmpty()
    val nowMinutes = nowTime.toMinuteOfDay()

    return DayOfWeek.entries.map { weekday ->
        val isToday = weekday == today.dayOfWeek
        val dayHours = hoursByWeekday[weekday]
        val historyEntry = history[weekday]
        val openingRanges = dayHours?.ranges.orEmpty()
        val timetableKnownClosed = dayHours != null && openingRanges.isEmpty()

        val state: LibraryDayCrowdingState = if (isToday) {
            val liveSlots = liveStatus?.todayForecast.orEmpty().map { it.toCrowdingSlot() }
            val effectiveSlots = liveSlots.ifEmpty {
                historyEntry?.takeUnless { it.isClosed }?.slots.orEmpty()
            }
            when {
                liveSlots.isEmpty() && liveStatus?.isOpen != true &&
                    (timetableKnownClosed || (dayHours == null && historyEntry?.isClosed == true)) ->
                    LibraryDayCrowdingState.Closed
                effectiveSlots.isEmpty() -> LibraryDayCrowdingState.Unavailable
                else -> {
                    val buckets = expandToHalfHourBuckets(effectiveSlots, openingRanges)
                    if (buckets.isEmpty()) {
                        LibraryDayCrowdingState.Unavailable
                    } else {
                        val liveOccupancy = liveStatus?.takeIf { it.isOpen }?.occupancyPercentage
                        val liveIndex = if (liveOccupancy != null) {
                            buckets.indexOfFirst { bucket ->
                                val s = bucket.start.toMinuteOfDay()
                                val e = endMinuteOfDay(bucket.start, bucket.end)
                                s <= nowMinutes && nowMinutes < e
                            }.takeIf { it >= 0 }
                        } else {
                            null
                        }
                        val peak = buckets
                            .filter { it.occupancyPercentage != null }
                            .maxByOrNull { it.occupancyPercentage!! }
                        LibraryDayCrowdingState.Data(
                            buckets = buckets,
                            sourceDate = if (liveSlots.isNotEmpty()) {
                                liveStatus?.forecastDate ?: today
                            } else {
                                historyEntry?.date
                            },
                            liveBucketIndex = liveIndex,
                            liveOccupancy = if (liveIndex != null) liveOccupancy else null,
                            peakBucket = peak,
                        )
                    }
                }
            }
        } else {
            when {
                timetableKnownClosed || (dayHours == null && historyEntry?.isClosed == true) ->
                    LibraryDayCrowdingState.Closed
                historyEntry == null || historyEntry.slots.isEmpty() -> LibraryDayCrowdingState.Unavailable
                else -> {
                    val buckets = expandToHalfHourBuckets(historyEntry.slots, openingRanges)
                    if (buckets.isEmpty()) {
                        LibraryDayCrowdingState.Unavailable
                    } else {
                        val peak = buckets
                            .filter { it.occupancyPercentage != null }
                            .maxByOrNull { it.occupancyPercentage!! }
                        LibraryDayCrowdingState.Data(
                            buckets = buckets,
                            sourceDate = historyEntry.date,
                            liveBucketIndex = null,
                            liveOccupancy = null,
                            peakBucket = peak,
                        )
                    }
                }
            }
        }

        LibraryWeekdayCrowdingPage(
            weekday = weekday,
            isToday = isToday,
            state = state,
        )
    }
}
