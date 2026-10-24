package it.attendance100.mybicocca.data.remote.esse3.dto

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The web portal pages the generated scrapers read. */
class Esse3ScrapersTest {
    private val listUrl = "https://s3w.si.unimib.it/auth/studente/Certificati/ListaCertificati.do"

    @Test
    fun `reads the self-declarations from their links`() {
        val page = """
            <html><body><div id="gu-contentSx">
              <a href="MessaggiCertificato.do?cert_conf_id=12&amp;doc_id=34&amp;tipo_cert_cod=I&amp;firma_dig_flg=1">Autodichiarazione Iscrizione con Esami</a>
              <a href="MessaggiCertificato.do?cert_conf_id=56&amp;doc_id=78&amp;tipo_cert_cod=TAX&amp;annosolare=2024">Autodichiarazione Tasse</a>
              <a href="MessaggiCertificato.do?cert_conf_id=90&amp;doc_id=91&amp;tipo_cert_cod=X">Altro</a>
              <a href="MessaggiCertificato.do?doc_id=1">Senza configurazione</a>
            </div></body></html>
        """.trimIndent()
        val result = Esse3Scrapers.certificatesPage(Jsoup.parse(page, listUrl))!!
        assertFalse(result.sessionExpired)
        assertEquals(3, result.certificates.size)

        val enrolment = result.certificates[0]
        assertEquals(12, enrolment.configurationId)
        assertEquals(34, enrolment.documentId)
        assertEquals("Autodichiarazione Iscrizione con Esami", enrolment.description)
        assertEquals(Esse3CertificateType.Enrolment, enrolment.type)
        assertNull(enrolment.solarYear)
        assertEquals("1", enrolment.signatureFlag)

        val fees = result.certificates[1]
        assertEquals(Esse3CertificateType.TuitionFees, fees.type)
        assertEquals(2024, fees.solarYear)
        assertNull(fees.signatureFlag)

        assertEquals(Esse3CertificateType.Unknown("X"), result.certificates[2].type)
    }

    @Test
    fun `tells the identity provider's login page apart`() {
        val login = """<html><body><form action="/idp/profile/SAML2/Redirect/SSO?execution=e1s3"><input name="j_username"></form></body></html>"""
        val result = Esse3Scrapers.certificatesPage(Jsoup.parse(login, "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO"))!!
        assertTrue(result.sessionExpired)
        assertTrue(result.certificates.isEmpty())
    }
}
