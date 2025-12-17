package it.attendance100.mybicocca.domain.model

import java.time.*

data class BookingSlot(
  val id: String,
  val title: String,
  val description: String?,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime,
  val location: String?,
  val isBooked: Boolean,
)
