package it.attendance100.mybicocca.domain.model.elearning.course

enum class CourseStaffRole(val raw: String) {
    Docente("Docente"),
    Tutor("Tutor"),
    Esercitatore("Esercitatore"),
    Other("Other");

    companion object {
        fun fromRaw(raw: String?): CourseStaffRole = when (raw?.trim()?.lowercase()) {
            null, "" -> Other
            "docente" -> Docente
            "tutor" -> Tutor
            "esercitatore" -> Esercitatore
            else -> Other
        }
    }
}
