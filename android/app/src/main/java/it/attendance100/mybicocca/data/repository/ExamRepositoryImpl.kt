package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.exam.ExamCacheDao
import it.attendance100.mybicocca.data.mapper.exam.toBookedExam
import it.attendance100.mybicocca.data.mapper.exam.toDomain
import it.attendance100.mybicocca.data.mapper.exam.toEntity
import it.attendance100.mybicocca.data.mapper.exam.toExamResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AcknowledgmentOfReceipt
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookableExamFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollmentParameters
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.repository.ExamRepository
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live Esse3 implementation of the exam contract. Booking data is volatile (seat counts,
 * windows) so every list read hits the server first and is written through to the offline exam
 * mirror ([ExamCacheDao]); the mirror is read back only when a live call fails with an
 * [IOException] (no network), so the appelli screens stay readable offline while every booking
 * action stays disabled. An offline read with no cached rows rethrows, surfacing the error state
 * rather than a misleading empty list.
 *
 * Endpoints: bookable calls and bookings come from the libretto-scoped lists
 * (`/libretti/{matId}/appelli`, `/libretti/{matId}/prenotazioni`); booking, cancellation
 * and the PDFs go through the per-appello calesa routes
 * (`/appelli/{cds}/{ad}/{app}/...`); outcome acknowledgment is a student-scoped PUT on
 * the prenotazione.
 */
