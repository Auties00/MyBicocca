package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoCertificateRepository @Inject constructor() : CertificateRepository {

    private val certificates = listOf(
        Certificate(CertificateId("ISCR"), "Certificato di iscrizione", CertificateType.Enrolment, null, true),
        Certificate(CertificateId("ISCR-ESAMI"), "Certificato di iscrizione con esami", CertificateType.Enrolment, null, true),
        Certificate(CertificateId("TASSE-2025"), "Dichiarazione delle tasse pagate", CertificateType.TuitionFees, 2025, true),
        Certificate(CertificateId("TASSE-2024"), "Dichiarazione delle tasse pagate", CertificateType.TuitionFees, 2024, true),
    )

    override suspend fun getCertificates(): List<Certificate> = certificates

    override suspend fun downloadCertificate(id: CertificateId): ByteArray {
        val certificate = certificates.first { it.id == id }
        return DemoCareer.pdf(
            certificate.description,
            listOf("Corso di laurea in ${DemoCareer.DEGREE}", "Anno accademico 2025/2026, secondo anno di corso."),
        )
    }
}
