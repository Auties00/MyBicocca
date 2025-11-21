package it.attendance100.mybicocca.domain.model

/**
 * Career statistics business model
 * Represents the student's academic progress and grades
 */
data class CareerStats(
  val mediaAritmetica: Float,
  val mediaPonderata: Float,
  val esamiSostenuti: Int,
  val esamiTotali: Int,
  val cfuAcquisiti: Int,
  val cfuTotali: Int,
  val grades: List<Float>,
)
