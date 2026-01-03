package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningSiteApiTest : ElearningTestBase() {

    @Test
    fun `getSiteInfo returns valid site information`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        
        println("Site Info: $siteInfo")
        assertNotNull(siteInfo)
        assertNotNull(siteInfo.siteName)
        assertNotNull(siteInfo.userId)
        assertTrue(siteInfo.userId > 0)
    }

    @Test
    fun `getAuthUrl returns a valid URL`() = runBlocking {
        val authUrl = api.site.getAuthUrl()
        
        println("Auth URL: $authUrl")
        assertNotNull(authUrl)
        assertTrue(authUrl.startsWith("https://"))
        assertTrue(authUrl.contains("passport="))
    }
}
