package it.attendance100.mybicocca.data.remote.bicoccapp.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappProfileApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getProfile() {
        val profile = api.profile.getProfile(session.fiscalCode)
        assertNotNull(profile)
        assertNotNull(profile.user)
        assertNotNull(profile.careers)
        assertTrue(profile.careers.isNotEmpty())
    }
}
