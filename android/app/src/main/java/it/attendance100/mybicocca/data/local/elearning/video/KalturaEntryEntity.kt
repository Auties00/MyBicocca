package it.attendance100.mybicocca.data.local.elearning.video

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The Kaltura entry a kalvidres course module plays, keyed by cm_id. Not account-scoped: the
 * binding is the same for every user. Only `mod/kalvidres/view.php` reveals it, and every load
 * of that page logs a "viewed" event in Moodle, so it is resolved once and kept.
 */
@Entity(tableName = "elearning_kaltura_entry")
data class KalturaEntryEntity(
    @PrimaryKey @ColumnInfo(name = "cm_id") val cmId: Int,
    @ColumnInfo(name = "entry_id") val entryId: String,
    @ColumnInfo(name = "resolved_at_ms") val resolvedAtMs: Long,
)
