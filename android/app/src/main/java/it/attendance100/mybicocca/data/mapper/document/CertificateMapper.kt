package it.attendance100.mybicocca.data.mapper.document

import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import io.ktor.http.parseQueryString
import it.attendance100.mybicocca.core.observability.UnknownValues
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Certificate
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CertificateType
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType

/**
 * Maps a self-declaration of the Esse3 portal to the API-agnostic domain model. The parameters
 * that print it are carried opaquely inside [CertificateId], as the query of its link, so the
 * repository can hand them back for the PDF download.
 */
fun Esse3Certificate.toDomain(): Certificate = Certificate(
    id = CertificateId(
        Parameters.build {
            append(CONFIGURATION_ID, configurationId.toString())
            append(DOCUMENT_ID, documentId.toString())
            type?.let { append(TYPE_CODE, it.value) }
            solarYear?.let { append(SOLAR_YEAR, it.toString()) }
            signatureFlag?.let { append(SIGNATURE_FLAG, it) }
        }.formUrlEncode()
    ),
    description = description,
    type = type.toDomain(),
    solarYear = solarYear,
    digitallySigned = !signatureFlag.isNullOrBlank(),
)

/**
 * The arguments of `Esse3CertificatesApi.getCertificatePdf` a [CertificateId] carries.
 */
data class CertificatePrintRequest(
    val configurationId: Int,
    val documentId: Int,
    val typeCode: String?,
    val solarYear: Int?,
    val signatureFlag: String?,
)

/**
 * Reads back the print parameters of [Esse3Certificate.toDomain]. Ids cached before the portal
 * was generated hold the whole link, whose query carries the same parameters.
 *
 * @return The parameters, or null when the id names no certificate.
 */
fun CertificateId.toPrintRequest(): CertificatePrintRequest? {
    val parameters = parseQueryString(value.substringAfter('?'))
    return CertificatePrintRequest(
        configurationId = parameters[CONFIGURATION_ID]?.toIntOrNull() ?: return null,
        documentId = parameters[DOCUMENT_ID]?.toIntOrNull() ?: return null,
        typeCode = parameters[TYPE_CODE],
        solarYear = parameters[SOLAR_YEAR]?.toIntOrNull(),
        signatureFlag = parameters[SIGNATURE_FLAG],
    )
}

private fun Esse3CertificateType?.toDomain(): CertificateType = when (this) {
    Esse3CertificateType.Enrolment -> CertificateType.Enrolment
    Esse3CertificateType.DegreeAward -> CertificateType.DegreeAward
    Esse3CertificateType.TuitionFees -> CertificateType.TuitionFees
    is Esse3CertificateType.Unknown -> {
        UnknownValues.report("certificate_type", value)
        CertificateType.Other
    }
    null -> CertificateType.Other
}

private const val CONFIGURATION_ID = "cert_conf_id"
private const val DOCUMENT_ID = "doc_id"
private const val TYPE_CODE = "tipo_cert_cod"
private const val SOLAR_YEAR = "annosolare"
private const val SIGNATURE_FLAG = "firma_dig_flg"
