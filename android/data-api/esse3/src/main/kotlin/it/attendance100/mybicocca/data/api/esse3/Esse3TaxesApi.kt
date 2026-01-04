package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.*
import it.attendance100.mybicocca.data.dto.esse3.Esse3PaymentStatus
import it.attendance100.mybicocca.data.dto.esse3.Esse3TaxBill
import it.attendance100.mybicocca.data.dto.esse3.Esse3TaxBillDetail
import it.attendance100.mybicocca.data.dto.esse3.Esse3TaxBillItem
import org.jsoup.nodes.Document
import java.math.BigDecimal

/**
 * API for tax and payment operations.
 *
 * Provides access to:
 * - List tax bills
 * - View bill details
 * - Download payment receipts
 * - Check payment status
 */
class Esse3TaxesApi(
    client: HttpClient
) : Esse3AbstractApi(client) {

    /**
     * Gets the list of tax bills.
     *
     * @return List of tax bills
     */
    suspend fun getTaxBills(): List<Esse3TaxBill> {
        val doc = executeGet(
            "/auth/studente/Tasse/ListaFatture.do",
            mapOf("menu_opened_cod" to "menu_link-navbox_studenti_Tasse")
        )
        return parseTaxBills(doc)
    }

    /**
     * Gets detailed information about a specific tax bill.
     *
     * @param billId The bill ID
     * @return The bill detail or null if not found
     */
    suspend fun getTaxBillDetail(billId: Long): Esse3TaxBillDetail {
        val doc = executeGet(
            "/auth/studente/Tasse/FatturaDettaglio.do",
            mapOf("fatt_id" to billId.toString())
        )
        return parseTaxBillDetail(doc, billId)
    }

    /**
     * Downloads a pagoPA payment receipt.
     *
     * @param billId The bill ID
     * @param rptId The RPT ID
     * @return The PDF bytes
     */
    suspend fun downloadPaymentReceipt(
        billId: Long,
        rptId: String
    ): ByteArray {
        val response = executeGetRaw(
            "/auth/studente/Tasse/StampaQuietanzaPagoPA.do",
            mapOf("fatt_id" to billId.toString(), "rpt_id" to rptId)
        )
        return response.body<ByteArray>()
    }

    /**
     * Refreshes payment status by checking with the payment system.
     *
     * @return Updated list of tax bills
     */
    suspend fun refreshPaymentStatus(): List<Esse3TaxBill> {
        // POST to refresh payments
        executePost(
            "/auth/studente/Tasse/ListaFatture.do",
            mapOf(
                "form_id_formCtrlPagamenti" to "formCtrlPagamenti",
                "bCtrlPagamenti" to "Pagamenti"
            )
        )

        // Then get updated list
        return getTaxBills()
    }

    private fun parseTaxBills(doc: Document): List<Esse3TaxBill> {
        val bills = mutableListOf<Esse3TaxBill>()

        val table = doc.selectFirst("table.breaks3.table-1") ?: doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 5) {
                    // Columns: Fattura, Codice Bollettino, Descrizione, Data Scadenza, Importo, Stato Pagamento, ...
                    val invoiceNumber = cells[0].text().cleanText()
                    val paymentSlipCode = cells[1].text().cleanText().takeIf { it.isNotBlank() }
                    val description = cells[2].text().cleanText()
                    val dueDate = cells[3].text().cleanText().let { parseDate(it) }
                    val amountText = cells[4].text().cleanText()
                    val amount = parseAmount(amountText) ?: BigDecimal.ZERO
                    val statusText = cells.getOrNull(5)?.text()?.cleanText() ?: ""
                    val paymentStatus = Esse3PaymentStatus.fromString(statusText)

                    // Check for pagoPA availability
                    val hasPagoPa = cells.getOrNull(6)?.selectFirst("a, button") != null

                    // Extract bill ID from link
                    val billIdMatch = row.selectFirst("a[href*=fatt_id]")
                        ?.attr("href")
                        ?.let { "fatt_id=(\\d+)".toRegex().find(it) }
                    val billId = billIdMatch?.groupValues?.get(1)?.toLongOrNull()
                        ?: invoiceNumber.toLongOrNull()
                        ?: 0L

                    bills.add(
                        Esse3TaxBill(
                            id = billId,
                            invoiceNumber = invoiceNumber,
                            paymentSlipCode = paymentSlipCode,
                            description = description,
                            dueDate = dueDate,
                            amount = amount,
                            paymentStatus = paymentStatus,
                            pagoPaAvailable = hasPagoPa
                        )
                    )
                }
            }
        }

        return bills
    }

    private fun parseTaxBillDetail(doc: Document, billId: Long): Esse3TaxBillDetail {
        // Parse bill header
        val record = doc.selectFirst("div.record, div.breaks3.record")
        val headerText = record?.text() ?: doc.text()

        val invoiceNumber = "Fattura\\s*(\\d+)".toRegex()
            .find(headerText)?.groupValues?.get(1) ?: billId.toString()

        val dueDate = "Data Scadenza\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex()
            .find(headerText)?.groupValues?.get(1)?.let { parseDate(it) }

        val amountMatch = "Importo\\s*([\\d.,]+\\s*€)".toRegex()
            .find(headerText)?.groupValues?.get(1)
        val amount = amountMatch?.let { parseAmount(it) } ?: BigDecimal.ZERO

        val paymentMethod = "Modalità\\s*(.+?)(?:\\s{2,}|Codice|$)".toRegex()
            .find(headerText)?.groupValues?.get(1)?.trim()

        val iuv = "IUV\\s*:?\\s*(\\S+)".toRegex()
            .find(headerText)?.groupValues?.get(1)

        // Extract payment code (Codice Avviso) - available in HAR as labeled value
        val paymentCode = "Codice Avviso\\s*:?\\s*(\\d+)".toRegex()
            .find(headerText)?.groupValues?.get(1)

        // Parse items table
        val items = mutableListOf<Esse3TaxBillItem>()
        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 3) {
                    val academicYear = cells.getOrNull(0)?.text()?.cleanText()
                    val installment = cells.getOrNull(1)?.text()?.cleanText()
                    val itemDescription = cells.getOrNull(2)?.text()?.cleanText() ?: ""
                    val itemAmountText = cells.getOrNull(3)?.text()?.cleanText() ?: ""
                    val itemAmount = parseAmount(itemAmountText) ?: BigDecimal.ZERO

                    if (itemDescription.isNotBlank()) {
                        items.add(
                            Esse3TaxBillItem(
                                academicYear = academicYear,
                                installment = installment,
                                description = itemDescription,
                                amount = itemAmount
                            )
                        )
                    }
                }
            }
        }

        // Check for RPT ID in forms
        val rptId = doc.selectFirst("input[name=rpt_id]")?.attr("value")
            ?: doc.html().let { "rpt_id=(\\w+)".toRegex().find(it)?.groupValues?.get(1) }

        // Determine payment status
        val statusText = doc.text()
        val paymentStatus = when {
            statusText.contains("pagato", ignoreCase = true) -> Esse3PaymentStatus.PAID_CONFIRMED
            statusText.contains("scaduto", ignoreCase = true) -> Esse3PaymentStatus.OVERDUE
            statusText.contains("in attesa", ignoreCase = true) -> Esse3PaymentStatus.PENDING
            else -> Esse3PaymentStatus.UNPAID
        }

        val bill = Esse3TaxBill(
            id = billId,
            invoiceNumber = invoiceNumber,
            paymentSlipCode = null,
            description = doc.pageTitle() ?: "Tax Bill $invoiceNumber",
            dueDate = dueDate,
            amount = amount,
            paymentStatus = paymentStatus,
            items = items,
            pagoPaAvailable = doc.selectFirst("form[action*=PagoPA]") != null
        )

        return Esse3TaxBillDetail(
            bill = bill,
            paymentMethod = paymentMethod,
            paymentCode = paymentCode,
            iuv = iuv,
            rptId = rptId
        )
    }

    /**
     * Parses an Italian-formatted currency string to BigDecimal.
     * Example: "1.234,56 €" -> 1234.56
     */
    private fun parseAmount(value: String): BigDecimal? {
        return runCatching {
            val cleaned = value
                .replace("€", "")
                .replace(" ", "")
                .replace(".", "") // Remove thousands separator
                .replace(",", ".") // Convert decimal separator
                .trim()
            BigDecimal(cleaned)
        }.getOrNull()
    }
}
