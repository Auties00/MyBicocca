package it.attendance100.mybicocca.data.local.tax

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one fee refund row (Esse3 rimborso) on a career's account. Written through
 * only on a successful fetch and read back only when the device has no network. Mirrors
 * [it.attendance100.mybicocca.domain.model.tax.Refund], which carries no stable id of its own,
 * so the row is keyed by career plus its list position; dates are kept as ISO-8601 strings.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property cacheOrder Server list position; pairs with [careerId] as the key and preserves the
 *   order the API returned.
 * @property invoiceId Esse3 invoice the refund originates from, when any.
 */
@Entity(tableName = "cached_tax_refund", primaryKeys = ["career_id", "cache_order"])
data class RefundEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    @ColumnInfo("invoice_id") val invoiceId: Long?,
    @ColumnInfo("academic_year") val academicYear: Int?,
    val amount: Double?,
    val description: String?,
    @ColumnInfo("reason_code") val reasonCode: String?,
    @ColumnInfo("mandate_number") val mandateNumber: String?,
    val refunded: Boolean,
    val note: String?,
    @ColumnInfo("collected_by") val collectedBy: String?,
    @ColumnInfo("issue_date") val issueDate: String?,
    @ColumnInfo("processing_date") val processingDate: String?,
    @ColumnInfo("payment_date") val paymentDate: String?,
    @ColumnInfo("credit_date") val creditDate: String?,
)
