package it.attendance100.mybicocca.domain.model.elearning.course

enum class CourseLevel(val short: String, val title: String) {
    Bachelor("L", "Triennale"),
    Master("LM", "Magistrale"),
    Doctorate("D", "Dottorato"),
}
