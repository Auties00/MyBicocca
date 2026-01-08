package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BicoccappCampusApiTest : BicoccappApiTestBase() {
    companion object {
        private const val TEACHER_EMAIL = "daniela.micucci@unimib.it"
    }

    @Test
    suspend fun getPointsOfInterest() {
        val pois = api.campus.getPointsOfInterest()
        assertNotNull(pois.maps)
    }

    @Test
    suspend fun getTeacherByEmail() {
        val teacher = api.campus.getTeacherByEmail(TEACHER_EMAIL)
        assertNotNull(teacher)
    }

    @Test
    suspend fun sendAppointmentRequest() {
        api.campus.sendAppointmentRequest(
            teacherKey = TEACHER_EMAIL,
            appUserId = profile.appUserId.toInt(),
            messageBody = "TEST - IGNORE THIS MESSAGE - AUTOMATED TEST"
        )
    }
}
