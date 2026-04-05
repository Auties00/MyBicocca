package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3Award
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeClass
import it.attendance100.mybicocca.data.dto.esse3.Esse3BadgeIssuanceNotification
import it.attendance100.mybicocca.data.dto.esse3.Esse3CustomData
import it.attendance100.mybicocca.data.dto.esse3.Esse3Issuer
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3BadgeImportApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3BadgeImportApiTest::class.java.name)

    private lateinit var userPermissions: Set<Esse3PermissionLevel>

    @BeforeAll
    fun setUp() = runTest {
        val loginResult = api.auth.login()
        val groupPermission = Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        val profilePermissions = loginResult.profiles.mapNotNull { profile ->
            profile.description?.let { Esse3PermissionLevel.fromProfileName(it) }
        }.toSet()

        userPermissions = buildSet {
            add(Esse3PermissionLevel.ANY)
            add(Esse3PermissionLevel.AUTHENTICATED_USER)
            add(groupPermission)
            addAll(profilePermissions)
        }

        logger.info("User permissions derived from login: $userPermissions")
        logger.info("User groupId=${loginResult.user.groupId}, profiles=${loginResult.profiles.map { it.description }}")
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostImportAward() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val hasPermission = userPermissions.any { it in requiredPermissions }
        logger.info("testPostImportAward: requiredPermissions=$requiredPermissions, hasPermission=$hasPermission")

        if (hasPermission) {
            val body = listOf(
                Esse3Award(
                    awardedUser = null,
                    earnedBadge = 1L,
                    awardedEmail = "test@example.com",
                    createdAt = "2026-01-01T00:00:00Z",
                    customData = listOf(
                        Esse3CustomData(
                            externalKey = "testKey",
                            externalValue = "testValue"
                        )
                    )
                )
            )
            logger.info("testPostImportAward: calling postImportAward with body=$body")
            try {
                val result = api.badgeImport.postImportAward(body)
                assertNotNull(result)
                logger.info("testPostImportAward: testId=${result.testId}, creationDate=${result.creationDate}, stateCode=${result.stateCode}, note=${result.note}")
                logger.info("testPostImportAward: badgeClassId=${result.badgeClassId}, badgeIdentifier=${result.badgeIdentifier}")
                logger.info("testPostImportAward: awardInfo count=${result.awardInfo.size}")
                for (info in result.awardInfo) {
                    logger.info("  awardInfo: id=${info.id}, stateCode=${info.stateCode}, awardedUser=${info.awardedUser}, earnedBadge=${info.earnedBadge}, awardedEmail=${info.awardedEmail}, createdAt=${info.createdAt}, note=${info.note}, personId=${info.personId}")
                }
                logger.info("testPostImportAward: awardErrorDetails count=${result.awardErrorDetails.size}")
                for (error in result.awardErrorDetails) {
                    logger.info("  awardError: id=${error.id}, stateCode=${error.stateCode}, awardedUser=${error.awardedUser}, earnedBadge=${error.earnedBadge}, awardedEmail=${error.awardedEmail}, note=${error.note}")
                }
            } catch (e: Esse3Exception) {
                logger.warning("testPostImportAward: validation error - ${e.message}")
            }
        } else {
            logger.info("testPostImportAward: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            val body = listOf(
                Esse3Award(
                    earnedBadge = 1L,
                    awardedEmail = "test@example.com",
                    createdAt = "2026-01-01T00:00:00Z"
                )
            )
            try {
                api.badgeImport.postImportAward(body)
                fail("Expected Esse3Exception to be thrown")
            } catch (e: Esse3Exception) {
                logger.info("testPostImportAward: correctly threw Esse3Exception - ${e.message}")
                assertNotNull(e.expectedPermissionLevels)
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostImportBadgeClass() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val hasPermission = userPermissions.any { it in requiredPermissions }
        logger.info("testPostImportBadgeClass: requiredPermissions=$requiredPermissions, hasPermission=$hasPermission")

        if (hasPermission) {
            val body = listOf(
                Esse3BadgeClass(
                    id = 1L,
                    identifier = "TEST_BADGE_CLASS_001",
                    status = "active",
                    obiName = "Test Badge Class",
                    obiDescription = "A test badge class for integration testing",
                    createdAt = "2026-01-01T00:00:00Z",
                    updatedAt = null,
                    imageUrl = null,
                    alignment = null,
                    issuer = Esse3Issuer(
                        issuerId = "TEST_ISSUER",
                        miurCode = null,
                        url = "https://example.com",
                        email = "issuer@example.com",
                        name = "Test Issuer",
                        imageUrl = null,
                        slug = "test-issuer"
                    ),
                    customData = listOf(
                        Esse3CustomData(
                            externalKey = "testKey",
                            externalValue = "testValue"
                        )
                    )
                )
            )
            logger.info("testPostImportBadgeClass: calling postImportBadgeClass with body=$body")
            try {
                val result = api.badgeImport.postImportBadgeClass(body)
                assertNotNull(result)
                logger.info("testPostImportBadgeClass: testId=${result.testId}, creationDate=${result.creationDate}, stateCode=${result.stateCode}, note=${result.note}")
                logger.info("testPostImportBadgeClass: badgeClassesInfo count=${result.badgeClassesInfo.size}")
                for (info in result.badgeClassesInfo) {
                    logger.info("  badgeClassInfo: id=${info.id}, numericId=${info.numericId}, stateCode=${info.stateCode}, identifier=${info.identifier}, status=${info.status}, name=${info.name}, description=${info.description}, createdAt=${info.createdAt}, updatedAt=${info.updatedAt}")
                    logger.info("    issuerId=${info.issuerId}, miurIssuerCode=${info.miurIssuerCode}, issuerUrl=${info.issuerUrl}, issuerEmail=${info.issuerEmail}, issuerName=${info.issuerName}, issuerSlug=${info.issuerSlug}")
                    logger.info("    titleCategoryCode=${info.titleCategoryCode}, titleTypeDescription=${info.titleTypeDescription}, levelCode=${info.levelCode}, levelDescription=${info.levelDescription}")
                }
                logger.info("testPostImportBadgeClass: badgeClassesErrorDetails count=${result.badgeClassesErrorDetails.size}")
                for (error in result.badgeClassesErrorDetails) {
                    logger.info("  badgeClassError: id=${error.id}, numericId=${error.numericId}, stateCode=${error.stateCode}, identifier=${error.identifier}, status=${error.status}, name=${error.name}, note=${error.note}")
                }
            } catch (e: Esse3Exception) {
                logger.warning("testPostImportBadgeClass: validation error - ${e.message}")
            }
        } else {
            logger.info("testPostImportBadgeClass: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            val body = listOf(
                Esse3BadgeClass(
                    id = 1L,
                    status = "active",
                    obiName = "Test Badge Class",
                    obiDescription = "Test description",
                    createdAt = "2026-01-01T00:00:00Z",
                    issuer = Esse3Issuer(issuerId = "TEST_ISSUER")
                )
            )
            try {
                api.badgeImport.postImportBadgeClass(body)
                fail("Expected Esse3Exception to be thrown")
            } catch (e: Esse3Exception) {
                logger.info("testPostImportBadgeClass: correctly threw Esse3Exception - ${e.message}")
                assertNotNull(e.expectedPermissionLevels)
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutBadgeIssuing() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val hasPermission = userPermissions.any { it in requiredPermissions }
        logger.info("testPutBadgeIssuing: requiredPermissions=$requiredPermissions, hasPermission=$hasPermission")

        if (hasPermission) {
            val body = Esse3BadgeIssuanceNotification(
                event = "badge_issued",
                referenceId = 1L,
                badgeId = "TEST_BADGE_001",
                email = "test@example.com",
                issuanceDate = "2026-01-01T00:00:00Z",
                reason = "Integration test badge issuance"
            )
            logger.info("testPutBadgeIssuing: calling putBadgeIssuing with body=$body")
            try {
                val result = api.badgeImport.putBadgeIssuing(body)
                assertNotNull(result)
                logger.info("testPutBadgeIssuing: result=$result")
            } catch (e: Esse3Exception) {
                logger.warning("testPutBadgeIssuing: validation error - ${e.message}")
            }
        } else {
            logger.info("testPutBadgeIssuing: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            val body = Esse3BadgeIssuanceNotification(
                event = "badge_issued",
                referenceId = 1L,
                badgeId = null,
                email = "test@example.com",
                issuanceDate = "2026-01-01T00:00:00Z",
                reason = null
            )
            try {
                api.badgeImport.putBadgeIssuing(body)
                fail("Expected Esse3Exception to be thrown")
            } catch (e: Esse3Exception) {
                logger.info("testPutBadgeIssuing: correctly threw Esse3Exception - ${e.message}")
                assertNotNull(e.expectedPermissionLevels)
            }
        }
    }
}
