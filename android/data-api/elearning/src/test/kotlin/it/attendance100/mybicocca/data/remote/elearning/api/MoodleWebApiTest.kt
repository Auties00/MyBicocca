package it.attendance100.mybicocca.data.remote.elearning.api

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
}
