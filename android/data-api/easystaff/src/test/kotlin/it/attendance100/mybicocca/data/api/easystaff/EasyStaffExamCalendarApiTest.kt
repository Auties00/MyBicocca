package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffExamsByProgramQuery
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffExamsBySubjectQuery
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffExamsByTeacherQuery
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffExamCalendarApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_START_DATE = LocalDate.now()
        private val MOCK_END_DATE = LocalDate.now().plusMonths(3)
    }

    @Test
    suspend fun getExamsByProgram() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        if (programs.studyPrograms.isEmpty()) return

        val program = programs.studyPrograms.first()
        val yearsOfStudy = program.years.map { it.value }
        if (yearsOfStudy.isEmpty()) return

        val query = EasyStaffExamsByProgramQuery(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code,
            studyProgramCode = program.code,
            yearsOfStudy = yearsOfStudy,
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )

        val results = api.exams.getExamsByProgram(query)
        assertNotNull(results.exams)
        assertNotNull(results.searchSummary)
    }

    @Test
    suspend fun getExamsByTeacher() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachers = api.schedule.getTeachers(academicYear)
        if (teachers.isEmpty()) return

        val teacher = teachers.first()
        val query = EasyStaffExamsByTeacherQuery(
            academicYear = academicYear,
            teacherId = teacher.id,
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )

        val results = api.exams.getExamsByTeacher(query)
        assertNotNull(results.exams)
        assertNotNull(results.searchSummary)
    }

    @Test
    suspend fun getExamsBySubject() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        if (programs.studyPrograms.isEmpty()) return

        val program = programs.studyPrograms.first()
        val subject = program.years.flatMap { it.subjects }.firstOrNull()
        if (subject == null) return

        val query = EasyStaffExamsBySubjectQuery(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code,
            subjectId = subject.id,
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )

        val results = api.exams.getExamsBySubject(query)
        assertNotNull(results.exams)
        assertNotNull(results.searchSummary)
    }

    @Test
    suspend fun getUpcomingExamsForProgram() {
        val options = api.schedule.getSearchOptions()
        if (options.academicYears.isEmpty() || options.teachingAreas.isEmpty()) return

        val academicYear = options.academicYears.first()
        val teachingArea = options.teachingAreas.first()
        val programs = api.schedule.getStudyPrograms(academicYear, teachingArea.code)
        if (programs.studyPrograms.isEmpty()) return

        val program = programs.studyPrograms.first()
        val yearsOfStudy = program.years.map { it.value }
        if (yearsOfStudy.isEmpty()) return

        val results = api.exams.getUpcomingExamsForProgram(
            academicYear = academicYear,
            teachingAreaCode = teachingArea.code,
            studyProgramCode = program.code,
            yearsOfStudy = yearsOfStudy
        )
        assertNotNull(results.exams)
        assertNotNull(results.searchSummary)
    }
}
