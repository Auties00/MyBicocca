package it.attendance100.mybicocca.domain.model

/**
Represents the career statistics of a user.
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
