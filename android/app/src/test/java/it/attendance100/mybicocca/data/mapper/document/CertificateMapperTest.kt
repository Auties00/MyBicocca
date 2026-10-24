package it.attendance100.mybicocca.data.mapper.document

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Certificate
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CertificateType
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import org.junit.Test

/**
 * Covers the mapping of the portal's self-declarations: the print parameters round-trip through
 * the opaque CertificateId, and every certificate-type variant maps to its domain counterpart,
 * with unknown and missing codes collapsing to the generic Other family.
 */
class CertificateMapperTest {

    private fun certificate(type: Esse3CertificateType?, signatureFlag: String? = "1") = Esse3Certificate(
        configurationId = 1,
        documentId = 2,
        description = "Autodichiarazione Iscrizione",
        type = type,
        solarYear = 2024,
        signatureFlag = signatureFlag,
    )

    @Test
    fun `carries the print parameters inside the certificate id`() {
        val certificate = certificate(Esse3CertificateType.Enrolment).toDomain()
        assertThat(certificate.description).isEqualTo("Autodichiarazione Iscrizione")
        assertThat(certificate.solarYear).isEqualTo(2024)
        assertThat(certificate.digitallySigned).isTrue()
        assertThat(certificate.id.toPrintRequest()).isEqualTo(CertificatePrintRequest(1, 2, "I", 2024, "1"))
    }

    @Test
    fun `an unsigned declaration has no signature flag`() {
        val certificate = certificate(Esse3CertificateType.Enrolment, signatureFlag = null).toDomain()
        assertThat(certificate.digitallySigned).isFalse()
        assertThat(certificate.id.toPrintRequest()?.signatureFlag).isNull()
    }

    @Test
    fun `reads the parameters of ids cached as whole links`() {
        assertThat(CertificateId("MessaggiCertificato.do?cert_conf_id=7&doc_id=8&annosolare=2023").toPrintRequest())
            .isEqualTo(CertificatePrintRequest(7, 8, null, 2023, null))
        assertThat(CertificateId("/elsewhere").toPrintRequest()).isNull()
    }

    @Test
    fun `maps every type`() {
        assertThat(certificate(Esse3CertificateType.Enrolment).toDomain().type).isEqualTo(CertificateType.Enrolment)
        assertThat(certificate(Esse3CertificateType.DegreeAward).toDomain().type).isEqualTo(CertificateType.DegreeAward)
        assertThat(certificate(Esse3CertificateType.TuitionFees).toDomain().type).isEqualTo(CertificateType.TuitionFees)
    }

    @Test
    fun `maps unknown and missing codes to the generic Other family`() {
        assertThat(certificate(Esse3CertificateType.Unknown("XYZ")).toDomain().type).isEqualTo(CertificateType.Other)
        assertThat(certificate(null).toDomain().type).isEqualTo(CertificateType.Other)
    }
}
