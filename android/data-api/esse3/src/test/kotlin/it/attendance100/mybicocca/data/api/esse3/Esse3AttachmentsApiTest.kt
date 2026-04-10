package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3AttachmentsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3AttachmentsApiTest::class.java.name)
        private lateinit var userPermissions: Set<Esse3PermissionLevel>
    }

    @BeforeAll
    fun setUp() = runTest {
        logger.info("Logging in to derive user permissions")
        val loginResult = api.auth.login()
        logger.info("Login successful: userId=${loginResult.user.userId}, groupId=${loginResult.user.groupId}")

        val permissions = mutableSetOf(
            Esse3PermissionLevel.ANY,
            Esse3PermissionLevel.AUTHENTICATED_USER,
            Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        )
        for (profile in loginResult.profiles) {
            val description = profile.description
            if (description != null) {
                permissions.add(Esse3PermissionLevel.fromProfileName(description))
            }
        }
        userPermissions = permissions
        logger.info("Derived user permissions: $userPermissions")
    }

    @Test
    fun testGetAttachmentTypologies() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentTypologies: calling getAttachmentTypologies()")
            val result = api.attachments.getAttachmentTypologies()
            logger.info("testGetAttachmentTypologies: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getAttachmentTypologies() should return a non-empty list")
            for (item in result) {
                logger.info("testGetAttachmentTypologies: typeId=${item.typeId}, typeCode=${item.typeCode}, downloadSupported=${item.downloadSupported}, uploadSupported=${item.uploadSupported}")
                assertNotNull(item.typeId, "typeId should not be null")
                assertNotNull(item.typeCode, "typeCode should not be null")
            }
        } else {
            logger.info("testGetAttachmentTypologies: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentTypologies()
            }
            logger.info("testGetAttachmentTypologies: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentTypeCodes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentTypeCodes: calling getAttachmentTypeCodes() with defaults")
            val defaultResult = api.attachments.getAttachmentTypeCodes()
            logger.info("testGetAttachmentTypeCodes: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getAttachmentTypeCodes() default should return a non-empty list")
            for (item in defaultResult) {
                logger.info("testGetAttachmentTypeCodes: languageId=${item.languageId}, languageCode=${item.languageCode}, attachmentTypeCode=${item.attachmentTypeCode}, description=${item.description}, maxUploadFileSize=${item.maxUploadFileSize}")
                assertNotNull(item.attachmentTypeCode, "attachmentTypeCode should not be null")
            }

            logger.info("testGetAttachmentTypeCodes: calling getAttachmentTypeCodes(sessionLanguageCode=\"it\")")
            val resultWithLanguage = api.attachments.getAttachmentTypeCodes(sessionLanguageCode = "it")
            logger.info("testGetAttachmentTypeCodes: result with sessionLanguageCode=it size=${resultWithLanguage.size}")
            assertNotNull(resultWithLanguage, "Result with sessionLanguageCode should not be null")

            logger.info("testGetAttachmentTypeCodes: calling getAttachmentTypeCodes(start=0, limit=5)")
            val paginatedResult = api.attachments.getAttachmentTypeCodes(start = 0, limit = 5)
            logger.info("testGetAttachmentTypeCodes: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            for (item in paginatedResult) {
                logger.info("testGetAttachmentTypeCodes: paginated item attachmentTypeCode=${item.attachmentTypeCode}, description=${item.description}")
                assertNotNull(item.attachmentTypeCode, "attachmentTypeCode should not be null in paginated result")
            }

            logger.info("testGetAttachmentTypeCodes: calling getAttachmentTypeCodes(sessionLanguageCode=\"it\", start=0, limit=5)")
            val fullParamResult = api.attachments.getAttachmentTypeCodes(sessionLanguageCode = "it", start = 0, limit = 5)
            logger.info("testGetAttachmentTypeCodes: full param result size=${fullParamResult.size}")
            assertTrue(fullParamResult.size <= 5, "Full param result size should be <= 5")
        } else {
            logger.info("testGetAttachmentTypeCodes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentTypeCodes()
            }
            logger.info("testGetAttachmentTypeCodes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentExtension() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentExtension: calling getAttachmentExtension() with defaults")
            val defaultResult = api.attachments.getAttachmentExtension()
            logger.info("testGetAttachmentExtension: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getAttachmentExtension() default should return a non-empty list")
            for (item in defaultResult) {
                logger.info("testGetAttachmentExtension: extensionId=${item.extensionId}, extension=${item.extension}, extensionDescription=${item.extensionDescription}")
                assertNotNull(item.extensionId, "extensionId should not be null")
                assertNotNull(item.extension, "extension should not be null")
            }

            logger.info("testGetAttachmentExtension: calling getAttachmentExtension(start=0, limit=5)")
            val paginatedResult = api.attachments.getAttachmentExtension(start = 0, limit = 5)
            logger.info("testGetAttachmentExtension: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            for (item in paginatedResult) {
                logger.info("testGetAttachmentExtension: paginated item extensionId=${item.extensionId}, extension=${item.extension}")
                assertNotNull(item.extensionId, "extensionId should not be null in paginated result")
            }

            logger.info("testGetAttachmentExtension: calling getAttachmentExtension(start=0)")
            val resultWithStart = api.attachments.getAttachmentExtension(start = 0)
            logger.info("testGetAttachmentExtension: result with start=0 size=${resultWithStart.size}")

            logger.info("testGetAttachmentExtension: calling getAttachmentExtension(limit=5)")
            val resultWithLimit = api.attachments.getAttachmentExtension(limit = 5)
            logger.info("testGetAttachmentExtension: result with limit=5 size=${resultWithLimit.size}")
            assertTrue(resultWithLimit.size <= 5, "Result with only limit should be <= 5")
        } else {
            logger.info("testGetAttachmentExtension: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentExtension()
            }
            logger.info("testGetAttachmentExtension: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentsByType() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentsByType: fetching attachment typologies to obtain attachmentType values")
            val typologies = api.attachments.getAttachmentTypologies()
            logger.info("testGetAttachmentsByType: typologies size=${typologies.size}")
            assertTrue(typologies.isNotEmpty(), "Need at least one attachment typology to test getAttachmentsByType()")

            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                logger.info("testGetAttachmentsByType: calling getAttachmentsByType(attachmentType=$typeCode) with defaults")
                val defaultResult = api.attachments.getAttachmentsByType(attachmentType = typeCode)
                logger.info("testGetAttachmentsByType: attachmentType=$typeCode default result size=${defaultResult.size}")
                assertNotNull(defaultResult, "getAttachmentsByType() result should not be null")
                for (item in defaultResult) {
                    logger.info("testGetAttachmentsByType: attachmentId=${item.attachmentId}, fileName=${item.fileName}, attachmentType=${item.attachmentType}, title=${item.title}, author=${item.author}")
                }

                logger.info("testGetAttachmentsByType: calling getAttachmentsByType(attachmentType=$typeCode, start=0, limit=5)")
                val paginatedResult = api.attachments.getAttachmentsByType(attachmentType = typeCode, start = 0, limit = 5)
                logger.info("testGetAttachmentsByType: attachmentType=$typeCode paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5 for attachmentType=$typeCode")
            }
        } else {
            logger.info("testGetAttachmentsByType: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentsByType(attachmentType = "GENERICO")
            }
            logger.info("testGetAttachmentsByType: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentMetadata: fetching attachment typologies to find type codes with existing attachments")
            val typologies = api.attachments.getAttachmentTypologies()
            var tested = false

            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                val attachments = api.attachments.getAttachmentsByType(attachmentType = typeCode, start = 0, limit = 3)
                logger.info("testGetAttachmentMetadata: attachmentType=$typeCode has ${attachments.size} attachments")
                for (attachment in attachments) {
                    val attachmentId = attachment.attachmentId ?: continue
                    logger.info("testGetAttachmentMetadata: calling getAttachmentMetadata(attachmentType=$typeCode, attachmentId=$attachmentId)")
                    val metadata = api.attachments.getAttachmentMetadata(attachmentType = typeCode, attachmentId = attachmentId)
                    logger.info("testGetAttachmentMetadata: result fileName=${metadata.fileName}, title=${metadata.title}, author=${metadata.author}, attachmentType=${metadata.attachmentType}, attachmentId=${metadata.attachmentId}, validFlag=${metadata.validFlag}")
                    assertNotNull(metadata.attachmentId, "attachmentId should not be null")
                    tested = true
                }
                if (tested) break
            }

            if (!tested) {
                logger.info("testGetAttachmentMetadata: no attachments found across any type, skipping detail assertions")
            }
        } else {
            logger.info("testGetAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentMetadata(attachmentType = "GENERICO", attachmentId = 1L)
            }
            logger.info("testGetAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentContent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAttachmentContent: fetching attachment typologies to find downloadable attachments")
            val typologies = api.attachments.getAttachmentTypologies()
            var tested = false

            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                val attachments = api.attachments.getAttachmentsByType(attachmentType = typeCode, start = 0, limit = 3)
                logger.info("testGetAttachmentContent: attachmentType=$typeCode has ${attachments.size} attachments")
                for (attachment in attachments) {
                    val attachmentId = attachment.attachmentId ?: continue
                    logger.info("testGetAttachmentContent: calling getAttachmentContent(attachmentType=$typeCode, attachmentId=$attachmentId)")
                    val channel = api.attachments.getAttachmentContent(attachmentType = typeCode, attachmentId = attachmentId)
                    assertNotNull(channel, "ByteReadChannel should not be null for attachmentType=$typeCode, attachmentId=$attachmentId")
                    logger.info("testGetAttachmentContent: successfully obtained ByteReadChannel for attachmentType=$typeCode, attachmentId=$attachmentId")
                    tested = true
                }
                if (tested) break
            }

            if (!tested) {
                logger.info("testGetAttachmentContent: no attachments found to download, skipping content assertions")
            }
        } else {
            logger.info("testGetAttachmentContent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getAttachmentContent(attachmentType = "GENERICO", attachmentId = 1L)
            }
            logger.info("testGetAttachmentContent: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUploadedAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUploadedAttachmentMetadata: attempting to retrieve upload metadata with a known uploadId")
            try {
                val result = api.attachments.getUploadedAttachmentMetadata(uploadId = 1L)
                logger.info("testGetUploadedAttachmentMetadata: uploadId=1 result uploadState=${result.uploadState}, fileName=${result.fileName}, attachmentType=${result.attachmentType}, attachmentId=${result.attachmentId}, uploadId=${result.uploadId}")
                assertNotNull(result, "getUploadedAttachmentMetadata() should not return null")
            } catch (e: Esse3Exception) {
                logger.info("testGetUploadedAttachmentMetadata: uploadId=1 not found or validation error: ${e.message}")
            }

            logger.info("testGetUploadedAttachmentMetadata: searching for valid uploadIds from existing attachments")
            val typologies = api.attachments.getAttachmentTypologies()
            var tested = false
            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                val attachments = api.attachments.getAttachmentsByType(attachmentType = typeCode, start = 0, limit = 3)
                for (attachment in attachments) {
                    val attachmentId = attachment.attachmentId ?: continue
                    try {
                        val result = api.attachments.getUploadedAttachmentMetadata(uploadId = attachmentId)
                        logger.info("testGetUploadedAttachmentMetadata: uploadId=$attachmentId result uploadState=${result.uploadState}, fileName=${result.fileName}, title=${result.title}, author=${result.author}")
                        assertNotNull(result, "getUploadedAttachmentMetadata() should not return null")
                        tested = true
                        break
                    } catch (e: Esse3Exception) {
                        logger.info("testGetUploadedAttachmentMetadata: uploadId=$attachmentId not valid as upload: ${e.message}")
                    }
                }
                if (tested) break
            }

            if (!tested) {
                logger.info("testGetUploadedAttachmentMetadata: no valid uploadId found, only tested with uploadId=1")
            }
        } else {
            logger.info("testGetUploadedAttachmentMetadata: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getUploadedAttachmentMetadata(uploadId = 1L)
            }
            logger.info("testGetUploadedAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUploadedAttachmentState() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUploadedAttachmentState: attempting to retrieve upload state with uploadId=1")
            try {
                api.attachments.getUploadedAttachmentState(uploadId = 1L)
                logger.info("testGetUploadedAttachmentState: getUploadedAttachmentState(uploadId=1) succeeded")
            } catch (e: Esse3Exception) {
                logger.info("testGetUploadedAttachmentState: uploadId=1 not found or validation error: ${e.message}")
            }

            logger.info("testGetUploadedAttachmentState: searching for valid uploadIds from existing attachments")
            val typologies = api.attachments.getAttachmentTypologies()
            var tested = false
            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                val attachments = api.attachments.getAttachmentsByType(attachmentType = typeCode, start = 0, limit = 3)
                for (attachment in attachments) {
                    val attachmentId = attachment.attachmentId ?: continue
                    try {
                        api.attachments.getUploadedAttachmentState(uploadId = attachmentId)
                        logger.info("testGetUploadedAttachmentState: getUploadedAttachmentState(uploadId=$attachmentId) succeeded")
                        tested = true
                        break
                    } catch (e: Esse3Exception) {
                        logger.info("testGetUploadedAttachmentState: uploadId=$attachmentId not valid: ${e.message}")
                    }
                }
                if (tested) break
            }

            if (!tested) {
                logger.info("testGetUploadedAttachmentState: no valid uploadId found, only tested with uploadId=1")
            }
        } else {
            logger.info("testGetUploadedAttachmentState: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.getUploadedAttachmentState(uploadId = 1L)
            }
            logger.info("testGetUploadedAttachmentState: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostGenericAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostGenericAttachmentMetadata: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostGenericAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.postGenericAttachmentMetadata(
                    attachmentType = "GENERICO",
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3GenericAttachmentInsertMetadata(
                        fileName = "test.txt",
                        author = "test",
                        title = "Test attachment",
                        description = "Test description"
                    )
                )
            }
            logger.info("testPostGenericAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testValidateAttachment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testValidateAttachment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testValidateAttachment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.validateAttachment(
                    attachmentType = "GENERICO",
                    attachmentId = 1L,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PatchAttachmentMetadata(validFlag = 1L)
                )
            }
            logger.info("testValidateAttachment: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteUploadedAttachment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteUploadedAttachment: user has required permissions, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteUploadedAttachment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.deleteUploadedAttachment(uploadId = 1L)
            }
            logger.info("testDeleteUploadedAttachment: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testUploadAttachment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testUploadAttachment: user has required permissions, but test is disabled to prevent data mutation")
        } else {
            logger.info("testUploadAttachment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.attachments.uploadAttachment(
                    uploadId = 1L,
                    body = kotlinx.serialization.json.JsonObject(emptyMap())
                )
            }
            logger.info("testUploadAttachment: Esse3Exception thrown as expected")
        }
    }
}
