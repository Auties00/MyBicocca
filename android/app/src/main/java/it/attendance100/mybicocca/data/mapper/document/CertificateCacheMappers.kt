package it.attendance100.mybicocca.data.mapper.document

import it.attendance100.mybicocca.data.local.certificate.CertificateEntity
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType

// Entity <-> domain mapping for the offline certificate mirror. The type enum round-trips by
// name with an Other fallback; the request path is carried opaquely inside CertificateId.

fun Certificate.toEntity(ownerId: Long, order: Int): CertificateEntity = CertificateEntity(
    ownerId = ownerId,
    certificateId = id.value,
    cacheOrder = order,
    description = description,
    type = type.name,
    solarYear = solarYear,
    digitallySigned = digitallySigned,
)

fun CertificateEntity.toDomain(): Certificate = Certificate(
    id = CertificateId(certificateId),
    description = description,
    type = runCatching { CertificateType.valueOf(type) }.getOrDefault(CertificateType.Other),
    solarYear = solarYear,
    digitallySigned = digitallySigned,
)
