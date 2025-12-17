package it.attendance100.mybicocca.domain.model

data class Location(
  val id: String,
  val name: String,
  val description: String?,
  val category: String,
  val latitude: Double,
  val longitude: Double,
  val address: String?,
)
