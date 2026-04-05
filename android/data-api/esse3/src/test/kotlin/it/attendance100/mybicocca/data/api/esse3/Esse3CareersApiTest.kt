package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CanteenBandParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerClosureParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3CareerParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionTypeParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3GraduationWaitingParameters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentTypeParameters
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3CareersApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3CareersApiTest::class.java.name)
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
    fun testGetCareers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareers: calling getCareers() with defaults")
            val defaultResult = api.careers.getCareers()
            logger.info("testGetCareers: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCareers() default should return non-empty list")
            for (career in defaultResult) {
                logger.info("testGetCareers: career studentId=${career.studentId}, personId=${career.personId}, enrollmentId=${career.enrollmentId}")
                assertNotNull(career.studentId, "studentId should not be null")
                assertNotNull(career.personId, "personId should not be null")
            }

            val firstCareer = defaultResult.first()

            logger.info("testGetCareers: calling getCareers(onlyActive=1)")
            val activeResult = api.careers.getCareers(onlyActive = 1)
            logger.info("testGetCareers: active result size=${activeResult.size}")

            logger.info("testGetCareers: calling getCareers with pagination start=0, limit=5")
            val paginatedResult = api.careers.getCareers(start = 0, limit = 5)
            logger.info("testGetCareers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetCareers: calling getCareers with studentStatusCode filter")
            val filteredByStatus = api.careers.getCareers(studentStatusCode = firstCareer.studentStatusCode)
            logger.info("testGetCareers: filtered by status result size=${filteredByStatus.size}")

            logger.info("testGetCareers: calling getCareers with courseOfStudyId filter")
            val filteredByCds = api.careers.getCareers(courseOfStudyId = firstCareer.courseOfStudyId)
            logger.info("testGetCareers: filtered by courseOfStudyId result size=${filteredByCds.size}")

            logger.info("testGetCareers: calling getCareers with onlyEnrolled=1")
            val enrolledResult = api.careers.getCareers(onlyEnrolled = 1)
            logger.info("testGetCareers: enrolled result size=${enrolledResult.size}")

            logger.info("testGetCareers: calling getCareers with userId filter")
            val filteredByUserId = api.careers.getCareers(userId = firstCareer.userId)
            logger.info("testGetCareers: filtered by userId result size=${filteredByUserId.size}")
        } else {
            logger.info("testGetCareers: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getCareers()
            }
        }
    }

    @Test
    fun testGetMinimalGdprCareersData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetMinimalGdprCareersData: calling getMinimalGdprCareersData() with defaults")
            val defaultResult = api.careers.getMinimalGdprCareersData()
            logger.info("testGetMinimalGdprCareersData: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getMinimalGdprCareersData() default should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetMinimalGdprCareersData: studentId=${item.studentId}, personId=${item.personId}, surname=${item.surname}")
                assertNotNull(item.studentId, "studentId should not be null")
                assertNotNull(item.personId, "personId should not be null")
            }

            logger.info("testGetMinimalGdprCareersData: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getMinimalGdprCareersData(start = 0, limit = 5)
            logger.info("testGetMinimalGdprCareersData: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetMinimalGdprCareersData: calling with onlyActive=1")
            val activeResult = api.careers.getMinimalGdprCareersData(onlyActive = 1)
            logger.info("testGetMinimalGdprCareersData: active result size=${activeResult.size}")

            logger.info("testGetMinimalGdprCareersData: calling with onlyEnrolled=1")
            val enrolledResult = api.careers.getMinimalGdprCareersData(onlyEnrolled = 1)
            logger.info("testGetMinimalGdprCareersData: enrolled result size=${enrolledResult.size}")
        } else {
            logger.info("testGetMinimalGdprCareersData: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getMinimalGdprCareersData()
            }
        }
    }

    @Test
    fun testGetGdprCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetGdprCareerByStudent: calling getGdprCareerByStudent(studentId=$studentId) with defaults")
            val defaultResult = api.careers.getGdprCareerByStudent(studentId = studentId)
            logger.info("testGetGdprCareerByStudent: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getGdprCareerByStudent() should return non-null result")
            for (item in defaultResult) {
                logger.info("testGetGdprCareerByStudent: personId=${item.personId}, surname=${item.surname}, studentStatusCode=${item.studentStatusCode}")
                assertNotNull(item.personId, "personId should not be null")
            }

            logger.info("testGetGdprCareerByStudent: calling with optionalFields=''")
            val withOptionalFields = api.careers.getGdprCareerByStudent(studentId = studentId, optionalFields = "")
            logger.info("testGetGdprCareerByStudent: optionalFields result size=${withOptionalFields.size}")

            val careers = api.careers.getCareers()
            for (career in careers.take(3)) {
                val stuId = career.studentId ?: continue
                logger.info("testGetGdprCareerByStudent: calling getGdprCareerByStudent(studentId=$stuId)")
                val result = api.careers.getGdprCareerByStudent(studentId = stuId)
                logger.info("testGetGdprCareerByStudent: result size=${result.size} for studentId=$stuId")
                assertNotNull(result, "Result should not be null for studentId=$stuId")
            }
        } else {
            logger.info("testGetGdprCareerByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getGdprCareerByStudent(studentId = studentProfile.studentId)
            }
        }
    }

    @Test
    fun testGetPhdCareersData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPhdCareersData: calling getPhdCareersData() with defaults")
            val defaultResult = api.careers.getPhdCareersData()
            logger.info("testGetPhdCareersData: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetPhdCareersData: personId=${item.personId}, studentId=${item.studentId}")
            }

            logger.info("testGetPhdCareersData: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getPhdCareersData(start = 0, limit = 5)
            logger.info("testGetPhdCareersData: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetPhdCareersData: calling with onlyActive=1")
            val activeResult = api.careers.getPhdCareersData(onlyActive = 1)
            logger.info("testGetPhdCareersData: active result size=${activeResult.size}")

            logger.info("testGetPhdCareersData: calling with studentId from profile")
            val byStudentId = api.careers.getPhdCareersData(studentId = studentProfile.studentId)
            logger.info("testGetPhdCareersData: filtered by studentId result size=${byStudentId.size}")

            logger.info("testGetPhdCareersData: calling with onlyEnrolled=1")
            val enrolledResult = api.careers.getPhdCareersData(onlyEnrolled = 1)
            logger.info("testGetPhdCareersData: enrolled result size=${enrolledResult.size}")
        } else {
            logger.info("testGetPhdCareersData: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getPhdCareersData()
            }
        }
    }

    @Test
    fun testGetMinimalCareersData() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetMinimalCareersData: calling getMinimalCareersData() with defaults")
            val defaultResult = api.careers.getMinimalCareersData()
            logger.info("testGetMinimalCareersData: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getMinimalCareersData() default should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetMinimalCareersData: studentId=${item.studentId}, personId=${item.personId}, surname=${item.surname}")
                assertNotNull(item.studentId, "studentId should not be null")
                assertNotNull(item.personId, "personId should not be null")
            }

            logger.info("testGetMinimalCareersData: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getMinimalCareersData(start = 0, limit = 5)
            logger.info("testGetMinimalCareersData: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetMinimalCareersData: calling with onlyActive=1")
            val activeResult = api.careers.getMinimalCareersData(onlyActive = 1)
            logger.info("testGetMinimalCareersData: active result size=${activeResult.size}")

            logger.info("testGetMinimalCareersData: calling with onlyEnrolled=1")
            val enrolledResult = api.careers.getMinimalCareersData(onlyEnrolled = 1)
            logger.info("testGetMinimalCareersData: enrolled result size=${enrolledResult.size}")

            val firstItem = defaultResult.first()
            logger.info("testGetMinimalCareersData: calling with userId filter")
            val byUserId = api.careers.getMinimalCareersData(userId = firstItem.userId)
            logger.info("testGetMinimalCareersData: filtered by userId result size=${byUserId.size}")
        } else {
            logger.info("testGetMinimalCareersData: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getMinimalCareersData()
            }
        }
    }

    @Test
    fun testVerifyEnrollmentCancellability() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testVerifyEnrollmentCancellability: calling verifyEnrollmentCancellability() with defaults")
            val defaultResult = api.careers.verifyEnrollmentCancellability()
            logger.info("testVerifyEnrollmentCancellability: default result=$defaultResult")
            assertNotNull(defaultResult, "verifyEnrollmentCancellability() should return non-null result")

            logger.info("testVerifyEnrollmentCancellability: calling with studentId from profile")
            val withStudentId = api.careers.verifyEnrollmentCancellability(studentId = studentProfile.studentId)
            logger.info("testVerifyEnrollmentCancellability: with studentId result=$withStudentId")
            assertNotNull(withStudentId, "Result with studentId should not be null")

            logger.info("testVerifyEnrollmentCancellability: calling with skipGraduationCheck=0")
            val withSkipCheck = api.careers.verifyEnrollmentCancellability(
                studentId = studentProfile.studentId,
                skipGraduationCheck = 0
            )
            logger.info("testVerifyEnrollmentCancellability: with skipGraduationCheck result=$withSkipCheck")
            assertNotNull(withSkipCheck, "Result with skipGraduationCheck should not be null")
        } else {
            logger.info("testVerifyEnrollmentCancellability: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.verifyEnrollmentCancellability()
            }
        }
    }

    @Test
    fun testVerifyEnrollment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testVerifyEnrollment: calling verifyEnrollment() with defaults")
            val defaultResult = api.careers.verifyEnrollment()
            logger.info("testVerifyEnrollment: default result=$defaultResult")
            assertNotNull(defaultResult, "verifyEnrollment() should return non-null result")

            val fiscalCode = session.fiscalCode
            if (fiscalCode != null) {
                logger.info("testVerifyEnrollment: calling with fiscalCode=$fiscalCode")
                val withFiscalCode = api.careers.verifyEnrollment(fiscalCode = fiscalCode)
                logger.info("testVerifyEnrollment: with fiscalCode result=$withFiscalCode")
                assertNotNull(withFiscalCode, "Result with fiscalCode should not be null")
            }

            logger.info("testVerifyEnrollment: calling with daysAfterGraduation=30")
            val withDays = api.careers.verifyEnrollment(daysAfterGraduation = 30)
            logger.info("testVerifyEnrollment: with daysAfterGraduation result=$withDays")
            assertNotNull(withDays, "Result with daysAfterGraduation should not be null")
        } else {
            logger.info("testVerifyEnrollment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.verifyEnrollment()
            }
        }
    }

    @Test
    fun testGetCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetCareerByStudent: calling getCareerByStudent(studentId=$studentId)")
            val result = api.careers.getCareerByStudent(studentId = studentId)
            logger.info("testGetCareerByStudent: result personId=${result.personId}, studentId=${result.studentId}, enrollmentId=${result.enrollmentId}")
            assertNotNull(result, "getCareerByStudent() should return non-null result")
            assertNotNull(result.studentId, "studentId should not be null")
            assertNotNull(result.personId, "personId should not be null")

            val careers = api.careers.getCareers()
            for (career in careers.take(3)) {
                val stuId = career.studentId ?: continue
                logger.info("testGetCareerByStudent: calling getCareerByStudent(studentId=$stuId)")
                val careerResult = api.careers.getCareerByStudent(studentId = stuId)
                logger.info("testGetCareerByStudent: result personId=${careerResult.personId}, studentId=${careerResult.studentId}")
                assertNotNull(careerResult.studentId, "studentId should not be null for stuId=$stuId")
                assertNotNull(careerResult.personId, "personId should not be null for stuId=$stuId")
            }
        } else {
            logger.info("testGetCareerByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getCareerByStudent(studentId = studentProfile.studentId)
            }
        }
    }

    @Test
    fun testGetAttendanceDays() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.UNKNOWN, Esse3PermissionLevel.ADMIN_OFFICE)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetAttendanceDays: calling getAttendanceDays(studentId=$studentId)")
            val result = api.careers.getAttendanceDays(studentId = studentId)
            logger.info("testGetAttendanceDays: courseOfStudyMandatoryAttendance=${result.courseOfStudyMandatoryAttendance}, daysToRecoverNumber=${result.daysToRecoverNumber}")
            assertNotNull(result, "getAttendanceDays() should return non-null result")

            val careers = api.careers.getCareers()
            for (career in careers.take(3)) {
                val stuId = career.studentId ?: continue
                logger.info("testGetAttendanceDays: calling getAttendanceDays(studentId=$stuId)")
                val attendanceResult = api.careers.getAttendanceDays(studentId = stuId)
                logger.info("testGetAttendanceDays: result for stuId=$stuId: courseOfStudyMandatoryAttendance=${attendanceResult.courseOfStudyMandatoryAttendance}")
                assertNotNull(attendanceResult, "Result should not be null for stuId=$stuId")
            }
        } else {
            logger.info("testGetAttendanceDays: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getAttendanceDays(studentId = studentProfile.studentId)
            }
        }
    }

    @Test
    fun testGetAnnualEnrollment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetAnnualEnrollment: calling getAnnualEnrollment(studentId=$studentId) with defaults")
            val defaultResult = api.careers.getAnnualEnrollment(studentId = studentId)
            logger.info("testGetAnnualEnrollment: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getAnnualEnrollment() should return non-null result")
            for (enrollment in defaultResult) {
                logger.info("testGetAnnualEnrollment: academicYearEnrollmentId=${enrollment.academicYearEnrollmentId}, courseYear=${enrollment.courseYear}, enrollmentTypeCode=${enrollment.enrollmentTypeCode}")
                assertNotNull(enrollment.academicYearEnrollmentId, "academicYearEnrollmentId should not be null")
            }

            logger.info("testGetAnnualEnrollment: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getAnnualEnrollment(studentId = studentId, start = 0, limit = 5)
            logger.info("testGetAnnualEnrollment: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetAnnualEnrollment: calling with lastEnrollmentFlag=1")
            val lastEnrollment = api.careers.getAnnualEnrollment(studentId = studentId, lastEnrollmentFlag = 1)
            logger.info("testGetAnnualEnrollment: lastEnrollmentFlag result size=${lastEnrollment.size}")

            if (defaultResult.isNotEmpty()) {
                val aaId = defaultResult.first().academicYearEnrollmentId
                logger.info("testGetAnnualEnrollment: calling with academicYear=$aaId")
                val byAcademicYear = api.careers.getAnnualEnrollment(studentId = studentId, academicYear = aaId)
                logger.info("testGetAnnualEnrollment: filtered by academicYear result size=${byAcademicYear.size}")
            }

            val careers = api.careers.getCareers()
            for (career in careers.take(3)) {
                val stuId = career.studentId ?: continue
                logger.info("testGetAnnualEnrollment: calling getAnnualEnrollment(studentId=$stuId)")
                val enrollments = api.careers.getAnnualEnrollment(studentId = stuId)
                logger.info("testGetAnnualEnrollment: result size=${enrollments.size} for studentId=$stuId")
                assertNotNull(enrollments, "Result should not be null for studentId=$stuId")
            }
        } else {
            logger.info("testGetAnnualEnrollment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getAnnualEnrollment(studentId = studentProfile.studentId)
            }
        }
    }

    @Test
    fun testGetEnrollmentClasses() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetEnrollmentClasses: fetching annual enrollments for studentId=$studentId")
            val enrollments = api.careers.getAnnualEnrollment(studentId = studentId)
            assertTrue(enrollments.isNotEmpty(), "Should have at least one enrollment to test enrollment classes")

            for (enrollment in enrollments.take(3)) {
                val aaIscrId = enrollment.academicYearEnrollmentId ?: continue
                logger.info("testGetEnrollmentClasses: calling getEnrollmentClasses(studentId=$studentId, academicYearEnrollmentId=$aaIscrId)")
                val defaultResult = api.careers.getEnrollmentClasses(
                    studentId = studentId,
                    academicYearEnrollmentId = aaIscrId
                )
                logger.info("testGetEnrollmentClasses: result size=${defaultResult.size} for aaIscrId=$aaIscrId")
                assertNotNull(defaultResult, "getEnrollmentClasses() should return non-null result")
                for (item in defaultResult) {
                    logger.info("testGetEnrollmentClasses: studentId=${item.studentId}, didacticTypeCode=${item.didacticTypeCode}, effectivePartialCode=${item.effectivePartialCode}")
                }
            }

            val firstEnrollment = enrollments.first()
            val firstAaIscrId = firstEnrollment.academicYearEnrollmentId!!
            logger.info("testGetEnrollmentClasses: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getEnrollmentClasses(
                studentId = studentId,
                academicYearEnrollmentId = firstAaIscrId,
                start = 0,
                limit = 5
            )
            logger.info("testGetEnrollmentClasses: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetEnrollmentClasses: user lacks required permissions, expecting Esse3Exception")
            val enrollments = try {
                api.careers.getAnnualEnrollment(studentId = studentProfile.studentId)
            } catch (_: Esse3Exception) {
                null
            }
            val aaIscrId = enrollments?.firstOrNull()?.academicYearEnrollmentId ?: 1L
            assertFailsWith<Esse3Exception> {
                api.careers.getEnrollmentClasses(
                    studentId = studentProfile.studentId,
                    academicYearEnrollmentId = aaIscrId
                )
            }
        }
    }

    @Test
    fun testGetCareerNotesByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetCareerNotesByStudent: calling getCareerNotesByStudent(studentId=$studentId) with defaults")
            val defaultResult = api.careers.getCareerNotesByStudent(studentId = studentId)
            logger.info("testGetCareerNotesByStudent: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getCareerNotesByStudent() should return non-null result")
            for (note in defaultResult) {
                logger.info("testGetCareerNotesByStudent: noteId=${note.noteId}, type=${note.type}, noteText=${note.noteText}")
            }

            logger.info("testGetCareerNotesByStudent: calling with blockingNote=1")
            val withBlocking = api.careers.getCareerNotesByStudent(studentId = studentId, blockingNote = 1)
            logger.info("testGetCareerNotesByStudent: blocking notes result size=${withBlocking.size}")

            logger.info("testGetCareerNotesByStudent: calling with webNote=1")
            val withWeb = api.careers.getCareerNotesByStudent(studentId = studentId, webNote = 1)
            logger.info("testGetCareerNotesByStudent: web notes result size=${withWeb.size}")

            val careers = api.careers.getCareers()
            for (career in careers.take(3)) {
                val stuId = career.studentId ?: continue
                logger.info("testGetCareerNotesByStudent: calling getCareerNotesByStudent(studentId=$stuId)")
                val notes = api.careers.getCareerNotesByStudent(studentId = stuId)
                logger.info("testGetCareerNotesByStudent: result size=${notes.size} for studentId=$stuId")
                assertNotNull(notes, "Result should not be null for studentId=$stuId")
            }
        } else {
            logger.info("testGetCareerNotesByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getCareerNotesByStudent(studentId = studentProfile.studentId)
            }
        }
    }

    @Test
    fun testGetEnrollmentInfo() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEnrollmentInfo: calling getEnrollmentInfo() with defaults")
            val defaultResult = api.careers.getEnrollmentInfo()
            logger.info("testGetEnrollmentInfo: default result=$defaultResult")
            assertNotNull(defaultResult, "getEnrollmentInfo() should return non-null result")

            logger.info("testGetEnrollmentInfo: calling with languageCode='it'")
            val withLang = api.careers.getEnrollmentInfo(languageCode = "it")
            logger.info("testGetEnrollmentInfo: with languageCode result=$withLang")
            assertNotNull(withLang, "Result with languageCode should not be null")

            logger.info("testGetEnrollmentInfo: calling with languageCode='en'")
            val withLangEn = api.careers.getEnrollmentInfo(languageCode = "en")
            logger.info("testGetEnrollmentInfo: with languageCode en result=$withLangEn")
            assertNotNull(withLangEn, "Result with languageCode en should not be null")
        } else {
            logger.info("testGetEnrollmentInfo: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getEnrollmentInfo()
            }
        }
    }

    @Test
    fun testGetAllEnrollmentInfo() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ADMIN_OFFICE, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAllEnrollmentInfo: calling getAllEnrollmentInfo() with defaults")
            val defaultResult = api.careers.getAllEnrollmentInfo()
            logger.info("testGetAllEnrollmentInfo: default result=$defaultResult")
            assertNotNull(defaultResult, "getAllEnrollmentInfo() should return non-null result")

            logger.info("testGetAllEnrollmentInfo: calling with languageCode='it'")
            val withLang = api.careers.getAllEnrollmentInfo(languageCode = "it")
            logger.info("testGetAllEnrollmentInfo: with languageCode result=$withLang")
            assertNotNull(withLang, "Result with languageCode should not be null")

            logger.info("testGetAllEnrollmentInfo: calling with languageCode='en'")
            val withLangEn = api.careers.getAllEnrollmentInfo(languageCode = "en")
            logger.info("testGetAllEnrollmentInfo: with languageCode en result=$withLangEn")
            assertNotNull(withLangEn, "Result with languageCode en should not be null")
        } else {
            logger.info("testGetAllEnrollmentInfo: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getAllEnrollmentInfo()
            }
        }
    }

    @Test
    fun testGetEnrollments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val enrollments = api.careers.getAnnualEnrollment(studentId = studentProfile.studentId)
            assertTrue(enrollments.isNotEmpty(), "Should have at least one enrollment to derive academicYearEnrollmentId")
            val aaIscrId = enrollments.first().academicYearEnrollmentId!!.toString()

            logger.info("testGetEnrollments: calling getEnrollments(academicYearEnrollmentId=$aaIscrId, includeSXH=0, includeCondition=0)")
            val defaultResult = api.careers.getEnrollments(
                academicYearEnrollmentId = aaIscrId,
                includeSXH = 0,
                includeCondition = 0
            )
            logger.info("testGetEnrollments: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getEnrollments() should return non-null result")
            for (item in defaultResult) {
                logger.info("testGetEnrollments: studentId=${item.studentId}, enrollmentTypeCode=${item.enrollmentTypeCode}, courseYear=${item.courseYear}")
            }

            logger.info("testGetEnrollments: calling with includeSXH=1, includeCondition=1")
            val withFlags = api.careers.getEnrollments(
                academicYearEnrollmentId = aaIscrId,
                includeSXH = 1,
                includeCondition = 1
            )
            logger.info("testGetEnrollments: with flags result size=${withFlags.size}")

            logger.info("testGetEnrollments: calling with pagination start=0, limit=5")
            val paginatedResult = api.careers.getEnrollments(
                academicYearEnrollmentId = aaIscrId,
                includeSXH = 0,
                includeCondition = 0,
                start = 0,
                limit = 5
            )
            logger.info("testGetEnrollments: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            for (enrollment in enrollments.take(3)) {
                val id = enrollment.academicYearEnrollmentId?.toString() ?: continue
                logger.info("testGetEnrollments: calling getEnrollments(academicYearEnrollmentId=$id)")
                val result = api.careers.getEnrollments(
                    academicYearEnrollmentId = id,
                    includeSXH = 0,
                    includeCondition = 0
                )
                logger.info("testGetEnrollments: result size=${result.size} for aaIscrId=$id")
                assertNotNull(result, "Result should not be null for aaIscrId=$id")
            }
        } else {
            logger.info("testGetEnrollments: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.getEnrollments(
                    academicYearEnrollmentId = "2024",
                    includeSXH = 0,
                    includeCondition = 0
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutGraduationWaiting() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutGraduationWaiting: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutGraduationWaiting: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putGraduationWaiting(
                    body = Esse3GraduationWaitingParameters(degreeAwardFlag = 0)
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutEnrollmentDateAndExemptionTypeByMatricola() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutEnrollmentDateAndExemptionTypeByMatricola: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutEnrollmentDateAndExemptionTypeByMatricola: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putEnrollmentDateAndExemptionTypeByMatricola(
                    matricola = studentProfile.matricola,
                    body = Esse3ExemptionTypeParameters(),
                    academicYear = 2024
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation - modifying career data cannot be safely rolled back")
    @Test
    fun testPutCareerByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCareerByStudent: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutCareerByStudent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putCareerByStudent(
                    studentId = studentProfile.studentId,
                    body = Esse3CareerParameters(protocolNumber = "")
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation - closing a career cannot be undone")
    @Test
    fun testCareerClosure() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testCareerClosure: skipped - irreversible mutating operation")
        } else {
            logger.info("testCareerClosure: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.careerClosure(
                    studentId = studentProfile.studentId,
                    body = Esse3CareerClosureParameters(statusReasonCode = "", closingDate = "")
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutEnrollmentDateAndExemptionTypeByStudentId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutEnrollmentDateAndExemptionTypeByStudentId: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutEnrollmentDateAndExemptionTypeByStudentId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putEnrollmentDateAndExemptionTypeByStudentId(
                    studentId = studentProfile.studentId,
                    body = Esse3ExemptionTypeParameters(),
                    academicYear = 2024
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutCanteenBand() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCanteenBand: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutCanteenBand: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putCanteenBand(
                    studentId = studentProfile.studentId,
                    body = Esse3CanteenBandParameters(),
                    academicYear = 2024
                )
            }
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutStudentTypeCode() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutStudentTypeCode: skipped - irreversible mutating operation")
        } else {
            logger.info("testPutStudentTypeCode: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careers.putStudentTypeCode(
                    studentId = studentProfile.studentId,
                    body = Esse3StudentTypeParameters(),
                    academicYear = 2024
                )
            }
        }
    }
}
