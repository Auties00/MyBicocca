package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningUserApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getUserById() {
        val usersResponse = api.users.getUserById(session.wsToken, profile.userId)
        assertNotNull(usersResponse)
    }

    @Test
    suspend fun getUserPreferences() {
        val preferences = api.users.getUserPreferences(session.wsToken, userId = profile.userId)
        assertNotNull(preferences)
        assertNotNull(preferences.preferences)
    }

    @Test
    suspend fun getPrivateFilesInfo() {
        val filesInfo = api.users.getPrivateFilesInfo(session.wsToken, profile.userId)
        assertNotNull(filesInfo)
    }
}
