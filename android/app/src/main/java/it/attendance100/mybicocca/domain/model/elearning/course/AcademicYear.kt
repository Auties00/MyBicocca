package it.attendance100.mybicocca.domain.model.elearning.course

data class AcademicYear(val startYear: Int, val endYear: Int) {
    override fun toString(): String = "%d/%02d".format(startYear, endYear % 100)
}
