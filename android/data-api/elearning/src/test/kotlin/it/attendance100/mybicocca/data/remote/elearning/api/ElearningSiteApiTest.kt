package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningSiteApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getSiteInfo() {
        val siteInfo = api.site.getSiteInfo(session.wsToken)
        assertNotNull(siteInfo)
        assertNotNull(siteInfo.siteName)
        assertTrue(siteInfo.userId > 0)
    }
}
