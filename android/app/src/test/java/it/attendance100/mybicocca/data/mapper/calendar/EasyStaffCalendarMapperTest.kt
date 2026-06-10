package it.attendance100.mybicocca.data.mapper.calendar

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the EasyStaff weekly-grid cell -> [CalendarEvent.Lesson] mapping: the namespaced id,
 * the caller-supplied activity code join, the derived short label, the confirmed/cancelled
 * status collapse, the curriculum-path-as-notes blanking, and the location built only from
 * usable components.
 */
class EasyStaffCalendarMapperTest {

    private val career = CareerId(7L)

    private fun cell(
        id: String = "12345",
        roomCode: String? = "U24-DISCO-C2",
        buildingCode: String? = "U24",
        status: EasyStaffBookingStatus = EasyStaffBookingStatus.CONFIRMED,
        subjectCode: String = "EC510731",
        name: String = "Algoritmi e Strutture Dati",
        teacherNames: List<String> = listOf("M. ROSSI"),
        curriculumPath: String = "Curriculum Generale",
        mapsUrl: String? = "https://maps.google/x",
    ): EasyStaffScheduleCell.Lesson = EasyStaffScheduleCell.Lesson(
        id = id,
        dateTime = LocalDateTime.of(2026, 3, 10, 9, 30),
        date = LocalDate.of(2026, 3, 10),
        startTime = LocalTime.of(9, 30),
        endTime = LocalTime.of(11, 30),
        roomCode = roomCode,
        buildingCode = buildingCode,
        roomName = "U24-DISCO-AulaC2",
        status = status,
        subjectCode = subjectCode,
        name = name,
        teacherNames = teacherNames,
        curriculumPath = curriculumPath,
        mapsUrl = mapsUrl,
    )

    @Test
    fun `id is namespaced under the lesson source`() {
        val event = cell(id = "999").toDomain(career, activityCode = null)
        assertThat(event.id).isEqualTo(CalendarEventId("lesson_999"))
    }

    @Test
    fun `core fields carry straight through`() {
        val event = cell().toDomain(career, activityCode = "E3101Q123")
        assertThat(event.careerId).isEqualTo(career)
        assertThat(event.date).isEqualTo(LocalDate.of(2026, 3, 10))
        assertThat(event.start).isEqualTo(LocalTime.of(9, 30))
        assertThat(event.end).isEqualTo(LocalTime.of(11, 30))
        assertThat(event.title).isEqualTo("Algoritmi e Strutture Dati")
        assertThat(event.subjectCode).isEqualTo("EC510731")
        assertThat(event.teachers).containsExactly("M. ROSSI")
        assertThat(event.cfu).isNull()
    }

    @Test
    fun `caller-supplied activity code is threaded onto the event`() {
        val event = cell().toDomain(career, activityCode = "E3101Q123")
        assertThat(event.activityCode).isEqualTo("E3101Q123")
    }

    @Test
    fun `missing activity code stays null`() {
        val event = cell().toDomain(career, activityCode = null)
        assertThat(event.activityCode).isNull()
    }

    @Test
    fun `short label is derived from the lesson name`() {
        val event = cell(name = "Algoritmi e Strutture Dati").toDomain(career, activityCode = null)
        assertThat(event.shortLabel).isEqualTo("ASD")
    }

    @Test
    fun `confirmed booking status maps to CONFIRMED`() {
        val event = cell(status = EasyStaffBookingStatus.CONFIRMED).toDomain(career, activityCode = null)
        assertThat(event.status).isEqualTo(EventStatus.CONFIRMED)
    }

    @Test
    fun `cancelled booking status maps to CANCELLED`() {
        val event = cell(status = EasyStaffBookingStatus.CANCELLED).toDomain(career, activityCode = null)
        assertThat(event.status).isEqualTo(EventStatus.CANCELLED)
    }

    @Test
    fun `non-blank curriculum path becomes the notes`() {
        val event = cell(curriculumPath = "Curriculum Generale").toDomain(career, activityCode = null)
        assertThat(event.notes).isEqualTo("Curriculum Generale")
    }

    @Test
    fun `blank curriculum path yields null notes`() {
        val event = cell(curriculumPath = "   ").toDomain(career, activityCode = null)
        assertThat(event.notes).isNull()
    }

    @Test
    fun `empty curriculum path yields null notes`() {
        val event = cell(curriculumPath = "").toDomain(career, activityCode = null)
        assertThat(event.notes).isNull()
    }

    @Test
    fun `location is built from room, building and maps url`() {
        val event = cell(roomCode = "U24-C2", buildingCode = "U24", mapsUrl = "https://maps/x")
            .toDomain(career, activityCode = null)
        val location = event.location
        assertThat(location).isNotNull()
        requireNotNull(location)
        assertThat(location.room).isEqualTo("U24-C2")
        assertThat(location.building).isEqualTo("U24")
        assertThat(location.mapsUrl).isEqualTo("https://maps/x")
    }

    @Test
    fun `location is null when every component is missing`() {
        val event = cell(roomCode = null, buildingCode = null, mapsUrl = null)
            .toDomain(career, activityCode = null)
        assertThat(event.location).isNull()
    }

    @Test
    fun `location is null when components are blank`() {
        val event = cell(roomCode = "  ", buildingCode = "", mapsUrl = null)
            .toDomain(career, activityCode = null)
        assertThat(event.location).isNull()
    }

    @Test
    fun `a single usable maps url still produces a location`() {
        val event = cell(roomCode = null, buildingCode = "  ", mapsUrl = "https://maps/only")
            .toDomain(career, activityCode = null)
        val location = event.location
        assertThat(location).isNotNull()
        requireNotNull(location)
        assertThat(location.room).isNull()
        assertThat(location.building).isNull()
        assertThat(location.mapsUrl).isEqualTo("https://maps/only")
    }

    @Test
    fun `blank room is blanked to null while building survives`() {
        val event = cell(roomCode = "   ", buildingCode = "U6", mapsUrl = null)
            .toDomain(career, activityCode = null)
        val location = event.location
        requireNotNull(location)
        assertThat(location.room).isNull()
        assertThat(location.building).isEqualTo("U6")
    }
}
