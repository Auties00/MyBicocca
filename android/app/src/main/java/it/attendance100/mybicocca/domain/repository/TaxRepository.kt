package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxSummary

/**
 * Tuition and fee data of the active student, from Esse3.
 *
 * Reads are live-first: payment state (paid/expired/pagoPA transaction outcome) is volatile,
 * so every call hits Esse3 while connectivity exists and throws on failure; the hoisted
 * TaxesViewModel holds the result in memory for the session. The list reads (invoices,
 * summary, ISEE, refunds) keep an offline snapshot of their last success purely for display
 * when the device has no network; live payment status and the pagoPA actions are never
 * served stale. Exam bookings follow the same live-first policy.
 */
interface TaxRepository {

    suspend fun getInvoices(careerId: CareerId): List<TaxInvoice>

    suspend fun getSummary(careerId: CareerId): TaxSummary

    suspend fun getIseeDeclarations(careerId: CareerId): List<IseeDeclaration>

    /** Starts an immediate pagoPA transaction and returns the redirect URL to open. */
    suspend fun startPagoPaPayment(careerId: CareerId, invoiceId: InvoiceId, returnUrl: String): String

    /** pagoPA payment notice ("avviso") PDF bytes. */
    suspend fun getPagoPaNotice(careerId: CareerId, invoiceId: InvoiceId): ByteArray

    /** pagoPA payment receipt ("quietanza") PDF bytes. */
    suspend fun getPagoPaReceipt(careerId: CareerId, invoiceId: InvoiceId, language: String = "it"): ByteArray

    /**
     * Live pagoPA status of an invoice from its latest transaction; null if none exists.
     * Student-accessible via /pagopa/transazioni (the admin-only chiediStatoVersamento 403s).
     */
    suspend fun getPaymentStatus(careerId: CareerId, invoiceId: InvoiceId): PaymentStatus?

    /** Fee refunds (rimborsi). Student-accessible via /lista-rimborsi/{persId}. */
    suspend fun getRefunds(careerId: CareerId): List<Refund>
}
