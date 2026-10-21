package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceHistoryRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCellType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgram
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Read-only calls of the generated Agenda Web API against the live site, which check that the
 * spec matches what the site sends: script variables, form searches and the scraped room
 * showcase. The attendance history needs a student number in `EASYSTAFF_STUDENT_ID`.
 */
class EasyStaffAgendaApiTest : EasyStaffTestBase() {
    private lateinit var academicYear: String
    private lateinit var program: EasyStaffStudyProgram
    private val monday: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    @BeforeAll
    fun setUp(): Unit = runBlocking {
        academicYear = api.core.getAcademicYears().values.maxOf { it.startYear }.toString()
        program = api.core.getStudyPrograms(academicYear).first { program -> program.years.any { it.subjects.isNotEmpty() } }
    }

    @Test
    fun `academic years, teachers and study programs decode`(): Unit = runBlocking {
        val teachers = api.core.getTeachers(academicYear)
        assertTrue(teachers.isNotEmpty(), "No teacher in $academicYear")
        assertTrue(teachers.all { it.code.isNotBlank() && it.fullName.isNotBlank() })
        assertTrue(program.years.all { '|' in it.code }, "Year codes should end with their number")
    }

    @Test
    fun `buildings, rooms, teaching areas and event types decode`(): Unit = runBlocking {
        val buildings = api.buildings.getBuildings()
        assertTrue(buildings.isNotEmpty())
        val rooms = api.buildings.getRooms()
        assertTrue(buildings.any { rooms[it.code].orEmpty().isNotEmpty() }, "No building has rooms")
        assertTrue(api.buildings.getTeachingAreas().isNotEmpty())
        assertTrue(api.events.getEventTypes().isNotEmpty())
    }

    @Test
    fun `the room showcase decodes`(): Unit = runBlocking {
        // U6, the Agorà building, lists the equipment of its rooms.
        val buildings = api.buildings.getRoomDetails(buildings = listOf("U06"))
        assertTrue(buildings.isNotEmpty(), "No building for U06")
        assertTrue(buildings.values.all { it.name.isNotBlank() }, "Every building has a name")
        val rooms = buildings.values.flatMap { it.rooms }
        assertTrue(rooms.any { it.code != null && it.capacity != null }, "Rooms should carry codes and capacities")
        assertTrue(rooms.any { it.fixedEquipment.isNotEmpty() }, "Some room should list equipment")
        assertTrue(buildings.values.all { it.googleMapsLink?.startsWith("https://") != false }, "Map links are the embeds' addresses")
    }

    @Test
    fun `the occupation of a building and the events of a week decode`(): Unit = runBlocking {
        val building = api.buildings.getBuildings().first { it.code.startsWith("U") }
        api.buildings.getBuildingOccupation(buildings = listOf(building.code), date = monday)
        api.events.getEvents(buildings = listOf("all", "senza_aula"), from = monday, to = monday.plusDays(6))
    }

    @Test
    fun `schedules by program and by subjects decode`(): Unit = runBlocking {
        val years = program.years.filter { it.subjects.isNotEmpty() }
        val byProgram = api.schedule.getScheduleByProgram(academicYear = academicYear, program = program.code, years = years.map { it.code }, week = monday)
        val subjects = years.flatMap { it.subjects }.take(5)
        val bySubjects = api.schedule.getScheduleBySubjects(academicYear = academicYear, subjects = subjects.map { it.code }, week = monday)
        for (cell in byProgram + bySubjects) {
            if (cell.type == EasyStaffScheduleCellType.Lesson) {
                assertTrue(cell.date != null && cell.startTime != null, "Lessons have a date and a time")
            }
        }
    }

    @Test
    fun `exam calendars by program and by teacher decode`(): Unit = runBlocking {
        val from = monday
        val to = monday.plusMonths(3)
        val numbers = program.years.map { it.code.substringAfter('|') }
        api.exams.getExamsByProgram(teachingArea = program.teachingAreaCode, program = program.code, years = numbers, from = from, to = to)
        val teacher = api.core.getTeachers(academicYear).first()
        api.exams.getExamsByTeacher(teacher = teacher.id.toString(), from = from, to = to)
    }

    @Test
    fun `a student's attendance history decodes`(): Unit = runBlocking {
        val studentId = System.getenv("EASYSTAFF_STUDENT_ID")
        assumeTrue(!studentId.isNullOrBlank(), "Set EASYSTAFF_STUDENT_ID to read an attendance history")
        val history = api.attendance.getAttendanceHistory(EasyStaffAttendanceHistoryRequest(studentId = studentId))
        assertEquals(setOf(studentId), history.keys)
    }
}
