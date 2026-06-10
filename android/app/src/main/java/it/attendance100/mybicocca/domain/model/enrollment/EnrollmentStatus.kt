package it.attendance100.mybicocca.domain.model.enrollment

/**
 * Administrative state of an annual enrollment, from Esse3 `staIscrCod`:
 * "A" = ATTIVA, "X" = ANNULLATA, "S" = SOSPESA. [Unknown] covers any code the server may
 * introduce that is not recognised.
 */
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
