package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleByProgramQuery
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleBySubjectQuery
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduleByTeacherQuery
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffScheduleApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_ACADEMIC_YEAR = EasyStaffAcademicYear(2024)
        private val MOCK_WEEK_START_DATE = LocalDate.now()
    }

    @Test
    suspend fun getSearchOptions() {
        val options = api.schedule.getSearchOptions()
        assertNotNull(options.academicYears)
        assertNotNull(options.teachingAreas)
        assertTrue(options.academicYears.isNotEmpty())
        assertTrue(options.teachingAreas.isNotEmpty())
    }

    @Test
    suspend fun getStudyPrograms() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty()) return

        val academicYear = options.academicYears.first()
        val programs = api.schedule.getStudyPrograms(academicYear)
        assertNotNull(programs.studyPrograms)
    }

    @Test
    suspend fun getStudyProgramsWithArea() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        assertNotNull(programs.studyPrograms)
    }

    @Test
    suspend fun getTeachers() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachers = api.schedule.getTeachers(academicYear)
        assertNotNull(teachers)
    }

    @Test
    suspend fun getScheduleByProgram() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        if (programs.studyPrograms.isEmpty()) return

        val program = programs.studyPrograms.first()
        val yearsOfStudy = program.years.map { it.value }
        if (yearsOfStudy.isEmpty()) return

        val query = EasyStaffScheduleByProgramQuery(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code,
            studyProgramCode = program.code,
            yearsOfStudy = yearsOfStudy,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        val schedule = api.schedule.getScheduleByProgram(query)
        assertNotNull(schedule.weekStartDate)
        assertNotNull(schedule.weekEndDate)
        assertNotNull(schedule.lessons)
    }

    @Test
    suspend fun getScheduleByTeacher() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachers = api.schedule.getTeachers(academicYear)
        if (teachers.isEmpty()) return

        val teacher = teachers.first()
        val query = EasyStaffScheduleByTeacherQuery(
            academicYear = academicYear,
            teacherId = teacher.id,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        val schedule = api.schedule.getScheduleByTeacher(query)
        assertNotNull(schedule.weekStartDate)
        assertNotNull(schedule.weekEndDate)
        assertNotNull(schedule.lessons)
    }

    @Test
    suspend fun getScheduleBySubject() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        if (programs.studyPrograms.isEmpty()) return

        val program = programs.studyPrograms.first()
        val subject = program.years.flatMap { it.subjects }.firstOrNull()
        if (subject == null) return

        val query = EasyStaffScheduleBySubjectQuery(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code,
            subjectId = subject.id,
            weekStartDate = MOCK_WEEK_START_DATE
        )

        val schedule = api.schedule.getScheduleBySubject(query)
        assertNotNull(schedule.weekStartDate)
        assertNotNull(schedule.weekEndDate)
        assertNotNull(schedule.lessons)
    }
}
