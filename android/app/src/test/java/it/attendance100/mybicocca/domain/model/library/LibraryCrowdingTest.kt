package it.attendance100.mybicocca.domain.model.library

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Coverage for 30-minute bucket expansion from uneven/gapped Affluences ranges, occupancy tier
 * thresholds, and weekly crowding chart page construction.
 */
class LibraryCrowdingTest {

    @Test
    fun `occupancyLevelOf follows 40 and 70 thresholds`() {
        assertThat(occupancyLevelOf(0)).isEqualTo(LibraryOccupancyLevel.Low)
        assertThat(occupancyLevelOf(39)).isEqualTo(LibraryOccupancyLevel.Low)
        assertThat(occupancyLevelOf(40)).isEqualTo(LibraryOccupancyLevel.Moderate)
        assertThat(occupancyLevelOf(69)).isEqualTo(LibraryOccupancyLevel.Moderate)
        assertThat(occupancyLevelOf(70)).isEqualTo(LibraryOccupancyLevel.High)
        assertThat(occupancyLevelOf(100)).isEqualTo(LibraryOccupancyLevel.High)
    }

    @Test
    fun `expandToHalfHourBuckets expands uneven and gapped ranges leaving uncovered buckets null`() {
        val unevenSlots = listOf(
            LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(9, 30), 20),
            LibraryCrowdingSlot(LocalTime.of(9, 30), LocalTime.of(10, 0), 35),
            LibraryCrowdingSlot(LocalTime.of(10, 0), LocalTime.of(10, 30), 50),
            LibraryCrowdingSlot(LocalTime.of(10, 30), LocalTime.of(11, 0), 60),
            LibraryCrowdingSlot(LocalTime.of(11, 0), LocalTime.of(12, 30), 75),
            // Gap from 12:30 to 18:30.
            LibraryCrowdingSlot(LocalTime.of(18, 30), LocalTime.of(19, 0), 45),
            // Gap from 19:00 to 20:00.
            LibraryCrowdingSlot(LocalTime.of(20, 0), LocalTime.of(21, 0), 0),
            // Tail from 21:00 to 22:00 uncovered.
        )

        val buckets = expandToHalfHourBuckets(
            slots = unevenSlots,
            openingTime = LocalTime.of(8, 30),
            closingTime = LocalTime.of(22, 0),
        )

        // 08:30 to 22:00 = 13.5 hours = 27 half-hour buckets.
        assertThat(buckets).hasSize(27)
        val byStart = buckets.associateBy { it.start }
        assertThat(byStart[LocalTime.of(8, 30)]?.occupancyPercentage).isEqualTo(20)
        assertThat(byStart[LocalTime.of(9, 0)]?.occupancyPercentage).isEqualTo(20)
        assertThat(byStart[LocalTime.of(9, 30)]?.occupancyPercentage).isEqualTo(35)
        assertThat(byStart[LocalTime.of(10, 0)]?.occupancyPercentage).isEqualTo(50)
        assertThat(byStart[LocalTime.of(10, 30)]?.occupancyPercentage).isEqualTo(60)
        assertThat(byStart[LocalTime.of(11, 0)]?.occupancyPercentage).isEqualTo(75)
        assertThat(byStart[LocalTime.of(11, 30)]?.occupancyPercentage).isEqualTo(75)
        assertThat(byStart[LocalTime.of(12, 0)]?.occupancyPercentage).isEqualTo(75)

        // Uncovered gap must stay null, never 0.
        assertThat(byStart[LocalTime.of(12, 30)]?.occupancyPercentage).isNull()
        assertThat(byStart[LocalTime.of(15, 0)]?.occupancyPercentage).isNull()
        assertThat(byStart[LocalTime.of(18, 0)]?.occupancyPercentage).isNull()

        assertThat(byStart[LocalTime.of(18, 30)]?.occupancyPercentage).isEqualTo(45)
        assertThat(byStart[LocalTime.of(19, 0)]?.occupancyPercentage).isNull()
        assertThat(byStart[LocalTime.of(19, 30)]?.occupancyPercentage).isNull()

        // Explicit 0% occupancy stays 0, distinct from null.
        assertThat(byStart[LocalTime.of(20, 0)]?.occupancyPercentage).isEqualTo(0)
        assertThat(byStart[LocalTime.of(20, 30)]?.occupancyPercentage).isEqualTo(0)

