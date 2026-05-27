package it.attendance100.mybicocca.data.mapper.account

import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSiteInfoResponse
import it.attendance100.mybicocca.domain.model.account.LearningIdentity

internal fun ElearningGetSiteInfoResponse.toLearningIdentity(): LearningIdentity = LearningIdentity(
    lmsUserId = userId,
    lmsUsername = username,
    locale = language,
    isSiteAdmin = userIsSiteAdmin,
    maxUploadFileSizeBytes = userMaxUploadFileSize.toLong(),
    storageQuotaBytes = userQuota.toLong(),
)
