package it.attendance100.mybicocca.data.local.certificate

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one row of the student's self-certifications list (Esse3 legacy
 * ListaCertificati.do). The list is live-first, so this table is written through only on a
 * successful fetch and read back only when the device has no network; the PDF download stays
 * live-only and its action is disabled offline. The row mirrors the
 * [it.attendance100.mybicocca.domain.model.document.Certificate] fields one-to-one, with the
 * type encoded by name.
 *
 * @property ownerId Owning person (Esse3 personId); scopes the row so accounts never cross.
 *   The list is person-scoped (legacy session), not career-scoped.
 * @property certificateId The Esse3 request path carried opaquely by
 *   [it.attendance100.mybicocca.domain.model.document.CertificateId]; together with [ownerId]
 *   the row's key.
 * @property cacheOrder Server list position, preserved so the offline list reads back in the
 *   same order the page returned.
 * @property type Broad family of the declaration, stored as the enum constant name.
 * @property solarYear Solar year the declaration refers to, or null when year-independent.
 * @property digitallySigned Whether the issued PDF carries a digital signature.
 */
@Entity(tableName = "cached_certificate", primaryKeys = ["owner_id", "certificate_id"])
data class CertificateEntity(
    @ColumnInfo("owner_id") val ownerId: Long,
    @ColumnInfo("certificate_id") val certificateId: String,
    @ColumnInfo("cache_order") val cacheOrder: Int,
    val description: String,
    val type: String,
    @ColumnInfo("solar_year") val solarYear: Int?,
    @ColumnInfo("digitally_signed") val digitallySigned: Boolean,
)
