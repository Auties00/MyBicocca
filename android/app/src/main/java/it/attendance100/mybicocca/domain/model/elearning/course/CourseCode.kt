package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

// Parsed form of the elearning idNumber. Moodle codes follow XXYY-N-CODE... where the
// leading 4-digit XXYY segment encodes the academic year as two two-digit halves and the
// next segment N is the year of study (annoCorso). The activity code that matches
// PlannedCourse.code is everything after those two segments (e.g. "2526-2-E1805M002" ->
// year 2025/26, course year 2, code "E1805M002"; "2526-1-A-B" -> code "A-B"). Every field
// is nullable when the input doesn't conform (announcements, Erasmus, privacy pages, ...).
//
// courseYear comes straight from the idNumber on purpose: it's verified to agree with the
// Esse3 plan's annoCorso 100% of the time while covering strictly more courses (turni and
// blended editions the plan omits), needs no network, and is always available offline.
data class CourseCode(
    val academicYear: AcademicYear?,
    val courseYear: StudyYear?,
    val code: String?,
) {
    companion object {
        val Empty = CourseCode(academicYear = null, courseYear = null, code = null)

        fun parse(idNumber: String?): CourseCode {
            if (idNumber.isNullOrBlank()) return Empty
            val firstSegment = idNumber.substringBefore('-', missingDelimiterValue = idNumber)
            val academicYear = parseAcademicYear(firstSegment)
            val afterYear = idNumber.substringAfter('-', missingDelimiterValue = "")
            // The year-of-study segment only carries meaning on the canonical
            // "<academicYear>-<courseYear>-<code>" form, so require a parsed academic year.
            val courseYear = if (academicYear == null) null
            else afterYear.substringBefore('-', missingDelimiterValue = "")
                .toIntOrNull()
                ?.takeIf { it in 1..9 }
                ?.let(::StudyYear)
            val code = afterYear.substringAfter('-', missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
            return CourseCode(academicYear = academicYear, courseYear = courseYear, code = code)
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
