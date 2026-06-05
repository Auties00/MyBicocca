package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

// A fee refund (rimborso) on the student's account.
data class Refund(
    val invoiceId: Long?,
    val academicYear: Int?,
    val amount: Double?,
    val reasonCode: String?,
    val mandateNumber: String?,
    val refunded: Boolean,
    val note: String?,
    val issueDate: LocalDate?,
    val paymentDate: LocalDate?,
    val creditDate: LocalDate?,
)
