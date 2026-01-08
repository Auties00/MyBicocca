package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
data class ElearningCourseArea(
    val name: String,
    val categories: List<ElearningCourseCategory>
)

@Serializable
data class ElearningCourseCategory(
    val name: String,
    val url: String
)

@Serializable
data class ElearningCourseCategoryContents(
    val name: String,
    val subcategories: List<ElearningCourseCategory>,
    val courses: List<ElearningCourse>
)

@Serializable
data class ElearningCourse(
    val name: String,
    val code: String,
    val url: String,
    val id: String
)
