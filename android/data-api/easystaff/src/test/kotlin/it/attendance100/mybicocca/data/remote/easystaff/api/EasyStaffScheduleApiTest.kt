package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class EasyStaffScheduleApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_WEEK_START_DATE = LocalDate.now()
    }

    @Test
    suspend fun getScheduleByProgram() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        val academicYear = academicYears.first()

        val teachingAreas = api.core.getTeachingAreas()
        assertNotNull(teachingAreas)
        assertTrue(teachingAreas.isNotEmpty(), "Teaching areas should not be empty")
        val teachingArea = teachingAreas.first()

        val programs = api.core.getStudyPrograms(academicYear, teachingArea.code)
        assertNotNull(programs)
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
        assertTrue(cells.isNotEmpty(), "Cells should not be empty")

        for (i in 0 until cells.size - 1) {
            assertTrue(
                cells[i] <= cells[i + 1],
                "Cells should be sorted by date and start time"
            )
        }

        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    @Test
    suspend fun getScheduleByTeacher() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        val academicYear = academicYears.first()

        val teachers = api.core.getTeachers(academicYear)
        assertNotNull(teachers)
        assertTrue(teachers.isNotEmpty(), "Teachers list should not be empty")
        val teacher = teachers.first()
        assertTrue(teacher.courses.isNotEmpty(), "Teacher should have courses")
        val course = teacher.courses.first()
        assertTrue(course.yearsOfStudy.isNotEmpty(), "Courses should have tracks")

        val cells = api.schedule.getScheduleByTeacher(
            academicYear = academicYear,
            course = course,
            teacher = teacher,
            yearsOfStudy = course.yearsOfStudy,
            weekStartDate = MOCK_WEEK_START_DATE
        )
        assertNotNull(cells)
        assertTrue(cells.isNotEmpty(), "Cells should not be empty")

        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    @Test
    suspend fun getScheduleBySubject() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        val academicYear = academicYears.first()

        val teachingAreas = api.core.getTeachingAreas()
        assertNotNull(teachingAreas)
        assertTrue(teachingAreas.isNotEmpty(), "Teaching areas should not be empty")
        val teachingArea = teachingAreas.first()

        val programs = api.core.getStudyPrograms(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code
        )
        assertNotNull(programs)
        assertTrue(programs.isNotEmpty(), "Study programs should not be empty")
        val program = programs.first()
        val subject = program.years.flatMap { it.subjects }.first()

        assertTrue(subject.id.isNotBlank(), "Subject id should not be blank")
        assertTrue(subject.code.isNotBlank(), "Subject code should not be blank")
        assertTrue(subject.name.isNotBlank(), "Subject name should not be blank")

        val cells = api.schedule.getScheduleBySubject(
            academicYear = academicYear,
            subject = subject,
            weekStartDate = MOCK_WEEK_START_DATE
        )
        assertNotNull(cells)
        assertTrue(cells.isNotEmpty(), "Cells should not be empty")

        cells.forEach { cell ->
            validateScheduleCell(cell)
        }
    }

    private fun validateScheduleCell(cell: EasyStaffScheduleCell) {
        assertTrue(cell.type.isNotBlank(), "Cell type should not be blank")

        when (cell) {
            is EasyStaffScheduleCell.Lesson -> {
                assertTrue(cell.name.isNotBlank(), "Lesson name should not be blank")
                assertNotNull(cell.date, "Lesson date should not be null")
                assertNotNull(cell.startTime, "Lesson start time should not be null")
                assertNotNull(cell.endTime, "Lesson end time should not be null")
                assertTrue(
                    cell.startTime.isBefore(cell.endTime),
                    "Lesson start time should be before end time"
                )

                val durationMinutes = Duration.between(cell.startTime, cell.endTime).toMinutes()
                assertTrue(
                    durationMinutes >= 15,
                    "Lesson duration should be at least 15 minutes, got $durationMinutes"
                )
                assertTrue(
                    durationMinutes <= 1440,
                    "Lesson duration should be at most 24 hours, got $durationMinutes"
                )
            }

            is EasyStaffScheduleCell.Closure -> {
                assertTrue(cell.name.isNotBlank(), "Closure name should not be blank")
                assertNotNull(cell.date, "Closure date should not be null")
                assertNotNull(cell.startTime, "Closure start time should not be null")
                assertNotNull(cell.endTime, "Closure end time should not be null")
            }

            is EasyStaffScheduleCell.Other -> {
                println("Unknown cell type: ${cell.type}, rawFields: ${cell.rawFields}")
            }
        }
    }
}
