package it.attendance100.mybicocca.domain.model.enrollment

// Esse3 staIscrCod: A = ATTIVA, X = ANNULLATA, S = SOSPESA. Unknown covers any code
// the server may introduce that we don't recognise.
enum class EnrollmentStatus {
    Active,
    Canceled,
    Suspended,
    Unknown;

    companion object {
        fun fromCode(code: String?): EnrollmentStatus = when (code?.uppercase()?.trim()) {
            "A" -> Active
            "X" -> Canceled
            "S" -> Suspended
            else -> Unknown
        }
    }
}
