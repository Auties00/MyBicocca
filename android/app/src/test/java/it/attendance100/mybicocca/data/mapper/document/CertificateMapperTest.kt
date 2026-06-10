package it.attendance100.mybicocca.data.mapper.document

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertificationType
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import org.junit.Test

/**
 * Covers the legacy-scrape self-certification mapping: the request path is carried opaquely
 * inside CertificateId and every certificate-type variant maps to its domain counterpart, with
 * the parametrized Other code collapsing to the generic Other family.
 */
class CertificateMapperTest {

    private fun certification(type: Esse3SelfCertificationType) = Esse3SelfCertification(
        configurationId = 1,
        documentId = 2,
        description = "Autodichiarazione Iscrizione",
        type = type,
        solarYear = 2024,
        digitallySigned = true,
        requestPath = "MessaggiCertificato.do?cert_conf_id=1&doc_id=2",
    )

    @Test
    fun `carries request path opaquely inside the certificate id`() {
        val certificate = certification(Esse3SelfCertificationType.Enrolment).toDomain()
        assertThat(certificate.id).isEqualTo(CertificateId("MessaggiCertificato.do?cert_conf_id=1&doc_id=2"))
        assertThat(certificate.description).isEqualTo("Autodichiarazione Iscrizione")
        assertThat(certificate.solarYear).isEqualTo(2024)
        assertThat(certificate.digitallySigned).isTrue()
    }

    @Test
    fun `maps enrolment type`() {
        assertThat(certification(Esse3SelfCertificationType.Enrolment).toDomain().type)
            .isEqualTo(CertificateType.Enrolment)
    }

    @Test
    fun `maps degree-award type`() {
        assertThat(certification(Esse3SelfCertificationType.DegreeAward).toDomain().type)
            .isEqualTo(CertificateType.DegreeAward)
    }

    @Test
    fun `maps tuition-fees type`() {
        assertThat(certification(Esse3SelfCertificationType.TuitionFees).toDomain().type)
            .isEqualTo(CertificateType.TuitionFees)
    }

    @Test
    fun `maps any other code to the generic Other family`() {
        assertThat(certification(Esse3SelfCertificationType.Other("XYZ")).toDomain().type)
            .isEqualTo(CertificateType.Other)
    }
}
