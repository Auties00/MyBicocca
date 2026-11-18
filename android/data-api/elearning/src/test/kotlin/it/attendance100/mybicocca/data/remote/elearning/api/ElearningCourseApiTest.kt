package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningCourseApiTest : ElearningTestApiBase() {
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
}
