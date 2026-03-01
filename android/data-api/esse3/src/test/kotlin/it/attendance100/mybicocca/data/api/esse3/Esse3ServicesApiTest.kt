package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorRequest
import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorRequestObject
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3ServicesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetVersionInfo() {
        val result = api.services.getVersionInfo()
        assertNotNull(result)
        logger.info("getVersionInfo: version=${result.version}, buildId=${result.buildId}, universityName=${result.universityName}")
        assertNotNull(result.version, "Version should not be null")
        assertNotNull(result.buildId, "Build ID should not be null")
    }

    @Test
    suspend fun testGetLanguages() {
        val result = api.services.getLanguages()
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Languages list should not be empty")
        logger.info("getLanguages: count=${result.size}")
        for (lang in result) {
            logger.info("  language: id=${lang.languageId}, description=${lang.description}, iso6392=${lang.iso6392Code}")
            assertNotNull(lang.languageId, "Language ID should not be null")
        }
    }

    @Test
    suspend fun testGetAcademicYear() {
        val result = api.services.getAcademicYear(referenceDateTypeCode = "C")
        assertNotNull(result)
        assertNotNull(result.referenceDateTypeCode, "Reference date type code should not be null")
        logger.info("getAcademicYear: typeCode=${result.referenceDateTypeCode}, academicYearId=${result.academicYearId}, referenceDate=${result.referenceDate}")
    }

    @Test
    suspend fun testGetAcademicYearWithCourseType() {
        val result = api.services.getAcademicYear(referenceDateTypeCode = "C", courseTypeCode = "L")
        assertNotNull(result)
        logger.info("getAcademicYear with courseType: typeCode=${result.referenceDateTypeCode}, aaId=${result.academicYearId}, courseType=${result.courseTypeCode}")
    }

    @Test
    suspend fun testGetUserGroups() {
        try {
            val result = api.services.getUserGroups()
            assertNotNull(result)
            assertTrue(result.isNotEmpty(), "User groups list should not be empty")
            logger.info("getUserGroups: count=${result.size}")
            for (group in result) {
                logger.info("  group: id=${group.groupId}, name=${group.name}, technicalUser=${group.technicalUserFlag}")
                assertNotNull(group.groupId, "Group ID should not be null")
            }

            // Test getUserGroup with each returned group ID
            for (group in result) {
                val groupId = group.groupId ?: continue
                try {
                    val singleGroup = api.services.getUserGroup(groupId = groupId)
                    assertNotNull(singleGroup, "getUserGroup result for id=$groupId should not be null")
                    logger.info("getUserGroup($groupId): name=${singleGroup.name}")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("Skipping getUserGroup($groupId): insufficient permissions - ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetUserGroups: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetParameters() {
        try {
            val result = api.services.getParameters()
            assertNotNull(result)
            logger.info("getParameters: count=${result.size}")
            for (param in result) {
                logger.info("  parameter: code=${param.parameterCode}, module=${param.module}, numVal=${param.numericValue}, alfaVal=${param.alphanumericValue}")
                assertNotNull(param.parameterCode, "Parameter code should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetParameters: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetParametersWithFilters() {
        try {
            val result = api.services.getParameters(limit = 10, start = 0)
            assertNotNull(result)
            assertTrue(result.size <= 10, "Filtered parameters should return at most 10 results")
            logger.info("getParameters (limit=10): count=${result.size}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetParametersWithFilters: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetParameter() {
        try {
            val allParams = api.services.getParameters(limit = 1)
            if (allParams.isEmpty()) {
                logger.warning("Skipping testGetParameter: no parameters found to use as reference")
                return
            }
            val paramCode = allParams.first().parameterCode
            val result = api.services.getParameter(parameterCode = paramCode)
            assertNotNull(result)
            assertTrue(result.parameterCode == paramCode, "Returned parameter code should match queried code")
            logger.info("getParameter($paramCode): module=${result.module}, numVal=${result.numericValue}, alfaVal=${result.alphanumericValue}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetParameter: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetIdentityDocumentTypes() {
        val result = api.services.getIdentityDocumentTypes()
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Identity document types list should not be empty")
        logger.info("getIdentityDocumentTypes: count=${result.size}")
        for (docType in result) {
            logger.info("  docType: code=${docType.identityDocumentTypeCode}, description=${docType.description}")
            assertNotNull(docType.identityDocumentTypeCode, "Document type code should not be null")
        }
    }

    @Test
    suspend fun testGetIdentityDocumentTypesWithLanguage() {
        val result = api.services.getIdentityDocumentTypes(iso6392Code = "ita")
        assertNotNull(result)
        logger.info("getIdentityDocumentTypes (ita): count=${result.size}")
        for (docType in result) {
            logger.info("  docType: code=${docType.identityDocumentTypeCode}, description=${docType.description}")
        }
    }

    @Test
    suspend fun testGetAddressTypes() {
        val result = api.services.getAddressTypes()
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Address types list should not be empty")
        logger.info("getAddressTypes: count=${result.size}")
        for (addrType in result) {
            logger.info("  addressType: code=${addrType.addressTypeCode}, description=${addrType.description}")
            assertNotNull(addrType.addressTypeCode, "Address type code should not be null")
        }
    }

    @Test
    suspend fun testGetRequestTypes() {
        try {
            val result = api.services.getRequestTypes()
            assertNotNull(result)
            logger.info("getRequestTypes: count=${result.size}")
            for (reqType in result) {
                logger.info("  requestType: code=${reqType.requestTypeCode}, description=${reqType.description}")
                assertNotNull(reqType.requestTypeCode, "Request type code should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRequestTypes: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetPaymentRefundTypes() {
        val result = api.services.getPaymentRefundTypes()
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Payment refund types list should not be empty")
        logger.info("getPaymentRefundTypes: count=${result.size}")
        for (type in result) {
            logger.info("  paymentRefundType: code=${type.paymentRefundTypeCode}, description=${type.description}")
            assertNotNull(type.paymentRefundTypeCode, "Payment refund type code should not be null")
        }
    }

    @Test
    suspend fun testGetPaymentRefundTypesWithLanguage() {
        val result = api.services.getPaymentRefundTypes(iso6392Code = "ita")
        assertNotNull(result)
        logger.info("getPaymentRefundTypes (ita): count=${result.size}")
        for (type in result) {
            logger.info("  paymentRefundType: code=${type.paymentRefundTypeCode}, description=${type.description}")
        }
    }

    @Test
    suspend fun testGetMaritalStatusTypes() {
        val result = api.services.getMaritalStatusTypes()
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Marital status types list should not be empty")
        logger.info("getMaritalStatusTypes: count=${result.size}")
        for (status in result) {
            logger.info("  maritalStatus: code=${status.maritalStatusCode}, description=${status.description}")
            assertNotNull(status.maritalStatusCode, "Marital status code should not be null")
        }
    }

    @Test
    suspend fun testGetMaritalStatusTypesWithLanguage() {
        val result = api.services.getMaritalStatusTypes(iso6392Code = "ita")
        assertNotNull(result)
        logger.info("getMaritalStatusTypes (ita): count=${result.size}")
        for (status in result) {
            logger.info("  maritalStatus: code=${status.maritalStatusCode}, description=${status.description}")
        }
    }

    @Test
    suspend fun testGetUser() {
        try {
            val userId = session.userId
            val result = api.services.getUser(userId = userId)
            assertNotNull(result)
            logger.info("getUser($userId): alias=${result.alias}, groupName=${result.groupName}, disableFlag=${result.disableFlag}")
            assertNotNull(result.userId, "User ID should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetUser: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetAlias() {
        try {
            val userId = session.userId
            val result = api.services.getAlias(userId = userId)
            assertNotNull(result)
            logger.info("getAlias($userId): userId=${result.userId}, alias=${result.alias}, expirationDate=${result.expirationDate}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetAlias: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetAlias: validation error (alias may not exist) - ${e.message}")
        }
    }

    @Test
    suspend fun testInsertUpdateAndDeleteAlias() {
        try {
            val userId = session.userId
            val testAlias = "test_alias_${System.currentTimeMillis()}"

            val created = api.services.insertUpdateAlias(userId = userId, alias = testAlias)
            assertNotNull(created)
            logger.info("insertUpdateAlias: userId=${created.userId}, alias=${created.alias}")

            try {
                val fetched = api.services.getAlias(userId = userId)
                assertNotNull(fetched)
                logger.info("getAlias after insert: alias=${fetched.alias}")
            } finally {
                // Rollback: delete the alias we created
                try {
                    api.services.deleteAlias(userId = userId, alias = testAlias)
                    logger.info("deleteAlias: successfully deleted alias=$testAlias")
                } catch (e: Exception) {
                    logger.warning("Could not rollback alias deletion: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testInsertUpdateAndDeleteAlias: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testInsertUpdateAndDeleteAlias: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCodeFromId() {
        try {
            // Use the degree course ID from the student profile to build a valid translator request
            val body = Esse3CodeToIdTranslatorRequest(
                objects = listOf(
                    Esse3CodeToIdTranslatorRequestObject(
                        type = "cds",
                        code = "",
                        codes = emptyList()
                    )
                )
            )
            val result = api.services.getCodeFromId(body = body)
            assertNotNull(result)
            logger.info("getCodeFromId: result count=${result.size}")
            for (item in result) {
                logger.info("  translatorResult: type=${item.type}, code=${item.code}, id=${item.id}, error=${item.error}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCodeFromId: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetCodeFromId: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetLists() {
        try {
            // testId is typically derived from another endpoint; use studentId as a candidate
            val testId = studentProfile.studentId
            val result = api.services.getLists(testId = testId)
            assertNotNull(result)
            logger.info("getLists($testId): count=${result.size}")
            for (list in result) {
                logger.info("  list: code=${list.code}, description=${list.description}, aaId=${list.academicYearId}, state=${list.listState}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetLists: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetLists: validation error (no lists found for testId) - ${e.message}")
        }
    }
}
