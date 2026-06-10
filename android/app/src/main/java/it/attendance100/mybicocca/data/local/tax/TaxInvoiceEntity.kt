package it.attendance100.mybicocca.data.local.tax

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one tuition invoice (Esse3 fattura) on a career's account. Taxes are
 * live-first, so this table is written through only on a successful fetch and read back only
 * when the device has no network; pagoPA payments and PDF downloads stay disabled offline. The
 * row mirrors the scalar [it.attendance100.mybicocca.domain.model.tax.TaxInvoice] fields, with
 * the invoice's charge breakdown stored in the child [TaxChargeItemEntity] table and dates kept
 * as ISO-8601 strings.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property invoiceId Together with [careerId], the invoice's key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the
 *   same order the API returned.
 * @property status Payment lifecycle state stored by enum name, read back with a fallback.
 */
@Entity(tableName = "cached_tax_invoice", primaryKeys = ["career_id", "invoice_id"])
data class TaxInvoiceEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("invoice_id") val invoiceId: Long,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("academic_year") val academicYear: Int?,
    val title: String,
    val amount: Double,
    @ColumnInfo("paid_amount") val paidAmount: Double?,
    val status: String,
    @ColumnInfo("issue_date") val issueDate: String?,
    val expiration: String?,
    @ColumnInfo("payment_date") val paymentDate: String?,
    @ColumnInfo("pagopa_enabled") val pagoPaEnabled: Boolean,
    @ColumnInfo("pagopa_immediate") val pagoPaImmediate: Boolean,
    @ColumnInfo("pagopa_notice") val pagoPaNotice: Boolean,
    val iuv: String?,
    @ColumnInfo("notice_code") val noticeCode: String?,
)
