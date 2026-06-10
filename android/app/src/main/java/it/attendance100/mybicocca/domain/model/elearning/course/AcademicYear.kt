package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * An academic year, parsed from the leading four-digit segment of a Moodle course's
 * idNumber (e.g. "2526" reads as 2025/26). Renders as "startYear/yy".
 *
 * @property startYear Full calendar year the academic year begins in.
 * @property endYear Full calendar year the academic year ends in.
 */
data class AcademicYear(val startYear: Int, val endYear: Int) {
    override fun toString(): String = "%d/%02d".format(startYear, endYear % 100)
}
