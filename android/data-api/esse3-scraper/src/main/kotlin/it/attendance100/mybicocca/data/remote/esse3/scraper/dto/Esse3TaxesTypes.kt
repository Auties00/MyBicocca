package it.attendance100.mybicocca.data.remote.esse3.scraper.dto

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Payment status for a tax bill.
 */
sealed interface Esse3PaymentStatus {
    /**
     * Bill has been paid and payment is confirmed by the system.
     */
    data object PaidConfirmed : Esse3PaymentStatus

    /**
     * Payment has been submitted but not yet confirmed.
     */
    data object PaidPending : Esse3PaymentStatus

    /**
     * Bill is unpaid but not yet overdue.
     */
    data object Unpaid : Esse3PaymentStatus

    /**
     * Bill is overdue (past due date and unpaid).
     */
    data object Overdue : Esse3PaymentStatus

    /**
     * Bill has been cancelled or voided.
     */
    data object Cancelled : Esse3PaymentStatus

    /**
     * Unknown status from the system.
     */
    data class Other(val value: String) : Esse3PaymentStatus

    companion object {
        fun fromString(value: String): Esse3PaymentStatus {
            val normalized = value.trim().lowercase()
            return when {
                normalized.contains("pagato") && normalized.contains("confermato") -> PaidConfirmed
                normalized.contains("pagato") -> PaidPending
                normalized.contains("non pagato") || normalized == "da pagare" -> Unpaid
                normalized.contains("scaduto") || normalized.contains("mora") -> Overdue
                normalized.contains("annullat") || normalized.contains("storn") -> Cancelled
                normalized.isBlank() -> Unpaid
                else -> Other(value)
            }
        }
    }
}

/**
 * Payment method for a tax bill.
 */
sealed interface Esse3PaymentMethod {
    /**
     * Payment via pagoPA (Italian electronic payment platform).
     */
    data object PagoPA : Esse3PaymentMethod

    /**
     * Bank transfer (MAV/RAV).
     */
    data object BankTransfer : Esse3PaymentMethod

    /**
     * Unknown payment method.
     */
    data class Other(val value: String) : Esse3PaymentMethod

    companion object {
        fun fromString(value: String): Esse3PaymentMethod {
            val normalized = value.trim().lowercase()
            return when {
                normalized.contains("pagopa") -> PagoPA
                normalized.contains("mav") || normalized.contains("rav") || normalized.contains("bonifico") -> BankTransfer
                else -> Other(value)
            }
        }
    }
}

/**
 * Status of a pagoPA RPT (Richiesta Pagamento Telematico) transaction.
 */
sealed interface Esse3RptStatus {
    /**
     * Transaction was accepted and payment completed successfully.
     */
    data object Accepted : Esse3RptStatus

    /**
     * Transaction is pending processing.
     */
    data object Pending : Esse3RptStatus

    /**
     * Transaction was rejected.
     */
    data object Rejected : Esse3RptStatus

    /**
     * Transaction had an error.
     */
    data class Error(val message: String) : Esse3RptStatus

    /**
     * Unknown status.
     */
    data class Other(val value: String) : Esse3RptStatus

    companion object {
        fun fromString(value: String): Esse3RptStatus? {
            if (value.isBlank()) return null
            val normalized = value.trim().lowercase()
            return when {
                normalized.contains("accettata") || normalized.contains("successo") -> Accepted
                normalized.contains("in attesa") || normalized.contains("pending") -> Pending
                normalized.contains("rifiutata") || normalized.contains("rejected") -> Rejected
                normalized.contains("errore") || normalized.contains("error") -> Error(value)
                else -> Other(value)
            }
        }
    }
}

/**
 * A tax bill (fattura) in the taxes list.
 */
data class Esse3TaxBill(
    /**
     * Unique bill identifier.
     */
    val id: Long,

    /**
     * Invoice number displayed to users.
     */
    val invoiceNumber: String,

    /**
     * Description of the bill (e.g., "Matricola 909697 - Corso di Laurea - INFORMATICA - Rata: 1 di 3").
     */
    val description: String,

    /**
     * Due date for payment.
     */
    val dueDate: LocalDate?,

    /**
     * Total amount to pay.
     */
    val amount: BigDecimal,

    /**
     * Current payment status.
     */
    val paymentStatus: Esse3PaymentStatus,

    /**
     * Whether pagoPA payment is available for this bill.
     */
    val pagoPaAvailable: Boolean
)

/**
 * An item (voce) within a tax bill.
 */
data class Esse3TaxBillItem(
    /**
     * Academic year (e.g., "2024/2025").
     */
    val academicYear: String,

    /**
     * Installment description (e.g., "3 di 3").
     */
    val installment: String,

    /**
     * Item description (e.g., "Contributi Universitari").
     */
    val description: String?,

    /**
     * Item amount.
     */
    val amount: BigDecimal
)

/**
 * Detailed information about a tax bill.
 */
data class Esse3TaxBillDetail(
    /**
     * Basic bill information.
     */
    val bill: Esse3TaxBill,

    /**
     * List of line items that make up the bill.
     */
    val items: List<Esse3TaxBillItem>,

    /**
     * Payment method used or required.
     */
    val paymentMethod: Esse3PaymentMethod,

    /**
     * pagoPA payment information, if available.
     */
    val pagoPAInfo: Esse3PagoPAInfo?
)

/**
 * pagoPA payment information for a tax bill.
 */
data class Esse3PagoPAInfo(
    /**
     * Payment notice code (Codice Avviso).
     */
    val noticeCode: String,

    /**
     * IUV (Identificativo Univoco Versamento) - unique payment identifier.
     */
    val id: String?,

    /**
     * RPT ID (Richiesta Pagamento Telematico identifier).
     */
    val requestId: String?,

    /**
     * Date when payment was made.
     */
    val date: LocalDate?,

    /**
     * Status of the RPT transaction.
     */
    val requestStatus: Esse3RptStatus?,

    /**
     * Detailed transaction outcome message.
     */
    val transactionOutcome: String?
)
