package it.attendance100.mybicocca.domain.model.elearning.course

// Parsed form of the elearning idNumber. Moodle codes follow XXYY-N1-N2-...
// where the leading 4-digit XXYY segment encodes the academic year as two
// two-digit halves. The activity code that matches PlannedCourse.code is
// everything after the year segment AND one more `-`-delimited segment
// (e.g. "2526-1-E1805M002" -> "E1805M002"; "2526-1-A-B" -> "A-B"). Both
// fields are nullable when the input doesn't conform.
data class CourseCode(
    val academicYear: AcademicYear?,
    val code: String?,
) {
    companion object {
        val Empty = CourseCode(academicYear = null, code = null)

        fun parse(idNumber: String?): CourseCode {
            if (idNumber.isNullOrBlank()) return Empty
            val firstSegment = idNumber.substringBefore('-', missingDelimiterValue = idNumber)
            val academicYear = parseAcademicYear(firstSegment)
            val afterYear = idNumber.substringAfter('-', missingDelimiterValue = "")
            val code = afterYear.substringAfter('-', missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
            return CourseCode(academicYear = academicYear, code = code)
        }

        private fun parseAcademicYear(segment: String): AcademicYear? {
            if (segment.length != 4 || !segment.all(Char::isDigit)) return null
            val start = 2000 + segment.substring(0, 2).toInt()
            val end = 2000 + segment.substring(2, 4).toInt()
            return AcademicYear(startYear = start, endYear = end)
        }
    }
}

fun EnrolledCourse.courseCode(): CourseCode = CourseCode.parse(idNumber)
