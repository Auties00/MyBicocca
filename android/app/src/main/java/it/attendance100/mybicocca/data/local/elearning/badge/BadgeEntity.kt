package it.attendance100.mybicocca.data.local.elearning.badge

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached Moodle badge awarded to the account, keyed by (account_id, badge_id).
 * `courseId` is null for site-wide badges, which is why per-course replacement matches
 * it with IS rather than =. `issuedAtMs` is epoch milliseconds.
 */
@Entity(
    tableName = "elearning_badges",
    primaryKeys = ["account_id", "badge_id"],
    indices = [Index("account_id", "course_id")],
)
data class BadgeEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "badge_id") val badgeId: Int,
    val name: String,
    val description: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "issued_at_ms") val issuedAtMs: Long?,
    @ColumnInfo(name = "course_id") val courseId: Int?,
)
