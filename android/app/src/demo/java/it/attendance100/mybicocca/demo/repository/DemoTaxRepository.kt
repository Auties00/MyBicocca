package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxChargeItem
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import it.attendance100.mybicocca.domain.repository.TaxRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Two academic years of fees, all paid except the current year's last instalment, due next month. */
@Singleton
class DemoTaxRepository @Inject constructor() : TaxRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private val invoices = listOf(
        invoice(6003, 2025, "Terza rata", 612.0, issued = today.minusDays(20), expires = today.plusDays(27), paidOn = null),
        invoice(6002, 2025, "Seconda rata", 612.0, issued = LocalDate.of(2026, 1, 12), expires = LocalDate.of(2026, 2, 16), paidOn = LocalDate.of(2026, 2, 9)),
        invoice(6001, 2025, "Prima rata", 156.0, issued = LocalDate.of(2025, 7, 15), expires = LocalDate.of(2025, 9, 30), paidOn = LocalDate.of(2025, 9, 2)),
        invoice(5003, 2024, "Terza rata", 598.0, issued = LocalDate.of(2025, 5, 28), expires = LocalDate.of(2025, 7, 15), paidOn = LocalDate.of(2025, 7, 1)),
        invoice(5002, 2024, "Seconda rata", 598.0, issued = LocalDate.of(2025, 1, 13), expires = LocalDate.of(2025, 2, 17), paidOn = LocalDate.of(2025, 2, 11)),
        invoice(5001, 2024, "Prima rata", 156.0, issued = LocalDate.of(2024, 7, 15), expires = LocalDate.of(2024, 9, 30), paidOn = LocalDate.of(2024, 9, 18)),
    )

    override suspend fun getInvoices(careerId: CareerId): List<TaxInvoice> = invoices

    override suspend fun getSummary(careerId: CareerId): TaxSummary {
        val due = invoices.filter { it.status == TaxStatus.PENDING }
        return TaxSummary(light = TaxLight.YELLOW, dueAmount = due.sumOf { it.amount }, expiredCount = 0, dueCount = due.size)
    }

    override suspend fun getIseeDeclarations(careerId: CareerId): List<IseeDeclaration> = listOf(
        IseeDeclaration(
            academicYearEnrollmentId = DemoStudent.career.academicYear.toLong(),
            courseDescription = DemoCareer.DEGREE,
            isee = 24_380.0,
            iseeThreshold = 30_000.0,
            exemptionDescription = "Esonero parziale per reddito",
        ),
    )

    override suspend fun startPagoPaPayment(careerId: CareerId, invoiceId: InvoiceId, returnUrl: String): String = returnUrl

    override suspend fun getPagoPaNotice(careerId: CareerId, invoiceId: InvoiceId): ByteArray =
        document("Avviso di pagamento pagoPA", invoiceId)

    override suspend fun getPagoPaReceipt(careerId: CareerId, invoiceId: InvoiceId, language: String): ByteArray =
        document("Ricevuta di pagamento", invoiceId)

    override suspend fun getPaymentStatus(careerId: CareerId, invoiceId: InvoiceId): PaymentStatus? =
        invoices.firstOrNull { it.id == invoiceId }?.let {
            val paid = it.status == TaxStatus.PAID
            PaymentStatus(
                invoiceId = invoiceId,
                outcome = if (paid) PaymentOutcome.Completed else PaymentOutcome.Pending,
                description = if (paid) "Pagamento eseguito" else "In attesa di pagamento",
                paid = paid,
                paymentDate = it.paymentDate,
                paidAmount = it.paidAmount,
                receiptPrintable = paid,
            )
        }

    override suspend fun getRefunds(careerId: CareerId): List<Refund> = listOf(
        Refund(
            invoiceId = 5003,
            academicYear = 2024,
            amount = 140.0,
            description = "Rimborso per ricalcolo fascia ISEE",
            reasonCode = "ISEE",
            mandateNumber = "2025/004417",
            refunded = true,
            note = null,
            collectedBy = "Accredito su conto corrente",
            issueDate = LocalDate.of(2025, 10, 6),
            processingDate = LocalDate.of(2025, 10, 20),
            paymentDate = LocalDate.of(2025, 11, 3),
            creditDate = LocalDate.of(2025, 11, 5),
        ),
    )

    private fun document(title: String, invoiceId: InvoiceId): ByteArray {
        val invoice = invoices.first { it.id == invoiceId }
        return DemoCareer.pdf(title, listOf(invoice.title, "Importo: %.2f €".format(invoice.amount), "Scadenza: ${invoice.expiration}"))
    }

    private fun invoice(
        id: Long,
        year: Int,
        title: String,
        amount: Double,
        issued: LocalDate,
        expires: LocalDate,
        paidOn: LocalDate?,
    ) = TaxInvoice(
        id = InvoiceId(id),
        academicYear = year,
        title = "$title $year/${(year + 1) % 100}",
        amount = amount,
        paidAmount = paidOn?.let { amount },
        status = if (paidOn != null) TaxStatus.PAID else TaxStatus.PENDING,
        issueDate = issued,
        expiration = expires,
        paymentDate = paidOn,
        pagoPaEnabled = true,
        pagoPaImmediate = paidOn == null,
        pagoPaNotice = paidOn == null,
        iuv = "0000000$id",
        noticeCode = "30100000000000$id",
        items = if (amount < 200) {
            listOf(
                TaxChargeItem("Tassa regionale per il diritto allo studio", 140.0, title, expires),
                TaxChargeItem("Imposta di bollo", 16.0, title, expires),
            )
        } else {
            listOf(TaxChargeItem("Contributo onnicomprensivo", amount, title, expires))
        },
    )
}
