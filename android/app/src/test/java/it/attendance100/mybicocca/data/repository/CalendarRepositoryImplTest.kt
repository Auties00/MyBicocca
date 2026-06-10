package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarEventDiscriminator
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ExamRepository
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.YearMonth

/**
 * Behaviour coverage for the unified calendar repository. The five sources are mocked at their
 * boundaries (session, EasyStaff, the two DAOs, the collaborating feature repos) so the tests
 * can assert the aggregation contract independently of any real network or Room:
 *
 * - `observeRange` merges the materialized rows with the live deadline / appointment / library
 *   streams and yields one chronologically sorted list.
 * - `refreshMonth` awaits every per-source job, propagates the first required-source failure
 *   (lessons / exams / deadlines), and swallows the best-effort appointment and library
 *   failures — including the normal "no linked library account" signal.
 * - A bus invalidation drops the affected source's sync stamps.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarRepositoryImplTest {

    private val careerId = CareerId(7L)
    private val accountId = AccountId("acc-1")
    private val month = YearMonth.of(2026, 6)

    private val career = Career(
        id = careerId,
        enrollmentTraitId = 100L,
        programId = 200L,
        easyStaffProgramCode = "E3101Q",
        academicYearEnrollmentId = 2025L,
        studentNumber = "123456",
        description = "Informatica",
        academicYear = 2024,
        status = CareerStatus.ACTIVE,
    )

    private val account = Account(
        id = accountId,
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "user-1",
            personId = 999L,
            fiscalCode = null,
            careers = listOf(career),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = 1,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val easyStaffApi = mockk<EasyStaffApi>(relaxed = true)
    private val dao = mockk<CalendarDao>(relaxed = true)
    private val syncStateDao = mockk<CalendarSyncStateDao>(relaxed = true)
    private val deadlineDao = mockk<DeadlineDao>(relaxed = true)
    private val studyPlanRepository = mockk<StudyPlanRepository>(relaxed = true)
    private val transcriptRepository = mockk<TranscriptRepository>(relaxed = true)
    private val examRepository = mockk<ExamRepository>(relaxed = true)
    private val elearningCourseRepository = mockk<ElearningCourseRepository>(relaxed = true)
    private val appointmentRepository = mockk<AppointmentRepository>(relaxed = true)
    private val libraryRepository = mockk<LibraryRepository>(relaxed = true)
    private val invalidator = CalendarSyncInvalidator()
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    /**
     * Wires every collaborator to its inert default and returns the repository under test on the
     * supplied scope. With `getState` null every source reads stale, so refreshes run their full
     * path; with an empty study plan the lesson job clears the slice without ever touching
     * EasyStaff; and the merge-at-observe streams start empty.
     */
    private fun newRepository(scope: CoroutineScope): CalendarRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        every { dao.observeInRange(any(), any(), any()) } returns flowOf(emptyList())
        every { deadlineDao.observeForAccount(any()) } returns flowOf(emptyList())
        every { appointmentRepository.observeReservations() } returns flowOf(emptyList())
        every { libraryRepository.observeReservations() } returns flowOf(emptyList())
        coEvery { studyPlanRepository.getPlannedCoursesForActiveCareer(any()) } returns emptyList()
        coEvery { transcriptRepository.getPassedCourseNames(any()) } returns emptySet()
        every { transcriptRepository.observeRows(any()) } returns flowOf(Loadable.Loaded(emptyList()))
        coEvery { examRepository.getBookings(any()) } returns emptyList()
        return CalendarRepositoryImpl(
            sessionManager = sessionManager,
            easyStaffApi = easyStaffApi,
            dao = dao,
            syncStateDao = syncStateDao,
            deadlineDao = deadlineDao,
            studyPlanRepository = studyPlanRepository,
            transcriptRepository = transcriptRepository,
            examRepository = examRepository,
            elearningCourseRepository = elearningCourseRepository,
            appointmentRepository = appointmentRepository,
            libraryRepository = libraryRepository,
            invalidator = invalidator,
            stalePolicy = stalePolicy,
            applicationScope = scope,
        )
    }

    private fun lessonRow(
        id: String,
        date: String,
        startTime: String,
    ): CalendarEventEntity = CalendarEventEntity(
        id = id,
        careerId = careerId.value,
        source = EventSource.LESSON.code,
        discriminator = CalendarEventDiscriminator.LESSON,
        date = date,
        startTime = startTime,
        endTime = "11:00",
        title = "Algoritmi",
        shortLabel = "ALG",
        room = null,
        building = null,
        mapsUrl = null,
        status = "CONFIRMED",
        notes = null,
        activityCode = null,
        subjectCode = null,
        teachersCsv = null,
        cfu = null,
        examTypeLabel = null,
        bookingPosition = null,
        bookedAt = null,
        cancellableUntil = null,
    )

    private fun appointmentReservation(start: String): AppointmentReservation {
        val startAt = java.time.LocalDateTime.parse("2026-06-15T$start")
        return AppointmentReservation(
            code = "APT-$start",
            email = "mario.rossi@campus.unimib.it",
            entryId = 1,
            serviceId = 10,
            serviceName = "Sportello",
            serviceGroup = "Carriere Studenti",
            areaName = null,
            areaAddress = null,
            start = startAt,
            end = startAt.plusMinutes(30),
            qrCodeDataUrl = null,
            webConferenceUrl = null,
        )
    }

    @Test
    fun `observeRange merges the stored and live sources sorted by date then start time`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { dao.observeInRange(any(), any(), any()) } returns flowOf(
            listOf(
                lessonRow(id = "lesson_b", date = "2026-06-20", startTime = "09:00"),
                lessonRow(id = "lesson_a", date = "2026-06-10", startTime = "14:00"),
            ),
        )
        every { appointmentRepository.observeReservations() } returns flowOf(
            listOf(appointmentReservation(start = "08:00")),
        )

        repository.observeMonth(careerId, month).test {
            val loaded = awaitItem()
            assertThat(loaded).isInstanceOf(Loadable.Loaded::class.java)
            val events = (loaded as Loadable.Loaded).value
            assertThat(events.map { it.id.value }).containsExactly(
                "lesson_a",
                "appointment_APT-08:00",
                "lesson_b",
            ).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeRange drops materialized rows that fail to rehydrate`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { dao.observeInRange(any(), any(), any()) } returns flowOf(
            listOf(
                lessonRow(id = "lesson_good", date = "2026-06-10", startTime = "09:00"),
                lessonRow(id = "lesson_bad", date = "not-a-date", startTime = "09:00"),
            ),
        )

        repository.observeMonth(careerId, month).test {
            val events = (awaitItem() as Loadable.Loaded).value
            assertThat(events.map { it.id.value }).containsExactly("lesson_good")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeUpcoming caps the merged result to the requested limit`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { dao.observeInRange(any(), any(), any()) } returns flowOf(
            listOf(
                lessonRow(id = "lesson_1", date = "2026-06-10", startTime = "09:00"),
                lessonRow(id = "lesson_2", date = "2026-06-11", startTime = "09:00"),
                lessonRow(id = "lesson_3", date = "2026-06-12", startTime = "09:00"),
            ),
        )

        repository.observeUpcoming(careerId, java.time.LocalDate.of(2026, 6, 1), limit = 2).test {
            val events = (awaitItem() as Loadable.Loaded).value
            assertThat(events.map { it.id.value }).containsExactly("lesson_1", "lesson_2").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeDeadlineEvents falls back to empty when the career is not on the active account`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        every { deadlineDao.observeForAccount(any()) } returns flowOf(
            listOf(
                DeadlineEntity(
                    accountId = accountId.value,
                    eventId = 1,
                    courseId = 5,
                    kind = DeadlineEntity.Kind.ASSIGNMENT,
                    instanceId = 9,
                    title = "Compito",
                    dueAtMs = java.time.LocalDateTime.of(2026, 6, 15, 10, 0)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                ),
            ),
        )
        val repository = newRepository(scope)

        repository.observeMonth(CareerId(999L), month).test {
            val events = (awaitItem() as Loadable.Loaded).value
            assertThat(events).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshMonth returns early when the career is not on the active account`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.refreshMonth(CareerId(404L), month, force = true)

        coVerify(exactly = 0) { examRepository.getBookings(any()) }
    }

    @Test
    fun `refreshMonth propagates a required exam-source failure`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { examRepository.getBookings(careerId) } throws IOException("offline")

        val thrown = runCatching {
            repository.refreshMonth(careerId, month, force = true)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
        assertThat(thrown).hasMessageThat().contains("offline")
    }

    @Test
    fun `refreshMonth swallows an appointment auxiliary failure`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        coEvery { appointmentRepository.refreshReservations() } throws IOException("appt down")
        val repository = newRepository(scope)

        repository.refreshMonth(careerId, month, force = true)

        coVerify { appointmentRepository.refreshReservations() }
    }

    @Test
    fun `refreshMonth swallows a generic library auxiliary failure`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        coEvery { libraryRepository.refreshReservations() } throws IOException("library down")
        val repository = newRepository(scope)

        repository.refreshMonth(careerId, month, force = true)

        coVerify { libraryRepository.refreshReservations() }
    }

    @Test
    fun `refreshMonth swallows a LibraryNotLoggedInException`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        coEvery { libraryRepository.refreshReservations() } throws LibraryNotLoggedInException()
        val repository = newRepository(scope)

        repository.refreshMonth(careerId, month, force = true)

        coVerify { libraryRepository.refreshReservations() }
    }

    @Test
    fun `refreshMonth stamps the library sync only after a successful refresh`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        coEvery { libraryRepository.refreshReservations() } throws LibraryNotLoggedInException()
        val repository = newRepository(scope)

        repository.refreshMonth(careerId, month, force = true)

        coVerify(exactly = 0) {
            syncStateDao.upsertState(match { it.source == EventSource.LIBRARY.code })
        }
    }

    @Test
    fun `bus invalidation drops the affected source sync stamps`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        newRepository(scope)

        invalidator.invalidate(careerId, EventSource.LESSON)

        coVerify {
            syncStateDao.deleteStatesForSource(careerId.value, EventSource.LESSON.code)
        }
    }
}