@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val calendarInvalidator: CalendarSyncInvalidator,
    private val examCacheDao: ExamCacheDao,
) : ExamRepository {

    /**
     * Loads bookable and future calls in one list request, asking the booking-sheet
     * fields (note, presidente*, tipoGestPrenDes) as optional fields so the modal needs
     * no per-appello detail GET — that endpoint costs 1.7–4s cold per appello
     * server-side and nothing (actor pinning, fields projection) avoids it. Verified
     * live: the list returns them all. See scripts/esse3_booking_benchmark.py.
     */
    override suspend fun getExamCalls(careerId: CareerId): List<ExamCall> {
        val career = requireCareer(careerId)
        return try {
            val dtos = sessionManager.esse3().transcript.getRecordBookExamCalls(
                matId = career.enrollmentTraitId,
                q = Esse3BookableExamFilter.AppelliPrenotabiliEFuturi,
                order = "+dataInizioApp",
                optionalFields = "dataInizioApp,oraEsa,dataInizioIscr,dataFineIscr,tipoEsaCod," +
                    "note,presidenteNome,presidenteCognome,presidenteId,tipoGestPrenDes",
            )
            val calls = withContext(Dispatchers.Default) { dtos.mapNotNull { it.toDomain() } }
            examCacheDao.replaceCalls(
                careerId.value,
                calls.mapIndexed { index, call -> call.toEntity(careerId, index) },
            )
            calls
        } catch (e: IOException) {
            examCacheDao.getCalls(careerId.value).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    /**
     * POSTs the enrollment on the call's iscritti list, then invalidates the calendar's
     * exam source — the calendar mirrors bookings, and the nudge makes the new exam show
     * up right away. The POST returns no body (the enrollment is implicit), so the
     * confirmation is constructed locally from already-known values to save a
     * round-trip.
     */
    override suspend fun bookExam(
        careerId: CareerId,
        key: ExamCallKey,
        request: BookExamRequest,
    ): ExamBooking {
        val career = requireCareer(careerId)
        sessionManager.esse3().examsCalendar.postExamCallEnrolledList(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
            body = Esse3ExamSessionEnrollmentParameters(
                activityChoiceId = request.activityChoiceId,
                studentNote = request.studentNote,
            ),
        )
        calendarInvalidator.invalidate(careerId, EventSource.EXAM)
        return ExamBooking(
            key = key,
            applicationListId = null,
            studentId = career.enrollmentTraitId,
            activityChoiceId = request.activityChoiceId,
        )
    }

    /**
     * Loads the full booking history with request parameters empirically verified
     * against the live server:
     *  - `attoreCod=STU` avoids the implicit-actor lookup, ~4× faster cold;
     *  - no date filter: the complete history (future + past) comes back in one call
     *    (72 rows / back to 2023 for a 3rd-year student), so the modal can split it into
     *    Attive (upcoming) and Passate (already sat) without a second request;
     *  - order DESC on `dataOraTurno`: most-recent first, which is the Passate order;
     *    the Attive section re-sorts ascending client-side;
     *  - the outcome fields (esito/pubblId/notaPubbl) ride along so a past appello shows
     *    its grade without hitting the esiti endpoint — esito carries `superatoFlg=0`
     *    even before publication, so the mapper only trusts it when pubblId is set;
     *  - `tipoAppCod` (prova parziale/finale) and `dataFineIscr` (cancellation deadline)
     *    are optional fields too, verified live to come back only when requested.
     */
    override suspend fun getBookings(careerId: CareerId): List<BookedExam> {
        val career = requireCareer(careerId)
        return try {
            val dtos = sessionManager.esse3().transcript.getBookingsByMatId(
                matId = career.enrollmentTraitId,
                actorCode = "STU",
                order = "-dataOraTurno",
                optionalFields = "adStuDes,desAppello,aulaDes,edificioDes,dataInizioApp,dataOraTurno," +
                    "tipoEsaCod,tipoAppCod,dataFineIscr,esito,pubblId,notaPubbl",
            )
            val bookings = withContext(Dispatchers.Default) { dtos.mapNotNull { it.toBookedExam() } }
            examCacheDao.replaceBookings(
                careerId.value,
                bookings.mapIndexed { index, booking -> booking.toEntity(careerId, index) },
            )
            bookings
        } catch (e: IOException) {
            examCacheDao.getBookings(careerId.value).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    /**
     * Loads bookings with a published outcome via `filter=pubblId=gt=0`, an empirically
     * verified server-side DB filter (~37 rows / 37 KB versus 67 / 67 KB unfiltered on a
     * reference career). Esse3's canonical `q=BACHECA_ESITI` is too narrow — it only
     * catches outcomes whose acknowledgment deadline has passed, which excludes most of
     * what a student wants to see ("show me my recent grades").
     */
    override suspend fun getExamResults(careerId: CareerId): List<ExamResult> {
        val career = requireCareer(careerId)
        return try {
            val dtos = sessionManager.esse3().transcript.getBookingsByMatId(
                matId = career.enrollmentTraitId,
                actorCode = "STU",
                filter = "pubblId=gt=0",
                order = "-dataOraTurno",
                optionalFields = "adStuDes,desAppello,dataOraTurno,notaPubbl,dataRifEsitoStu,pubblId",
            )
            val results = withContext(Dispatchers.Default) { dtos.mapNotNull { it.toExamResult() } }
            examCacheDao.replaceResults(
                careerId.value,
                results.mapIndexed { index, result -> result.toEntity(careerId, index) },
            )
            results
        } catch (e: IOException) {
            examCacheDao.getResults(careerId.value).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    /** PUT `/prenotazioni/{matId}/{applicationListId}/presaVisione` — student-scoped. */
    override suspend fun acknowledgeExamResult(
        careerId: CareerId,
        applicationListId: Long,
        accept: Boolean,
    ) {
        val career = requireCareer(careerId)
        sessionManager.esse3().examsCalendar.putAcknowledgmentOfReceiptApplicationList(
            matId = career.enrollmentTraitId,
            applicationListId = applicationListId,
            acknowledgmentOfReceipt = if (accept) {
                Esse3AcknowledgmentOfReceipt.Accepted
            } else {
                Esse3AcknowledgmentOfReceipt.Rejected
            },
        )
    }

    /**
     * DELETEs the enrollment from the call's iscritti list and invalidates the
     * calendar's exam source. studentId is the booking's `stuId` (career-of-student id),
     * NOT the user's `matId`; the booked-exams screen carries it through from
     * [getBookings] on the booked exam.
     */
    override suspend fun cancelBooking(careerId: CareerId, key: ExamCallKey, studentId: Long) {
        requireCareer(careerId)
        sessionManager.esse3().examsCalendar.deleteExamCallEnrolledList(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
            studentId = studentId,
        )
        calendarInvalidator.invalidate(careerId, EventSource.EXAM)
    }

    override suspend fun getBookingSlip(
        careerId: CareerId,
        key: ExamCallKey,
        studentId: Long,
    ): ByteArray {
        requireCareer(careerId)
        return sessionManager.esse3().examsCalendar.getBookingStatino(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
            studentId = studentId,
        ).drainToByteArray()
    }

    override suspend fun getPresenceCertificate(
        careerId: CareerId,
        key: ExamCallKey,
        studentId: Long,
    ): ByteArray {
        requireCareer(careerId)
        return sessionManager.esse3().examsCalendar.getPresenceCertificate(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
            studentId = studentId,
        ).drainToByteArray()
    }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for exam booking.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    /** Fully reads a streamed PDF response into memory off the main thread. */
    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }
}
