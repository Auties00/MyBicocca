package it.attendance100.mybicocca.data.mapper.calendar

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Covers the four merge-at-observe source mappers: Esse3 booked exam, Moodle deadline,
 * planning-portal appointment and Affluences library reservation. Focuses on the documented
 * branches — null-returns for missing date/title, the nominal two-hour exam block clamped at
 * 23:59, the title-preference and exam-type-label rules, the normalized-name activity-code
 * join, the deadline kind filter / suffix stripping / zone conversion, and the cancelled
 * library filter.
 */
class CalendarSourceMappersTest {

    private val career = CareerId(7L)

    private fun bookedExam(
        examDateTime: LocalDateTime? = LocalDateTime.of(2026, 6, 12, 14, 0),
        activityDescription: String? = "Analisi Matematica I",
        examCallDescription: String? = "Appello di Analisi",
        examType: ExamType = ExamType.Written,
        callType: ExamCallType = ExamCallType.Final,
        classroomDescription: String? = "Aula 7",
        buildingDescription: String? = "U2",
        examModeDescription: String? = null,
        position: Int? = 3,
        bookingDate: LocalDateTime? = LocalDateTime.of(2026, 5, 1, 9, 0),
        cancellableUntil: LocalDate? = LocalDate.of(2026, 6, 10),
        studentNote: String? = "nota studente",
    ): BookedExam = BookedExam(
        key = ExamCallKey(courseOfStudyId = 11L, activityId = 22L, callId = 33),
        applicationListId = 1L,
        studentId = 2L,
        activityChoiceId = 3L,
        activityDescription = activityDescription,
        examCallDescription = examCallDescription,
        examType = examType,
        callType = callType,
        examDateTime = examDateTime,
        classroomDescription = classroomDescription,
        buildingDescription = buildingDescription,
        credits = 8f,
        examModeDescription = examModeDescription,
        position = position,
        bookingDate = bookingDate,
        cancellableUntil = cancellableUntil,
        studentNote = studentNote,
    )

    @Test
    fun `exam id is built from the course-activity-call key triple`() {
        val event = bookedExam().toCalendarEvent(career, emptyMap())
        assertThat(event?.id).isEqualTo(CalendarEventId("exam_11_22_33"))
    }

