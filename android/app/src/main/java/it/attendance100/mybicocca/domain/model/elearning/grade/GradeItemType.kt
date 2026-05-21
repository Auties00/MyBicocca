package it.attendance100.mybicocca.domain.model.elearning.grade

enum class GradeItemType(val raw: String) {
    Activity("activity"),
    Category("category"),
    Course("course"),
    Manual("manual"),
    Other("other");

    companion object {
        fun fromRaw(raw: String?): GradeItemType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
