package it.attendance100.mybicocca.data.mapper.document

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.certificate.CertificateEntity
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import org.junit.Test

/**
 * Round-trip checks for the offline certificate mirror: the type enum round-trips by name with
 * an Other fallback, the request path is carried opaquely inside CertificateId, and the owner /
 * order keys are stamped onto the entity.
 */
class CertificateCacheMappersTest {

    private fun certificate(type: CertificateType = CertificateType.Enrolment, solarYear: Int? = 2024) =
        Certificate(
            id = CertificateId("path?cert=1"),
            description = "Autodichiarazione",
            type = type,
            solarYear = solarYear,
            digitallySigned = true,
        )

    @Test
    fun `certificate round-trips through the entity`() {
        val domain = certificate(CertificateType.TuitionFees)
        val entity = domain.toEntity(ownerId = 900L, order = 5)

        assertThat(entity.ownerId).isEqualTo(900L)
        assertThat(entity.certificateId).isEqualTo("path?cert=1")
        assertThat(entity.cacheOrder).isEqualTo(5)
        assertThat(entity.type).isEqualTo("TuitionFees")

        assertThat(entity.toDomain()).isEqualTo(domain)
    }

    @Test
    fun `unknown stored type name falls back to Other`() {
        val entity = CertificateEntity(
            ownerId = 1L,
            certificateId = "p",
            cacheOrder = 0,
            description = "d",
            type = "Mystery",
            solarYear = null,
            digitallySigned = false,
        )
        assertThat(entity.toDomain().type).isEqualTo(CertificateType.Other)
    }

    @Test
    fun `null solar year round-trips`() {
        val entity = certificate(solarYear = null).toEntity(1L, 0)
        assertThat(entity.solarYear).isNull()
        assertThat(entity.toDomain().solarYear).isNull()
    }
}
