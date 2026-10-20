package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Read-only calls of the attendance API against the live site, which lists the attendance
 * modules through the generated Moodle client. Nothing is marked. Needs a web service token in
 * `MOODLE_TOKEN`.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("live")
class ElearningAttendanceApiTest {
    private val api = ElearningApi()
    private lateinit var token: String
    private var courseIds = emptyList<Int>()

    @BeforeAll
    fun setUp() = runBlocking {
        token = System.getenv("MOODLE_TOKEN").orEmpty()
        assumeTrue(token.isNotBlank(), "Set MOODLE_TOKEN to run the attendance live tests")
        val auth = MoodleTokenAuth(token)
        val userId = api.moodle.site.getSiteInfo(auth = auth).userId
        courseIds = api.moodle.course.getUsersCourses(userId, auth = auth).map { it.id }
    }

    @AfterAll
    fun tearDown() = api.close()

    @Test
    fun `attendance modules decode`(): Unit = runBlocking {
        var found = false
        for (courseId in courseIds) {
            val modules = api.attendance.getAttendanceModules(token, courseId)
            assertTrue(modules.all { it.modName == "attendance" })
            found = modules.isNotEmpty()
            if (found) break
        }
        assumeTrue(found, "No attendance module in the user's courses")
    }
}
