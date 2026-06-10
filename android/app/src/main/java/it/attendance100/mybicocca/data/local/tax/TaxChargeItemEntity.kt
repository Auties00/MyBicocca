package it.attendance100.mybicocca.data.local.tax

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one charge line ("voce") of a cached tuition invoice. Child rows of
 * [TaxInvoiceEntity]: the invoice's `items` list maps to these, keyed by the owning invoice and
 * preserving their order. Written through and read back under the same offline-only contract as
 * the parent invoice; the expiration date is kept as an ISO-8601 string.
 *
 * @property careerId Owning career (Esse3 stuId); scopes the row so accounts never cross.
 * @property invoiceId Invoice the charge belongs to; pairs with [careerId] to join the parent.
 * @property itemOrder Position of the charge within its invoice, preserved so the offline
 *   breakdown reads back in the same order the API returned.
 */
@Entity(tableName = "cached_tax_charge_item", primaryKeys = ["career_id", "invoice_id", "item_order"])
data class TaxChargeItemEntity(
    @ColumnInfo("career_id") val careerId: Long,
    @ColumnInfo("invoice_id") val invoiceId: Long,
    @ColumnInfo("item_order") val itemOrder: Int,
    val description: String,
    val amount: Double,
    @ColumnInfo("installment_description") val installmentDescription: String?,
    val expiration: String?,
)
