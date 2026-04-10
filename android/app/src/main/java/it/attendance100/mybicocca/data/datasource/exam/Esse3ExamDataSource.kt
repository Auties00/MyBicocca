package it.attendance100.mybicocca.data.datasource.exam

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExamSessionEnrollmentParameters
import it.attendance100.mybicocca.data.model.document.AppDocument
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.data.util.toAppDocument
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3ExamDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    suspend fun getExamCalls(careerId: Long, matricolaId: Long? = null): List<ExamCall> =
        withContext(ioDispatcher) {
            if (matricolaId == null) return@withContext emptyList()
            val todayFormatted = LocalDate.now().format(dateFormatter)
            esse3Api.transcript
                .getRecordBookRows(
                    matId = matricolaId,
                    optionalFields = "ALL",
                    filter = "dataInizioApp>=\"$todayFormatted\""
                )
                .map { activity ->
                    async {
                        val activityChoiceId = activity.activityChoiceId
                        esse3Api.transcript
                            .getExamCallsByRecordBookRow(
                                matId = matricolaId,
                                activityChoiceId = activityChoiceId,
                            )
                            .mapNotNull { session ->
                                val callId = session.callId?.toLong() ?: return@mapNotNull null
                                val parsedDate = parseDate(session.callStartDate)
                                ExamCall(
                                    id = session.examCallId ?: buildFallbackCallId(
                                        courseOfStudyId = session.courseOfStudyId,
                                        activityId = session.activityId,
                                        callId = callId,
                                        activityChoiceId = activityChoiceId,
                                    ),
                                    careerId = careerId,
                                    courseOfStudyId = session.courseOfStudyId ?: 0L,
                                    activityId = session.activityId ?: 0L,
                                    callId = callId,
                                    activityChoiceId = activityChoiceId,
                                    activityName = session.studentActivityDescription
                                        ?: session.activityDescription
                                        ?: "",
                                    activityCode = session.studentActivityCode ?: session.activityCode,
                                    date = parsedDate,
                                    startTime = parseTime(session.graduationTime),
                                    endTime = null,
                                    room = null,
                                    building = null,
                                    enrollmentStartDate = session.enrollmentStartDate,
                                    enrollmentEndDate = session.enrollmentEndDate,
                                    enrolledCount = session.enrolledNumber,
                                    stateDescription = session.stateDescription,
                                    callDescription = session.callDescription,
                                    examiners = null,
                                )
                            }
                    }
                }
                .awaitAll()
                .flatten()
                .distinctBy { it.id }
                .sortedWith(compareBy(nullsLast()) { it.date })
        }

    suspend fun getExamCallDetail(examCall: ExamCall): ExamCall = withContext(ioDispatcher) {
        if (examCall.courseOfStudyId == 0L || examCall.activityId == 0L || examCall.callId == 0L) {
            return@withContext examCall
        }

        val detail = esse3Api.examsCalendar.getExamCall(
            courseOfStudyId = examCall.courseOfStudyId,
            activityId = examCall.activityId,
            callId = examCall.callId,
            optionalFields = "ALL",
        )
        val firstShift = detail.shifts.minByOrNull { shift ->
            parseDateTime(shift.graduationDateTime) ?: LocalDateTime.MAX
        }
        val examiners = detail.committee
            .mapNotNull { teacher ->
                listOfNotNull(teacher.lecturerName, teacher.lecturerSurname)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
            }
            .distinct()

        examCall.copy(
            startTime = firstShift?.graduationDateTime?.let { parseDateTime(it)?.toLocalTime() }
                ?: examCall.startTime,
            building = firstShift?.buildingDescription ?: examCall.building,
            room = firstShift?.classroomDescription ?: examCall.room,
            stateDescription = detail.stateDescription ?: examCall.stateDescription,
            callDescription = detail.callDescription ?: examCall.callDescription,
            examiners = examiners.ifEmpty { examCall.examiners },
        )
    }

    suspend fun getBookings(matricolaId: Long?): List<ExamBooking> = withContext(ioDispatcher) {
        if (matricolaId == null) return@withContext emptyList()

        val nowFormatted = LocalDateTime.now().format(dateTimeFormatter)
        val values = esse3Api.examsCalendar.getBookingsByMatId(
            matId = matricolaId,
            optionalFields = "ALL",
            filter = "dataOraTurno>=\"$nowFormatted\"",
        )

        values.mapNotNull { enrollment ->
            val id = enrollment.applicationListId ?: return@mapNotNull null
            val courseOfStudyId = enrollment.courseOfStudyId ?: return@mapNotNull null
            val activityId = enrollment.activityId?.toLong() ?: return@mapNotNull null
            val callId = enrollment.callId?.toLong() ?: return@mapNotNull null
            val studentId = enrollment.studentId?.toLong() ?: return@mapNotNull null
            val activityChoiceId = enrollment.activityChoiceId ?: return@mapNotNull null
            val matId = enrollment.matId ?: matricolaId

            ExamBooking(
                id = id,
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                studentId = studentId,
                activityChoiceId = activityChoiceId,
                matricolaId = matId,
                activityCode = enrollment.studentActivityCode,
                activityName = enrollment.studentActivityDescription ?: "",
                examDate = enrollment.shiftDateTime,
                bookingDate = enrollment.insertionDate,
                position = enrollment.position,
                callDescription = enrollment.examCallDescription,
                building = enrollment.buildingDescription,
                room = enrollment.classroomDescription,
            )
        }
    }

    suspend fun bookExam(examCall: ExamCall) = withContext(ioDispatcher) {
        require(examCall.courseOfStudyId != 0L && examCall.activityId != 0L && examCall.callId != 0L) {
            "Dati appello incompleti"
        }
        require(examCall.activityChoiceId != 0L) {
            "Manca l'identificativo dell'attività da prenotare"
        }

        esse3Api.examsCalendar.postExamCallEnrolledList(
            courseOfStudyId = examCall.courseOfStudyId,
            activityId = examCall.activityId,
            callId = examCall.callId,
            body = Esse3ExamSessionEnrollmentParameters(
                activityChoiceId = examCall.activityChoiceId,
            ),
        )
    }

    suspend fun cancelBooking(booking: ExamBooking) = withContext(ioDispatcher) {
        require(booking.courseOfStudyId != 0L && booking.activityId != 0L && booking.callId != 0L) {
            "Dati prenotazione incompleti"
        }
        require(booking.studentId != 0L) {
            "Manca l'identificativo dello studente"
        }

        esse3Api.examsCalendar.deleteExamCallEnrolledList(
            courseOfStudyId = booking.courseOfStudyId,
            activityId = booking.activityId,
            callId = booking.callId,
            studentId = booking.studentId,
        )
    }

    suspend fun getBookingStatino(booking: ExamBooking): AppDocument = withContext(ioDispatcher) {
        require(booking.activityChoiceId != 0L && booking.matricolaId != 0L) {
            "Dati prenotazione incompleti"
        }

        esse3Api.transcript.getBookingStatinoByApplicationListId(
            matId = booking.matricolaId,
            activityChoiceId = booking.activityChoiceId,
            applicationListId = booking.id,
        ).toAppDocument(fileName = "statino_prenotazione_${booking.id}.pdf")
    }

    suspend fun getPresenceCertificate(booking: ExamBooking): AppDocument = withContext(ioDispatcher) {
        require(booking.activityChoiceId != 0L && booking.matricolaId != 0L) {
            "Dati prenotazione incompleti"
        }

        esse3Api.transcript.getPresenceCertificateByApplicationListId(
            matId = booking.matricolaId,
            activityChoiceId = booking.activityChoiceId,
            applicationListId = booking.id,
        ).toAppDocument(fileName = "attestato_presenza_${booking.id}.pdf")
    }

    private fun parseDate(value: String?): LocalDate? =
        value?.let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }

    private fun parseDateTime(value: String?): LocalDateTime? =
        value?.let { runCatching { LocalDateTime.parse(it, dateTimeFormatter) }.getOrNull() }

    private fun parseTime(value: String?): LocalTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { LocalTime.parse(value) }.getOrNull()
            ?: runCatching { LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm:ss")) }.getOrNull()
            ?: parseDateTime(value)?.toLocalTime()
    }

    private fun buildFallbackCallId(
        courseOfStudyId: Long?,
        activityId: Long?,
        callId: Long,
        activityChoiceId: Long,
    ): Long = "${courseOfStudyId ?: 0L}_${activityId ?: 0L}_${callId}_${activityChoiceId}"
        .hashCode()
        .toLong()
}
