package it.attendance100.mybicocca.domain.model

import java.time.*

data class Payment(
  val id: String,
  val title: String,
  val amount: Double,
  val deadline: LocalDate,
  val status: PaymentStatus,
  val invoiceUrl: String?,
)

enum class PaymentStatus {
  PAID,
  UNPAID,
  OVERDUE,
  CANCELLED
}
