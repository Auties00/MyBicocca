package it.attendance100.mybicocca.domain.model.elearning.grade

/**
 * What a Moodle gradebook row grades: a single activity, a category aggregate, the
 * course total, or a manually-entered item. Unrecognized kinds collapse to [Other].
 *
 * @property raw The `itemtype` value the grade-items web service reports.
 */
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
