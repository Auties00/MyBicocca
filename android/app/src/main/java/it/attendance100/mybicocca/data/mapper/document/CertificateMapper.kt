package it.attendance100.mybicocca.data.mapper.document

import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertificationType
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType

/**
 * Maps the legacy-scrape self-certification DTO to the API-agnostic domain model. The request
 * path is carried opaquely inside [CertificateId] so the repository can hand it straight back
 * to the scraper for the PDF download.
 */
fun Esse3SelfCertification.toDomain(): Certificate = Certificate(
    id = CertificateId(requestPath),
    description = description,
    type = type.toDomain(),
    solarYear = solarYear,
    digitallySigned = digitallySigned,
)

private fun Esse3SelfCertificationType.toDomain(): CertificateType = when (this) {
    Esse3SelfCertificationType.Enrolment -> CertificateType.Enrolment
    Esse3SelfCertificationType.DegreeAward -> CertificateType.DegreeAward
    Esse3SelfCertificationType.TuitionFees -> CertificateType.TuitionFees
    is Esse3SelfCertificationType.Other -> CertificateType.Other
}
