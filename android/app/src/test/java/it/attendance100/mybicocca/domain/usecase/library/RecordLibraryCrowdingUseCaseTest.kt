package it.attendance100.mybicocca.domain.usecase.library

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
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
import java.time.ZoneOffset

/**
 * Coverage for [RecordLibraryCrowdingUseCase]: range-date authority (never device clock),
 * closed-day persistence when timetable confirms closed, and skipping empty forecasts otherwise.
 */
class RecordLibraryCrowdingUseCaseTest {

    private val libraryRepository: LibraryRepository = mockk()
    private val historyRepository: LibraryCrowdingHistoryRepository = mockk(relaxed = true)
    private val useCase = RecordLibraryCrowdingUseCase(libraryRepository, historyRepository)

    @Before
    fun setUp() {
        // Deliberately set device clock to a different day than the server forecast ranges.
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-19T00:05:00Z"), ZoneOffset.UTC)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    @Test
    fun `non-empty forecast upserts using date from forecast ranges instead of device clock`() = runTest {
        val serverDate = LocalDate.of(2026, 6, 18) // Thursday, while device clock is Friday
        val live = LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            occupancyPercentage = 55,
            crowdLevel = null,
            todayForecast = listOf(
                LibraryForecastPoint(LocalTime.of(8, 30), 20, rising = true, endTime = LocalTime.of(9, 30)),
                LibraryForecastPoint(LocalTime.of(9, 30), 40, rising = true, endTime = LocalTime.of(10, 0)),
            ),
            forecastDate = serverDate,
        )
        val captured = slot<LibraryWeekdayCrowding>()
        coEvery { historyRepository.upsert(LibraryCatalog.CENTRALE_ID, capture(captured)) } returns Unit

        useCase(LibraryCatalog.CENTRALE_ID, live)

        assertThat(captured.captured.date).isEqualTo(serverDate)
        assertThat(captured.captured.weekday).isEqualTo(DayOfWeek.THURSDAY)
        assertThat(captured.captured.isClosed).isFalse()
        assertThat(captured.captured.slots).hasSize(2)
        coVerify(exactly = 0) { libraryRepository.getWeekHours(any(), any()) }
    }

    @Test
    fun `non-empty forecast without a server date is not stored`() = runTest {
        val live = LibraryLiveStatus(
            isOpen = true,
            statusText = null,
            occupancyPercentage = 50,
            crowdLevel = null,
            todayForecast = listOf(LibraryForecastPoint(LocalTime.of(9, 0), 50, null)),
            forecastDate = null,
        )

        useCase(LibraryCatalog.CENTRALE_ID, live)

        coVerify(exactly = 0) { historyRepository.upsert(any(), any()) }
    }

    @Test
    fun `empty forecast on a day the timetable marks closed is stored as closed`() = runTest {
        val sunday = LocalDate.of(2026, 6, 21)
        val live = LibraryLiveStatus(
            isOpen = false,
            statusText = "Apre domani alle 08:30",
            occupancyPercentage = null,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = sunday,
        )
        coEvery { libraryRepository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns LibraryWeekHours(
            days = listOf(LibraryDayHours(day = sunday, isToday = true, ranges = emptyList())),
        )
        val captured = slot<LibraryWeekdayCrowding>()
        coEvery { historyRepository.upsert(LibraryCatalog.CENTRALE_ID, capture(captured)) } returns Unit

        useCase(LibraryCatalog.CENTRALE_ID, live)

        assertThat(captured.captured.date).isEqualTo(sunday)
        assertThat(captured.captured.weekday).isEqualTo(DayOfWeek.SUNDAY)
        assertThat(captured.captured.isClosed).isTrue()
        assertThat(captured.captured.slots).isEmpty()
    }

    @Test
    fun `empty forecast on an open day is not stored`() = runTest {
        val thursday = LocalDate.of(2026, 6, 18)
        val live = LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta",
            occupancyPercentage = 40,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = thursday,
        )
        coEvery { libraryRepository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } returns LibraryWeekHours(
            days = listOf(
                LibraryDayHours(
                    day = thursday,
                    isToday = true,
                    ranges = listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0))),
                ),
            ),
        )

        useCase(LibraryCatalog.CENTRALE_ID, live)

        coVerify(exactly = 0) { historyRepository.upsert(any(), any()) }
    }

    @Test
    fun `empty forecast when timetable fetch fails is not stored`() = runTest {
        val live = LibraryLiveStatus(
            isOpen = false,
            statusText = null,
            occupancyPercentage = null,
            crowdLevel = null,
            todayForecast = emptyList(),
            forecastDate = null,
        )
        coEvery { libraryRepository.getWeekHours(LibraryCatalog.CENTRALE_ID, 0) } throws IOException("offline")

        useCase(LibraryCatalog.CENTRALE_ID, live)

        coVerify(exactly = 0) { historyRepository.upsert(any(), any()) }
    }
}