    @Test
    fun `exam end is the nominal two-hour block`() {
        val event = bookedExam(examDateTime = LocalDateTime.of(2026, 6, 12, 14, 0))
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.start).isEqualTo(LocalTime.of(14, 0))
        assertThat(event?.end).isEqualTo(LocalTime.of(16, 0))
        assertThat(event?.date).isEqualTo(LocalDate.of(2026, 6, 12))
    }

    @Test
    fun `exam end is clamped to 23-59 when the nominal block spills past midnight`() {
        val event = bookedExam(examDateTime = LocalDateTime.of(2026, 6, 12, 22, 30))
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.start).isEqualTo(LocalTime.of(22, 30))
        assertThat(event?.end).isEqualTo(LocalTime.of(23, 59))
    }

    @Test
    fun `null exam date-time yields null`() {
        assertThat(bookedExam(examDateTime = null).toCalendarEvent(career, emptyMap())).isNull()
    }

    @Test
    fun `title prefers the activity description`() {
        val event = bookedExam(
            activityDescription = "Analisi Matematica I",
            examCallDescription = "Appello di Analisi",
        ).toCalendarEvent(career, emptyMap())
        assertThat(event?.title).isEqualTo("Analisi Matematica I")
    }

    @Test
    fun `title falls back to the call description when activity is blank`() {
        val event = bookedExam(
            activityDescription = "   ",
            examCallDescription = "Appello di Analisi",
        ).toCalendarEvent(career, emptyMap())
        assertThat(event?.title).isEqualTo("Appello di Analisi")
    }

    @Test
    fun `null when neither activity nor call description is usable`() {
        val event = bookedExam(activityDescription = null, examCallDescription = "  ")
            .toCalendarEvent(career, emptyMap())
        assertThat(event).isNull()
    }

    @Test
    fun `short label is derived from the resolved title`() {
        val event = bookedExam(activityDescription = "Analisi Matematica I")
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.shortLabel).isEqualTo("AM1")
    }

    @Test
    fun `activity code is resolved via the normalized-name join`() {
        val byName = mapOf(normalizeSubjectName("Analisi Matematica I") to "E3101Q999")
        val event = bookedExam(activityDescription = "  analisi   matematica I ")
            .toCalendarEvent(career, byName)
        assertThat(event?.activityCode).isEqualTo("E3101Q999")
    }

    @Test
    fun `activity code stays null when the name is not in the join map`() {
        val event = bookedExam(activityDescription = "Sconosciuto")
            .toCalendarEvent(career, mapOf("OTHER" to "X"))
        assertThat(event?.activityCode).isNull()
    }

    @Test
    fun `exam status is always confirmed`() {
        val event = bookedExam().toCalendarEvent(career, emptyMap())
        assertThat(event?.status).isEqualTo(EventStatus.CONFIRMED)
    }

    @Test
    fun `booking position, dates and student note pass through`() {
        val event = bookedExam().toCalendarEvent(career, emptyMap())
        assertThat(event?.bookingPosition).isEqualTo(3)
        assertThat(event?.bookedAt).isEqualTo(LocalDateTime.of(2026, 5, 1, 9, 0))
        assertThat(event?.cancellableUntil).isEqualTo(LocalDate.of(2026, 6, 10))
        assertThat(event?.notes).isEqualTo("nota studente")
    }

    @Test
    fun `location is dropped when both classroom and building are null`() {
        val event = bookedExam(classroomDescription = null, buildingDescription = null)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.location).isNull()
    }

    @Test
    fun `location keeps a classroom even without a building`() {
        val event = bookedExam(classroomDescription = "Aula 9", buildingDescription = null)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.location?.room).isEqualTo("Aula 9")
        assertThat(event?.location?.building).isNull()
    }

    @Test
    fun `exam type label prefers the Esse3 mode description`() {
        val event = bookedExam(examModeDescription = "Scritto e orale", examType = ExamType.Written)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Scritto e orale")
    }

    @Test
    fun `exam type label derives Scritto from a written exam`() {
        val event = bookedExam(examModeDescription = null, examType = ExamType.Written)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Scritto")
    }

    @Test
    fun `exam type label derives Orale from an oral exam`() {
        val event = bookedExam(examModeDescription = null, examType = ExamType.Oral)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Orale")
    }

    @Test
    fun `exam type label derives joint written-and-oral`() {
        val event = bookedExam(examModeDescription = null, examType = ExamType.WrittenAndOralJoint)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Scritto e orale")
    }

    @Test
    fun `exam type label derives separate written-and-oral`() {
        val event = bookedExam(examModeDescription = null, examType = ExamType.WrittenAndOralSeparate)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Scritto e orale")
    }

    @Test
    fun `exam type label is null when mode is unknown and no description`() {
        val event = bookedExam(examModeDescription = null, examType = ExamType.Unknown)
            .toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isNull()
    }

    @Test
    fun `partial call prefixes the mode with the partial marker`() {
        val event = bookedExam(
            examModeDescription = null,
            examType = ExamType.Written,
            callType = ExamCallType.Partial,
        ).toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Prova parziale · Scritto")
    }

    @Test
    fun `partial call with unknown mode shows only the partial marker`() {
        val event = bookedExam(
            examModeDescription = null,
            examType = ExamType.Unknown,
            callType = ExamCallType.Partial,
        ).toCalendarEvent(career, emptyMap())
        assertThat(event?.examTypeLabel).isEqualTo("Prova parziale")
    }

    private fun deadline(
        kind: String = DeadlineEntity.Kind.ASSIGNMENT,
        title: String = "Compito 1 è in scadenza",
        dueAtMs: Long = LocalDateTime.of(2026, 4, 1, 23, 59)
            .toInstant(ZoneOffset.UTC).toEpochMilli(),
    ): DeadlineEntity = DeadlineEntity(
        accountId = "acc-1",
        eventId = 555,
        courseId = 100,
        kind = kind,
        instanceId = 777,
        title = title,
        dueAtMs = dueAtMs,
    )

    @Test
    fun `non-assignment deadline kind yields null`() {
        assertThat(deadline(kind = DeadlineEntity.Kind.QUIZ).toCalendarEvent(career, ZoneOffset.UTC))
            .isNull()
    }

    @Test
    fun `assignment deadline maps with namespaced id and stripped italian suffix`() {
        val event = deadline(title = "Compito 1 è in scadenza")
            .toCalendarEvent(career, ZoneOffset.UTC)
        assertThat(event).isNotNull()
        requireNotNull(event)
        assertThat(event.id).isEqualTo(CalendarEventId("deadline_555"))
        assertThat(event.title).isEqualTo("Compito 1")
        assertThat(event.source).isEqualTo(EventSource.DEADLINE)
        assertThat(event.courseId).isEqualTo(100)
        assertThat(event.assignmentId).isEqualTo(777)
    }

    @Test
    fun `assignment deadline strips the english due suffix`() {
        val event = deadline(title = "Homework 2 is due").toCalendarEvent(career, ZoneOffset.UTC)
        assertThat(event?.title).isEqualTo("Homework 2")
    }

    @Test
    fun `deadline suffix is only stripped after a leading activity name`() {
        val event = deadline(title = "Tema A è in scadenza").toCalendarEvent(career, ZoneOffset.UTC)
        assertThat(event?.title).isEqualTo("Tema A")
    }

    @Test
    fun `deadline title is trimmed and the bare suffix without an activity is not removed`() {
        val event = deadline(title = " è in scadenza").toCalendarEvent(career, ZoneOffset.UTC)
        assertThat(event?.title).isEqualTo("è in scadenza")
    }

    @Test
    fun `deadline is a point in time with start equal to end`() {
        val event = deadline().toCalendarEvent(career, ZoneOffset.UTC)
        requireNotNull(event)
        assertThat(event.start).isEqualTo(event.end)
    }

    @Test
    fun `deadline due instant converts to wall-clock in the supplied zone`() {
        val dueAtMs = LocalDateTime.of(2026, 4, 1, 12, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli()
        val romeEvent = deadline(dueAtMs = dueAtMs)
            .toCalendarEvent(career, ZoneId.of("Europe/Rome"))
        requireNotNull(romeEvent)
        assertThat(romeEvent.date).isEqualTo(LocalDate.of(2026, 4, 1))
        assertThat(romeEvent.start).isEqualTo(LocalTime.of(14, 0))
    }

    @Test
    fun `deadline activity code is always null`() {
        val event = deadline().toCalendarEvent(career, ZoneOffset.UTC)
        assertThat(event?.activityCode).isNull()
    }

    private fun appointment(
        areaName: String? = "Sportello Centrale",
        areaAddress: String? = "Via Bicocca 1",
        serviceGroup: String? = "Carriere Studenti",
        webConferenceUrl: String? = "https://meet/x",
    ): AppointmentReservation = AppointmentReservation(
        code = "ABC123",
        email = "s@x.it",
        entryId = 9,
        serviceId = 4,
        serviceName = "Rilascio certificato",
        serviceGroup = serviceGroup,
        areaName = areaName,
        areaAddress = areaAddress,
        start = LocalDateTime.of(2026, 4, 2, 10, 0),
        end = LocalDateTime.of(2026, 4, 2, 10, 30),
        qrCodeDataUrl = null,
        webConferenceUrl = webConferenceUrl,
    )

    @Test
    fun `appointment maps with namespaced id and office as location`() {
        val event = appointment().toCalendarEvent(career)
        assertThat(event.id).isEqualTo(CalendarEventId("appointment_ABC123"))
        assertThat(event.title).isEqualTo("Rilascio certificato")
        assertThat(event.date).isEqualTo(LocalDate.of(2026, 4, 2))
        assertThat(event.start).isEqualTo(LocalTime.of(10, 0))
        assertThat(event.end).isEqualTo(LocalTime.of(10, 30))
        assertThat(event.location?.building).isEqualTo("Sportello Centrale")
        assertThat(event.location?.room).isEqualTo("Via Bicocca 1")
        assertThat(event.serviceGroup).isEqualTo("Carriere Studenti")
        assertThat(event.webConferenceUrl).isEqualTo("https://meet/x")
    }

    @Test
    fun `appointment location is null when both area fields are null`() {
        val event = appointment(areaName = null, areaAddress = null).toCalendarEvent(career)
        assertThat(event.location).isNull()
    }

    @Test
    fun `appointment keeps a location when only the area name is present`() {
        val event = appointment(areaName = "Sede X", areaAddress = null).toCalendarEvent(career)
        assertThat(event.location?.building).isEqualTo("Sede X")
        assertThat(event.location?.room).isNull()
    }

    @Test
    fun `appointment activity code and notes are always null`() {
        val event = appointment().toCalendarEvent(career)
        assertThat(event.activityCode).isNull()
        assertThat(event.notes).isNull()
    }

    private fun reservation(
        state: LibraryReservationState = LibraryReservationState.Upcoming,
        secondaryName: String? = "Sezione Scientifica",
        note: String? = "tavolo accanto alla finestra",
    ): LibraryReservation = LibraryReservation(
        reservationId = 4242,
        libraryName = "Biblioteca Centrale",
        librarySecondaryName = secondaryName,
        seatName = "A12",
        start = LocalDateTime.of(2026, 4, 3, 9, 0),
        end = LocalDateTime.of(2026, 4, 3, 13, 0),
        note = note,
        reservationCode = "R-1",
        cancellationToken = "tok",
        state = state,
    )

    @Test
    fun `library reservation maps with namespaced id and seat as room`() {
        val event = reservation().toCalendarEvent(career)
        assertThat(event).isNotNull()
        requireNotNull(event)
        assertThat(event.id).isEqualTo(CalendarEventId("library_4242"))
        assertThat(event.title).isEqualTo("Biblioteca Centrale")
        assertThat(event.date).isEqualTo(LocalDate.of(2026, 4, 3))
        assertThat(event.start).isEqualTo(LocalTime.of(9, 0))
        assertThat(event.end).isEqualTo(LocalTime.of(13, 0))
        assertThat(event.seatName).isEqualTo("A12")
        assertThat(event.location?.room).isEqualTo("A12")
        assertThat(event.location?.building).isEqualTo("Sezione Scientifica")
        assertThat(event.notes).isEqualTo("tavolo accanto alla finestra")
    }

    @Test
    fun `cancelled library reservation yields null`() {
        assertThat(reservation(state = LibraryReservationState.Cancelled).toCalendarEvent(career))
            .isNull()
    }

    @Test
    fun `awaiting-confirmation library reservation still surfaces as confirmed`() {
        val event = reservation(state = LibraryReservationState.AwaitingConfirmation)
            .toCalendarEvent(career)
        assertThat(event).isNotNull()
        assertThat(event?.status).isEqualTo(EventStatus.CONFIRMED)
    }

    @Test
    fun `library reservation activity code is always null`() {
        val event = reservation().toCalendarEvent(career)
        assertThat(event?.activityCode).isNull()
    }
}
