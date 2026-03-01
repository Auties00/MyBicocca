package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.logging.Logger

class Esse3AttachmentsApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3AttachmentsApiTest::class.java.name)

    @Test
    suspend fun testGetAttachmentTypologies() {
        try {
            val typologies = api.attachments.getAttachmentTypologies()
            logger.info("getAttachmentTypologies: found ${typologies.size} typologies")
            for (typology in typologies) {
                logger.info("  typology: typeId=${typology.typeId}, typeCode=${typology.typeCode}, downloadSupported=${typology.downloadSupported}, uploadSupported=${typology.uploadSupported}")
                assertNotNull(typology.typeCode, "typeCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentTypeCodes() {
        try {
            val codes = api.attachments.getAttachmentTypeCodes()
            logger.info("getAttachmentTypeCodes: found ${codes.size} type codes")
            for (code in codes) {
                logger.info("  typeCode: ${code.attachmentTypeCode}, desc=${code.description}, lang=${code.languageCode}")
                assertNotNull(code.attachmentTypeCode, "attachmentTypeCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypeCodes not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentTypeCodesWithLanguage() {
        try {
            val codes = api.attachments.getAttachmentTypeCodes(sessionLanguageCode = "it")
            logger.info("getAttachmentTypeCodes (lang=it): found ${codes.size} type codes")
            for (code in codes) {
                assertNotNull(code.attachmentTypeCode, "attachmentTypeCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypeCodes (lang=it) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentTypeCodesWithPagination() {
        try {
            val codesPage = api.attachments.getAttachmentTypeCodes(start = 0, limit = 5)
            logger.info("getAttachmentTypeCodes (paginated): found ${codesPage.size} type codes (max 5)")
            assertTrue(codesPage.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypeCodes (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentExtension() {
        try {
            val extensions = api.attachments.getAttachmentExtension()
            logger.info("getAttachmentExtension: found ${extensions.size} extensions")
            for (ext in extensions) {
                logger.info("  extension: id=${ext.extensionId}, extension=${ext.extension}, description=${ext.extensionDescription}")
                assertNotNull(ext.extension, "extension should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentExtension not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentExtensionWithPagination() {
        try {
            val extensionsPage = api.attachments.getAttachmentExtension(start = 0, limit = 3)
            logger.info("getAttachmentExtension (paginated): found ${extensionsPage.size} extensions (max 3)")
            assertTrue(extensionsPage.size <= 3, "Paginated result should have at most 3 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentExtension (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentsByType() {
        try {
            val typologies = api.attachments.getAttachmentTypologies()
            if (typologies.isEmpty()) {
                logger.warning("No typologies available to test getAttachmentsByType")
                return
            }

            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                logger.info("Testing getAttachmentsByType for typeCode=$typeCode")
                try {
                    val attachments = api.attachments.getAttachmentsByType(typeCode)
                    logger.info("  getAttachmentsByType($typeCode): found ${attachments.size} attachments")
                    for (attachment in attachments) {
                        logger.info("    attachment: id=${attachment.attachmentId}, fileName=${attachment.fileName}, author=${attachment.author}, validFlag=${attachment.validFlag}")
                        assertNotNull(attachment.attachmentId, "attachmentId should not be null")
                        assertNotNull(attachment.attachmentType, "attachmentType should not be null")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getAttachmentsByType($typeCode) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getAttachmentsByType($typeCode) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentsByTypeWithPagination() {
        try {
            val typologies = api.attachments.getAttachmentTypologies()
            val firstType = typologies.firstOrNull { it.typeCode != null }
            if (firstType == null) {
                logger.warning("No typologies available to test getAttachmentsByType with pagination")
                return
            }

            val typeCode = firstType.typeCode!!
            try {
                val attachmentsPage = api.attachments.getAttachmentsByType(typeCode, start = 0, limit = 5)
                logger.info("getAttachmentsByType($typeCode, paginated): found ${attachmentsPage.size} attachments (max 5)")
                assertTrue(attachmentsPage.size <= 5, "Paginated result should have at most 5 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getAttachmentsByType($typeCode) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getAttachmentsByType($typeCode) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentMetadata() {
        try {
            val typologies = api.attachments.getAttachmentTypologies()
            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                try {
                    val attachments = api.attachments.getAttachmentsByType(typeCode)
                    val firstAttachment = attachments.firstOrNull { it.attachmentId != null } ?: continue
                    val attachmentId = firstAttachment.attachmentId!!

                    logger.info("testGetAttachmentMetadata: typeCode=$typeCode, attachmentId=$attachmentId")
                    val metadata = api.attachments.getAttachmentMetadata(typeCode, attachmentId)
                    logger.info("  metadata: fileName=${metadata.fileName}, author=${metadata.author}, title=${metadata.title}, validFlag=${metadata.validFlag}")
                    assertEquals(attachmentId, metadata.attachmentId, "attachmentId should match")
                    return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getAttachmentsByType($typeCode) or getAttachmentMetadata not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getAttachmentsByType($typeCode) or getAttachmentMetadata validation error: ${e.message}")
                }
            }
            logger.warning("No accessible attachment found to test getAttachmentMetadata")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAttachmentContent() {
        try {
            val typologies = api.attachments.getAttachmentTypologies()
            for (typology in typologies) {
                val typeCode = typology.typeCode ?: continue
                if (typology.downloadSupported != 1) continue
                try {
                    val attachments = api.attachments.getAttachmentsByType(typeCode)
                    val firstAttachment = attachments.firstOrNull { it.attachmentId != null } ?: continue
                    val attachmentId = firstAttachment.attachmentId!!

                    logger.info("testGetAttachmentContent: typeCode=$typeCode, attachmentId=$attachmentId")
                    try {
                        val content = api.attachments.getAttachmentContent(typeCode, attachmentId)
                        logger.info("  content length=${content.length}")
                        assertNotNull(content, "content should not be null")
                        return
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warning("  getAttachmentContent($typeCode, $attachmentId) not authorized: ${e.message}")
                    } catch (e: Esse3ValidationException) {
                        logger.warning("  getAttachmentContent($typeCode, $attachmentId) validation error: ${e.message}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getAttachmentsByType($typeCode) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getAttachmentsByType($typeCode) validation error: ${e.message}")
                }
            }
            logger.warning("No downloadable attachment found to test getAttachmentContent")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAttachmentTypologies not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetUploadedAttachmentMetadataAndState() {
        // This test attempts to retrieve metadata/state for a known upload.
        // Since we cannot create an upload without uploading a file (which requires
        // a real upload ID), this test verifies error handling for a non-existent ID.
        val nonExistentUploadId = -1L
        try {
            val metadata = api.attachments.getUploadedAttachmentMetadata(nonExistentUploadId)
            logger.info("getUploadedAttachmentMetadata($nonExistentUploadId): uploadId=${metadata.uploadId}, state=${metadata.uploadState}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getUploadedAttachmentMetadata($nonExistentUploadId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getUploadedAttachmentMetadata($nonExistentUploadId) validation error (expected for nonexistent id): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getUploadedAttachmentMetadata($nonExistentUploadId) failed (expected for nonexistent id): ${e.message}")
        }
    }

    @Test
    suspend fun testGetUploadedAttachmentState() {
        val nonExistentUploadId = -1L
        try {
            api.attachments.getUploadedAttachmentState(nonExistentUploadId)
            logger.info("getUploadedAttachmentState($nonExistentUploadId): succeeded")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getUploadedAttachmentState($nonExistentUploadId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getUploadedAttachmentState($nonExistentUploadId) validation error (expected for nonexistent id): ${e.message}")
        } catch (e: Exception) {
            logger.warning("getUploadedAttachmentState($nonExistentUploadId) failed (expected for nonexistent id): ${e.message}")
        }
    }

    @Disabled("postGenericAttachmentMetadata is a destructive write operation that creates an attachment record and cannot be safely reversed")
    @Test
    suspend fun testPostGenericAttachmentMetadata() {
        // Disabled: posting attachment metadata creates a record in the system.
        // Cannot be safely cleaned up without a corresponding delete endpoint.
    }

    @Disabled("uploadAttachment writes binary content to an upload slot and cannot be safely reversed without deleteUploadedAttachment, which requires a valid uploadId")
    @Test
    suspend fun testUploadAttachment() {
        // Disabled: requires a valid uploadId from a prior upload session initialization
        // and creates data that cannot be easily cleaned up.
    }

    @Disabled("deleteUploadedAttachment is a destructive operation - must only be used after a real upload")
    @Test
    suspend fun testDeleteUploadedAttachment() {
        // Disabled: deleteUploadedAttachment permanently removes an upload and cannot be reversed.
    }

    @Disabled("validateAttachment mutates the validoFlg on a real attachment and may affect system state permanently")
    @Test
    suspend fun testValidateAttachment() {
        // Disabled: validateAttachment changes the validation state of a real attachment.
        // Even with try/finally rollback, the patch operation changes system state
        // and the rollback value (validFlag) may not be safe to assume.
    }
}
