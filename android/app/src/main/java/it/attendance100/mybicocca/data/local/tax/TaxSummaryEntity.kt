package it.attendance100.mybicocca.data.local.tax

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of a career's tax traffic-light summary (Esse3 semaforo). A single row per
 * career, written through only on a successful fetch and read back only when the device has no
 * network. Mirrors [it.attendance100.mybicocca.domain.model.tax.TaxSummary], with the overall
 * standing stored by enum name and read back with a fallback.
 *
 * @property careerId Owning career (Esse3 stuId) and sole primary key, so each career keeps one
 *   summary row.
 * @property light Overall standing stored by enum name.
 */
@Entity(tableName = "cached_tax_summary", primaryKeys = ["career_id"])
data class TaxSummaryEntity(
    @ColumnInfo("career_id") val careerId: Long,
    val light: String,
    @ColumnInfo("due_amount") val dueAmount: Double,
    @ColumnInfo("expired_count") val expiredCount: Int,
    @ColumnInfo("due_count") val dueCount: Int,
)
