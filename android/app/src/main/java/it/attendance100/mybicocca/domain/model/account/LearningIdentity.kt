package it.attendance100.mybicocca.domain.model.account

data class LearningIdentity(
    val lmsUserId: Int,
    val lmsUsername: String,
    val locale: String,
    val isSiteAdmin: Boolean,
    val maxUploadFileSizeBytes: Long,
    val storageQuotaBytes: Long,
)
