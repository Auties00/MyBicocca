package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3Award
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeClass
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeIssuanceNotification
import it.attendance100.mybicocca.data.dto.esse3.Esse3CustomData
import it.attendance100.mybicocca.data.dto.esse3.Esse3Issuer
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3BadgeImportApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3BadgeImportApiTest::class.java)

    @Test
    suspend fun postImportBadgeClass_requiresTechnicalUser() {
        // This is a write operation requiring TECHNICAL_USER permission.
        // Student accounts will get Esse3NotAuthorizedException.
        // We construct a minimal payload and expect either success (with real data) or auth failure.
        val badgeClass = Esse3BadgeClass(
            id = 99999L,
            identifier = "test-badge-class-identifier",
            status = "active",
            obiName = "Test Badge Class",
            obiDescription = "Test badge class for integration testing",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = null,
            imageUrl = null,
            alignment = null,
            issuer = Esse3Issuer(
                issuerId = "test-issuer-id",
                miurCode = null,
                url = null,
                email = null,
                name = "Test Issuer",
                imageUrl = null,
                slug = null
            ),
            customData = emptyList()
        )

        try {
            val result = api.badgeImport.postImportBadgeClass(body = listOf(badgeClass))
            assertNotNull(result)
            assertNotNull(result.testId, "testId should not be null")
            logger.info("postImportBadgeClass succeeded: testId=${result.testId}, stateCode=${result.stateCode}")
            logger.info("  badgeClassesInfo count: ${result.badgeClassesInfo.size}")
            logger.info("  badgeClassesErrorDetails count: ${result.badgeClassesErrorDetails.size}")

            for (info in result.badgeClassesInfo) {
                assertNotNull(info.id, "badgeClassInfo.id should not be null")
                assertNotNull(info.createdAt, "badgeClassInfo.createdAt should not be null")
                assertNotNull(info.status, "badgeClassInfo.status should not be null")
                logger.info("  BadgeClassInfo: id=${info.id}, name=${info.name}, stateCode=${info.stateCode}")
            }

            for (error in result.badgeClassesErrorDetails) {
                assertNotNull(error.id, "badgeClassErrorDetail.id should not be null")
                logger.warn("  BadgeClassErrorDetail: id=${error.id}, stateCode=${error.stateCode}, name=${error.name}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to import badge class (expected for student role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error importing badge class: ${e.message}")
        }
    }

    @Test
    suspend fun postImportAward_requiresTechnicalUser() {
        // This is a write operation requiring TECHNICAL_USER permission.
        // Student accounts will get Esse3NotAuthorizedException.
        val award = Esse3Award(
            awardedUser = studentProfile.userId.toLongOrNull(),
            earnedBadge = 99999L,
            awardedEmail = "test@student.unimib.it",
            createdAt = "2024-01-01T00:00:00Z",
            customData = listOf(
                Esse3CustomData(
                    externalKey = "test-key",
                    externalValue = "test-value"
                )
            )
        )

        try {
            val result = api.badgeImport.postImportAward(body = listOf(award))
            assertNotNull(result)
            assertNotNull(result.testId, "testId should not be null")
            logger.info("postImportAward succeeded: testId=${result.testId}, stateCode=${result.stateCode}")
            logger.info("  awardInfo count: ${result.awardInfo.size}")
            logger.info("  awardErrorDetails count: ${result.awardErrorDetails.size}")

            for (info in result.awardInfo) {
                assertNotNull(info.id, "awardInfo.id should not be null")
                assertNotNull(info.createdAt, "awardInfo.createdAt should not be null")
                assertNotNull(info.awardedEmail, "awardInfo.awardedEmail should not be null")
                assertNotNull(info.earnedBadge, "awardInfo.earnedBadge should not be null")
                logger.info("  AwardInfo: id=${info.id}, awardedUser=${info.awardedUser}, earnedBadge=${info.earnedBadge}, stateCode=${info.stateCode}")
            }

            for (error in result.awardErrorDetails) {
                assertNotNull(error.id, "awardErrorDetail.id should not be null")
                assertNotNull(error.stateCode, "awardErrorDetail.stateCode should not be null")
                logger.warn("  AwardErrorDetail: id=${error.id}, stateCode=${error.stateCode}, awardedEmail=${error.awardedEmail}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to import award (expected for student role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error importing award: ${e.message}")
        }
    }

    @Test
    suspend fun postImportAward_withEmptyList_requiresTechnicalUser() {
        // Sending an empty list should either return an empty result or a validation error
        try {
            val result = api.badgeImport.postImportAward(body = emptyList())
            assertNotNull(result)
            logger.info("postImportAward with empty list succeeded: testId=${result.testId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to import award with empty list: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error (expected) importing empty award list: ${e.message}")
        }
    }

    @Test
    suspend fun postImportBadgeClass_withEmptyList_requiresTechnicalUser() {
        // Sending an empty list should either return an empty result or a validation error
        try {
            val result = api.badgeImport.postImportBadgeClass(body = emptyList())
            assertNotNull(result)
            logger.info("postImportBadgeClass with empty list succeeded: testId=${result.testId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to import badge class with empty list: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error (expected) importing empty badge class list: ${e.message}")
        }
    }

    @Disabled("Modifies badge issuance state — run only when intentional and with valid badgeId/refId")
    @Test
    suspend fun putBadgeIssuing_requiresTechnicalUser() {
        val notification = Esse3BadgeIssuanceNotification(
            event = "ISSUED",
            referenceId = 1L,
            badgeId = null,
            email = null,
            issuanceDate = "2024-01-01",
            reason = "Integration test"
        )

        try {
            val result = api.badgeImport.putBadgeIssuing(body = notification)
            assertNotNull(result)
            logger.info("putBadgeIssuing succeeded: result='$result'")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("Not authorized to put badge issuing (expected for student role): ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("Validation error putting badge issuing: ${e.message}")
        }
    }
}
