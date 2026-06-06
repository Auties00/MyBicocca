package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

// Parsed form of the elearning idNumber. Moodle codes follow XXYY-N-CODE...[-T<stream>] where
// the leading 4-digit XXYY segment encodes the academic year as two two-digit halves, the
// next segment N is the year of study (annoCorso), and an optional trailing "-T<stream>"
// segment marks a lecture stream (Italian "turno"). `code` is the base activity code with
// that stream suffix stripped, so the same course's base and streams share one code and group
// together (e.g. "2526-2-E1805M002" -> code "E1805M002"; "2425-2-E3101Q130-T1" -> code
// "E3101Q130", stream 1; "2526-1-A-B" -> code "A-B"). Every field is nullable when the input
// doesn't conform (announcements, Erasmus, privacy pages, ...).
//
// courseYear comes straight from the idNumber on purpose: it's verified to agree with the
// Esse3 plan's annoCorso 100% of the time while covering strictly more courses (streams and
// blended editions the plan omits), needs no network, and is always available offline.
data class CourseCode(
    val academicYear: AcademicYear?,
    val courseYear: StudyYear?,
    val code: String?,
    val stream: Int?,
) {
    // "<academic year> T<stream>" for the edition header, e.g. "2024/25 T1" or "2024/25".
    // Null when the idNumber carries no academic year (a stream never appears without one).
    val periodLabel: String?
        get() = academicYear?.let { year -> stream?.let { "$year T$it" } ?: year.toString() }

    companion object {
        val Empty = CourseCode(academicYear = null, courseYear = null, code = null, stream = null)

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
            val fullCode = afterYear.substringAfter('-', missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
            val (code, stream) = splitStream(fullCode)
            return CourseCode(academicYear = academicYear, courseYear = courseYear, code = code, stream = stream)
        }

        // Splits a trailing "-T<digits>" stream suffix off the code. The dash must have a
        // base in front of it so a code that is itself "T123" isn't mistaken for a stream.
        private fun splitStream(code: String?): Pair<String?, Int?> {
            if (code == null) return null to null
            val lastDash = code.lastIndexOf('-')
            if (lastDash <= 0) return code to null
            val suffix = code.substring(lastDash + 1)
            val stream = suffix.takeIf { it.length >= 2 && it[0] == 'T' && it.drop(1).all(Char::isDigit) }
                ?.drop(1)?.toIntOrNull()
                ?: return code to null
            return code.substring(0, lastDash) to stream
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
