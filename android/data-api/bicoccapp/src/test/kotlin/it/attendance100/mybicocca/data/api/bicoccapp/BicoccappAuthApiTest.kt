package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLoginResponse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappAuthApiTest : BicoccappApiTestBase() {
    @Test
    suspend fun login() {
        // Try to logout
        val logoutResult = api.auth.logout()
        assertTrue(logoutResult, "Logout failed")

        // Try to login
        when (val loginResult = api.auth.login(username, password)) {
            is BicoccappLoginResponse.Error -> fail("Login failed")
            is BicoccappLoginResponse.Success -> {
                assertFalse(session.accessToken == loginResult.accessToken)
                BicoccappGlobalApiData.session = loginResult.toSession()
            }
        }

        // Try to login again
        when (val loginResult = api.auth.login(username, password)) {
            is BicoccappLoginResponse.Error -> fail("Relogin failed")
            is BicoccappLoginResponse.Success ->  BicoccappGlobalApiData.session = loginResult.toSession()
        }
    }
}
