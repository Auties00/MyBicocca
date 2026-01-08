package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningSiteApiTest : ElearningTestBase() {

    @Test
    suspend fun getSiteInfo() {
        val siteInfo = api.site.getSiteInfo(session.wsToken)
        assertNotNull(siteInfo)
        assertNotNull(siteInfo.siteName)
        assertTrue(siteInfo.userId > 0)
    }

    @Test
    suspend fun getAuthUrl() {
        val authUrl = api.site.getAuthUrl()
        assertNotNull(authUrl)
        assertTrue(authUrl.startsWith("https://"))
        assertTrue(authUrl.contains("passport="))
    }
}
