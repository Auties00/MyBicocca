package it.attendance100.mybicocca.domain.model.document

// A self-declaration ("autocertificazione") template the student can generate as a PDF.
// Sourced from the Esse3 legacy ListaCertificati.do page (no REST surface). [id] is the
// opaque token the repository needs to fetch the PDF; the UI never interprets it.
data class Certificate(
    val id: CertificateId,
    // Human-readable label, e.g. "Autodichiarazione Iscrizione con Esami".
    val description: String,
    val type: CertificateType,
    // Solar year the declaration refers to (mainly the tax ones); null when year-independent.
    val solarYear: Int?,
    // Whether the issued PDF carries a digital signature.
    val digitallySigned: Boolean,
)

// Opaque handle that round-trips to the repository to download the PDF. Wraps the Esse3
// request path; the UI treats it as a black box.
@JvmInline
value class CertificateId(val value: String)

enum class CertificateType {
    Enrolment,
    DegreeAward,
    TuitionFees,
    Other,
}
