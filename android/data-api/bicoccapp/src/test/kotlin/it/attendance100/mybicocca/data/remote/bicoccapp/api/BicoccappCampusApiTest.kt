package it.attendance100.mybicocca.data.remote.bicoccapp.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class BicoccappCampusApiTest : BicoccappApiTestBase() {
    companion object {
        private const val TEACHER_EMAIL = "daniela.micucci@unimib.it"
        private const val MESSAGE = "TEST - IGNORE THIS MESSAGE - AUTOMATED TEST"
    }

    @Test
    suspend fun getPointsOfInterest() {
        val maps = api.campus.getPointsOfInterest()
        assertNotNull(maps)
        assertNotNull(maps.locations)
        assertTrue(maps.locations.isNotEmpty())
        assertNotNull(maps.filters)
        assertTrue(maps.filters.isNotEmpty())
    }

    @Test
    suspend fun getTeacherDetails() {
        val teacher = api.campus.getTeacherDetails(TEACHER_EMAIL)
        assertNotNull(teacher)
    }

    @Test
    @Disabled
    suspend fun sendAppointmentRequest() {
        val result = api.campus.sendAppointmentRequest(
            teacherKey = TEACHER_EMAIL,
            appUserId = profile.appUserId,
            messageBody = MESSAGE
        )
        assertTrue(result)
    }
}
