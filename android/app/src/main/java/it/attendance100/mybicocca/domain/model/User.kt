package it.attendance100.mybicocca.domain.model

data class User(
  val name: String,
  val surname: String,
  val matricola: String,
  val course: String,
  val year: String,
  val email: String,
)
