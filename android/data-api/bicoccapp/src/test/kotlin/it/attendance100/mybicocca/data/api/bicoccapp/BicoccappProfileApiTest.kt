package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BicoccappProfileApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getProfile() {
        val profile = api.profile.getProfile(session.fiscalCode)
        assertNotNull(profile.user)
    }
}
