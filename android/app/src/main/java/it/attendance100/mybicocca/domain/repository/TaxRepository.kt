package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxSummary

// Tax data is intentionally NOT cached locally — payment state (paid/expired/pagoPA
// transaction outcome) is volatile and a stale read is misleading. Every call hits Esse3;
// the hoisted TaxesViewModel holds the result in memory for the session. Mirrors
// ExamRepository (see project_booking_no_cache.md).
interface TaxRepository {

    suspend fun getInvoices(careerId: CareerId): List<TaxInvoice>

    suspend fun getSummary(careerId: CareerId): TaxSummary

    suspend fun getIseeDeclarations(careerId: CareerId): List<IseeDeclaration>

    // Starts an immediate pagoPA transaction and returns the redirect URL to open.
    suspend fun startPagoPaPayment(careerId: CareerId, invoiceId: InvoiceId, returnUrl: String): String

    // pagoPA payment notice ("avviso") PDF bytes.
    suspend fun getPagoPaNotice(careerId: CareerId, invoiceId: InvoiceId): ByteArray

    // pagoPA payment receipt ("quietanza") PDF bytes.
    suspend fun getPagoPaReceipt(careerId: CareerId, invoiceId: InvoiceId, language: String = "it"): ByteArray

    // Live pagoPA status of an invoice from its latest transaction; null if none exists.
    // Student-accessible via /pagopa/transazioni (the admin-only chiediStatoVersamento 403s).
    suspend fun getPaymentStatus(careerId: CareerId, invoiceId: InvoiceId): PaymentStatus?

    // Fee refunds (rimborsi). Student-accessible via /lista-rimborsi/{persId}.
    suspend fun getRefunds(careerId: CareerId): List<Refund>
}
