package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.dto.esse3.*
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
    companion object {
        private const val TAX_BILLS_ENTRYPOINT = "/auth/studente/Tasse/ListaFatture.do?menu_opened_cod=menu_link-navbox_studenti_Tasse"
        private const val TAX_BILL_DETAIL_PATH = "/auth/studente/Tasse/FatturaDettaglio.do"
        private const val PAYMENT_RECEIPT_PATH = "/auth/studente/Tasse/StampaQuietanzaPagoPA.do"
        private const val REFRESH_PAYMENT_PATH = "/auth/studente/Tasse/ListaFatture.do"

        private val IUV_REGEX = "IUV\\s*:?\\s*(\\S+)".toRegex()
        private val PAYMENT_METHOD_REGEX = "Modalità\\s*:?\\s*(.+?)(?=\\s{2,}|Codice|IUV|$)".toRegex()
        private val PAYMENT_NOTICE_CODE_REGEX = "Codice Avviso\\s*:?\\s*(\\d+)".toRegex()
        private val PAYMENT_DATE_REGEX = "Data Pagamento\\s*:?\\s*([\\d/]+)".toRegex()
        private val RPT_STATUS_REGEX = "Stato RPT\\s*:?\\s*(.+?)(?=\\s{2,}|Esito|$)".toRegex()
        private val TRANSACTION_OUTCOME_REGEX = "Esito transazione pagoPA\\s*:?\\s*(.+?)(?=\\s{2,}|$)".toRegex()
    }

    /**
     * Extracts a query parameter from a URL or href string.
     */
    private fun extractQueryParam(href: String, vararg paramNames: String): String? {
        val queryString = href.substringAfter("?", "")
        val params = parseQueryString(queryString)
        return paramNames.firstNotNullOfOrNull { params[it] }
    }

    /**
     * Gets the list of tax bills.
     *
     * @return List of tax bills
     */
    suspend fun getTaxBills(): List<Esse3TaxBill> {
        val doc = executeGet(TAX_BILLS_ENTRYPOINT)

        val table = doc.selectFirst("table#tasse-tableFatt")
            ?: doc.selectFirst("table.table-1")
            ?: throw IllegalStateException("Cannot get tax bills: missing tax bills table")

        val headers = table.select("thead tr th").map { it.text().cleanText().lowercase() }
        val rows = table.select("tbody tr")

        if (rows.isEmpty()) {
            return emptyList()
        }

        return rows.map { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val invoiceCell = rowMap["fattura"]
                ?: throw IllegalStateException("Cannot get tax bills: missing 'fattura' column")
            val invoiceNumber = invoiceCell.text().cleanText()

            val billId = invoiceCell.selectFirst("a[href*=fatt_id]")
                ?.attr("href")
                ?.let { extractQueryParam(it, "fatt_id")?.toLongOrNull() }
                ?: invoiceNumber.toLongOrNull()
                ?: throw IllegalStateException("Cannot get tax bills: missing bill ID for invoice '$invoiceNumber'")

            val description = rowMap["descrizione"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get tax bills: missing 'descrizione' column")

            val dueDateText = rowMap["data scadenza"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get tax bills: missing 'data scadenza' column")
            val dueDate = parseDate(dueDateText)

            val amountText = rowMap["importo"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get tax bills: missing 'importo' column")
            val amount = parseAmount(amountText)

            val statusText = rowMap["stato pagamento"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get tax bills: missing 'stato pagamento' column")
            val paymentStatus = Esse3PaymentStatus.fromString(statusText)

            val hasPagoPa = rowMap["pagamento pagopa"]?.selectFirst("a, button, form") != null ||
                    rowMap["avviso pagopa"]?.selectFirst("a, button, form") != null

            Esse3TaxBill(
                id = billId,
                invoiceNumber = invoiceNumber,
                description = description,
                dueDate = dueDate,
                amount = amount,
                paymentStatus = paymentStatus,
                pagoPaAvailable = hasPagoPa
            )
        }
    }

    /**
     * Gets detailed information about a specific tax bill.
     *
     * @param bill The tax bill
     * @return The bill detail
     */
    suspend fun getTaxBillDetail(bill: Esse3TaxBill): Esse3TaxBillDetail {
        val doc = executeGet(TAX_BILL_DETAIL_PATH, mapOf("fatt_id" to bill.id.toString()))

        // Parse record info from dl structure
        val dlElement = doc.selectFirst("dl.record-riga")
        val recordMap = mutableMapOf<String, String>()
        if (dlElement != null) {
            val dts = dlElement.select("dt")
            val dds = dlElement.select("dd")
            dts.forEachIndexed { index, dt ->
                val key = dt.text().cleanText().lowercase()
                val value = dds.getOrNull(index)?.text()?.cleanText() ?: ""
                recordMap[key] = value
            }
        }

        val html = doc.html()

        val paymentDateText = recordMap["data pagamento"]
        val paymentDate = paymentDateText?.let { parseDate(it) }

        val paymentMethodText = recordMap["modalità"]
        val paymentMethod = paymentMethodText
            ?.let { Esse3PaymentMethod.fromString(it) }
            ?: Esse3PaymentMethod.Other("")

        val paymentNoticeCode = recordMap["codice avviso"]
        val rptStatusText = recordMap["stato rpt"]
        val rptStatus = rptStatusText?.let { Esse3RptStatus.fromString(it) }

        val transactionOutcome = doc.selectFirst("#tasse-textInfoTransPagoPA")?.text()
            ?.let { TRANSACTION_OUTCOME_REGEX.find(it)?.groupValues?.get(1)?.cleanText() }

        val rptId = doc.selectFirst("input[name=rpt_id]")?.attr("value")
            ?: doc.selectFirst("a[href*=rpt_id]")?.attr("href")?.let { extractQueryParam(it, "rpt_id") }

        val iuv = IUV_REGEX.find(html)?.groupValues?.get(1)

        val pagoPaInfo = paymentNoticeCode?.let {
            Esse3PagoPAInfo(
                noticeCode = it,
                id = iuv,
                requestId = rptId,
                date = paymentDate,
                requestStatus = rptStatus,
                transactionOutcome = transactionOutcome
            )
        }

        val itemsTable = doc.selectFirst("table#tasse-tableVociFattura")
            ?: doc.selectFirst("table.table-1")

        val items = if (itemsTable != null) {
            val headerRow = itemsTable.selectFirst("tbody tr")
            val headers = headerRow?.select("th")?.map { it.text().cleanText().lowercase() } ?: emptyList()

            val dataRows = itemsTable.select("tbody tr")
                .drop(1) // Skip header row
                .filter { row ->
                    // Skip section header rows (th with colspan spanning all columns)
                    val ths = row.select("th[colspan]")
                    ths.isEmpty()
                }

            // Track cells that span multiple rows: Map<columnIndex, Pair<value, remainingRows>>
            val spanningCells = mutableMapOf<Int, Pair<String, Int>>()

            dataRows.mapNotNull { row ->
                val cells = row.select("td")
                if (cells.isEmpty()) return@mapNotNull null

                val rowMap = mutableMapOf<String, String>()
                var cellIndex = 0

                for ((headerIndex, header) in headers.withIndex()) {
                    val spanning = spanningCells[headerIndex]
                    if (spanning != null && spanning.second > 0) {
                        rowMap[header] = spanning.first
                        spanningCells[headerIndex] = spanning.first to (spanning.second - 1)
                    } else {
                        val cell = cells.getOrNull(cellIndex)
                        val value = cell?.text()?.cleanText() ?: ""
                        rowMap[header] = value

                        val rowspan = cell?.attr("rowspan")?.toIntOrNull() ?: 1
                        if (rowspan > 1) {
                            spanningCells[headerIndex] = value to (rowspan - 1)
                        }
                        cellIndex++
                    }
                }

                val academicYear = rowMap["anno"]
                    ?: throw IllegalStateException("Cannot get tax bill items: missing 'anno' column")

                val installment = rowMap["rata"]
                    ?: throw IllegalStateException("Cannot get tax bill items: missing 'rata' column")

                val itemDescription = rowMap["voce"]
                    ?: throw IllegalStateException("Cannot get tax bill items: missing 'voce' column")

                val itemAmountText = rowMap["importo"]
                    ?: throw IllegalStateException("Cannot get tax bill items: missing 'importo' column")
                val itemAmount = parseAmount(itemAmountText)

                Esse3TaxBillItem(
                    academicYear = academicYear,
                    installment = installment,
                    description = itemDescription,
                    amount = itemAmount
                )
            }
        } else {
            emptyList()
        }

        return Esse3TaxBillDetail(
            bill = bill,
            items = items,
            paymentMethod = paymentMethod,
            pagoPAInfo = pagoPaInfo
        )
    }

    /**
     * Downloads a pagoPA payment receipt.
     *
     * @param detail The tax bill detail (must have pagoPaInfo with rptId)
     * @return The PDF content as a byte channel
     * @throws IllegalStateException if the bill has no pagoPA info or rptId
     */
    suspend fun downloadPaymentReceipt(detail: Esse3TaxBillDetail): ByteReadChannel? {
        val rptId = detail.pagoPAInfo?.requestId
            ?: return null
        val response = executeGetRaw(
            PAYMENT_RECEIPT_PATH,
            mapOf("fatt_id" to detail.bill.id.toString(), "rpt_id" to rptId)
        )
        return response.bodyAsChannel()
    }

    /**
     * Refreshes payment status by checking with the payment system.
     *
     * @return Updated list of tax bills
     */
    suspend fun refreshPaymentStatus(): List<Esse3TaxBill> {
        executePost(
            REFRESH_PAYMENT_PATH,
            mapOf(
                "form_id_formCtrlPagamenti" to "formCtrlPagamenti",
                "bCtrlPagamenti" to "Pagamenti"
            )
        )
        return getTaxBills()
    }

    private fun parseAmount(value: String): BigDecimal {
        val cleaned = value
            .replace("€", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return cleaned.toBigDecimalOrNull()
            ?: throw IllegalStateException("Invalid amount: $value")
    }
}
