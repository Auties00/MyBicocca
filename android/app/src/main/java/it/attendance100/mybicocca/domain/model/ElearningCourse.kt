package it.attendance100.mybicocca.domain.model

data class ElearningCourse(
  val id: Int,
  val fullname: String,
  val shortname: String,
  val idNumber: String?,
  val summary: String?,
  val categoryId: Int,
)
