package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorRequest
import it.attendance100.mybicocca.data.dto.esse3.Esse3CodeToIdTranslatorRequestObject
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

class Esse3ServicesApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3ServicesApiTest::class.java.name)
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
    fun testGetAlias() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAlias: calling getAlias() with userId=${session.userId}")
            val result = api.services.getAlias(userId = session.userId)
            logger.info("testGetAlias: result userId=${result.userId}, alias=${result.alias}, expirationDate=${result.expirationDate}")
            assertNotNull(result.userId, "userId should not be null")
        } else {
            logger.info("testGetAlias: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getAlias(userId = session.userId)
            }
            logger.info("testGetAlias: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testInsertUpdateAlias() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testInsertUpdateAlias: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testInsertUpdateAlias: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.insertUpdateAlias(
                    userId = session.userId,
                    alias = "test-alias"
                )
            }
            logger.info("testInsertUpdateAlias: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteAlias() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteAlias: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteAlias: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.deleteAlias(
                    userId = session.userId,
                    alias = "test-alias"
                )
            }
            logger.info("testDeleteAlias: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAcademicYear() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetAcademicYear: calling getAcademicYear() with referenceDateTypeCode='AA_CORR' (defaults)")
        val result = api.services.getAcademicYear(referenceDateTypeCode = "AA_CORR")
        logger.info("testGetAcademicYear: result referenceDateTypeCode=${result.referenceDateTypeCode}, courseTypeCode=${result.courseTypeCode}, academicYearId=${result.academicYearId}, referenceDate=${result.referenceDate}")
        assertNotNull(result.referenceDateTypeCode, "referenceDateTypeCode should not be null")
        assertNotNull(result.academicYearId, "academicYearId should not be null")

        logger.info("testGetAcademicYear: calling getAcademicYear() with courseTypeCode param")
        val resultWithCourseType = api.services.getAcademicYear(
            referenceDateTypeCode = "AA_CORR",
            courseTypeCode = "L"
        )
        logger.info("testGetAcademicYear: result with courseTypeCode referenceDateTypeCode=${resultWithCourseType.referenceDateTypeCode}, academicYearId=${resultWithCourseType.academicYearId}")
        assertNotNull(resultWithCourseType.academicYearId, "academicYearId should not be null with courseTypeCode filter")

        logger.info("testGetAcademicYear: calling getAcademicYear() with referenceDate param")
        val resultWithRefDate = api.services.getAcademicYear(
            referenceDateTypeCode = "AA_CORR",
            referenceDate = "2024-01-15"
        )
        logger.info("testGetAcademicYear: result with referenceDate referenceDateTypeCode=${resultWithRefDate.referenceDateTypeCode}, academicYearId=${resultWithRefDate.academicYearId}")
        assertNotNull(resultWithRefDate.academicYearId, "academicYearId should not be null with referenceDate filter")
    }

    @Test
    fun testGetLanguages() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetLanguages: calling getLanguages() with defaults")
        val result = api.services.getLanguages()
        logger.info("testGetLanguages: result size=${result.size}")
        assertTrue(result.isNotEmpty(), "getLanguages() should return a non-empty list")
        for (language in result.take(5)) {
            logger.info("testGetLanguages: languageId=${language.languageId}, description=${language.description}, iso6392Code=${language.iso6392Code}, iso6391Code=${language.iso6391Code}")
        }
        val first = result.first()
        assertNotNull(first.languageId, "languageId should not be null")
        assertNotNull(first.description, "description should not be null")
    }

    @Test
    fun testGetLists() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLists: calling getLists() with testId=1 and defaults")
            val defaultResult = api.services.getLists(testId = 1)
            logger.info("testGetLists: default result size=${defaultResult.size}")
            for (item in defaultResult.take(5)) {
                logger.info("testGetLists: code=${item.code}, description=${item.description}, academicYearId=${item.academicYearId}, listTypeCode=${item.listTypeCode}")
            }

            logger.info("testGetLists: calling getLists() with pagination start=0, limit=5")
            val paginatedResult = api.services.getLists(testId = 1, start = 0, limit = 5)
            logger.info("testGetLists: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetLists: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getLists(testId = 1)
            }
            logger.info("testGetLists: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserGroups() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUserGroups: calling getUserGroups() with defaults")
            val result = api.services.getUserGroups()
            logger.info("testGetUserGroups: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUserGroups() should return a non-empty list")
            for (group in result.take(5)) {
                logger.info("testGetUserGroups: groupId=${group.groupId}, name=${group.name}, technicalUserFlag=${group.technicalUserFlag}, httpSessionTimeout=${group.httpSessionTimeout}")
            }
            val first = result.first()
            assertNotNull(first.groupId, "groupId should not be null")
        } else {
            logger.info("testGetUserGroups: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getUserGroups()
            }
            logger.info("testGetUserGroups: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserGroup() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUserGroup: calling getUserGroup() with groupId=6 (STUDENT)")
            val result = api.services.getUserGroup(groupId = 6)
            logger.info("testGetUserGroup: result groupId=${result.groupId}, name=${result.name}, technicalUserFlag=${result.technicalUserFlag}")
            assertNotNull(result.groupId, "groupId should not be null")
            assertNotNull(result.name, "name should not be null")
        } else {
            logger.info("testGetUserGroup: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getUserGroup(groupId = 6)
            }
            logger.info("testGetUserGroup: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetParameters() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetParameters: calling getParameters() with defaults")
            val defaultResult = api.services.getParameters()
            logger.info("testGetParameters: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getParameters() should return a non-empty list")
            for (param in defaultResult.take(5)) {
                logger.info("testGetParameters: parameterCode=${param.parameterCode}, module=${param.module}, product=${param.product}, description=${param.description}")
            }

            logger.info("testGetParameters: calling getParameters() with pagination start=0, limit=5")
            val paginatedResult = api.services.getParameters(start = 0, limit = 5)
            logger.info("testGetParameters: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            val firstParam = defaultResult.first()
            if (firstParam.module != null) {
                logger.info("testGetParameters: calling getParameters() with module=${firstParam.module}")
                val filteredByModule = api.services.getParameters(module = firstParam.module)
                logger.info("testGetParameters: filtered by module result size=${filteredByModule.size}")
                assertTrue(filteredByModule.isNotEmpty(), "Filtered by module should return non-empty list")
            }

            if (firstParam.product != null) {
                logger.info("testGetParameters: calling getParameters() with product=${firstParam.product}")
                val filteredByProduct = api.services.getParameters(product = firstParam.product)
                logger.info("testGetParameters: filtered by product result size=${filteredByProduct.size}")
                assertTrue(filteredByProduct.isNotEmpty(), "Filtered by product should return non-empty list")
            }
        } else {
            logger.info("testGetParameters: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getParameters()
            }
            logger.info("testGetParameters: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetParameter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetParameter: fetching parameter list to get a valid parameterCode")
            val params = api.services.getParameters(start = 0, limit = 1)
            assertTrue(params.isNotEmpty(), "Need at least one parameter to test getParameter()")
            val paramCode = params.first().parameterCode
            logger.info("testGetParameter: calling getParameter() with parameterCode=$paramCode")
            val result = api.services.getParameter(parameterCode = paramCode)
            logger.info("testGetParameter: result parameterCode=${result.parameterCode}, module=${result.module}, product=${result.product}, description=${result.description}, numericValue=${result.numericValue}, alphanumericValue=${result.alphanumericValue}")
            assertNotNull(result.parameterCode, "parameterCode should not be null")
        } else {
            logger.info("testGetParameter: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getParameter(parameterCode = "DUMMY_PARAM")
            }
            logger.info("testGetParameter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetIdentityDocumentTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetIdentityDocumentTypes: calling getIdentityDocumentTypes() with defaults")
        val defaultResult = api.services.getIdentityDocumentTypes()
        logger.info("testGetIdentityDocumentTypes: default result size=${defaultResult.size}")
        assertTrue(defaultResult.isNotEmpty(), "getIdentityDocumentTypes() should return a non-empty list")
        for (docType in defaultResult.take(5)) {
            logger.info("testGetIdentityDocumentTypes: identityDocumentTypeCode=${docType.identityDocumentTypeCode}, description=${docType.description}")
        }
        val first = defaultResult.first()
        assertNotNull(first.identityDocumentTypeCode, "identityDocumentTypeCode should not be null")

        logger.info("testGetIdentityDocumentTypes: calling getIdentityDocumentTypes() with iso6392Code='ita'")
        val filteredResult = api.services.getIdentityDocumentTypes(iso6392Code = "ita")
        logger.info("testGetIdentityDocumentTypes: filtered result size=${filteredResult.size}")
        assertTrue(filteredResult.isNotEmpty(), "getIdentityDocumentTypes() with iso6392Code='ita' should return a non-empty list")
        for (docType in filteredResult.take(5)) {
            logger.info("testGetIdentityDocumentTypes: identityDocumentTypeCode=${docType.identityDocumentTypeCode}, description=${docType.description}")
        }
    }

    @Test
    fun testGetAddressTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetAddressTypes: calling getAddressTypes() with defaults")
        val result = api.services.getAddressTypes()
        logger.info("testGetAddressTypes: result size=${result.size}")
        assertTrue(result.isNotEmpty(), "getAddressTypes() should return a non-empty list")
        for (addrType in result.take(5)) {
            logger.info("testGetAddressTypes: addressTypeCode=${addrType.addressTypeCode}, description=${addrType.description}")
        }
        val first = result.first()
        assertNotNull(first.addressTypeCode, "addressTypeCode should not be null")
        assertNotNull(first.description, "description should not be null")
    }

    @Test
    fun testGetRequestTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetRequestTypes: calling getRequestTypes() with defaults")
            val result = api.services.getRequestTypes()
            logger.info("testGetRequestTypes: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getRequestTypes() should return a non-empty list")
            for (reqType in result.take(5)) {
                logger.info("testGetRequestTypes: requestTypeCode=${reqType.requestTypeCode}, description=${reqType.description}, systemFlag=${reqType.systemFlag}, foreignFlag=${reqType.foreignFlag}")
            }
            val first = result.first()
            assertNotNull(first.requestTypeCode, "requestTypeCode should not be null")
        } else {
            logger.info("testGetRequestTypes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getRequestTypes()
            }
            logger.info("testGetRequestTypes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPaymentRefundTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetPaymentRefundTypes: calling getPaymentRefundTypes() with defaults")
        val defaultResult = api.services.getPaymentRefundTypes()
        logger.info("testGetPaymentRefundTypes: default result size=${defaultResult.size}")
        assertTrue(defaultResult.isNotEmpty(), "getPaymentRefundTypes() should return a non-empty list")
        for (refundType in defaultResult.take(5)) {
            logger.info("testGetPaymentRefundTypes: paymentRefundTypeCode=${refundType.paymentRefundTypeCode}, description=${refundType.description}, refundPayment=${refundType.refundPayment}, webFlag=${refundType.webFlag}")
        }
        val first = defaultResult.first()
        assertNotNull(first.paymentRefundTypeCode, "paymentRefundTypeCode should not be null")

        logger.info("testGetPaymentRefundTypes: calling getPaymentRefundTypes() with iso6392Code='ita'")
        val filteredResult = api.services.getPaymentRefundTypes(iso6392Code = "ita")
        logger.info("testGetPaymentRefundTypes: filtered result size=${filteredResult.size}")
        assertTrue(filteredResult.isNotEmpty(), "getPaymentRefundTypes() with iso6392Code='ita' should return a non-empty list")
    }

    @Test
    fun testGetMaritalStatusTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetMaritalStatusTypes: calling getMaritalStatusTypes() with defaults")
        val defaultResult = api.services.getMaritalStatusTypes()
        logger.info("testGetMaritalStatusTypes: default result size=${defaultResult.size}")
        assertTrue(defaultResult.isNotEmpty(), "getMaritalStatusTypes() should return a non-empty list")
        for (status in defaultResult.take(5)) {
            logger.info("testGetMaritalStatusTypes: maritalStatusCode=${status.maritalStatusCode}, description=${status.description}")
        }
        val first = defaultResult.first()
        assertNotNull(first.maritalStatusCode, "maritalStatusCode should not be null")

        logger.info("testGetMaritalStatusTypes: calling getMaritalStatusTypes() with iso6392Code='ita'")
        val filteredResult = api.services.getMaritalStatusTypes(iso6392Code = "ita")
        logger.info("testGetMaritalStatusTypes: filtered result size=${filteredResult.size}")
        assertTrue(filteredResult.isNotEmpty(), "getMaritalStatusTypes() with iso6392Code='ita' should return a non-empty list")
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testGetCodeFromId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCodeFromId: user has TECHNICAL_USER permission, but test is disabled to prevent unintended side effects")
        } else {
            logger.info("testGetCodeFromId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getCodeFromId(
                    body = Esse3CodeToIdTranslatorRequest(
                        objects = listOf(
                            Esse3CodeToIdTranslatorRequestObject(
                                type = "CDS",
                                code = "TEST"
                            )
                        )
                    )
                )
            }
            logger.info("testGetCodeFromId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUser() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUser: calling getUser() with userId=${session.userId} and defaults")
            val result = api.services.getUser(userId = session.userId)
            logger.info("testGetUser: result userId=${result.userId}, alias=${result.alias}, groupName=${result.groupName}, disableFlag=${result.disableFlag}, userName=${result.userName}")
            assertNotNull(result.userId, "userId should not be null")

            logger.info("testGetUser: calling getUser() with aliasRecoveryAuthorization=0")
            val resultWithAlias = api.services.getUser(
                userId = session.userId,
                aliasRecoveryAuthorization = 0
            )
            logger.info("testGetUser: result with aliasRecoveryAuthorization userId=${resultWithAlias.userId}, alias=${resultWithAlias.alias}")
            assertNotNull(resultWithAlias.userId, "userId should not be null with aliasRecoveryAuthorization")
        } else {
            logger.info("testGetUser: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.services.getUser(userId = session.userId)
            }
            logger.info("testGetUser: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetVersionInfo() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        logger.info("testGetVersionInfo: calling getVersionInfo() with defaults")
        val result = api.services.getVersionInfo()
        logger.info("testGetVersionInfo: result universityName=${result.universityName}, environmentType=${result.environmentType}, buildVersion=${result.buildVersion}, version=${result.version}, encoding=${result.encoding}, remoteAddress=${result.remoteAddress}")
        assertNotNull(result.universityName, "universityName should not be null")
        assertNotNull(result.buildVersion, "buildVersion should not be null")
        assertNotNull(result.version, "version should not be null")
    }
}
