package it.attendance100.mybicocca.domain.usecase.library

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Coverage for building a rolling 7-day timetable from this week and, when needed, next week. */
class GetLibraryWeekHoursUseCaseTest {

    private val repository: LibraryRepository = mockk()
    private val useCase = GetLibraryWeekHoursUseCase(repository)
    private val zone = ZoneId.of("Europe/Rome")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-18T08:30:00Z"), zone)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    @Test
    fun `Monday returns seven days from weekOffset 0 without fetching next week`() = runTest {
        val monday = LocalDate.of(2026, 6, 15)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns weekStarting(monday, today = monday)

        val result = useCase(LibraryCatalog.CENTRALE_ID)

        assertThat(result.days).hasSize(7)
        assertThat(result.days.first().day).isEqualTo(monday)
        assertThat(result.days.first().isToday).isTrue()
        assertThat(result.days.drop(1).all { !it.isToday }).isTrue()
        assertThat(result.days.last().day).isEqualTo(monday.plusDays(6))
        coVerify(exactly = 0) { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 1) }
    }

    @Test
    fun `Thursday stitches this week and next week into seven consecutive days`() = runTest {
        val monday = LocalDate.of(2026, 6, 15)
        val thursday = LocalDate.of(2026, 6, 18)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns weekStarting(monday, today = thursday)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 1) } returns weekStarting(monday.plusWeeks(1), today = thursday)

        val result = useCase(LibraryCatalog.CENTRALE_ID)

        assertThat(result.days.map { it.day }).containsExactly(
            LocalDate.of(2026, 6, 18),
            LocalDate.of(2026, 6, 19),
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21),
            LocalDate.of(2026, 6, 22),
            LocalDate.of(2026, 6, 23),
            LocalDate.of(2026, 6, 24),
        ).inOrder()
        assertThat(result.days.first().isToday).isTrue()
        assertThat(result.days.drop(1).all { !it.isToday }).isTrue()
    }

    @Test
    fun `Sunday fetches next week to complete all seven days`() = runTest {
        val monday = LocalDate.of(2026, 6, 15)
        val sunday = LocalDate.of(2026, 6, 21)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns weekStarting(monday, today = sunday)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 1) } returns weekStarting(monday.plusWeeks(1), today = sunday)

        val result = useCase(LibraryCatalog.CENTRALE_ID)

        assertThat(result.days).hasSize(7)
        assertThat(result.days.first().day).isEqualTo(sunday)
        assertThat(result.days.first().ranges).isEmpty()
        assertThat(result.days.last().day).isEqualTo(LocalDate.of(2026, 6, 27))
    }

    @Test
    fun `next week failure degrades gracefully to remaining days from this week`() = runTest {
        val monday = LocalDate.of(2026, 6, 15)
        val thursday = LocalDate.of(2026, 6, 18)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns weekStarting(monday, today = thursday)
        coEvery { repository.getWeekHours(LibraryCatalog.CENTRALE_ID, 1) } throws IOException("offline")

        val result = useCase(LibraryCatalog.CENTRALE_ID)

        assertThat(result.days.map { it.day }).containsExactly(
            LocalDate.of(2026, 6, 18),
            LocalDate.of(2026, 6, 19),
            LocalDate.of(2026, 6, 20),
            LocalDate.of(2026, 6, 21),
        ).inOrder()
    }

    private fun weekStarting(monday: LocalDate, today: LocalDate) = LibraryWeekHours(
        days = (0L..6L).map { offset ->
            val day = monday.plusDays(offset)
            LibraryDayHours(
                day = day,
                isToday = day == today,
                ranges = when (day.dayOfWeek) {
                    DayOfWeek.SUNDAY -> emptyList()
                    DayOfWeek.SATURDAY -> listOf(LibraryOpeningRange(LocalTime.of(9, 0), LocalTime.of(13, 45)))
                    else -> listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0)))
                },
            )
        },
    )
}
