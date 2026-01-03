package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningUserApiTest : ElearningTestBase() {

    @Test
    fun `user operations work`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val userId = siteInfo.userId
        
        // Get User By ID
        val usersResponse = api.users.getUserById(wsToken, userId)
        assertNotNull(usersResponse)
        println("User info retrieved")

        // Get User Preferences
        try {
            val preferences = api.users.getUserPreferences(wsToken, userId = userId)
            assertNotNull(preferences)
            assertNotNull(preferences.preferences)
            println("Found ${preferences.preferences.size} preferences")
        } catch (e: Exception) {
            println("Failed to get preferences: ${e.message}")
        }
        
        // Private Files Info
        try {
             val filesInfo = api.users.getPrivateFilesInfo(wsToken, userId)
             assertNotNull(filesInfo)
             println("Private files info: filecount=${filesInfo.fileCount}")
        } catch (e: Exception) {
            println("Failed to get private files info: ${e.message}")
        }
    }
}
