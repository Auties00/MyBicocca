package it.attendance100.mybicocca.data.local.document

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Offline mirror of one side of a student card's printable artwork. The image download is
 * live-first, so a side is written through only on a successful fetch and read back only when the
 * device has no network. Keyed by the Esse3 blob id and the requested side rather than by career:
 * the same blob can be referenced from any career, and the raw bytes are identical, so the cache is
 * deliberately not account-scoped. Room persists [bytes] natively as a BLOB.
 *
 * @property blobId Esse3 blob id ([it.attendance100.mybicocca.domain.model.document.BadgeBlobId]
 *   value); with [side], the row's key.
 * @property side Which face the bytes are for: `front` or `rear`.
 * @property bytes The downloaded image bytes.
 */
@Entity(tableName = "cached_badge_image", primaryKeys = ["blob_id", "side"])
data class BadgeImageEntity(
    @ColumnInfo("blob_id") val blobId: Long,
    val side: String,
    val bytes: ByteArray,
)
