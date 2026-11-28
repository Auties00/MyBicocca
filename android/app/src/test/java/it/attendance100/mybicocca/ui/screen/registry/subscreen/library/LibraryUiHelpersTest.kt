package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingBucket
import it.attendance100.mybicocca.domain.model.library.LibraryDayCrowdingState
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowdingPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.cachedForecastDate
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.formatCrowdingSlotRange
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.selectWholeHourTickIndices
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.toggleCrowdingSelection
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formatLibraryFloor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.formatSeatDisplayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.resolveHoursDetailText
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

/** Unit coverage for pure UI formatting and chart-axis helpers used by the Biblioteca screens. */
class LibraryUiHelpersTest {

    @Test
    fun formatSeatDisplayName_adds_prefix_for_zone_codes_and_avoids_duplicating_existing_prefixes() {
        assertThat(formatSeatDisplayName("R 42", "Posto %s")).isEqualTo("Posto R 42")
        assertThat(formatSeatDisplayName("V 17", "Seat %s")).isEqualTo("Seat V 17")
        assertThat(formatSeatDisplayName("Posto 101", "Posto %s")).isEqualTo("Posto 101")
        assertThat(formatSeatDisplayName("Seat 12", "Seat %s")).isEqualTo("Seat 12")
        assertThat(formatSeatDisplayName("Carrel 12", "Posto %s")).isEqualTo("Carrel 12")
    }

