package it.attendance100.mybicocca.domain.usecase.library

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Validity enforcement for persisted library pending validation and booking draft: 60-minute TTL
 * and future slot start requirement, clearing invalid values from storage.
 */
class GetLibraryPendingStateUseCaseTest {

    private val repository: LibraryRepository = mockk(relaxed = true)
    private val useCase = GetLibraryPendingStateUseCase(repository)

    private val zone = ZoneId.of("Europe/Rome")
    private val now = Instant.parse("2026-06-15T10:00:00Z")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(now, zone)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    @Test
    fun `valid pendingValidation within 60 minutes is returned`() = runTest {
        val pending = LibraryPendingValidation("student@campus.unimib.it", "req-1", now.minusSeconds(59 * 60))
        coEvery { repository.pendingValidation() } returns pending

        assertThat(useCase.pendingValidation()).isEqualTo(pending)
        coVerify(exactly = 0) { repository.clearPendingValidation() }
    }

    @Test
    fun `expired pendingValidation at 60 minutes is cleared and returns null`() = runTest {
        val pending = LibraryPendingValidation("student@campus.unimib.it", "req-1", now.minusSeconds(60 * 60))
        coEvery { repository.pendingValidation() } returns pending

        assertThat(useCase.pendingValidation()).isNull()
        coVerify(exactly = 1) { repository.clearPendingValidation() }
    }

    @Test
    fun `valid bookingDraft within 60 minutes and in the future is returned`() = runTest {
        val draft = draft(
            date = LocalDate.of(2026, 6, 15),
            startTime = LocalTime.of(14, 0),
            savedAt = now.minusSeconds(30 * 60),
        )
        coEvery { repository.bookingDraft() } returns draft

        assertThat(useCase.bookingDraft()).isEqualTo(draft)
        coVerify(exactly = 0) { repository.clearBookingDraft() }
    }

    @Test
    fun `expired bookingDraft is cleared and returns null`() = runTest {
        val draft = draft(
            date = LocalDate.of(2026, 6, 16),
            startTime = LocalTime.of(10, 0),
            savedAt = now.minusSeconds(61 * 60),
        )
        coEvery { repository.bookingDraft() } returns draft

        assertThat(useCase.bookingDraft()).isNull()
        coVerify(exactly = 1) { repository.clearBookingDraft() }
    }

    @Test
    fun `bookingDraft whose start time is not in the future is cleared and returns null`() = runTest {
        val draft = draft(
            date = LocalDate.of(2026, 6, 15),
            startTime = LocalTime.of(12, 0),
            savedAt = now.minusSeconds(5 * 60),
        )
        coEvery { repository.bookingDraft() } returns draft

        assertThat(useCase.bookingDraft()).isNull()
        coVerify(exactly = 1) { repository.clearBookingDraft() }
    }

    private fun draft(date: LocalDate, startTime: LocalTime, savedAt: Instant) = LibraryBookingDraft(
        libraryId = "lib-1",
        zoneId = 1,
        date = date,
        durationMinutes = 60,
        startTime = startTime,
        seatResourceId = 10,
        note = "Test",
        consentAccepted = true,
        savedAt = savedAt,
    )
}
