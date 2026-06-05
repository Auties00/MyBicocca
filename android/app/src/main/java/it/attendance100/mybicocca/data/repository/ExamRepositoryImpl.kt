package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.exam.ESSE3_DATE
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
import it.attendance100.mybicocca.domain.model.exam.ExamCallDetail
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.repository.ExamRepository
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : ExamRepository {

    override suspend fun getExamCalls(careerId: CareerId): List<ExamCall> {
        val career = requireCareer(careerId)
        val dtos = sessionManager.esse3().transcript.getRecordBookExamCalls(
            matId = career.enrollmentTraitId,
            q = Esse3BookableExamFilter.AppelliPrenotabiliEFuturi,
            order = "+dataInizioApp",
            optionalFields = "dataInizioApp,oraEsa,dataInizioIscr,dataFineIscr",
        )
        return withContext(Dispatchers.Default) { dtos.mapNotNull { it.toDomain() } }
    }

    override suspend fun getExamCallDetail(
        careerId: CareerId,
        key: ExamCallKey,
    ): ExamCallDetail {
        requireCareer(careerId)
        val dto = sessionManager.esse3().examsCalendar.getExamCall(
            courseOfStudyId = key.courseOfStudyId,
            activityId = key.activityId,
            callId = key.callId.toLong(),
        )
        return withContext(Dispatchers.Default) {
            dto.toDomain() ?: error("Esse3 returned an exam call without identity keys.")
        }
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
        //  - filter on dataOraTurno (combined date+time), NOT dataEsa (often blank)
        //  - optionalFields requests the UI-needed fields not in the default response
        //  - order server-side avoids client re-sort
        val today = LocalDate.now().format(ESSE3_DATE)
        val dtos = sessionManager.esse3().transcript.getBookingsByMatId(
            matId = career.enrollmentTraitId,
            actorCode = "STU",
            filter = "dataOraTurno=ge=$today",
            order = "+dataOraTurno",
            // tipoAppCod (prova parziale/finale) and dataFineIscr (cancellation deadline)
            // are optional too — verified live: they only come back when requested.
            optionalFields = "adStuDes,desAppello,aulaDes,edificioDes,dataInizioApp,dataOraTurno,tipoEsaCod,tipoAppCod,dataFineIscr",
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
