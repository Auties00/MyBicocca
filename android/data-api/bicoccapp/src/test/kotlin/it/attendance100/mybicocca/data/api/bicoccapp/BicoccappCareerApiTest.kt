package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BicoccappCareerApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getCareer() {
        val career = api.career.getCareer(
            personId = profile.personId,
            enrollmentId = profile.enrollmentId,
            studentId = profile.studentId
        )
        assertNotNull(career.career.averages)
    }

    @Test
    suspend fun getRegistrations() {
        val registrations = api.career.getRegistrations(profile.enrollmentId)
        assertNotNull(registrations.career.registrations)
    }
}
