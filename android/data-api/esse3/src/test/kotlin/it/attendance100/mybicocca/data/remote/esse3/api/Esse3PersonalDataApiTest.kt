package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3PersonalDataApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3PersonalDataApiTest::class.java.name)
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
    @Disabled("Irreversible mutating operation")
    fun testRefreshToken() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testRefreshToken: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testRefreshToken: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.refreshToken(applicationId = "/test-app-id")
            }
            logger.info("testRefreshToken: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAuthorizedAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAuthorizedAttachmentMetadata: calling with authorizedId=1 and defaults")
            val result = api.personalData.getAuthorizedAttachmentMetadata(authorizedId = 1)
            logger.info("testGetAuthorizedAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetAuthorizedAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}, fileName=${item.fileName}")
            }
        } else {
            logger.info("testGetAuthorizedAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getAuthorizedAttachmentMetadata(authorizedId = 1)
            }
            logger.info("testGetAuthorizedAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostAuthorizedAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAuthorizedAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostAuthorizedAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetAuthorizedPersonalDocumentAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAuthorizedPersonalDocumentAttachmentMetadata: calling with authorizedId=1, identityDocumentTypeCode=CI")
            val result = api.personalData.getAuthorizedPersonalDocumentAttachmentMetadata(
                authorizedId = 1,
                identityDocumentTypeCode = "CI"
            )
            logger.info("testGetAuthorizedPersonalDocumentAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetAuthorizedPersonalDocumentAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetAuthorizedPersonalDocumentAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getAuthorizedPersonalDocumentAttachmentMetadata(
                    authorizedId = 1,
                    identityDocumentTypeCode = "CI"
                )
            }
            logger.info("testGetAuthorizedPersonalDocumentAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostAuthorizedPersonalDocumentAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAuthorizedPersonalDocumentAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostAuthorizedPersonalDocumentAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteCareerAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteCareerAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteCareerAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.deleteCareerAttachmentMetadata(matId = 1, attachmentId = 1)
            }
            logger.info("testDeleteCareerAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeletePersonalDocumentAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeletePersonalDocumentAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeletePersonalDocumentAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.deletePersonalDocumentAttachmentMetadata(personalDocumentId = 1, attachmentId = 1)
            }
            logger.info("testDeletePersonalDocumentAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHandicapDeclarationAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetHandicapDeclarationAttachmentMetadata: calling with personId=$personId, handicapType=DSA")
            val result = api.personalData.getHandicapDeclarationAttachmentMetadata(
                personId = personId,
                handicapType = "DSA"
            )
            logger.info("testGetHandicapDeclarationAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetHandicapDeclarationAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetHandicapDeclarationAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapDeclarationAttachmentMetadata(
                    personId = studentProfile.personId,
                    handicapType = "DSA"
                )
            }
            logger.info("testGetHandicapDeclarationAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostHandicapDeclarationAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostHandicapDeclarationAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostHandicapDeclarationAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetIdentityDocumentAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetIdentityDocumentAttachmentMetadata: calling with personId=$personId, identityDocumentTypeCode=CI")
            val result = api.personalData.getIdentityDocumentAttachmentMetadata(
                personId = personId,
                identityDocumentTypeCode = "CI"
            )
            logger.info("testGetIdentityDocumentAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetIdentityDocumentAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetIdentityDocumentAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getIdentityDocumentAttachmentMetadata(
                    personId = studentProfile.personId,
                    identityDocumentTypeCode = "CI"
                )
            }
            logger.info("testGetIdentityDocumentAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostIdentityDocumentAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostIdentityDocumentAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostIdentityDocumentAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostPersonPhotoAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostPersonPhotoAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostPersonPhotoAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetHighSchoolGraduationAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetHighSchoolGraduationAttachmentMetadata: calling with personId=$personId, highSchoolGraduationYear=2020")
            val result = api.personalData.getHighSchoolGraduationAttachmentMetadata(
                personId = personId,
                highSchoolGraduationYear = 2020
            )
            logger.info("testGetHighSchoolGraduationAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetHighSchoolGraduationAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetHighSchoolGraduationAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHighSchoolGraduationAttachmentMetadata(
                    personId = studentProfile.personId,
                    highSchoolGraduationYear = 2020
                )
            }
            logger.info("testGetHighSchoolGraduationAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostHighSchoolGraduationAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostHighSchoolGraduationAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostHighSchoolGraduationAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetItalianTitleAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetItalianTitleAttachmentMetadata: calling with personId=$personId, titleCategoryCode=L")
            val result = api.personalData.getItalianTitleAttachmentMetadata(
                personId = personId,
                titleCategoryCode = "L"
            )
            logger.info("testGetItalianTitleAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetItalianTitleAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetItalianTitleAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getItalianTitleAttachmentMetadata(
                    personId = studentProfile.personId,
                    titleCategoryCode = "L"
                )
            }
            logger.info("testGetItalianTitleAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostItalianTitleAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostItalianTitleAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostItalianTitleAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetForeignTitleAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetForeignTitleAttachmentMetadata: calling with personId=$personId, academicYearAwardedTitle=2020")
            val result = api.personalData.getForeignTitleAttachmentMetadata(
                personId = personId,
                academicYearAwardedTitle = 2020
            )
            logger.info("testGetForeignTitleAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetForeignTitleAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}")
            }
        } else {
            logger.info("testGetForeignTitleAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getForeignTitleAttachmentMetadata(
                    personId = studentProfile.personId,
                    academicYearAwardedTitle = 2020
                )
            }
            logger.info("testGetForeignTitleAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostForeignTitleAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostForeignTitleAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostForeignTitleAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetMatricolaAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetMatricolaAttachmentMetadata: calling with studentId=$studentId and defaults")
            val result = api.personalData.getMatricolaAttachmentMetadata(studentId = studentId)
            logger.info("testGetMatricolaAttachmentMetadata: result size=${result.size}")
            for (item in result) {
                logger.info("testGetMatricolaAttachmentMetadata: attachmentId=${item.attachmentId}, title=${item.title}, fileName=${item.fileName}")
            }
        } else {
            logger.info("testGetMatricolaAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getMatricolaAttachmentMetadata(studentId = studentProfile.studentId)
            }
            logger.info("testGetMatricolaAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostMatricolaAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostMatricolaAttachmentMetadata: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostMatricolaAttachmentMetadata: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetUniversities() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUniversities: calling with defaults")
            val defaultResult = api.personalData.getUniversities()
            logger.info("testGetUniversities: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getUniversities() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetUniversities: universityId=${item.universityId}, description=${item.description}")
            }

            logger.info("testGetUniversities: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getUniversities(start = 0, limit = 5)
            logger.info("testGetUniversities: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetUniversities: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getUniversities()
            }
            logger.info("testGetUniversities: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUniversityCourses() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUniversityCourses: calling with defaults")
            val defaultResult = api.personalData.getUniversityCourses()
            logger.info("testGetUniversityCourses: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getUniversityCourses() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetUniversityCourses: courseOfStudyAteId=${item.courseOfStudyAteId}, description=${item.description}, courseTypeCode=${item.courseTypeCode}")
            }

            logger.info("testGetUniversityCourses: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getUniversityCourses(start = 0, limit = 5)
            logger.info("testGetUniversityCourses: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetUniversityCourses: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getUniversityCourses()
            }
            logger.info("testGetUniversityCourses: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetForeignUniversities() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetForeignUniversities: calling with defaults")
            val defaultResult = api.personalData.getForeignUniversities()
            logger.info("testGetForeignUniversities: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getForeignUniversities() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetForeignUniversities: foreignUniversityId=${item.foreignUniversityId}, description=${item.description}")
            }

            logger.info("testGetForeignUniversities: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getForeignUniversities(start = 0, limit = 5)
            logger.info("testGetForeignUniversities: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetForeignUniversities: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getForeignUniversities()
            }
            logger.info("testGetForeignUniversities: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareers: calling with defaults")
            val defaultResult = api.personalData.getCareers()
            logger.info("testGetCareers: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCareers() default should return non-empty list")
            for (career in defaultResult) {
                logger.info("testGetCareers: studentId=${career.studentId}, personId=${career.personId}, courseOfStudyId=${career.courseOfStudyId}, studentStatusCode=${career.studentStatusCode}")
                assertNotNull(career.studentId, "studentId should not be null")
            }

            logger.info("testGetCareers: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getCareers(start = 0, limit = 5)
            logger.info("testGetCareers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetCareers: calling with userId filter")
            val userIdResult = api.personalData.getCareers(userId = session.userId)
            logger.info("testGetCareers: userId filter result size=${userIdResult.size}")
            assertTrue(userIdResult.isNotEmpty(), "getCareers(userId) should return non-empty list for the current user")

            if (session.fiscalCode != null) {
                logger.info("testGetCareers: calling with fiscalCode filter")
                val fiscalCodeResult = api.personalData.getCareers(fiscalCode = session.fiscalCode)
                logger.info("testGetCareers: fiscalCode filter result size=${fiscalCodeResult.size}")
                assertTrue(fiscalCodeResult.isNotEmpty(), "getCareers(fiscalCode) should return non-empty list for the current user")
            }
        } else {
            logger.info("testGetCareers: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getCareers()
            }
            logger.info("testGetCareers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetGdprCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetGdprCareerByStudent: calling with studentId=$studentId")
            val result = api.personalData.getGdprCareerByStudent(studentId = studentId)
            logger.info("testGetGdprCareerByStudent: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getGdprCareerByStudent() should return non-empty list")
            for (item in result) {
                logger.info("testGetGdprCareerByStudent: personId=${item.personId}, surname=${item.surname}, name=${item.name}")
            }
        } else {
            logger.info("testGetGdprCareerByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getGdprCareerByStudent(studentId = studentProfile.studentId)
            }
            logger.info("testGetGdprCareerByStudent: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutGraduationWaiting() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutGraduationWaiting: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutGraduationWaiting: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetPhdCareersData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPhdCareersData: calling with defaults")
            val defaultResult = api.personalData.getPhdCareersData()
            logger.info("testGetPhdCareersData: default result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetPhdCareersData: studentId=${item.studentId}, personId=${item.personId}")
            }

            logger.info("testGetPhdCareersData: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getPhdCareersData(start = 0, limit = 5)
            logger.info("testGetPhdCareersData: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetPhdCareersData: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPhdCareersData()
            }
            logger.info("testGetPhdCareersData: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetMinimalCareersData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetMinimalCareersData: calling with defaults")
            val defaultResult = api.personalData.getMinimalCareersData()
            logger.info("testGetMinimalCareersData: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getMinimalCareersData() should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetMinimalCareersData: studentId=${item.studentId}, personId=${item.personId}, courseOfStudyId=${item.courseOfStudyId}")
            }

            logger.info("testGetMinimalCareersData: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getMinimalCareersData(start = 0, limit = 5)
            logger.info("testGetMinimalCareersData: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetMinimalCareersData: calling with userId filter")
            val filteredResult = api.personalData.getMinimalCareersData(userId = session.userId)
            logger.info("testGetMinimalCareersData: userId filter result size=${filteredResult.size}")
            assertTrue(filteredResult.isNotEmpty(), "getMinimalCareersData(userId) should return non-empty list")
        } else {
            logger.info("testGetMinimalCareersData: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getMinimalCareersData()
            }
            logger.info("testGetMinimalCareersData: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutEnrollmentDateAndExemptionTypeByMatricola() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutEnrollmentDateAndExemptionTypeByMatricola: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutEnrollmentDateAndExemptionTypeByMatricola: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetCareerByStudent: calling with studentId=$studentId")
            val result = api.personalData.getCareerByStudent(studentId = studentId)
            logger.info("testGetCareerByStudent: personId=${result.personId}, studentId=${result.studentId}, userId=${result.userId}, courseOfStudyId=${result.courseOfStudyId}, studentStatusCode=${result.studentStatusCode}")
            assertNotNull(result.studentId, "studentId should not be null")
            assertNotNull(result.personId, "personId should not be null")
        } else {
            logger.info("testGetCareerByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getCareerByStudent(studentId = studentProfile.studentId)
            }
            logger.info("testGetCareerByStudent: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCareerByStudent: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCareerByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putCareerByStudent(
                    studentId = studentProfile.studentId,
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CareerParameters("pirupiru")
                )
            }
            logger.info("testPutCareerByStudent: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testCareerClosure() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testCareerClosure: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testCareerClosure: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetAnnualEnrollment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetAnnualEnrollment: calling with studentId=$studentId and defaults")
            val defaultResult = api.personalData.getAnnualEnrollment(studentId = studentId)
            logger.info("testGetAnnualEnrollment: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getAnnualEnrollment() should return non-empty list")
            for (enrollment in defaultResult) {
                logger.info("testGetAnnualEnrollment: academicYearEnrollmentId=${enrollment.academicYearEnrollmentId}, courseOfStudyCode=${enrollment.courseOfStudyCode}, enrollmentDate=${enrollment.enrollmentDate}")
            }

            logger.info("testGetAnnualEnrollment: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getAnnualEnrollment(studentId = studentId, start = 0, limit = 5)
            logger.info("testGetAnnualEnrollment: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetAnnualEnrollment: calling with lastEnrollmentFlag=1")
            val lastEnrollment = api.personalData.getAnnualEnrollment(studentId = studentId, lastEnrollmentFlag = 1)
            logger.info("testGetAnnualEnrollment: lastEnrollmentFlag result size=${lastEnrollment.size}")
            assertTrue(lastEnrollment.isNotEmpty(), "getAnnualEnrollment(lastEnrollmentFlag=1) should return non-empty list")
        } else {
            logger.info("testGetAnnualEnrollment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getAnnualEnrollment(studentId = studentProfile.studentId)
            }
            logger.info("testGetAnnualEnrollment: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutEnrollmentDateAndExemptionTypeByStudentId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutEnrollmentDateAndExemptionTypeByStudentId: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutEnrollmentDateAndExemptionTypeByStudentId: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCanteenBand() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCanteenBand: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCanteenBand: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutStudentTypeCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutStudentTypeCode: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutStudentTypeCode: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetBankDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetBankDetails: calling with personId=$personId and defaults")
            val defaultResult = api.personalData.getBankDetails(personId = personId)
            logger.info("testGetBankDetails: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetBankDetails: ibanCode=${item.ibanCode}, bankDescription=${item.bankDescription}")
            }

            logger.info("testGetBankDetails: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getBankDetails(personId = personId, start = 0, limit = 5)
            logger.info("testGetBankDetails: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetBankDetails: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getBankDetails(personId = studentProfile.personId)
            }
            logger.info("testGetBankDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturer: calling with lecturerId=1 and defaults")
            val result = api.personalData.getLecturer(lecturerId = 1)
            logger.info("testGetLecturer: result size=${result.size}")
            for (item in result) {
                logger.info("testGetLecturer: lecturerSurname=${item.lecturerSurname}, lecturerName=${item.lecturerName}")
            }
        } else {
            logger.info("testGetLecturer: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getLecturer(lecturerId = 1)
            }
            logger.info("testGetLecturer: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetEnrollments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEnrollments: calling with academicYearEnrollmentId=2024, includeSXH=0, includeCondition=0")
            val defaultResult = api.personalData.getEnrollments(
                academicYearEnrollmentId = "2024",
                includeSXH = 0,
                includeCondition = 0
            )
            logger.info("testGetEnrollments: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getEnrollments() should return non-empty list")
            for (enrollment in defaultResult.take(3)) {
                logger.info("testGetEnrollments: studentId=${enrollment.studentId}, courseOfStudyCode=${enrollment.courseOfStudyCode}, enrollmentDate=${enrollment.enrollmentDate}")
            }

            logger.info("testGetEnrollments: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getEnrollments(
                academicYearEnrollmentId = "2024",
                includeSXH = 0,
                includeCondition = 0,
                start = 0,
                limit = 5
            )
            logger.info("testGetEnrollments: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetEnrollments: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getEnrollments(
                    academicYearEnrollmentId = "2024",
                    includeSXH = 0,
                    includeCondition = 0
                )
            }
            logger.info("testGetEnrollments: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInstitutions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInstitutions: calling with defaults")
            val defaultResult = api.personalData.getInstitutions()
            logger.info("testGetInstitutions: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getInstitutions() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetInstitutions: higherSchoolId=${item.higherSchoolId}, description=${item.description}")
            }

            logger.info("testGetInstitutions: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getInstitutions(start = 0, limit = 5)
            logger.info("testGetInstitutions: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetInstitutions: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getInstitutions()
            }
            logger.info("testGetInstitutions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompensatoryMeasures() = runTest {
        logger.info("testGetCompensatoryMeasures: calling with defaults (ANY permission)")
        val defaultResult = api.personalData.getCompensatoryMeasures()
        logger.info("testGetCompensatoryMeasures: default result size=${defaultResult.size}")
        for (item in defaultResult) {
            logger.info("testGetCompensatoryMeasures: code=${item.compensatoryMeasureCode}, description=${item.description}, webVisibleFlag=${item.webVisibleFlag}")
        }

        logger.info("testGetCompensatoryMeasures: calling with handicapType=DSA")
        val filteredResult = api.personalData.getCompensatoryMeasures(handicapType = "DSA")
        logger.info("testGetCompensatoryMeasures: filtered result size=${filteredResult.size}")
    }

    @Test
    fun testGetHandicapRegulations() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHandicapRegulations: calling with defaults")
            val result = api.personalData.getHandicapRegulations()
            logger.info("testGetHandicapRegulations: result size=${result.size}")
            for (item in result) {
                logger.info("testGetHandicapRegulations: handicapRegulationCode=${item.handicapRegulationCode}, description=${item.description}")
            }
        } else {
            logger.info("testGetHandicapRegulations: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapRegulations()
            }
            logger.info("testGetHandicapRegulations: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersons() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersons: calling with personId=$personId")
            val result = api.personalData.getPersons(personId = personId)
            logger.info("testGetPersons: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getPersons(personId) should return non-empty list")
            for (person in result) {
                logger.info("testGetPersons: personId=${person.personId}, surname=${person.surname}, name=${person.name}, birthDate=${person.birthDate}")
                assertNotNull(person.personId, "personId should not be null")
            }

            logger.info("testGetPersons: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getPersons(personId = personId, start = 0, limit = 5)
            logger.info("testGetPersons: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetPersons: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersons(personId = studentProfile.personId)
            }
            logger.info("testGetPersons: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetGdprPersons() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetGdprPersons: calling with defaults and pagination start=0, limit=5")
            val paginatedResult = api.personalData.getGdprPersons(start = 0, limit = 5)
            logger.info("testGetGdprPersons: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            for (person in paginatedResult) {
                logger.info("testGetGdprPersons: personId=${person.personId}, surname=${person.surname}, name=${person.name}")
            }
        } else {
            logger.info("testGetGdprPersons: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getGdprPersons()
            }
            logger.info("testGetGdprPersons: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetGdprPerson() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetGdprPerson: calling with personId=$personId")
            val result = api.personalData.getGdprPerson(personId = personId)
            logger.info("testGetGdprPerson: personId=${result.personId}, surname=${result.surname}, name=${result.name}")
            assertNotNull(result.personId, "personId should not be null")
        } else {
            logger.info("testGetGdprPerson: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getGdprPerson(personId = studentProfile.personId)
            }
            logger.info("testGetGdprPerson: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDismissEmailByAteEmail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDismissEmailByAteEmail: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDismissEmailByAteEmail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.dismissEmailByAteEmail(universityEmail = "test@campus.unimib.it")
            }
            logger.info("testDismissEmailByAteEmail: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPerson() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPerson: calling with personId=$personId")
            val result = api.personalData.getPerson(personId = personId)
            logger.info("testGetPerson: personId=${result.personId}, surname=${result.surname}, name=${result.name}, birthDate=${result.birthDate}, fiscalCode=${result.fiscalCode}")
            assertNotNull(result.personId, "personId should not be null")
            assertNotNull(result.surname, "surname should not be null")
            assertNotNull(result.name, "name should not be null")
        } else {
            logger.info("testGetPerson: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPerson(personId = studentProfile.personId)
            }
            logger.info("testGetPerson: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutPersonCompensatoryMeasuresHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutPersonCompensatoryMeasuresHandicapDeclaration: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutPersonCompensatoryMeasuresHandicapDeclaration: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetAuthorizedForPerson() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetAuthorizedForPerson: calling with personId=$personId and defaults")
            val result = api.personalData.getAuthorizedForPerson(personId = personId)
            logger.info("testGetAuthorizedForPerson: result size=${result.size}")
            for (item in result) {
                logger.info("testGetAuthorizedForPerson: authorizedId=${item.authorizedId}, surname=${item.surname}, name=${item.name}")
            }
        } else {
            logger.info("testGetAuthorizedForPerson: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getAuthorizedForPerson(personId = studentProfile.personId)
            }
            logger.info("testGetAuthorizedForPerson: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerByStudentPerson() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            val studentId = studentProfile.studentId
            logger.info("testGetCareerByStudentPerson: calling with personId=$personId, studentId=$studentId")
            val result = api.personalData.getCareerByStudentPerson(personId = personId, studentId = studentId)
            logger.info("testGetCareerByStudentPerson: personId=${result.personId}, studentId=${result.studentId}, courseOfStudyId=${result.courseOfStudyId}, studentStatusCode=${result.studentStatusCode}")
            assertNotNull(result.studentId, "studentId should not be null")
            assertNotNull(result.personId, "personId should not be null")
        } else {
            logger.info("testGetCareerByStudentPerson: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getCareerByStudentPerson(
                    personId = studentProfile.personId,
                    studentId = studentProfile.studentId
                )
            }
            logger.info("testGetCareerByStudentPerson: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutMobilePhone() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutMobilePhone: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutMobilePhone: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putMobilePhone(
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3MobileParameter()
                )
            }
            logger.info("testPutMobilePhone: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentConsents() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetStudentConsents: calling with personId=$personId, webProcedureCode=IMMA")
            val result = api.personalData.getStudentConsents(
                personId = personId,
                webProcedureCode = "IMMA"
            )
            logger.info("testGetStudentConsents: result size=${result.size}")
            for (item in result) {
                logger.info("testGetStudentConsents: consentTypesConsentTypeCode=${item.consentTypesConsentTypeCode}, consentFlag=${item.consentFlag}")
            }
        } else {
            logger.info("testGetStudentConsents: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getStudentConsents(
                    personId = studentProfile.personId,
                    webProcedureCode = "IMMA"
                )
            }
            logger.info("testGetStudentConsents: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutStudentConsents() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutStudentConsents: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutStudentConsents: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testInsertCompensatoryMeasuresHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testInsertCompensatoryMeasuresHandicapDeclaration: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testInsertCompensatoryMeasuresHandicapDeclaration: user lacks required permissions")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testUpdatePersonCompensatoryMeasuresHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testUpdatePersonCompensatoryMeasuresHandicapDeclaration: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testUpdatePersonCompensatoryMeasuresHandicapDeclaration: user lacks required permissions")
        }
    }

    @Test
    fun testGetHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetHandicapDeclaration: calling with personId=$personId and defaults")
            val result = api.personalData.getHandicapDeclaration(personId = personId)
            logger.info("testGetHandicapDeclaration: result size=${result.size}")
            for (item in result) {
                logger.info("testGetHandicapDeclaration: declarationId=${item.declarationId}, handicapType=${item.handicapType}")
            }
        } else {
            logger.info("testGetHandicapDeclaration: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapDeclaration(personId = studentProfile.personId)
            }
            logger.info("testGetHandicapDeclaration: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutHandicapDeclaration: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutHandicapDeclaration: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutHandicapDeclarationById() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutHandicapDeclarationById: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutHandicapDeclarationById: user lacks required permissions")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDismissEmail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDismissEmail: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDismissEmail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.dismissEmail(personId = studentProfile.personId)
            }
            logger.info("testDismissEmail: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutStudentEmail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutStudentEmail: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutStudentEmail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putStudentEmail(personId = studentProfile.personId, email = "test@test.com")
            }
            logger.info("testPutStudentEmail: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutStudentAteEmail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutStudentAteEmail: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutStudentAteEmail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putStudentAteEmail(
                    personId = studentProfile.personId,
                    universityEmail = "test@campus.unimib.it"
                )
            }
            logger.info("testPutStudentAteEmail: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonPhoto() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersonPhoto: calling with personId=$personId")
            val result = api.personalData.getPersonPhoto(personId = personId)
            logger.info("testGetPersonPhoto: received ByteReadChannel for person photo")
            assertNotNull(result, "Person photo stream should not be null")
        } else {
            logger.info("testGetPersonPhoto: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersonPhoto(personId = studentProfile.personId)
            }
            logger.info("testGetPersonPhoto: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostCompensatoryMeasuresHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostCompensatoryMeasuresHandicapDeclaration: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostCompensatoryMeasuresHandicapDeclaration: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetPersonCompensatoryMeasures() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersonCompensatoryMeasures: calling with personId=$personId and defaults")
            val result = api.personalData.getPersonCompensatoryMeasures(personId = personId)
            logger.info("testGetPersonCompensatoryMeasures: result size=${result.size}")
            for (item in result) {
                logger.info("testGetPersonCompensatoryMeasures: compensatoryMeasureCode=${item.compensatoryMeasureCode}, compensatoryMeasureDescription=${item.compensatoryMeasureDescription}, handicapType=${item.handicapType}")
            }
        } else {
            logger.info("testGetPersonCompensatoryMeasures: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersonCompensatoryMeasures(personId = studentProfile.personId)
            }
            logger.info("testGetPersonCompensatoryMeasures: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonCompensatoryMeasuresHandicapDeclaration() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersonCompensatoryMeasuresHandicapDeclaration: calling with personId=$personId and defaults")
            val result = api.personalData.getPersonCompensatoryMeasuresHandicapDeclaration(personId = personId)
            logger.info("testGetPersonCompensatoryMeasuresHandicapDeclaration: result size=${result.size}")
            for (item in result) {
                logger.info("testGetPersonCompensatoryMeasuresHandicapDeclaration: compensatoryMeasureCode=${item.compensatoryMeasureCode}, compensatoryMeasureDescription=${item.compensatoryMeasureDescription}, handicapType=${item.handicapType}")
            }
        } else {
            logger.info("testGetPersonCompensatoryMeasuresHandicapDeclaration: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersonCompensatoryMeasuresHandicapDeclaration(personId = studentProfile.personId)
            }
            logger.info("testGetPersonCompensatoryMeasuresHandicapDeclaration: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutDomicilePhone() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutDomicilePhone: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutDomicilePhone: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putDomicilePhone(
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PhoneParameters()
                )
            }
            logger.info("testPutDomicilePhone: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutResidencePhone() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutResidencePhone: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutResidencePhone: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.putResidencePhone(
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PhoneParameters()
                )
            }
            logger.info("testPutResidencePhone: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonTutors() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersonTutors: calling with personId=$personId and defaults")
            val result = api.personalData.getPersonTutors(personId = personId)
            logger.info("testGetPersonTutors: result size=${result.size}")
            for (item in result) {
                logger.info("testGetPersonTutors: authorizedId=${item.authorizedId}, surname=${item.surname}, name=${item.name}")
            }
        } else {
            logger.info("testGetPersonTutors: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersonTutors(personId = studentProfile.personId)
            }
            logger.info("testGetPersonTutors: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPhotoValidationFlag() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPhotoValidationFlag: calling with personId=$personId")
            val result = api.personalData.getPhotoValidationFlag(personId = personId)
            logger.info("testGetPhotoValidationFlag: validationFlag=${result.validationFlag}")
            assertNotNull(result.validationFlag, "validationFlag should not be null")
        } else {
            logger.info("testGetPhotoValidationFlag: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPhotoValidationFlag(personId = studentProfile.personId)
            }
            logger.info("testGetPhotoValidationFlag: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHighSchoolGradeRange() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHighSchoolGradeRange: calling with defaults")
            val result = api.personalData.getHighSchoolGradeRange()
            logger.info("testGetHighSchoolGradeRange: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getHighSchoolGradeRange() should return non-empty list")
            for (item in result) {
                logger.info("testGetHighSchoolGradeRange: minGrade=${item.minGrade}, maxGrade=${item.maxGrade}")
            }
        } else {
            logger.info("testGetHighSchoolGradeRange: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHighSchoolGradeRange()
            }
            logger.info("testGetHighSchoolGradeRange: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExternalSubject() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExternalSubject: calling with defaults")
            val defaultResult = api.personalData.getExternalSubject()
            logger.info("testGetExternalSubject: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getExternalSubject() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetExternalSubject: externalSubjectId=${item.externalSubjectId}, surname=${item.surname}, name=${item.name}, externalSubjectTypeCode=${item.externalSubjectTypeCode}")
            }

            logger.info("testGetExternalSubject: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getExternalSubject(start = 0, limit = 5)
            logger.info("testGetExternalSubject: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetExternalSubject: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getExternalSubject()
            }
            logger.info("testGetExternalSubject: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutExternalSubject() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutExternalSubject: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutExternalSubject: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteExternalSubject() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteExternalSubject: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteExternalSubject: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.deleteExternalSubject(externalSubjectId = 1)
            }
            logger.info("testDeleteExternalSubject: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExternalSubjectConsents() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExternalSubjectConsents: calling with externalSubjectId=1, webProcedureCode=IMMA")
            val result = api.personalData.getExternalSubjectConsents(
                externalSubjectId = 1,
                webProcedureCode = "IMMA"
            )
            logger.info("testGetExternalSubjectConsents: result size=${result.size}")
            for (item in result) {
                logger.info("testGetExternalSubjectConsents: consentTypesConsentTypeCode=${item.consentTypesConsentTypeCode}, consentFlag=${item.consentFlag}")
            }
        } else {
            logger.info("testGetExternalSubjectConsents: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getExternalSubjectConsents(
                    externalSubjectId = 1,
                    webProcedureCode = "IMMA"
                )
            }
            logger.info("testGetExternalSubjectConsents: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutExternalSubjectConsents() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutExternalSubjectConsents: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutExternalSubjectConsents: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetExternalSubjectsReplica() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExternalSubjectsReplica: calling with defaults")
            val defaultResult = api.personalData.getExternalSubjectsReplica()
            logger.info("testGetExternalSubjectsReplica: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getExternalSubjectsReplica() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetExternalSubjectsReplica: externalSubjectId=${item.externalSubjectId}, surname=${item.surname}, name=${item.name}")
            }

            logger.info("testGetExternalSubjectsReplica: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getExternalSubjectsReplica(start = 0, limit = 5)
            logger.info("testGetExternalSubjectsReplica: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetExternalSubjectsReplica: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getExternalSubjectsReplica()
            }
            logger.info("testGetExternalSubjectsReplica: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExternalSubjectReplica() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExternalSubjectReplica: calling with externalSubjectId=1")
            val result = api.personalData.getExternalSubjectReplica(externalSubjectId = 1)
            logger.info("testGetExternalSubjectReplica: externalSubjectId=${result.externalSubjectId}, surname=${result.surname}, name=${result.name}")
        } else {
            logger.info("testGetExternalSubjectReplica: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getExternalSubjectReplica(externalSubjectId = 1)
            }
            logger.info("testGetExternalSubjectReplica: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHigherInstitutionTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHigherInstitutionTypes: calling with defaults")
            val result = api.personalData.getHigherInstitutionTypes()
            logger.info("testGetHigherInstitutionTypes: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getHigherInstitutionTypes() should return non-empty list")
            for (item in result) {
                logger.info("testGetHigherInstitutionTypes: typologyCode=${item.typologyCode}, description=${item.description}")
            }
        } else {
            logger.info("testGetHigherInstitutionTypes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHigherInstitutionTypes()
            }
            logger.info("testGetHigherInstitutionTypes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHigherSchoolTitleTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHigherSchoolTitleTypes: calling with defaults")
            val defaultResult = api.personalData.getHigherSchoolTitleTypes()
            logger.info("testGetHigherSchoolTitleTypes: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getHigherSchoolTitleTypes() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetHigherSchoolTitleTypes: titleTypeCode=${item.titleTypeCode}, description=${item.description}")
            }

            logger.info("testGetHigherSchoolTitleTypes: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getHigherSchoolTitleTypes(start = 0, limit = 5)
            logger.info("testGetHigherSchoolTitleTypes: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetHigherSchoolTitleTypes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHigherSchoolTitleTypes()
            }
            logger.info("testGetHigherSchoolTitleTypes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetForeignTitleTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetForeignTitleTypes: calling with defaults")
            val defaultResult = api.personalData.getForeignTitleTypes()
            logger.info("testGetForeignTitleTypes: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getForeignTitleTypes() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetForeignTitleTypes: titleTypeCode=${item.titleStatusTypeCode}, description=${item.description}")
            }

            logger.info("testGetForeignTitleTypes: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getForeignTitleTypes(start = 0, limit = 5)
            logger.info("testGetForeignTitleTypes: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetForeignTitleTypes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getForeignTitleTypes()
            }
            logger.info("testGetForeignTitleTypes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetForeignTitleValueDeclarationTypologies() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetForeignTitleValueDeclarationTypologies: calling with defaults")
            val defaultResult = api.personalData.getForeignTitleValueDeclarationTypologies()
            logger.info("testGetForeignTitleValueDeclarationTypologies: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getForeignTitleValueDeclarationTypologies() should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetForeignTitleValueDeclarationTypologies: valueDeclarationTypeCode=${item.valueDeclarationTypeCode}, description=${item.description}")
            }

            logger.info("testGetForeignTitleValueDeclarationTypologies: calling with pagination start=0, limit=5")
            val paginatedResult = api.personalData.getForeignTitleValueDeclarationTypologies(start = 0, limit = 5)
            logger.info("testGetForeignTitleValueDeclarationTypologies: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetForeignTitleValueDeclarationTypologies: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getForeignTitleValueDeclarationTypologies()
            }
            logger.info("testGetForeignTitleValueDeclarationTypologies: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHandicapTypologies() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHandicapTypologies: calling with defaults")
            val result = api.personalData.getHandicapTypologies()
            logger.info("testGetHandicapTypologies: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getHandicapTypologies() should return non-empty list")
            for (item in result) {
                logger.info("testGetHandicapTypologies: handicapType=${item.handicapType}, description=${item.description}")
            }
        } else {
            logger.info("testGetHandicapTypologies: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapTypologies()
            }
            logger.info("testGetHandicapTypologies: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHandicapTypologiesToEvaluate() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetHandicapTypologiesToEvaluate: calling with defaults")
            val result = api.personalData.getHandicapTypologiesToEvaluate()
            logger.info("testGetHandicapTypologiesToEvaluate: result size=${result.size}")
            for (item in result) {
                logger.info("testGetHandicapTypologiesToEvaluate: handicapType=${item.handicapType}, description=${item.description}")
            }
        } else {
            logger.info("testGetHandicapTypologiesToEvaluate: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapTypologiesToEvaluate()
            }
            logger.info("testGetHandicapTypologiesToEvaluate: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRelationshipTypologies() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetRelationshipTypologies: calling with defaults")
            val result = api.personalData.getRelationshipTypologies()
            logger.info("testGetRelationshipTypologies: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getRelationshipTypologies() should return non-empty list")
            for (item in result) {
                logger.info("testGetRelationshipTypologies: paragraphTypeCode=${item.paragraphTypeCode}, description=${item.description}")
            }
        } else {
            logger.info("testGetRelationshipTypologies: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getRelationshipTypologies()
            }
            logger.info("testGetRelationshipTypologies: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutTitles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutTitles: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutTitles: user lacks TECHNICAL_USER permission")
        }
    }

    @Test
    fun testGetTitles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetTitles: calling with personId=$personId and defaults")
            val result = api.personalData.getTitles(personId = personId)
            logger.info("testGetTitles: personId=${result.personId}, SUP size=${result.SUP.size}, foreignTitle size=${result.foreignTitle.size}, italianTitle size=${result.italianTitle.size}")
            assertNotNull(result.personId, "personId should not be null")

            logger.info("testGetTitles: calling with studentId parameter")
            val resultWithStudentId = api.personalData.getTitles(
                personId = personId,
                studentId = studentProfile.studentId
            )
            logger.info("testGetTitles: with studentId personId=${resultWithStudentId.personId}, SUP size=${resultWithStudentId.SUP.size}")
        } else {
            logger.info("testGetTitles: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getTitles(personId = studentProfile.personId)
            }
            logger.info("testGetTitles: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonTitles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetPersonTitles: calling with personId=$personId")
            val result = api.personalData.getPersonTitles(personId = personId)
            logger.info("testGetPersonTitles: personId=${result.personId}, SUP size=${result.SUP.size}, foreignTitle size=${result.foreignTitle.size}, italianTitle size=${result.italianTitle.size}")
            assertNotNull(result.personId, "personId should not be null")

            logger.info("testGetPersonTitles: calling with studentId parameter")
            val resultWithStudentId = api.personalData.getPersonTitles(studentId = studentProfile.studentId)
            logger.info("testGetPersonTitles: with studentId personId=${resultWithStudentId.personId}, SUP size=${resultWithStudentId.SUP.size}")
        } else {
            logger.info("testGetPersonTitles: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getPersonTitles(personId = studentProfile.personId)
            }
            logger.info("testGetPersonTitles: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTutorRules() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetTutorRules: calling with defaults")
            val result = api.personalData.getTutorRules()
            logger.info("testGetTutorRules: result size=${result.size}")
            for (item in result) {
                logger.info("testGetTutorRules: tutorsTestRegistrationId=${item.tutorsTestRegistrationId}, code=${item.code}, description=${item.description}")
            }
        } else {
            logger.info("testGetTutorRules: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getTutorRules()
            }
            logger.info("testGetTutorRules: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetHandicapDeclarationById() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetHandicapDeclarationById: first fetching handicap declarations for personId=$personId")
            val declarations = api.personalData.getHandicapDeclaration(personId = personId)
            if (declarations.isNotEmpty()) {
                val firstDeclaration = declarations.first()
                val declarationId = firstDeclaration.declarationId
                assertNotNull(declarationId, "declarationId should not be null")
                logger.info("testGetHandicapDeclarationById: calling with personId=$personId, handicapDeclarationId=$declarationId")
                val result = api.personalData.getHandicapDeclarationById(
                    personId = personId,
                    handicapDeclarationId = declarationId
                )
                logger.info("testGetHandicapDeclarationById: declarationId=${result.declarationId}, handicapType=${result.handicapType}")
                assertNotNull(result.declarationId, "declarationId should not be null")
            } else {
                logger.info("testGetHandicapDeclarationById: no handicap declarations found for person, skipping detail fetch")
            }
        } else {
            logger.info("testGetHandicapDeclarationById: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getHandicapDeclarationById(
                    personId = studentProfile.personId,
                    handicapDeclarationId = 1
                )
            }
            logger.info("testGetHandicapDeclarationById: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAttachmentContent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.EXTERNAL_SUBJECT)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            logger.info("testGetAttachmentContent: first fetching handicap declarations for personId=$personId")
            val declarations = api.personalData.getHandicapDeclaration(personId = personId)
            if (declarations.isNotEmpty()) {
                val declarationId = declarations.first().declarationId
                assertNotNull(declarationId, "declarationId should not be null")
                logger.info("testGetAttachmentContent: handicap declaration found, but no attachment ID available to test streaming content")
            } else {
                logger.info("testGetAttachmentContent: no handicap declarations found, skipping attachment content test")
            }
        } else {
            logger.info("testGetAttachmentContent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.personalData.getAttachmentContent(
                    personId = studentProfile.personId,
                    handicapDeclarationId = 1,
                    attachmentId = 1
                )
            }
            logger.info("testGetAttachmentContent: Esse3Exception thrown as expected")
        }
    }
}
