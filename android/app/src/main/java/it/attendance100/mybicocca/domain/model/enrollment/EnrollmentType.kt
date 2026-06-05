package it.attendance100.mybicocca.domain.model.enrollment

// Esse3 tipoIscrCod: IC = in corso, FC = fuori corso, RI = ripetente. Unknown keeps the
// server-provided description so the UI can still render an unrecognised code.
enum class EnrollmentType {
    InProgress,
    OutOfCourse,
    Repeating,
    Unknown;

    companion object {
        fun fromCode(code: String?): EnrollmentType = when (code?.uppercase()?.trim()) {
            "IC" -> InProgress
            "FC" -> OutOfCourse
            "RI" -> Repeating
            else -> Unknown
        }
    }
}