    @Test
    fun resolveHoursDetailText_uses_today_closing_time_when_open_and_strips_duplicate_status_prefix() {
        val today = LocalDate.of(2026, 6, 18)
        val weekHours = LibraryWeekHours(
            days = listOf(
                LibraryDayHours(
                    day = today,
                    isToday = true,
                    ranges = listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0))),
                ),
            ),
        )

        val openDetail = resolveHoursDetailText(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            weekHours = weekHours,
            nowTime = LocalTime.of(10, 30),
            closesAtFormatter = { "Chiude alle $it" },
            opensAtFormatter = { "Apre alle $it" },
        )
        assertThat(openDetail).isEqualTo("Chiude alle 22:00")

        val closedDetail = resolveHoursDetailText(
            isOpen = false,
            statusText = "Chiusa · Apre alle 08:30",
            weekHours = weekHours,
            nowTime = LocalTime.of(7, 30),
            closesAtFormatter = { "Chiude alle $it" },
            opensAtFormatter = { "Apre alle $it" },
        )
        assertThat(closedDetail).isEqualTo("Apre alle 08:30")

        val closedFallback = resolveHoursDetailText(
            isOpen = false,
            statusText = "Chiusa",
            weekHours = weekHours,
            nowTime = LocalTime.of(7, 30),
            closesAtFormatter = { "Chiude alle $it" },
            opensAtFormatter = { "Apre alle $it" },
        )
        assertThat(closedFallback).isEqualTo("Apre alle 08:30")
    }

    @Test
    fun resolveHoursDetailText_words_a_closed_library_from_the_timetable_for_today_and_tomorrow() {
        val thursday = LocalDate.of(2026, 6, 18)
        val hours = listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0)))
        val weekHours = LibraryWeekHours(
            days = listOf(
                LibraryDayHours(day = thursday, isToday = true, ranges = hours),
                LibraryDayHours(day = thursday.plusDays(1), isToday = false, ranges = hours),
            ),
        )

        listOf(LocalTime.of(7, 30), LocalTime.of(23, 0)).forEach { now ->
            val detail = resolveHoursDetailText(
                isOpen = false,
                statusText = "Apre alle 08:30",
                weekHours = weekHours,
                nowTime = now,
                closesAtFormatter = { "Closes at $it" },
                opensAtFormatter = { "Opens at $it" },
            )
            assertThat(detail).isEqualTo("Opens at 08:30")
        }
    }

    @Test
    fun resolveHoursDetailText_keeps_the_service_text_when_the_next_opening_is_later_than_tomorrow() {
        val saturday = LocalDate.of(2026, 6, 20)
        val weekHours = LibraryWeekHours(
            days = listOf(
                LibraryDayHours(
                    day = saturday,
                    isToday = true,
                    ranges = listOf(LibraryOpeningRange(LocalTime.of(9, 0), LocalTime.of(13, 45))),
                ),
                LibraryDayHours(day = saturday.plusDays(1), isToday = false, ranges = emptyList()),
                LibraryDayHours(
                    day = saturday.plusDays(2),
                    isToday = false,
                    ranges = listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0))),
                ),
            ),
        )

        fun detail(statusText: String?) = resolveHoursDetailText(
            isOpen = false,
            statusText = statusText,
            weekHours = weekHours,
            nowTime = LocalTime.of(15, 0),
            closesAtFormatter = { "Closes at $it" },
            opensAtFormatter = { "Opens at $it" },
        )

        assertThat(detail("Chiusa · Apre lunedì alle 08:30")).isEqualTo("Apre lunedì alle 08:30")
        assertThat(detail(null)).isEqualTo("Opens at 08:30")
    }

    @Test
    fun selectWholeHourTickIndices_picks_spaced_whole_hours_for_weekday_and_saturday_spans() {
        val weekdayBuckets = generateSequence(LocalTime.of(8, 30)) { it.plusMinutes(30) }
            .takeWhile { it.isBefore(LocalTime.of(22, 0)) }
            .map { LibraryCrowdingBucket(it, it.plusMinutes(30), 40) }
            .toList()
        val weekdayTickTimes = selectWholeHourTickIndices(weekdayBuckets).map { weekdayBuckets[it].start }
        assertThat(weekdayTickTimes).containsExactly(
            LocalTime.of(9, 0),
            LocalTime.of(12, 0),
            LocalTime.of(15, 0),
            LocalTime.of(18, 0),
            LocalTime.of(21, 0),
        ).inOrder()

        val saturdayBuckets = generateSequence(LocalTime.of(9, 0)) { it.plusMinutes(30) }
            .takeWhile { it.isBefore(LocalTime.of(13, 45)) }
            .map { LibraryCrowdingBucket(it, it.plusMinutes(30), 35) }
            .toList()
        val saturdayTickTimes = selectWholeHourTickIndices(saturdayBuckets).map { saturdayBuckets[it].start }
        assertThat(saturdayTickTimes).containsExactly(
            LocalTime.of(9, 0),
            LocalTime.of(11, 0),
            LocalTime.of(13, 0),
        ).inOrder()
    }

    @Test
    fun formatLibraryFloor_capitalises_catalogue_floors_and_hides_unknown_libraries() {
        assertThat(formatLibraryFloor(LibraryCatalog.CENTRALE_ID, Locale.ITALIAN)).isEqualTo("II piano")
        assertThat(formatLibraryFloor(LibraryCatalog.MEDICINA_ID, Locale.ITALIAN)).isEqualTo("Piano terra")
        assertThat(formatLibraryFloor(LibraryCatalog.CENTRALE_ID, Locale.ENGLISH)).isEqualTo("2nd floor")
        assertThat(formatLibraryFloor(LibraryCatalog.MEDICINA_ID, Locale.ENGLISH)).isEqualTo("Ground floor")
        assertThat(formatLibraryFloor("unknown-library", Locale.ITALIAN)).isNull()
        assertThat(formatLibraryFloor("unknown-library", Locale.ENGLISH)).isNull()
    }

    @Test
    fun toggleCrowdingSelection_selects_toggles_restores_live_and_ignores_empty_slots() {
        // Empty slot is ignored whether nothing or another slot is selected.
        assertThat(toggleCrowdingSelection(null, tappedIndex = 2, hasData = false, liveIndex = 4)).isNull()
        assertThat(toggleCrowdingSelection(6, tappedIndex = 2, hasData = false, liveIndex = 4)).isEqualTo(6)

        // Selecting a data slot, moving to another, and tapping again to clear.
        assertThat(toggleCrowdingSelection(null, tappedIndex = 6, hasData = true, liveIndex = 4)).isEqualTo(6)
        assertThat(toggleCrowdingSelection(6, tappedIndex = 9, hasData = true, liveIndex = 4)).isEqualTo(9)
        assertThat(toggleCrowdingSelection(9, tappedIndex = 9, hasData = true, liveIndex = 4)).isNull()

        // Tapping the live bar clears any selection so the live callout returns.
        assertThat(toggleCrowdingSelection(9, tappedIndex = 4, hasData = true, liveIndex = 4)).isNull()
        assertThat(toggleCrowdingSelection(null, tappedIndex = 4, hasData = true, liveIndex = 4)).isNull()

        // Non-today pages (no live bar) select and clear cleanly.
        assertThat(toggleCrowdingSelection(null, tappedIndex = 0, hasData = true, liveIndex = null)).isEqualTo(0)
        assertThat(toggleCrowdingSelection(0, tappedIndex = 0, hasData = true, liveIndex = null)).isNull()
    }

    @Test
    fun cachedForecastDate_captions_other_days_and_a_stale_today_only() {
        val today = LocalDate.of(2026, 6, 18)
        fun page(isToday: Boolean, sourceDate: LocalDate?) = LibraryWeekdayCrowdingPage(
            weekday = today.dayOfWeek,
            isToday = isToday,
            state = LibraryDayCrowdingState.Data(emptyList(), sourceDate, null, null, null),
        )

        assertThat(cachedForecastDate(page(isToday = true, sourceDate = today), today)).isNull()
        assertThat(cachedForecastDate(page(isToday = true, sourceDate = today.minusWeeks(1)), today))
            .isEqualTo(today.minusWeeks(1))
        assertThat(cachedForecastDate(page(isToday = false, sourceDate = today.minusDays(6)), today))
            .isEqualTo(today.minusDays(6))
        assertThat(cachedForecastDate(page(isToday = false, sourceDate = null), today)).isNull()
        assertThat(
            cachedForecastDate(LibraryWeekdayCrowdingPage(today.dayOfWeek, false, LibraryDayCrowdingState.Closed), today),
        ).isNull()
    }

    @Test
    fun formatCrowdingSlotRange_formats_half_hour_and_clipped_slots_with_en_dash() {
        assertThat(
            formatCrowdingSlotRange(LibraryCrowdingBucket(LocalTime.of(10, 30), LocalTime.of(11, 0), 55)),
        ).isEqualTo("10:30–11:00")
        assertThat(
            formatCrowdingSlotRange(LibraryCrowdingBucket(LocalTime.of(13, 30), LocalTime.of(13, 45), 30)),
        ).isEqualTo("13:30–13:45")
    }
}
