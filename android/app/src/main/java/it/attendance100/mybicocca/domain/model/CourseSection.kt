package it.attendance100.mybicocca.domain.model

data class CourseSection(
  val id: Int,
  val name: String,
  val summary: String?,
  val modules: List<CourseModule>,
)

data class CourseModule(
  val id: Int,
  val name: String,
  val type: String, // e.g. "resource", "url", "forum"
  val url: String?,
  val isCompleted: Boolean,
)
