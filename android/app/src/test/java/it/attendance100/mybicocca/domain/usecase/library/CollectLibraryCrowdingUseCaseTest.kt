package it.attendance100.mybicocca.domain.usecase.library

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingSyncScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Coverage for [CollectLibraryCrowdingUseCase] and [StartLibraryCrowdingSyncUseCase]:
 * first-run recording, fetching both libraries, completion once all seven weekdays exist for
 * every library, the 21-day cutoff, retry on total failure, and skipping scheduling once done.
 */
class CollectLibraryCrowdingUseCaseTest {

    private val getLiveStatus: GetLibraryLiveStatusUseCase = mockk()
    private val historyRepository: LibraryCrowdingHistoryRepository = mockk(relaxed = true)
    private val scheduler: LibraryCrowdingSyncScheduler = mockk(relaxed = true)

    private val collect = CollectLibraryCrowdingUseCase(getLiveStatus, historyRepository, scheduler)
    private val startSync = StartLibraryCrowdingSyncUseCase(historyRepository, scheduler)

    private val day1 = Instant.parse("2026-06-01T08:00:00Z")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(day1, ZoneOffset.UTC)
        coEvery { historyRepository.isInitialCollectionDone() } returns false
        coEvery { historyRepository.firstCollectionRunAt() } returns null
        coEvery { historyRepository.getHistory(any()) } returns emptyMap()
        coEvery { getLiveStatus(any()) } returns dummyLiveStatus()
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    @Test
    fun `first run records timestamp, fetches both libraries, prunes and returns Continue`() = runTest {
        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Continue)
        coVerify(exactly = 1) { historyRepository.recordFirstCollectionRunAt(day1) }
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.CENTRALE_ID) }
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.MEDICINA_ID) }
        coVerify(exactly = 1) { historyRepository.pruneExpired() }
        coVerify(exactly = 0) { historyRepository.markInitialCollectionDone() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `stops permanently once both libraries have all seven weekdays after fetching`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        val historyById = mutableMapOf(
            LibraryCatalog.CENTRALE_ID to weekMap(DayOfWeek.entries.dropLast(1)),
            LibraryCatalog.MEDICINA_ID to weekMap(DayOfWeek.entries.dropLast(1)),
        )
        coEvery { historyRepository.getHistory(any()) } answers {
            historyById[firstArg<String>()].orEmpty()
        }
        coEvery { getLiveStatus(any()) } answers {
            val id = firstArg<String>()
            historyById[id] = weekMap(DayOfWeek.entries)
            dummyLiveStatus()
        }

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Completed)
        coVerify(exactly = 1) { historyRepository.markInitialCollectionDone() }
        verify(exactly = 1) { scheduler.cancel() }
    }

    @Test
    fun `keeps collecting when only one library has all seven weekdays`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        coEvery { historyRepository.getHistory(LibraryCatalog.CENTRALE_ID) } returns weekMap(DayOfWeek.entries)
        coEvery { historyRepository.getHistory(LibraryCatalog.MEDICINA_ID) } returns weekMap(DayOfWeek.entries.dropLast(1))

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Continue)
        coVerify(exactly = 0) { historyRepository.markInitialCollectionDone() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `stops permanently at 21 days after first run without fetching`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        AppClock.clock = Clock.fixed(day1.plus(CollectLibraryCrowdingUseCase.MAX_COLLECTION_WINDOW), ZoneOffset.UTC)

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Completed)
        coVerify(exactly = 0) { getLiveStatus(any()) }
        coVerify(exactly = 1) { historyRepository.markInitialCollectionDone() }
        verify(exactly = 1) { scheduler.cancel() }
    }

    @Test
    fun `continues just before 21 days have elapsed`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        AppClock.clock = Clock.fixed(
            day1.plus(CollectLibraryCrowdingUseCase.MAX_COLLECTION_WINDOW).minusSeconds(60),
            ZoneOffset.UTC,
        )

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Continue)
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.CENTRALE_ID) }
        coVerify(exactly = 1) { getLiveStatus(LibraryCatalog.MEDICINA_ID) }
    }

    @Test
    fun `returns Completed immediately when already marked done`() = runTest {
        coEvery { historyRepository.isInitialCollectionDone() } returns true

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Completed)
        coVerify(exactly = 0) { getLiveStatus(any()) }
        verify(exactly = 1) { scheduler.cancel() }
    }

    @Test
    fun `returns Retry when every library fetch fails`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        coEvery { getLiveStatus(any()) } throws IOException("offline")

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Retry)
        coVerify(exactly = 0) { historyRepository.markInitialCollectionDone() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `returns Continue when one library succeeds and the other fails`() = runTest {
        coEvery { historyRepository.firstCollectionRunAt() } returns day1
        coEvery { getLiveStatus(LibraryCatalog.CENTRALE_ID) } returns dummyLiveStatus()
        coEvery { getLiveStatus(LibraryCatalog.MEDICINA_ID) } throws IOException("timeout")

        val outcome = collect()

        assertThat(outcome).isEqualTo(LibraryCrowdingCollectionOutcome.Continue)
    }

    @Test
    fun `startSync schedules only when initial collection is not done`() = runTest {
        coEvery { historyRepository.isInitialCollectionDone() } returns false
        startSync()
        verify(exactly = 1) { scheduler.schedule() }

        coEvery { historyRepository.isInitialCollectionDone() } returns true
        startSync()
        verify(exactly = 1) { scheduler.schedule() }
    }

    private fun dummyLiveStatus() = LibraryLiveStatus(
        isOpen = true,
        statusText = "Aperta",
        occupancyPercentage = 40,
        crowdLevel = null,
        todayForecast = emptyList(),
    )

    private fun weekMap(days: List<DayOfWeek>): Map<DayOfWeek, LibraryWeekdayCrowding> =
        days.associateWith { dow ->
            LibraryWeekdayCrowding(
                weekday = dow,
                date = LocalDate.of(2026, 6, 1),
                slots = emptyList(),
                isClosed = dow == DayOfWeek.SUNDAY,
            )
        }
}
