package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduledExam
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffExamCalendarApiTest : EasyStaffTestBase() {
    @Test
    suspend fun getExamsByProgram() {
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
        assertTrue(program.years.isNotEmpty(), "Program should have years")

        val results = api.exams.getExamsByProgram(
            studyProgram = program,
            yearsOfStudy = program.years,
            startDate = academicYear.toStartDate(),
            endDate = academicYear.toEndDate()
        )
        assertNotNull(results)

        // Verify each exam if results are not empty
        results.forEach { exam ->
            validateExam(exam)

            // Exam should be within the date range
            assertTrue(
                !exam.date.isBefore(academicYear.toStartDate()) && !exam.date.isAfter(academicYear.toEndDate()),
                "Exam date ${exam.date} should be within range $academicYear.toStartDate() - $academicYear.toEndDate()"
            )
        }

        // Verify exams are sorted by date (ascending)
        for (i in 0 until results.size - 1) {
            assertTrue(
                !results[i].date.isAfter(results[i + 1].date) ||
                    (results[i].date == results[i + 1].date && !results[i].startTime.isAfter(results[i + 1].startTime)),
                "Exams should be sorted by date and time"
            )
        }
    }

    @Test
    suspend fun getExamsByTeacher() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        val academicYear = academicYears.first()

        val teachers = api.core.getTeachers(academicYear)
        assertNotNull(teachers)
        assertTrue(teachers.isNotEmpty(), "Teachers should not be empty")
        val teacher = teachers.first()

        val results = api.exams.getExamsByTeacher(
            teacher = teacher,
            startDate = academicYear.toStartDate(),
            endDate = academicYear.toEndDate()
        )
        assertNotNull(results)
        assertTrue(results.isNotEmpty(), "Exams should not be empty")

        // Verify each exam if results are not empty
        results.forEach { exam ->
            validateExam(exam)

            // Exam should be within the date range
            assertTrue(
                !exam.date.isBefore(academicYear.toStartDate()) && !exam.date.isAfter(academicYear.toEndDate()),
                "Exam date ${exam.date} should be within range"
            )
        }
    }

    @Test
    suspend fun getExamsBySubject() {
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
        val results = api.exams.getExamsBySubject(
            subject = subject,
            startDate = academicYear.toStartDate(),
            endDate = academicYear.toEndDate()
        )
        assertNotNull(results)
        assertTrue(results.isNotEmpty(), "Exams should not be empty")

        // Verify each exam if results are not empty
        results.forEach { exam ->
            validateExam(exam)

            // Exam should be within the date range
            assertTrue(
                !exam.date.isBefore(academicYear.toStartDate()) && !exam.date.isAfter(academicYear.toEndDate()),
                "Exam date ${exam.date} should be within range"
            )
        }
    }

    private fun validateExam(exam: EasyStaffScheduledExam) {
        // Verify basic exam properties
        assertTrue(exam.subjectName.isNotBlank(), "Exam subject name should not be blank")
        assertNotNull(exam.date, "Exam date should not be null")

        // Verify date is reasonable (within academic year range)
        assertTrue(
            exam.date.year >= 2020,
            "Exam date ${exam.date} should be recent"
        )
        assertTrue(
            exam.date.year <= LocalDate.now().year + 2,
            "Exam date ${exam.date} should not be too far in the future"
        )

        // Verify time
        assertNotNull(exam.startTime, "Exam start time should not be null")
        assertNotNull(exam.endTime, "Exam end time should not be null")

        // Verify room
        assertTrue(exam.room.isNotBlank(), "Room should not be blank")

        // Verify building
        assertTrue(exam.building == null || exam.building.isNotBlank(), "Building should not be blank if present")

        // Verify examiners (List<EasyStaffTeacher>)
        assertNotNull(exam.examiners, "Examiners should not be null")
        exam.examiners.forEach { teacher ->
            assertTrue(teacher.code != null || teacher.email != null, "Teacher code and email cannot be null at the same time")
            assertTrue(teacher.code == null || teacher.code.isNotBlank(), "Teacher code should not be blank if present")
            assertTrue(teacher.email == null || teacher.email.isNotBlank(), "Teacher email should not be blank if present")
        }

        // Verify exam type
        assertNotNull(exam.examType, "Exam type should not be null")

        // Verify event ID
        assertTrue(exam.eventId.isNotBlank(), "Event ID should not be blank")

        // Notes may be empty, so just check it's not null
        assertNotNull(exam.notes, "Notes should not be null")
    }
}
