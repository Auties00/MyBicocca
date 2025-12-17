package it.attendance100.mybicocca.domain.model

import java.time.*

data class Registration(
  val academicYear: String,
  val date: LocalDate,
  val type: String, // e.g. "Immatricolazione", "Rinnovo"
  val status: String, // e.g. "Attivo"
  val courseName: String,
)
