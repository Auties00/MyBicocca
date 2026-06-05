package it.attendance100.mybicocca.data.local.internship

import androidx.room.ColumnInfo
import androidx.room.Entity

// A locally-bookmarked internship opportunity. Esse3 has no student backend for "salvati",
// so this is the sole source of truth. Scoped per account.
@Entity(tableName = "saved_opportunity", primaryKeys = ["account_id", "opportunity_id"])
data class SavedOpportunityEntity(
    @ColumnInfo("account_id") val accountId: String,
    @ColumnInfo("opportunity_id") val opportunityId: String,
    val title: String,
    val company: String?,
    val url: String?,
    @ColumnInfo("saved_at") val savedAt: Long,
)
