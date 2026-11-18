package it.attendance100.mybicocca.codegen.resources

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RatingsDirectoryTest {
    @Test
    fun `finds the study courses missing from a department's numbering`() {
        // Psicologia's: F5102P was never listed, nor E2003P.
        val holes = RatingsDirectory.studyCourseHoles(mapOf("F5101P" to "LM", "F5103P" to "LM", "F5104P" to "LM", "E2004P" to "L2"))
        assertEquals(mapOf("E2001P" to "L2", "E2002P" to "L2", "E2003P" to "L2", "E2005P" to "L2", "F5102P" to "LM", "F5105P" to "LM"), holes)
    }

    @Test
    fun `finds the courses missing from a study course's numbering, within each hundred`() {
        assertEquals(setOf("I0102D002", "I0102D004", "I0102D900", "I0102D902"), RatingsDirectory.courseHoles(setOf("I0102D001", "I0102D003", "I0102D901")))
    }

    @Test
    fun `takes the one choice that makes up what a listing lacks`() {
        // Economia 2015 lacked 270; the study courses with data for the year, under any department, had these.
        assertEquals(listOf(2), RatingsDirectory.closing(listOf(3976, 835, 270, 345), 270))
        // Psicologia 2014 lacked 2,125: E2003P and F5102P.
        assertEquals(listOf(0, 1), RatingsDirectory.closing(listOf(1228, 897, 5), 2125))
    }

    @Test
    fun `takes nothing when no choice, or more than one, makes it up`() {
        assertNull(RatingsDirectory.closing(listOf(5, 7), 6))
        assertNull(RatingsDirectory.closing(listOf(2, 3, 5), 5))
        assertNull(RatingsDirectory.closing(emptyList(), 5))
    }

    private fun course(id: String, studyCourse: String, department: String, year: Int, vararg teachers: String): Pair<String, JsonObject> = id to buildJsonObject {
        put("id", id)
        put("url", "${CourseRatingsBuilder.BASE_URL}opinioniAD.vm?idAD=$id&idAA=$year&idDIP=$department&idCDS=$studyCourse&idTC=L2")
        put("teachers", JsonArray(teachers.map { teacher ->
            buildJsonObject { put("url", "${CourseRatingsBuilder.BASE_URL}opinioniDOC.vm?idDOC=$teacher&idDIP=$department&idCDS=$studyCourse&idTC=L2&idAD=$id&idAA=$year") }
        }))
    }

    private fun year(year: Int, vararg courses: Pair<String, JsonObject>) = buildJsonObject {
        put("id", year)
        put("courses", JsonObject(courses.toMap()))
    }

    @Test
    fun `reads the codes of every year's pages`() {
        val directory = RatingsDirectory(
            mapOf(
                2016 to year(2016, course("I0102D001", "I0102D", "001144", 2016, "000001", "000002"), course("I0102D003", "I0102D", "001144", 2016, "000003")),
                2017 to year(2017, course("E2701Q001", "E2701Q", "2954", 2017, "000004")),
            ),
        )
        assertEquals(setOf("001144", "2954"), directory.departments.toSet())
        assertEquals(mapOf("I0102D" to "L2"), directory.studyCourses["001144"]?.toMap())
        assertEquals(setOf("E2701Q"), directory.listedStudyCourses[2017]?.toSet())
        assertEquals(setOf("I0102D001", "I0102D003"), directory.courses["I0102D"]?.toSet())
        assertEquals(setOf("000001", "000002"), directory.teachers["I0102D001"]?.toSet())
        assertEquals(setOf("000001", "000002", "000003"), directory.studyCourseTeachers["I0102D" to 2016]?.toSet())
    }
}
