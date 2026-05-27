package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

data class TaxInvoice(
    val id: InvoiceId,
    val academicYear: Int?,
    val title: String,
    val amount: Double,
    val paidAmount: Double?,
    val status: TaxStatus,
    val issueDate: LocalDate?,
    val expiration: LocalDate?,
    val paymentDate: LocalDate?,
    val pagoPaEnabled: Boolean,
    val pagoPaImmediate: Boolean,
    val pagoPaNotice: Boolean,
    val iuv: String?,
    val noticeCode: String?,
    val items: List<TaxChargeItem>,
)
