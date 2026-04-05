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

class Esse3UsersApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3UsersApiTest::class.java.name)
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
    fun testGetUsersDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUsersDefaults: calling getUsers() with no params")
            val result = api.users.getUsers()
            logger.info("testGetUsersDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUsers() default should return non-empty list")
            val first = result.first()
            logger.info("testGetUsersDefaults: first user userId=${first.userId}, firstName=${first.firstName}, lastName=${first.lastName}, groupId=${first.groupId}, groupDescription=${first.groupDescription}")
            assertNotNull(first.userId, "userId should not be null")
            assertNotNull(first.firstName, "firstName should not be null")
            assertNotNull(first.lastName, "lastName should not be null")
        } else {
            logger.info("testGetUsersDefaults: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUsers()
            }
            logger.info("testGetUsersDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUsersPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUsersPagination: calling getUsers() with start=0, limit=5")
            val result = api.users.getUsers(start = 0, limit = 5)
            logger.info("testGetUsersPagination: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (user in result) {
                logger.info("testGetUsersPagination: user userId=${user.userId}, firstName=${user.firstName}, lastName=${user.lastName}")
            }
        } else {
            logger.info("testGetUsersPagination: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUsers(start = 0, limit = 5)
            }
            logger.info("testGetUsersPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUsersWithUserIdFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val targetUserId = studentProfile.userId
            logger.info("testGetUsersWithUserIdFilter: calling getUsers() with userId=$targetUserId")
            val result = api.users.getUsers(userId = targetUserId)
            logger.info("testGetUsersWithUserIdFilter: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUsers() filtered by userId should return non-empty list")
            val matched = result.first()
            logger.info("testGetUsersWithUserIdFilter: matched user userId=${matched.userId}, firstName=${matched.firstName}, lastName=${matched.lastName}, groupId=${matched.groupId}")
            assertNotNull(matched.userId, "userId should not be null")
        } else {
            logger.info("testGetUsersWithUserIdFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUsers(userId = studentProfile.userId)
            }
            logger.info("testGetUsersWithUserIdFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUsersWithGroupIdFilter() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val targetGroupId = Esse3PermissionLevel.STUDENT.groupId?.toLong() ?: 6L
            logger.info("testGetUsersWithGroupIdFilter: calling getUsers() with groupId=$targetGroupId, start=0, limit=5")
            val result = api.users.getUsers(groupId = targetGroupId, start = 0, limit = 5)
            logger.info("testGetUsersWithGroupIdFilter: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (user in result) {
                logger.info("testGetUsersWithGroupIdFilter: user userId=${user.userId}, groupId=${user.groupId}, groupDescription=${user.groupDescription}")
            }
        } else {
            logger.info("testGetUsersWithGroupIdFilter: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUsers(groupId = 6L)
            }
            logger.info("testGetUsersWithGroupIdFilter: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostUserSignatureData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostUserSignatureData: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostUserSignatureData: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.postUserSignatureData(
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3SignatureImportData(
                        signatureData = emptyList()
                    )
                )
            }
            logger.info("testPostUserSignatureData: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostUserSignatureDataCsv() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostUserSignatureDataCsv: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostUserSignatureDataCsv: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.postUserSignatureDataCsv(body = "")
            }
            logger.info("testPostUserSignatureDataCsv: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserByUserId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUserByUserId: calling getUser() with userId=$targetUserId")
            val result = api.users.getUser(userId = targetUserId)
            logger.info("testGetUserByUserId: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUser() should return non-empty list for known userId")
            val user = result.first()
            logger.info("testGetUserByUserId: user userId=${user.userId}, firstName=${user.firstName}, lastName=${user.lastName}, id=${user.id}, groupId=${user.groupId}, groupDescription=${user.groupDescription}, personId=${user.personId}, fiscalCode=${user.fiscalCode}")
            assertNotNull(user.userId, "userId should not be null")
            assertNotNull(user.firstName, "firstName should not be null")
            assertNotNull(user.lastName, "lastName should not be null")
        } else {
            logger.info("testGetUserByUserId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUser(userId = targetUserId)
            }
            logger.info("testGetUserByUserId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserWithOptionalFields() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUserWithOptionalFields: calling getUser() with userId=$targetUserId and optionalFields=trattiCarriera")
            val result = api.users.getUser(userId = targetUserId, optionalFields = "trattiCarriera")
            logger.info("testGetUserWithOptionalFields: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUser() with optionalFields should return non-empty list")
            val user = result.first()
            logger.info("testGetUserWithOptionalFields: user userId=${user.userId}, careerSegments size=${user.careerSegments.size}")
            for (segment in user.careerSegments) {
                logger.info("testGetUserWithOptionalFields: segment studentId=${segment.studentId}, matId=${segment.matId}, matricola=${segment.matricola}, courseOfStudyId=${segment.courseOfStudyId}, courseOfStudyDescription=${segment.courseOfStudyDescription}, studentStatusCode=${segment.studentStatusCode}")
            }
        } else {
            logger.info("testGetUserWithOptionalFields: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUser(userId = targetUserId, optionalFields = "trattiCarriera")
            }
            logger.info("testGetUserWithOptionalFields: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserWithCaseSensitive() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUserWithCaseSensitive: calling getUser() with userId=$targetUserId and caseSensitive=0")
            val result = api.users.getUser(userId = targetUserId, caseSensitive = 0)
            logger.info("testGetUserWithCaseSensitive: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getUser() with caseSensitive=0 should return non-empty list")
            val user = result.first()
            logger.info("testGetUserWithCaseSensitive: user userId=${user.userId}, firstName=${user.firstName}, lastName=${user.lastName}")

            logger.info("testGetUserWithCaseSensitive: calling getUser() with userId=$targetUserId and caseSensitive=1")
            val resultCaseSensitive = api.users.getUser(userId = targetUserId, caseSensitive = 1)
            logger.info("testGetUserWithCaseSensitive: caseSensitive=1 result size=${resultCaseSensitive.size}")
            assertTrue(resultCaseSensitive.isNotEmpty(), "getUser() with caseSensitive=1 should return non-empty list for exact userId")
        } else {
            logger.info("testGetUserWithCaseSensitive: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getUser(userId = targetUserId, caseSensitive = 0)
            }
            logger.info("testGetUserWithCaseSensitive: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCieCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCieCode: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCieCode: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.putCieCode(userId = targetUserId, cieCode = "TEST_CIE_CODE")
            }
            logger.info("testPutCieCode: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDigitalIdentity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDigitalIdentity: calling getDigitalIdentity() with userId=$targetUserId")
            val result = api.users.getDigitalIdentity(userId = targetUserId)
            logger.info("testGetDigitalIdentity: result userId=${result.userId}, spidCode=${result.spidCode}, cieCode=${result.cieCode}")
            assertNotNull(result, "getDigitalIdentity() should return a non-null result")
        } else {
            logger.info("testGetDigitalIdentity: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getDigitalIdentity(userId = targetUserId)
            }
            logger.info("testGetDigitalIdentity: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutSpidCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutSpidCode: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutSpidCode: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.putSpidCode(userId = targetUserId, spidCode = "TEST_SPID_CODE")
            }
            logger.info("testPutSpidCode: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExtendedCareerDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExtendedCareerDefaults: calling getExtendedCareer() with userId=$targetUserId")
            val result = api.users.getExtendedCareer(userId = targetUserId)
            logger.info("testGetExtendedCareerDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getExtendedCareer() should return non-empty list for authenticated student")
            val first = result.first()
            logger.info("testGetExtendedCareerDefaults: first personId=${first.personId}, studentId=${first.studentId}, courseOfStudyId=${first.courseOfStudyId}, matricola=${first.matricola}, userId=${first.userId}, studentStatusCode=${first.studentStatusCode}, studentStatusDescription=${first.studentStatusDescription}")
            logger.info("testGetExtendedCareerDefaults: courseOfStudyCode=${first.courseOfStudyCode}, courseOfStudyDescription=${first.courseOfStudyDescription}, courseTypeCode=${first.courseTypeCode}, courseTypeDescription=${first.courseTypeDescription}")
            logger.info("testGetExtendedCareerDefaults: studyPlanId=${first.studyPlanId}, studyPlanCode=${first.studyPlanCode}, studyPlanDescription=${first.studyPlanDescription}, academicYearId=${first.academicYearId}")
            logger.info("testGetExtendedCareerDefaults: matriculationDate=${first.matriculationDate}, closingDate=${first.closingDate}, statusReasonCode=${first.statusReasonCode}, statusReasonDescription=${first.statusReasonDescription}")
            assertNotNull(first.personId, "personId should not be null")
            assertNotNull(first.studentId, "studentId should not be null")
            assertNotNull(first.courseOfStudyId, "courseOfStudyId should not be null")
            assertNotNull(first.matricola, "matricola should not be null")
            assertNotNull(first.userId, "userId should not be null")
        } else {
            logger.info("testGetExtendedCareerDefaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getExtendedCareer(userId = targetUserId)
            }
            logger.info("testGetExtendedCareerDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExtendedCareerWithFilters() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExtendedCareerWithFilters: fetching default results first to derive filter values")
            val defaultResult = api.users.getExtendedCareer(userId = targetUserId)
            assertTrue(defaultResult.isNotEmpty(), "getExtendedCareer() default should return non-empty list")
            val first = defaultResult.first()

            if (first.studentStatusCode != null) {
                logger.info("testGetExtendedCareerWithFilters: calling getExtendedCareer() with studentStatusCode=${first.studentStatusCode}")
                val filtered = api.users.getExtendedCareer(userId = targetUserId, studentStatusCode = first.studentStatusCode)
                logger.info("testGetExtendedCareerWithFilters: filtered by studentStatusCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtered by studentStatusCode should return non-empty list")
                for (career in filtered) {
                    logger.info("testGetExtendedCareerWithFilters: career personId=${career.personId}, studentStatusCode=${career.studentStatusCode}, matricola=${career.matricola}")
                }
            }

            if (first.statusReasonCode != null) {
                logger.info("testGetExtendedCareerWithFilters: calling getExtendedCareer() with statusReasonCode=${first.statusReasonCode}")
                val filtered = api.users.getExtendedCareer(userId = targetUserId, statusReasonCode = first.statusReasonCode)
                logger.info("testGetExtendedCareerWithFilters: filtered by statusReasonCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtered by statusReasonCode should return non-empty list")
            }

            logger.info("testGetExtendedCareerWithFilters: calling getExtendedCareer() with order=+matricola")
            val ordered = api.users.getExtendedCareer(userId = targetUserId, order = "+matricola")
            logger.info("testGetExtendedCareerWithFilters: ordered result size=${ordered.size}")
            assertTrue(ordered.isNotEmpty(), "Ordered result should return non-empty list")
        } else {
            logger.info("testGetExtendedCareerWithFilters: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getExtendedCareer(userId = targetUserId)
            }
            logger.info("testGetExtendedCareerWithFilters: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegmentsDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegmentsDefaults: calling getCareerSegments() with userId=$targetUserId")
            val result = api.users.getCareerSegments(userId = targetUserId)
            logger.info("testGetCareerSegmentsDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getCareerSegments() should return non-empty list for authenticated student")
            val first = result.first()
            logger.info("testGetCareerSegmentsDefaults: first studentId=${first.studentId}, matId=${first.matId}, courseOfStudyId=${first.courseOfStudyId}, courseOfStudyCode=${first.courseOfStudyCode}")
            logger.info("testGetCareerSegmentsDefaults: studentStatusCode=${first.studentStatusCode}, statusReasonCode=${first.statusReasonCode}, enrollmentId=${first.enrollmentId}, courseYear=${first.courseYear}")
            logger.info("testGetCareerSegmentsDefaults: courseTypeCode=${first.courseTypeCode}, studyPlanId=${first.studyPlanId}, studyPlanCode=${first.studyPlanCode}, academicYearEnrollmentId=${first.academicYearEnrollmentId}")
            logger.info("testGetCareerSegmentsDefaults: durationYears=${first.durationYears}, lastYearFlag=${first.lastYearFlag}, enrollmentTypeCode=${first.enrollmentTypeCode}, matStatusCode=${first.matStatusCode}")
        } else {
            logger.info("testGetCareerSegmentsDefaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getCareerSegments(userId = targetUserId)
            }
            logger.info("testGetCareerSegmentsDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegmentsWithFilters() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        val targetUserId = studentProfile.userId
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegmentsWithFilters: fetching default results first to derive filter values")
            val defaultResult = api.users.getCareerSegments(userId = targetUserId)
            assertTrue(defaultResult.isNotEmpty(), "getCareerSegments() default should return non-empty list")
            val first = defaultResult.first()

            if (first.studentStatusCode != null) {
                logger.info("testGetCareerSegmentsWithFilters: calling getCareerSegments() with studentStatusCode=${first.studentStatusCode}")
                val filtered = api.users.getCareerSegments(userId = targetUserId, studentStatusCode = first.studentStatusCode)
                logger.info("testGetCareerSegmentsWithFilters: filtered by studentStatusCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtered by studentStatusCode should return non-empty list")
                for (segment in filtered) {
                    logger.info("testGetCareerSegmentsWithFilters: segment studentId=${segment.studentId}, studentStatusCode=${segment.studentStatusCode}, courseOfStudyCode=${segment.courseOfStudyCode}")
                }
            }

            if (first.statusReasonCode != null) {
                logger.info("testGetCareerSegmentsWithFilters: calling getCareerSegments() with statusReasonCode=${first.statusReasonCode}")
                val filtered = api.users.getCareerSegments(userId = targetUserId, statusReasonCode = first.statusReasonCode)
                logger.info("testGetCareerSegmentsWithFilters: filtered by statusReasonCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtered by statusReasonCode should return non-empty list")
            }

            logger.info("testGetCareerSegmentsWithFilters: calling getCareerSegments() with order=+stuId")
            val ordered = api.users.getCareerSegments(userId = targetUserId, order = "+stuId")
            logger.info("testGetCareerSegmentsWithFilters: ordered result size=${ordered.size}")
            assertTrue(ordered.isNotEmpty(), "Ordered result should return non-empty list")
        } else {
            logger.info("testGetCareerSegmentsWithFilters: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.users.getCareerSegments(userId = targetUserId)
            }
            logger.info("testGetCareerSegmentsWithFilters: Esse3Exception thrown as expected")
        }
    }
}
