package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class Esse3LegacyApiTest : Esse3TestBase() {

    @Test
    suspend fun getSelfCertifications() {
        val certifications = api.legacy.getSelfCertifications()
        assertNotNull(certifications)
    }

}
