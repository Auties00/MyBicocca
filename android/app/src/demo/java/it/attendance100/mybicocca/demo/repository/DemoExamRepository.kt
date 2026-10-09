package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoActivity
import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamExaminer
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.repository.ExamRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two summer calls for each course being attended, two of them already booked (the same exams
 * the demo calendar shows) and two published results, one still waiting for a decision.
 * Booking, cancelling and accepting change the lists in memory.
 */
@Singleton
class DemoExamRepository @Inject constructor() : ExamRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private data class Call(
        val course: DemoCourse,
        val number: Int,
        val date: LocalDate,
        val time: LocalTime,
        val room: String,
        val building: String,
        val enrolled: Int,
    ) {
        val activity: DemoActivity get() = DemoCareer.activity(course)
        val key: ExamCallKey get() = DemoCareer.callKey(activity, number)
    }

    private val calls = listOf(
        Call(DemoCourses.statistics, 1, today.plusDays(8), LocalTime.of(9, 30), "U4-08", "U4", 84),
        Call(DemoCourses.statistics, 2, today.plusDays(29), LocalTime.of(9, 30), "U4-08", "U4", 31),
        Call(DemoCourses.algorithms, 1, today.plusDays(13), LocalTime.of(14, 0), "U6-06", "U6", 112),
        Call(DemoCourses.algorithms, 2, today.plusDays(34), LocalTime.of(14, 0), "U6-06", "U6", 47),
        Call(DemoCourses.databases, 1, today.plusDays(11), LocalTime.of(10, 0), "U24-C2", "U24", 96),
        Call(DemoCourses.databases, 2, today.plusDays(32), LocalTime.of(10, 0), "U24-C2", "U24", 22),
        Call(DemoCourses.operatingSystems, 1, today.plusDays(19), LocalTime.of(9, 0), "U14-T024", "U14", 73),
        Call(DemoCourses.operatingSystems, 2, today.plusDays(40), LocalTime.of(9, 0), "U14-T024", "U14", 15),
        Call(DemoCourses.languages, 1, today.plusDays(22), LocalTime.of(14, 30), "U1-09", "U1", 58),
        Call(DemoCourses.languages, 2, today.plusDays(43), LocalTime.of(14, 30), "U1-09", "U1", 9),
    )

    private val bookings = mutableListOf(
        booked(calls[0], position = 37, bookedOn = today.minusDays(9)),
        booked(calls[2], position = 12, bookedOn = today.minusDays(9)),
    )

    private val results = mutableListOf(
        result(20009, "RICERCA OPERATIVA", 28, today.minusDays(133), AcknowledgmentStatus.Accepted, deadline = null),
        ExamResult(
            key = ExamCallKey(DemoCareer.COURSE_OF_STUDY_ID, DemoCareer.WEB_LAB_ID, 1),
            applicationListId = 920110,
            publicationId = 930110,
            activityDescription = DemoCareer.activities.first { it.id == DemoCareer.WEB_LAB_ID }.name,
            examDateTime = today.minusDays(6).atTime(9, 30),
            grade = ExamGrade.Numeric(27),
            acknowledgment = AcknowledgmentStatus.NotViewed,
            publishedNote = "Visione dei compiti venerdì alle 14:00 in U14, studio 2041.",
            acknowledgmentDeadline = today.plusDays(5),
        ),
    )

    override suspend fun getExamCalls(careerId: CareerId): List<ExamCall> = calls.map { call ->
        ExamCall(
            key = call.key,
            examCallId = call.activity.id * 10 + call.number,
            activityChoiceId = call.activity.id,
            activityCode = call.course.code,
            activityDescription = call.course.name,
            courseOfStudyDescription = DemoCareer.DEGREE,
            callDescription = "Appello ${call.number} sessione estiva",
            callDate = call.date,
            callTime = call.time,
            enrollmentWindow = ExamEnrollmentWindow(opensAt = call.date.minusDays(30), closesAt = call.date.minusDays(3)),
            enrolledNumber = call.enrolled,
            state = "A",
            stateDescription = "Prenotazioni aperte",
            callType = ExamCallType.Final,
            examType = ExamType.Written,
            isReserved = false,
            matId = careerId.value,
            notes = "Presentarsi con il badge universitario. ${call.building}, aula ${call.room}.",
            president = examiner(call.course),
            bookingTypeDescription = "Prenotazione online",
        )
    }

    override suspend fun bookExam(careerId: CareerId, key: ExamCallKey, request: BookExamRequest): ExamBooking {
        val call = calls.first { it.key == key }
        val booking = booked(call, position = call.enrolled + 1, bookedOn = today, note = request.studentNote)
        bookings.removeAll { it.key == key }
        bookings += booking
        return ExamBooking(key, booking.applicationListId, booking.studentId, booking.activityChoiceId)
    }

    override suspend fun getBookings(careerId: CareerId): List<BookedExam> = bookings.sortedBy { it.examDateTime }

    override suspend fun getCallTotalBookings(careerId: CareerId, key: ExamCallKey): Int? =
        calls.firstOrNull { it.key == key }?.enrolled

    override suspend fun getExamResults(careerId: CareerId): List<ExamResult> =
        results.sortedByDescending { it.examDateTime }

    override suspend fun acknowledgeExamResult(careerId: CareerId, applicationListId: Long, accept: Boolean) {
        val index = results.indexOfFirst { it.applicationListId == applicationListId }
        if (index < 0) return
        results[index] = results[index].copy(
            acknowledgment = if (accept) AcknowledgmentStatus.Accepted else AcknowledgmentStatus.Rejected,
        )
    }

    override suspend fun cancelBooking(careerId: CareerId, key: ExamCallKey, studentId: Long) {
        bookings.removeAll { it.key == key }
    }

    override suspend fun getBookingSlip(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray =
        document("Promemoria di prenotazione", key)

    override suspend fun getPresenceCertificate(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray =
        document("Attestato di presenza all'esame", key)

    private fun document(title: String, key: ExamCallKey): ByteArray {
        val booking = bookings.firstOrNull { it.key == key }
        return DemoCareer.pdf(
            title,
            listOfNotNull(
                booking?.activityDescription?.let { "Insegnamento: $it" },
                booking?.examDateTime?.let { "Data e ora: ${it.toLocalDate()} ${it.toLocalTime()}" },
                booking?.classroomDescription?.let { "Aula: $it" },
            ),
        )
    }

    private fun booked(call: Call, position: Int, bookedOn: LocalDate, note: String? = null) = BookedExam(
        key = call.key,
        applicationListId = 900000 + call.activity.id * 10 + call.number,
        studentId = DemoStudent.careerId.value,
        activityChoiceId = call.activity.id,
        activityDescription = call.course.name,
        examCallDescription = "Appello ${call.number} sessione estiva",
        examType = ExamType.Written,
        callType = ExamCallType.Final,
        examDateTime = LocalDateTime.of(call.date, call.time),
        classroomDescription = call.room,
        buildingDescription = call.building,
        credits = call.course.cfu.toFloat(),
        examModeDescription = "In presenza",
        position = position,
        totalBookings = call.enrolled,
        bookingDate = bookedOn.atTime(18, 47),
        cancellableUntil = call.date.minusDays(3),
        studentNote = note,
    )

    private fun result(
        activityId: Long,
        name: String,
        grade: Int,
        on: LocalDate,
        status: AcknowledgmentStatus,
        deadline: LocalDate?,
    ) = ExamResult(
        key = ExamCallKey(DemoCareer.COURSE_OF_STUDY_ID, activityId, 1),
        applicationListId = 900000 + activityId,
        publicationId = 910000 + activityId,
        activityDescription = name,
        examDateTime = on.atTime(9, 30),
        grade = ExamGrade.Numeric(grade),
        acknowledgment = status,
        publishedNote = null,
        acknowledgmentDeadline = deadline,
    )

    private fun examiner(course: DemoCourse): ExamExaminer {
        val (name, surname) = course.teacher.split(' ', limit = 2)
        return ExamExaminer(id = 40000L + course.moodleCourseId, name = name, surname = surname)
    }
}
