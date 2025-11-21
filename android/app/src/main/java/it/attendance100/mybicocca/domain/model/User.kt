package it.attendance100.mybicocca.domain.model

/**
 * User business model
 * Represents the student's profile information
 */
data class User(
  val name: String,
  val surname: String,
  val matricola: String,
  val course: String,
  val year: String,
  val email: String,
)
