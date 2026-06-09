package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.exam.toBookedExam
import it.attendance100.mybicocca.data.mapper.exam.toDomain
import it.attendance100.mybicocca.data.mapper.exam.toExamResult
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AcknowledgmentOfReceipt
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookableExamFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollmentParameters
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : ExamRepository {

    override suspend fun getExamCalls(careerId: CareerId): List<ExamCall> {
        val career = requireCareer(careerId)
        // Also asks for the booking-sheet fields (note, presidente*, tipoGestPrenDes) so the
        // modal needs no per-appello detail GET — that endpoint costs 1.7-4s cold per appello
        // server-side and nothing (actor pinning, fields projection) avoids it. Verified live:
        // the list returns them all. See scripts/esse3_booking_benchmark.py.
        val dtos = sessionManager.esse3().transcript.getRecordBookExamCalls(
            matId = career.enrollmentTraitId,
            q = Esse3BookableExamFilter.AppelliPrenotabiliEFuturi,
            order = "+dataInizioApp",
            optionalFields = "dataInizioApp,oraEsa,dataInizioIscr,dataFineIscr,tipoEsaCod," +
                "note,presidenteNome,presidenteCognome,presidenteId,tipoGestPrenDes",
        )
        return withContext(Dispatchers.Default) { dtos.mapNotNull { it.toDomain() } }
    }

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
        // POST returns no body; the enrollment is implicit. Construct the confirmation
        // locally from what we already know to save a round-trip.
        return ExamBooking(
            key = key,
            applicationListId = null,
            studentId = career.enrollmentTraitId,
            activityChoiceId = request.activityChoiceId,
        )
    }

    override suspend fun getBookings(careerId: CareerId): List<BookedExam> {
        val career = requireCareer(careerId)
        // Empirically verified against the live server:
        //  - attoreCod=STU avoids the implicit-actor lookup, ~4× faster cold
        //  - NO date filter: the full booking history (future + past) comes back in one
        //    call (72 rows / back to 2023 for a 3rd-year student), so the modal can split
        //    it into Attive (upcoming) and Passate (already sat) without a second request.
        //  - order DESC on dataOraTurno: most-recent first, which is the Passate order; the
        //    Attive section re-sorts ascending client-side.
        //  - the outcome fields (esito/pubblId/notaPubbl) ride along so a past appello shows
        //    its grade without hitting the esiti endpoint. esito carries superatoFlg=0 even
        //    before publication, so the mapper only trusts it when pubblId is set.
        val dtos = sessionManager.esse3().transcript.getBookingsByMatId(
            matId = career.enrollmentTraitId,
            actorCode = "STU",
            order = "-dataOraTurno",
            // tipoAppCod (prova parziale/finale) and dataFineIscr (cancellation deadline)
            // are optional too — verified live: they only come back when requested.
            optionalFields = "adStuDes,desAppello,aulaDes,edificioDes,dataInizioApp,dataOraTurno," +
                "tipoEsaCod,tipoAppCod,dataFineIscr,esito,pubblId,notaPubbl",
        )
        return withContext(Dispatchers.Default) { dtos.mapNotNull { it.toBookedExam() } }
    }

    override suspend fun getExamResults(careerId: CareerId): List<ExamResult> {
        val career = requireCareer(careerId)
        // Empirically verified: filter=pubblId=gt=0 does a server-side DB filter for
        // bookings with a published outcome (~37 rows / 37 KB for me vs 67 / 67 KB with
        // no filter). Esse3's canonical q=BACHECA_ESITI is too narrow — it only catches
        // outcomes whose acknowledgment deadline has passed, which excludes most of what
        // a student wants to see ("show me my recent grades").
        val dtos = sessionManager.esse3().transcript.getBookingsByMatId(
            matId = career.enrollmentTraitId,
            actorCode = "STU",
            filter = "pubblId=gt=0",
            order = "-dataOraTurno",
            optionalFields = "adStuDes,desAppello,dataOraTurno,notaPubbl,dataRifEsitoStu,pubblId",
        )
        return withContext(Dispatchers.Default) { dtos.mapNotNull { it.toExamResult() } }
    }

    override suspend fun acknowledgeExamResult(
        careerId: CareerId,
        applicationListId: Long,
        accept: Boolean,
    ) {
        val career = requireCareer(careerId)
        // PUT /prenotazioni/{matId}/{applicationListId}/presaVisione — STUDENT-scoped.
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

    override suspend fun cancelBooking(careerId: CareerId, key: ExamCallKey, studentId: Long) {
        requireCareer(careerId)
        // studentId is the booking's `stuId` (career-of-student id), NOT the user's matId.
        // The Booked screen carries it through from getBookings → BookedExam.studentId.
        sessionManager.esse3().examsCalendar.deleteExamCallEnrolledList(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
            studentId = studentId,
        )
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

    // Fully reads a streamed PDF response into memory off the main thread.
    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }
}
