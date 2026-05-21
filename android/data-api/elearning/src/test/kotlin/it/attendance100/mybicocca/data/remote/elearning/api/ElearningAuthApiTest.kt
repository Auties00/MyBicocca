package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningLoginResponse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningAuthApiTest : ElearningTestApiBase() {
    @Test
    suspend fun login() {
        // Try to logout
        val logoutResult = api.auth.logout()
        assertTrue(logoutResult, "Logout failed")

        // Try to login
        when (val loginResult = api.auth.login(username, password)) {
            is ElearningLoginResponse.Error -> fail("Login failed")
            is ElearningLoginResponse.Success -> ElearningGlobalApiData.session = loginResult.toSession()
        }

        // Try to login again
        when (val loginResult = api.auth.login(username, password)) {
            is ElearningLoginResponse.Error -> fail("Relogin failed")
            is ElearningLoginResponse.Success -> ElearningGlobalApiData.session = loginResult.toSession()
        }
    }
}
