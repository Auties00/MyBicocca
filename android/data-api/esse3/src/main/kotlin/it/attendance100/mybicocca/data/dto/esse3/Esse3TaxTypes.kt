package it.attendance100.mybicocca.data.dto.esse3

import java.math.BigDecimal
import java.time.LocalDate

/**
 * A tax bill (fattura).
 */
data class Esse3TaxBill(
    val id: Long,
    val invoiceNumber: String,
    val paymentSlipCode: String?,
    val description: String,
    val dueDate: LocalDate?,
    val amount: BigDecimal,
    val currency: String = "EUR",
    val paymentStatus: Esse3PaymentStatus,
    val items: List<Esse3TaxBillItem> = emptyList(),
    val pagoPaAvailable: Boolean
)

/**
 * An item within a tax bill.
 */
data class Esse3TaxBillItem(
    val academicYear: String?,
    val installment: String?,
    val description: String,
    val amount: BigDecimal
)

/**
 * Payment status of a tax bill.
 */
enum class Esse3PaymentStatus {
    UNPAID,
    PENDING,
    PAID_CONFIRMED,
    OVERDUE,
    PARTIALLY_PAID;

    companion object {
        fun fromString(value: String): Esse3PaymentStatus {
            val normalized = value.lowercase().trim()
            return when {
                normalized.contains("pagato") && normalized.contains("confermato") -> PAID_CONFIRMED
                normalized.contains("pagato") -> PAID_CONFIRMED
                normalized.contains("in attesa") || normalized.contains("pending") -> PENDING
                normalized.contains("scaduto") || normalized.contains("overdue") -> OVERDUE
                normalized.contains("parziale") -> PARTIALLY_PAID
                else -> UNPAID
            }
        }
    }
}

/**
 * Tax bill detail with full information.
 */
data class Esse3TaxBillDetail(
    val bill: Esse3TaxBill,
    val paymentMethod: String?,
    val paymentCode: String?,
    val iuv: String?, // Identificativo Univoco Versamento for pagoPA
    val rptId: String? // For pagoPA receipt
)