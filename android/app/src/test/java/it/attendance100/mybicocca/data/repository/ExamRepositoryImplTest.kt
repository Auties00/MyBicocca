package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.exam.BookedExamEntity
import it.attendance100.mybicocca.data.local.exam.ExamCacheDao
import it.attendance100.mybicocca.data.local.exam.ExamCallEntity
import it.attendance100.mybicocca.data.local.exam.ExamResultEntity
import it.attendance100.mybicocca.data.mapper.exam.toEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionTranscript
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Live-first triad for the Esse3 exam repository: each list read (calls, bookings,
 * results) maps the DTOs and writes through to [ExamCacheDao] on success, returns the
 * cached rows on [IOException] when the mirror is non-empty, and rethrows when it is
 * empty. Booking mutations must nudge the calendar's EXAM source.
 */
class ExamRepositoryImplTest {

    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val calendarInvalidator: CalendarSyncInvalidator = mockk(relaxed = true)
    private val examCacheDao: ExamCacheDao = mockk(relaxed = true)
    private val esse3Api: Esse3Api = mockk(relaxed = true)

    private val careerId = RepositoryTestFixtures.careerId
    private val matId = RepositoryTestFixtures.ENROLLMENT_TRAIT_ID

    private lateinit var repository: ExamRepositoryImpl

    @Before
    fun setUp() {
        every { sessionManager.activeAccount } returns MutableStateFlow(RepositoryTestFixtures.account())
        coEvery { sessionManager.esse3() } returns esse3Api
        repository = ExamRepositoryImpl(sessionManager, calendarInvalidator, examCacheDao)
    }

    private fun transcriptDto() = Esse3ExamSessionTranscript(
        courseOfStudyId = 10L,
        activityId = 20L,
        callId = 3,
        activityDescription = "Analisi Matematica",
        callDescription = "Appello di gennaio",
    )

    private fun enrollmentDto(publicationId: Long? = null) = Esse3ExamSessionEnrollment(
        applicationListId = 555L,
        courseOfStudyId = 10L,
        activityId = 20,
        callId = 3,
        studentId = 77,
        studentActivityDescription = "Analisi Matematica",
        examCallDescription = "Appello di gennaio",
        publicationId = publicationId,
    )

    @Test
    fun `getExamCalls success maps domain and writes through to the cache`() = runTest {
        coEvery {
            esse3Api.transcript.getRecordBookExamCalls(any(), any(), any(), any(), any(), any(), any())
        } returns listOf(transcriptDto())

        val result = repository.getExamCalls(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().key).isEqualTo(ExamCallKey(10L, 20L, 3))
        assertThat(result.single().activityDescription).isEqualTo("Analisi Matematica")
        coVerify(exactly = 1) { examCacheDao.replaceCalls(careerId.value, any()) }
    }

