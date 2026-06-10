package it.attendance100.mybicocca.data.remote.easystaff.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class EasyStaffCoreApiTest : EasyStaffTestBase() {
    
    @Test
    suspend fun getAcademicYears() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")

        for (i in 0 until academicYears.size - 1) {
            assertTrue(academicYears[i].startYear >= academicYears[i + 1].startYear)
        }

        academicYears.forEach { year ->
            assertTrue(year.startYear > 2000, "Start year should be after 2000")
            assertEquals(year.startYear + 1, year.endYear, "End year should be start year + 1")
            assertTrue(year.label.contains("/"), "Label should contain '/'")
            assertEquals(year.startYear.toString(), year.value, "Value should match start year")
        }
    }

    @Test
    suspend fun getTeachingAreas() {
        val teachingAreas = api.core.getTeachingAreas()
        assertNotNull(teachingAreas)
        assertTrue(teachingAreas.isNotEmpty(), "Teaching areas should not be empty")

        teachingAreas.forEach { area ->
            assertTrue(area.code.isNotBlank(), "Area code should not be blank")
            assertTrue(area.name.isNotBlank(), "Area name should not be blank")
        }
    }

    @Test
    suspend fun getStudyPrograms() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")

        val academicYear = academicYears.first()
        val programs = api.core.getStudyPrograms(academicYear)
        assertNotNull(programs)
        assertTrue(programs.isNotEmpty(), "Study programs should not be empty")

        programs.forEach { program ->
            assertTrue(program.code.isNotBlank(), "Program code should not be blank")
            assertTrue(program.name.isNotBlank(), "Program name should not be blank")
            assertTrue(program.internalId.isNotBlank(), "Program internalId should not be blank")
            assertTrue(program.teachingAreaCode.isNotBlank(), "Program teachingAreaCode should not be blank")
            assertNotNull(program.degreeType, "Program degreeType should not be null")
            assertNotNull(program.displayMode, "Program displayMode should not be null")
            assertNotNull(program.years, "Program years should not be null")
            assertTrue(program.years.isNotEmpty(), "Program should have at least one year")

            program.years.forEach { year ->
                assertTrue(year.code.isNotBlank(), "Year value should not be blank")
                assertTrue(year.name.isNotBlank(), "Year label should not be blank")
                assertNotNull(year.subjects, "Year subjects should not be null")
            }

            program.teachingPeriods.forEach { period ->
                assertTrue(period.id.isNotBlank(), "Period id should not be blank")
                assertTrue(period.code.isNotBlank(), "Period code should not be blank")
                assertTrue(period.name.isNotBlank(), "Period label should not be blank")
            }
        }

        val programCodes = programs.map { it.code }
        assertEquals(
            programCodes.size,
            programCodes.toSet().size,
            "Program codes should be unique"
        )
    }

    @Test
    suspend fun getStudyProgramsWithArea() {
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

        programs.forEach { program ->
            assertTrue(program.code.isNotBlank(), "Program code should not be blank")
            assertTrue(program.name.isNotBlank(), "Program name should not be blank")
            assertEquals(
                teachingArea.code,
                program.teachingAreaCode,
                "Program should belong to requested teaching area"
            )
        }
    }

    @Test
    suspend fun getTeachers() {
        val academicYears = api.core.getAcademicYears()
        assertNotNull(academicYears)
        assertTrue(academicYears.isNotEmpty(), "Academic years should not be empty")
        val academicYear = academicYears.first()

        val teachers = api.core.getTeachers(academicYear)
        assertNotNull(teachers)
        assertTrue(teachers.isNotEmpty(), "Teachers list should not be empty")

        teachers.forEach { teacher ->
            assertTrue(teacher.id > 0, "Teacher id should be positive")
            assertTrue(teacher.fullName.isNotBlank(), "Teacher name should not be blank")
            assertTrue(
                teacher.fullName.contains(" ") || teacher.fullName.length > 3,
                "Teacher name '${teacher.fullName}' seems incomplete"
            )
        }

        val teacherIds = teachers.map { it.id }
        assertEquals(
            teacherIds.size,
            teacherIds.toSet().size,
            "Teacher ids should be unique"
        )
    }
}