        // Uncovered tail before 22:00 closing stays null.
        assertThat(byStart[LocalTime.of(21, 0)]?.occupancyPercentage).isNull()
        assertThat(byStart[LocalTime.of(21, 30)]?.occupancyPercentage).isNull()
    }

    @Test
    fun `expandToHalfHourBuckets caps final Saturday bucket at 13_45`() {
        val slots = listOf(LibraryCrowdingSlot(LocalTime.of(9, 0), LocalTime.of(13, 45), 30))

        val buckets = expandToHalfHourBuckets(
            slots = slots,
            openingTime = LocalTime.of(9, 0),
            closingTime = LocalTime.of(13, 45),
        )

        assertThat(buckets).hasSize(10)
        assertThat(buckets.last()).isEqualTo(
            LibraryCrowdingBucket(LocalTime.of(13, 30), LocalTime.of(13, 45), 30),
        )
    }

    @Test
    fun `buildWeeklyCrowdingChart builds today live highlight, closed now, cached, missing and closed days`() {
        val today = LocalDate.of(2026, 6, 18) // Thursday
        val liveOpen = LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            occupancyPercentage = 64,
            crowdLevel = LibraryCrowd.Moderate,
            todayForecast = listOf(
                LibraryForecastPoint(LocalTime.of(8, 30), 25, rising = true, endTime = LocalTime.of(10, 0)),
                LibraryForecastPoint(LocalTime.of(10, 0), 50, rising = true, endTime = LocalTime.of(11, 0)),
                LibraryForecastPoint(LocalTime.of(11, 0), 80, rising = true, endTime = LocalTime.of(12, 0)),
            ),
            forecastDate = today,
        )
        val hours = LibraryWeekHours(
            days = listOf(
                LibraryDayHours(today, isToday = true, listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(12, 0)))),
                LibraryDayHours(today.plusDays(1), isToday = false, listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(12, 0)))),
                LibraryDayHours(today.plusDays(3), isToday = false, emptyList()), // Sunday closed
            ),
        )
        val history = mapOf(
            DayOfWeek.MONDAY to LibraryWeekdayCrowding(
                weekday = DayOfWeek.MONDAY,
                date = LocalDate.of(2026, 6, 15),
                slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(10, 0), 42)),
                isClosed = false,
            ),
        )

        val pages = buildWeeklyCrowdingChart(
            today = today,
            nowTime = LocalTime.of(10, 15),
            liveStatus = liveOpen,
            weekHours = hours,
            history = history,
        )

        assertThat(pages.map { it.weekday }).containsExactlyElementsIn(DayOfWeek.entries).inOrder()

        val thursday = pages.first { it.weekday == DayOfWeek.THURSDAY }
        assertThat(thursday.isToday).isTrue()
        val thursdayData = thursday.state as LibraryDayCrowdingState.Data
        assertThat(thursdayData.liveBucketIndex).isEqualTo(3) // 10:00..10:30
        assertThat(thursdayData.liveOccupancy).isEqualTo(64)
        assertThat(thursdayData.peakBucket?.occupancyPercentage).isEqualTo(80)

        val monday = pages.first { it.weekday == DayOfWeek.MONDAY }
        val mondayData = monday.state as LibraryDayCrowdingState.Data
        assertThat(mondayData.sourceDate).isEqualTo(LocalDate.of(2026, 6, 15))
        assertThat(mondayData.liveBucketIndex).isNull()

        val tuesday = pages.first { it.weekday == DayOfWeek.TUESDAY }
        assertThat(tuesday.state).isEqualTo(LibraryDayCrowdingState.Unavailable)

        val sunday = pages.first { it.weekday == DayOfWeek.SUNDAY }
        assertThat(sunday.state).isEqualTo(LibraryDayCrowdingState.Closed)

        // When closed now, today keeps its forecast without a live highlight.
        val closedNowPages = buildWeeklyCrowdingChart(
            today = today,
            nowTime = LocalTime.of(22, 30),
            liveStatus = liveOpen.copy(isOpen = false),
            weekHours = hours,
            history = history,
        )
        val closedTodayData = closedNowPages.first { it.isToday }.state as LibraryDayCrowdingState.Data
        assertThat(closedTodayData.liveBucketIndex).isNull()
        assertThat(closedTodayData.liveOccupancy).isNull()
    }

    @Test
    fun `buildWeeklyCrowdingChart trusts the timetable over older cached days`() {
        val today = LocalDate.of(2026, 6, 18) // Thursday
        val closedNow = LibraryLiveStatus(
            isOpen = false,
            statusText = "Apre alle 08:30",
            occupancyPercentage = null,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = today,
        )
        val open = listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(12, 0)))
        val lastThursday = LibraryWeekdayCrowding(
            weekday = DayOfWeek.THURSDAY,
            date = today.minusWeeks(1),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(10, 0), 42)),
            isClosed = false,
        )
        val lastFridayClosed = LibraryWeekdayCrowding(
            weekday = DayOfWeek.FRIDAY,
            date = today.minusDays(6),
            slots = emptyList(),
            isClosed = true,
        )
        val history = mapOf(
            DayOfWeek.THURSDAY to lastThursday,
            DayOfWeek.FRIDAY to lastFridayClosed,
        )
        fun chart(todayRanges: List<LibraryOpeningRange>) = buildWeeklyCrowdingChart(
            today = today,
            nowTime = LocalTime.of(10, 15),
            liveStatus = closedNow,
            weekHours = LibraryWeekHours(
                days = listOf(
                    LibraryDayHours(today, isToday = true, todayRanges),
                    LibraryDayHours(today.plusDays(1), isToday = false, open),
                ),
            ),
            history = history,
        ).associateBy { it.weekday }

        val holiday = chart(todayRanges = emptyList())
        assertThat(holiday.getValue(DayOfWeek.THURSDAY).state).isEqualTo(LibraryDayCrowdingState.Closed)
        assertThat(holiday.getValue(DayOfWeek.FRIDAY).state).isEqualTo(LibraryDayCrowdingState.Unavailable)

        val ordinary = chart(todayRanges = open)
        val cached = ordinary.getValue(DayOfWeek.THURSDAY).state as LibraryDayCrowdingState.Data
        assertThat(cached.sourceDate).isEqualTo(today.minusWeeks(1))
    }
}
