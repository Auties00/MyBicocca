package it.attendance100.mybicocca.data.mapper.account

import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleSiteInfo
import it.attendance100.mybicocca.domain.model.account.LearningIdentity

internal fun MoodleSiteInfo.toLearningIdentity(): LearningIdentity = LearningIdentity(
    lmsUserId = userId,
    lmsUsername = username,
    locale = lang,
    isSiteAdmin = userIsSiteAdmin == true,
    maxUploadFileSizeBytes = userMaxUploadFileSize ?: 0L,
    storageQuotaBytes = (userQuota ?: 0).toLong(),
)
