package it.attendance100.mybicocca.domain.model.elearning.course

data class CourseSection(
    val id: Int,
    val sectionNumber: Int,
    val name: String,
    val summary: String?,
    val visible: Boolean,
    val modules: List<CourseModule>,
)
