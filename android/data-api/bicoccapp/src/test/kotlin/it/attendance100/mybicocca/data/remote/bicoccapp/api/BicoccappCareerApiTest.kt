package it.attendance100.mybicocca.data.remote.bicoccapp.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappCareerApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getCareer() {
        val career = api.career.getCareer(
            personId = profile.personId,
            enrollmentId = profile.enrollmentId,
            studentId = profile.studentId
        )
        assertNotNull(career.averages)
    }

    @Test
    suspend fun getRegistrations() {
        val registrations = api.career.getRegistrations(profile.enrollmentId)
        assertNotNull(registrations)
    }
}
