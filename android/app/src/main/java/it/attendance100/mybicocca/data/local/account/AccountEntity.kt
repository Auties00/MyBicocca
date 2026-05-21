package it.attendance100.mybicocca.data.local.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["record_user_id"], unique = true)],
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "record_user_id") val recordUserId: String,
    @ColumnInfo(name = "person_id") val personId: Long,
    @ColumnInfo(name = "fiscal_code") val fiscalCode: String?,
    @ColumnInfo(name = "selected_career_id") val selectedCareerId: Long,
    @ColumnInfo(name = "lms_user_id") val lmsUserId: Int,
    @ColumnInfo(name = "lms_username") val lmsUsername: String,
    @ColumnInfo(name = "lms_locale") val lmsLocale: String,
    @ColumnInfo(name = "lms_is_site_admin") val lmsIsSiteAdmin: Boolean,
    @ColumnInfo(name = "lms_max_upload_bytes") val lmsMaxUploadBytes: Long,
    @ColumnInfo(name = "lms_storage_quota_bytes") val lmsStorageQuotaBytes: Long,
    @ColumnInfo(name = "created_at") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "last_used_at") val lastUsedAtEpochMillis: Long,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAtEpochMillis: Long,
)
