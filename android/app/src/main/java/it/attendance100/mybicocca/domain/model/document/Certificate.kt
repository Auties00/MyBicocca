package it.attendance100.mybicocca.domain.model.document

/**
 * A self-declaration ("autocertificazione") template the student can generate as a PDF.
 * Sourced from the Esse3 legacy ListaCertificati.do page — there is no REST surface for it —
 * and listed on the registry "Certificati" sub-screen.
 *
 * @property id Opaque token the repository needs to fetch the PDF; the UI never interprets it.
 * @property description Human-readable label, e.g. "Autodichiarazione Iscrizione con Esami".
 * @property type Broad family the declaration belongs to.
 * @property solarYear Solar year the declaration refers to (mainly the tax ones); null when
 *   year-independent.
 * @property digitallySigned Whether the issued PDF carries a digital signature.
 */
data class Certificate(
    val id: CertificateId,
    val description: String,
    val type: CertificateType,
    val solarYear: Int?,
    val digitallySigned: Boolean,
)

/**
 * Opaque handle that round-trips to the repository to download a certificate PDF. Wraps the
 * Esse3 request path; the UI treats it as a black box.
 *
 * @property value The Esse3 request path of the certificate.
 */
@JvmInline
value class CertificateId(val value: String)

/** Broad family of a self-declaration certificate. */
enum class CertificateType {
    Enrolment,
    DegreeAward,
    TuitionFees,
    Other,
}