    @Test
    fun `getExamCalls offline with cache returns the mirrored rows`() = runTest {
        coEvery {
            esse3Api.transcript.getRecordBookExamCalls(any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getCalls(careerId.value) } returns listOf(cachedCallEntity())

        val result = repository.getExamCalls(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().key).isEqualTo(ExamCallKey(10L, 20L, 3))
        coVerify(exactly = 0) { examCacheDao.replaceCalls(any(), any()) }
    }

    @Test
    fun `getExamCalls offline with empty cache rethrows`() = runTest {
        coEvery {
            esse3Api.transcript.getRecordBookExamCalls(any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getCalls(careerId.value) } returns emptyList()

        val thrown = runCatching { repository.getExamCalls(careerId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getBookings success maps domain and writes through to the cache`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(enrollmentDto())

        val result = repository.getBookings(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().key).isEqualTo(ExamCallKey(10L, 20L, 3))
        assertThat(result.single().studentId).isEqualTo(77L)
        coVerify(exactly = 1) { examCacheDao.replaceBookings(careerId.value, any()) }
    }

    @Test
    fun `getBookings offline with cache returns the mirrored rows`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getBookings(careerId.value) } returns listOf(cachedBookingEntity())

        val result = repository.getBookings(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().key).isEqualTo(ExamCallKey(10L, 20L, 3))
        coVerify(exactly = 0) { examCacheDao.replaceBookings(any(), any()) }
    }

    @Test
    fun `getBookings offline with empty cache rethrows`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getBookings(careerId.value) } returns emptyList()

        val thrown = runCatching { repository.getBookings(careerId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getExamResults success maps domain and writes through to the cache`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(enrollmentDto(publicationId = 88L))

        val result = repository.getExamResults(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().publicationId).isEqualTo(88L)
        coVerify(exactly = 1) { examCacheDao.replaceResults(careerId.value, any()) }
    }

    @Test
    fun `getExamResults offline with cache returns the mirrored rows`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getResults(careerId.value) } returns listOf(cachedResultEntity())

        val result = repository.getExamResults(careerId)

        assertThat(result).hasSize(1)
        assertThat(result.single().publicationId).isEqualTo(88L)
        coVerify(exactly = 0) { examCacheDao.replaceResults(any(), any()) }
    }

    @Test
    fun `getExamResults offline with empty cache rethrows`() = runTest {
        coEvery {
            esse3Api.transcript.getBookingsByMatId(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IOException("offline")
        coEvery { examCacheDao.getResults(careerId.value) } returns emptyList()

        val thrown = runCatching { repository.getExamResults(careerId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `bookExam invalidates the calendar exam source`() = runTest {
        val key = ExamCallKey(10L, 20L, 3)

        repository.bookExam(careerId, key, BookExamRequest(activityChoiceId = 99L))

        coVerify(exactly = 1) {
            esse3Api.examsCalendar.postExamCallEnrolledList(10L, 20L, 3L, any())
        }
        verifyExamInvalidated()
    }

    @Test
    fun `bookExam builds the confirmation from known values`() = runTest {
        val key = ExamCallKey(10L, 20L, 3)

        val booking = repository.bookExam(careerId, key, BookExamRequest(activityChoiceId = 99L))

        assertThat(booking.key).isEqualTo(key)
        assertThat(booking.applicationListId).isNull()
        assertThat(booking.activityChoiceId).isEqualTo(99L)
        assertThat(booking.studentId).isEqualTo(matId)
    }

    @Test
    fun `cancelBooking invalidates the calendar exam source`() = runTest {
        val key = ExamCallKey(10L, 20L, 3)

        repository.cancelBooking(careerId, key, studentId = 77L)

        coVerify(exactly = 1) {
            esse3Api.examsCalendar.deleteExamCallEnrolledList(10L, 20L, 3L, 77L)
        }
        verifyExamInvalidated()
    }

    @Test
    fun `requireCareer errors when the career is absent from the active account`() = runTest {
        coEvery {
            esse3Api.transcript.getRecordBookExamCalls(any(), any(), any(), any(), any(), any(), any())
        } returns emptyList()

        val thrown = runCatching { repository.getExamCalls(CareerId(1L)) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }

    private fun verifyExamInvalidated() {
        val careerSlot = slot<CareerId>()
        val sourceSlot = slot<EventSource>()
        verify(exactly = 1) { calendarInvalidator.invalidate(capture(careerSlot), capture(sourceSlot)) }
        assertThat(careerSlot.captured).isEqualTo(careerId)
        assertThat(sourceSlot.captured).isEqualTo(EventSource.EXAM)
    }

    private fun cachedCallEntity(): ExamCallEntity =
        ExamCall(
            key = ExamCallKey(10L, 20L, 3),
            examCallId = 1L,
            activityChoiceId = null,
            activityCode = null,
            activityDescription = "Analisi Matematica",
            courseOfStudyDescription = null,
            callDescription = "Appello di gennaio",
            callDate = null,
            callTime = null,
            enrollmentWindow = ExamEnrollmentWindow(opensAt = null, closesAt = null),
            enrolledNumber = null,
            state = null,
            stateDescription = null,
            callType = ExamCallType.Final,
            examType = ExamType.Written,
            isReserved = false,
            matId = matId,
            notes = null,
            president = null,
            bookingTypeDescription = null,
        ).toEntity(careerId, 0)

    private fun cachedBookingEntity(): BookedExamEntity =
        BookedExam(
            key = ExamCallKey(10L, 20L, 3),
            applicationListId = 555L,
            studentId = 77L,
            activityChoiceId = null,
            activityDescription = "Analisi Matematica",
            examCallDescription = "Appello di gennaio",
            examType = ExamType.Written,
            callType = ExamCallType.Final,
            examDateTime = null,
            classroomDescription = null,
            buildingDescription = null,
            credits = null,
            examModeDescription = null,
            position = null,
            bookingDate = null,
            cancellableUntil = null,
            studentNote = null,
        ).toEntity(careerId, 0)

    private fun cachedResultEntity(): ExamResultEntity =
        ExamResult(
            key = ExamCallKey(10L, 20L, 3),
            applicationListId = 555L,
            publicationId = 88L,
            activityDescription = "Analisi Matematica",
            examDateTime = null,
            grade = ExamGrade.Numeric(30),
            acknowledgment = AcknowledgmentStatus.NotViewed,
            publishedNote = null,
            acknowledgmentDeadline = null,
        ).toEntity(careerId, 0)
}
