package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class Esse3LegacyApiTest : Esse3TestBase() {

    @Test
    suspend fun getSelfCertifications() {
        val certifications = api.legacy.getSelfCertifications()
        assertNotNull(certifications)
    }

    // Tirocini management flows are stubbed (TODO) in Esse3LegacyApi — they need an
    // account with an active internship to scrape the form fields. These tests are
    // placeholders, disabled until the methods are implemented.

    @Disabled("Not implemented — needs an active-internship test account")
    @Test
    suspend fun withdrawInternshipApplication() {
        // TODO: api.legacy.withdrawInternshipApplication(stuId, domTiroId)
    }

    @Disabled("Not implemented — needs an active-internship test account")
    @Test
    suspend fun registerInternshipHours() {
        // TODO: api.legacy.registerInternshipHours(stuId, domTiroId, date, hours, description)
    }

    @Disabled("Not implemented — needs an active-internship test account")
    @Test
    suspend fun signTrainingProject() {
        // TODO: api.legacy.signTrainingProject(stuId, domTiroId, accept)
    }

    @Disabled("Not implemented — needs an active-internship test account")
    @Test
    suspend fun uploadInternshipAttachment() {
        // TODO: api.legacy.uploadInternshipAttachment(stuId, domTiroId, fileName, content, title)
    }

    // Procedure flows (proroga / cambio percorso) are stubbed (TODO) in Esse3LegacyApi —
    // they need an account that can actually file the request to scrape the wizard
    // fields. Placeholders, disabled until the methods are implemented.

    @Disabled("Not implemented — needs an account mid-proroga")
    @Test
    suspend fun submitEnrollmentExtension() {
        // TODO: api.legacy.submitEnrollmentExtension(stuId)
    }

    @Disabled("Not implemented — needs a career that allows passaggio")
    @Test
    suspend fun submitCourseChange() {
        // TODO: api.legacy.submitCourseChange(stuId)
    }
}
