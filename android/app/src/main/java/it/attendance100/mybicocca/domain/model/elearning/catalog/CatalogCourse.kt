package it.attendance100.mybicocca.domain.model.elearning.catalog

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

data class CatalogCourse(
    val id: CourseId,
    val name: String,
    val code: String,
    val url: String,
)
