package it.attendance100.mybicocca.data.mapper.account

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetSiteInfoResponse
import org.junit.Test

/**
 * Covers [toLearningIdentity]: the Moodle site-info fields are carried straight onto the domain
 * identity, and the upload/quota sizes are widened from the DTO's `Int` to `Long`.
 */
class LearningMapperTest {

    private fun siteInfo(
        userId: Int = 4242,
        username: String = "name.surname@campus.unimib.it",
        language: String = "it",
        userIsSiteAdmin: Boolean = false,
        userMaxUploadFileSize: Int = 52_428_800,
        userQuota: Int = 104_857_600,
    ) = ElearningGetSiteInfoResponse(
        advancedFeatures = emptyList(),
        downloadFilesEnabled = 1,
        firstName = "Name",
        fullName = "Name Surname",
        functions = emptyList(),
        language = language,
        lastName = "Surname",
        limitConcurrentLogins = 0,
        mobileCascadingStyleSheetUrl = "",
        policyAgreed = 1,
        release = "4.1",
        siteCalendarType = "gregorian",
        siteId = 1,
        siteName = "E-Learning",
        siteUrl = "https://elearning.unimib.it",
        theme = "boost",
        uploadFilesEnabled = 1,
        userCalendarType = "gregorian",
        userCanManageOwnFiles = true,
        userHomePage = 0,
        userId = userId,
        userIsSiteAdmin = userIsSiteAdmin,
        userMaxUploadFileSize = userMaxUploadFileSize,
        username = username,
        userPictureUrl = "",
        userPrivateAccessKey = "",
        userQuota = userQuota,
        version = "2022112800",
    )

    @Test
    fun `maps the moodle identity fields onto the domain learning identity`() {
        val identity = siteInfo().toLearningIdentity()

        assertThat(identity.lmsUserId).isEqualTo(4242)
        assertThat(identity.lmsUsername).isEqualTo("name.surname@campus.unimib.it")
        assertThat(identity.locale).isEqualTo("it")
        assertThat(identity.isSiteAdmin).isFalse()
    }

    @Test
    fun `widens the upload size and quota from int to long`() {
        val identity = siteInfo(userMaxUploadFileSize = 52_428_800, userQuota = 104_857_600)
            .toLearningIdentity()

        assertThat(identity.maxUploadFileSizeBytes).isEqualTo(52_428_800L)
        assertThat(identity.storageQuotaBytes).isEqualTo(104_857_600L)
    }

    @Test
    fun `carries a site-admin flag through`() {
        val identity = siteInfo(userIsSiteAdmin = true).toLearningIdentity()

        assertThat(identity.isSiteAdmin).isTrue()
    }
}
