package it.attendance100.mybicocca.data.mapper.calendar

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.calendar.CalendarEventDiscriminator
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the Room <-> domain mapping for materialized calendar events. Asserts that only
 * lessons and exams produce rows (deadline / appointment / library subtypes return null),
 * the lesson and exam round-trips preserve every persisted field, and the defensive
 * rehydration drops rows with unparseable date/time or an unknown discriminator while
 * falling back to CONFIRMED on an unrecognized status.
 */
class CalendarEntityMapperTest {

    private val career = CareerId(42L)

    private fun lesson(): CalendarEvent.Lesson = CalendarEvent.Lesson(
        id = CalendarEventId("lesson_99"),
        careerId = career,
        date = LocalDate.of(2026, 3, 10),
        start = LocalTime.of(9, 30),
        end = LocalTime.of(11, 0),
        title = "Algoritmi e Strutture Dati",
        shortLabel = "ASD",
        location = EventLocation(room = "U6-01", building = "U6", mapsUrl = "https://maps/x"),
        status = EventStatus.CONFIRMED,
        notes = "Curriculum A",
        activityCode = "E3101Q123",
        subjectCode = "EC1234",
        teachers = listOf("M. Rossi", "L. Bianchi"),
        cfu = 8,
    )

    private fun exam(): CalendarEvent.Exam = CalendarEvent.Exam(
        id = CalendarEventId("exam_1_2_3"),
        careerId = career,
        date = LocalDate.of(2026, 6, 12),
        start = LocalTime.of(14, 0),
        end = LocalTime.of(16, 0),
        title = "Analisi Matematica I",
        shortLabel = "AM1",
        location = EventLocation(room = "Aula 7", building = "U2", mapsUrl = null),
        status = EventStatus.CONFIRMED,
        notes = "porto la calcolatrice",
        activityCode = "E3101Q999",
        examTypeLabel = "Scritto",
        bookingPosition = 5,
        bookedAt = LocalDateTime.of(2026, 5, 1, 8, 30),
        cancellableUntil = LocalDate.of(2026, 6, 10),
    )

    @Test
    fun `lesson maps to a LESSON row with its extras`() {
        val entity = lesson().toEntity()
        assertThat(entity).isNotNull()
        requireNotNull(entity)
        assertThat(entity.discriminator).isEqualTo(CalendarEventDiscriminator.LESSON)
        assertThat(entity.source).isEqualTo("lesson")
        assertThat(entity.id).isEqualTo("lesson_99")
        assertThat(entity.careerId).isEqualTo(42L)
        assertThat(entity.date).isEqualTo("2026-03-10")
        assertThat(entity.startTime).isEqualTo("09:30")
        assertThat(entity.endTime).isEqualTo("11:00")
        assertThat(entity.subjectCode).isEqualTo("EC1234")
        assertThat(entity.cfu).isEqualTo(8)
        assertThat(entity.teachersCsv).isEqualTo("M. Rossi\nL. Bianchi")
        assertThat(entity.examTypeLabel).isNull()
        assertThat(entity.bookingPosition).isNull()
    }

    @Test
    fun `empty teachers list persists as null teachersCsv`() {
        val entity = lesson().copy(teachers = emptyList()).toEntity()
        assertThat(entity?.teachersCsv).isNull()
    }

    @Test
    fun `exam maps to an EXAM row with its extras`() {
        val entity = exam().toEntity()
        assertThat(entity).isNotNull()
        requireNotNull(entity)
        assertThat(entity.discriminator).isEqualTo(CalendarEventDiscriminator.EXAM)
        assertThat(entity.source).isEqualTo("exam")
        assertThat(entity.examTypeLabel).isEqualTo("Scritto")
        assertThat(entity.bookingPosition).isEqualTo(5)
        assertThat(entity.bookedAt).isEqualTo("2026-05-01T08:30")
        assertThat(entity.cancellableUntil).isEqualTo("2026-06-10")
        assertThat(entity.subjectCode).isNull()
        assertThat(entity.cfu).isNull()
    }

    @Test
    fun `exam with null booking timestamps persists nulls`() {
        val entity = exam().copy(bookedAt = null, cancellableUntil = null).toEntity()
        assertThat(entity?.bookedAt).isNull()
        assertThat(entity?.cancellableUntil).isNull()
    }

    @Test
    fun `assignment deadline is never materialized`() {
        val deadline = CalendarEvent.AssignmentDeadline(
            id = CalendarEventId("deadline_7"),
            careerId = career,
            date = LocalDate.of(2026, 4, 1),
            start = LocalTime.NOON,
            end = LocalTime.NOON,
            title = "Compito 1",
            shortLabel = null,
            status = EventStatus.CONFIRMED,
            activityCode = null,
            courseId = 100,
            assignmentId = 200,
        )
        assertThat(deadline.toEntity()).isNull()
    }

