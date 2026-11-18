package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Reads the public course pages of the live site with the generated scrapers, which check that
 * the page structure still matches the spec. Needs no login.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("live")
class MoodleWebApiTest {
    private val api = MoodleWebApi()

    @AfterAll
    fun tearDown() = api.close()

    @Test
    fun `a course's public page reads its syllabus and staff`(): Unit = runBlocking {
        val info = api.catalog.getCourseInfo(60702)
        assertEquals(60702, info.id)
        assertTrue(info.name.isNotBlank())
        assertTrue(info.code.isNotBlank())
        assertTrue(info.viewUrl.contains("course/view.php"))
        assertTrue(info.syllabus.any { it.language == "it" })
        assertTrue(info.syllabus.all { it.fields.isNotEmpty() })
        assertTrue(info.staff.isNotEmpty())
        assertTrue(info.staff.flatMap { it.members }.any { it.email != null && it.userId != null })
        assertTrue(info.enrolmentMethods.isNotEmpty())
    }

    @Test
    fun `an attendance page is not shown without a session`(): Unit = runBlocking {
        val token = System.getenv("MOODLE_TOKEN")
        assumeTrue(!token.isNullOrBlank(), "Set MOODLE_TOKEN to find an attendance activity")
        val moduleId = MoodleApi().use { moodle -> attendanceModuleId(moodle, MoodleTokenAuth(token)) }
        assumeTrue(moduleId != null, "No attendance activity in the user's courses")
        val view = api.attendance.getAttendanceView(courseModuleId = moduleId!!)
        assertFalse(view.shown, "Moodle should serve its login page to an anonymous visitor")
    }

    private suspend fun attendanceModuleId(moodle: MoodleApi, auth: MoodleTokenAuth): Int? {
        val userId = moodle.site.getSiteInfo(auth = auth).userId
        for (course in moodle.course.getUsersCourses(userId, auth = auth)) {
            val options = listOf(MoodleOption("modname", "attendance"), MoodleOption("excludecontents", "1"))
            moodle.course.getContents(course.id, options = options, auth = auth)
                .flatMap { it.modules }
                .firstOrNull { it.modName == "attendance" }
                ?.let { return it.id }
        }
        return null
    }
}
