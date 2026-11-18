package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningCourseApiTest : ElearningTestApiBase() {
    @Test
    suspend fun getUserCourses() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        assertNotNull(coursesResponse)
        assertNotNull(coursesResponse.courses)
    }

    @Test
    suspend fun getCourseContents() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val firstCourse = coursesResponse.courses.first()
            val contents = api.courses.getCourseContents(session.wsToken, firstCourse.id)
            assertNotNull(contents)
            assertNotNull(contents.sections)
        }
    }

    @Test
    suspend fun getCoursePublicInfo() {
        val info = api.courses.getCoursePublicInfo(60702)
        assertEquals(60702, info.id)
        assertTrue(info.name.isNotBlank())
        assertTrue(info.code.isNotBlank())
        assertTrue(info.viewUrl.contains("course/view.php"))
        assertTrue(info.syllabus.isNotEmpty())
        assertTrue(info.syllabus.any { it.language == "it" })
        assertTrue(info.syllabus.all { it.fields.isNotEmpty() })
        assertTrue(info.staff.isNotEmpty())
        assertTrue(info.enrolmentMethods.isNotEmpty())
    }

    @Test
    suspend fun getCourseMetadata() {
        val metadata = api.courses.getCourseMetadata(session.wsToken, 60702)
        assertNotNull(metadata)
        assertEquals("INF/01", metadata!!.disciplinarySector)
        assertEquals(8, metadata.cfu)
        assertNotNull(metadata.period)
        assertNotNull(metadata.degreeType)
    }

}