    @Test
    fun `appointment is never materialized`() {
        val appointment = CalendarEvent.Appointment(
            id = CalendarEventId("appointment_x"),
            careerId = career,
            date = LocalDate.of(2026, 4, 2),
            start = LocalTime.of(10, 0),
            end = LocalTime.of(10, 30),
            title = "Sportello",
            shortLabel = null,
            location = null,
            status = EventStatus.CONFIRMED,
            notes = null,
            serviceGroup = null,
            webConferenceUrl = null,
        )
        assertThat(appointment.toEntity()).isNull()
    }

    @Test
    fun `library reservation is never materialized`() {
        val reservation = CalendarEvent.LibraryReservation(
            id = CalendarEventId("library_5"),
            careerId = career,
            date = LocalDate.of(2026, 4, 3),
            start = LocalTime.of(9, 0),
            end = LocalTime.of(13, 0),
            title = "Biblioteca Centrale",
            shortLabel = null,
            location = EventLocation(room = "A12", building = null),
            status = EventStatus.CONFIRMED,
            notes = null,
            seatName = "A12",
        )
        assertThat(reservation.toEntity()).isNull()
    }

    @Test
    fun `lesson round-trips through entity and back`() {
        val original = lesson()
        val restored = original.toEntity()!!.toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `exam round-trips through entity and back`() {
        val original = exam()
        val restored = original.toEntity()!!.toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `cancelled lesson status round-trips`() {
        val original = lesson().copy(status = EventStatus.CANCELLED)
        val restored = original.toEntity()!!.toDomain()
        assertThat((restored as CalendarEvent.Lesson).status).isEqualTo(EventStatus.CANCELLED)
    }

    @Test
    fun `row with no location components rehydrates with null location`() {
        val restored = lesson().copy(location = null).toEntity()!!.toDomain()
        assertThat(restored?.location).isNull()
    }

    @Test
    fun `location with only a building survives the round-trip`() {
        val original = lesson().copy(location = EventLocation(room = null, building = "U6", mapsUrl = null))
        val restored = original.toEntity()!!.toDomain()
        assertThat(restored?.location).isEqualTo(EventLocation(room = null, building = "U6", mapsUrl = null))
    }

    @Test
    fun `unparseable date column drops the row`() {
        val entity = lesson().toEntity()!!.copy(date = "not-a-date")
        assertThat(entity.toDomain()).isNull()
    }

    @Test
    fun `unparseable start time drops the row`() {
        val entity = lesson().toEntity()!!.copy(startTime = "99:99")
        assertThat(entity.toDomain()).isNull()
    }

    @Test
    fun `unparseable end time drops the row`() {
        val entity = lesson().toEntity()!!.copy(endTime = "bad")
        assertThat(entity.toDomain()).isNull()
    }

    @Test
    fun `unknown discriminator drops the row`() {
        val entity = lesson().toEntity()!!.copy(discriminator = "Mystery")
        assertThat(entity.toDomain()).isNull()
    }

    @Test
    fun `unrecognized status falls back to confirmed`() {
        val entity = lesson().toEntity()!!.copy(status = "PURGATORY")
        assertThat(entity.toDomain()?.status).isEqualTo(EventStatus.CONFIRMED)
    }

    @Test
    fun `blank teacher names are filtered out on rehydration`() {
        val entity = lesson().toEntity()!!.copy(teachersCsv = "M. Rossi\n\n  \nL. Bianchi")
        val restored = entity.toDomain() as CalendarEvent.Lesson
        assertThat(restored.teachers).containsExactly("M. Rossi", "L. Bianchi").inOrder()
    }

    @Test
    fun `null teachersCsv rehydrates to empty teachers`() {
        val entity = lesson().toEntity()!!.copy(teachersCsv = null)
        val restored = entity.toDomain() as CalendarEvent.Lesson
        assertThat(restored.teachers).isEmpty()
    }

    @Test
    fun `exam with unparseable booked-at timestamp rehydrates that field as null`() {
        val entity = exam().toEntity()!!.copy(bookedAt = "garbage")
        val restored = entity.toDomain() as CalendarEvent.Exam
        assertThat(restored.bookedAt).isNull()
    }

    @Test
    fun `exam with unparseable cancellable-until rehydrates that field as null`() {
        val entity = exam().toEntity()!!.copy(cancellableUntil = "garbage")
        val restored = entity.toDomain() as CalendarEvent.Exam
        assertThat(restored.cancellableUntil).isNull()
    }
}
