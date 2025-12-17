package it.attendance100.mybicocca.domain.model

import java.time.*

data class ElearningAssignment(
  val id: Int,
  val cmId: Int,
  val courseId: Int,
  val name: String,
  val intro: String?,
  val dueDate: LocalDateTime,
  val allowSubmissionsFrom: LocalDateTime?,
)
