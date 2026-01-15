package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleCell
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class EasyStaffScheduleApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_WEEK_START_DATE = LocalDate.now().withMonth(3).with(DayOfWeek.MONDAY)
    }
    
    @Test
    suspend fun getScheduleByProgram() {
        val academicYears = api.core.getAcademicYears()
        val teachingAreas = api.core.getTeachingAreas()
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        assertTrue(teachingAreas.isNotEmpty(), "Teaching areas should not be empty")

        val academicYear = academicYears.first()
        val teachingArea = teachingAreas.first()
        val programs = api.core.getStudyPrograms(academicYear, teachingArea.code)
        assertTrue(programs.isNotEmpty(), "Study programs should not be empty")

        val program = programs.first()
        assertTrue(program.years.isNotEmpty(), "Program should have years")

        val cells = api.schedule.getScheduleByProgram(
            academicYear = academicYear,
            studyProgram = program,
            yearsOfStudy = program.years,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        assertNotNull(cells)

        // Verify cells are sorted by dateTime
        for (i in 0 until cells.size - 1) {
            assertTrue(
                !cells[i].dateTime.isAfter(cells[i + 1].dateTime),
                "Cells should be sorted by dateTime"
            )
        }

        // Verify each cell
        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    @Test
    suspend fun getScheduleByTeacher() {
        val academicYears = api.core.getAcademicYears()
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")

        val academicYear = academicYears.first()
        val teachers = api.core.getTeachers(academicYear)
        assertTrue(teachers.isNotEmpty(), "Teachers list should not be empty")

        val teacher = teachers.first()
        val cells = api.schedule.getScheduleByTeacher(
            academicYear = academicYear,
            teacher = teacher,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        assertNotNull(cells)

        // Verify each cell
        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    @Test
    suspend fun getScheduleBySubject() {
        val academicYears = api.core.getAcademicYears()
        val teachingAreas = api.core.getTeachingAreas()
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        assertTrue(teachingAreas.isNotEmpty(), "Teaching areas should not be empty")

        val academicYear = academicYears.first()
        val teachingArea = teachingAreas.first()
        val programs = api.core.getStudyPrograms(academicYear, teachingArea.code)
        assertTrue(programs.isNotEmpty(), "Study programs should not be empty")

        val program = programs.first()
        val subject = program.years.flatMap { it.subjects }.firstOrNull()
        if (subject == null) {
            // Skip test if no subjects found
            return
        }

        // Verify subject has valid properties
        assertTrue(subject.id.isNotBlank(), "Subject id should not be blank")
        assertTrue(subject.code.isNotBlank(), "Subject code should not be blank")
        assertTrue(subject.name.isNotBlank(), "Subject name should not be blank")

        val cells = api.schedule.getScheduleBySubject(
            academicYear = academicYear,
            teachingArea = teachingArea,
            subject = subject,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        assertNotNull(cells)

        // Verify each cell
        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    private fun validateScheduleCell(cell: EasyStaffScheduleCell) {
        // Verify basic cell properties
        assertTrue(cell.id.isNotBlank(), "Cell ID should not be blank")

        // Verify date and time
        assertNotNull(cell.date, "Cell date should not be null")
        assertNotNull(cell.dateTime, "Cell dateTime should not be null")
        assertNotNull(cell.startTime, "Cell start time should not be null")
        assertNotNull(cell.endTime, "Cell end time should not be null")
        assertTrue(
            cell.startTime.isBefore(cell.endTime),
            "Cell start time should be before end time"
        )

        // Verify cell duration is reasonable (15 min to 8 hours)
        val durationMinutes = Duration.between(cell.startTime, cell.endTime).toMinutes()
        assertTrue(
            durationMinutes >= 15,
            "Cell duration should be at least 15 minutes, got $durationMinutes"
        )
        assertTrue(
            durationMinutes <= 480,
            "Cell duration should be at most 8 hours, got $durationMinutes"
        )

        // Verify eventType is present
        assertTrue(cell.eventType.isNotBlank(), "Cell eventType should not be blank")

        // Verify room information
        assertTrue(cell.roomName.isNotBlank(), "Room name should not be blank")
        assertTrue(cell.roomCode == null || cell.roomCode.isNotBlank(), "Room code should not be blank if present")
        assertTrue(cell.buildingCode == null || cell.buildingCode.isNotBlank(), "Building code should not be blank if present")

        // Verify subject information
        assertTrue(cell.subjectCode.isNotBlank(), "Subject code should not be blank")
        assertTrue(cell.subjectName.isNotBlank(), "Subject name should not be blank")

        // Verify teacher information is parsed correctly (lists, not comma-separated strings)
        assertNotNull(cell.teacherNames, "Teacher names should not be null")
        assertNotNull(cell.teacherCodes, "Teacher codes should not be null")
        assertNotNull(cell.teacherEmails, "Teacher emails should not be null")
        assertNotNull(cell.teacherPhones, "Teacher phones should not be null")

        // Verify display fields are parsed
        assertNotNull(cell.displayFields, "Display fields should not be null")

        // Verify maps URL is parsed correctly (null or valid URL)
        cell.mapsUrl?.let { url ->
            assertTrue(url.startsWith("https://"), "Maps URL should be a valid https URL")
        }
    }
}
