package it.attendance100.mybicocca.domain.model.enrollment

/**
 * How the student is enrolled for the year, from Esse3 `tipoIscrCod`: "IC" = in corso,
 * "FC" = fuori corso, "RI" = ripetente. For [Unknown] codes the server-provided
 * description travels separately on the enrollment, so the UI can still render them.
 */
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
