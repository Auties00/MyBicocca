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

class Esse3LogisticsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3LogisticsApiTest::class.java.name)
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

    @Disabled("easystaff integration is disabled")
    @Test
    fun testGetEasystaffLogistics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetEasystaffLogistics: calling getEasystaffLogistics() with academicYearOfferId=$currentYear and defaults")
            val defaultResult = api.logistics.getEasystaffLogistics(academicYearOfferId = currentYear)
            logger.info("testGetEasystaffLogistics: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getEasystaffLogistics() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetEasystaffLogistics: activityLogId=${item.activityLogId}, academicYearOfferId=${item.academicYearOfferId}, partialCode=${item.partialCode}, invoicePartialCode=${item.invoicePartialCode}")
            }

            val firstItem = defaultResult.first()
            val firstActivity = firstItem.activity.firstOrNull()
            if (firstActivity?.departmentCode != null) {
                logger.info("testGetEasystaffLogistics: calling getEasystaffLogistics() with departmentCode=${firstActivity.departmentCode}")
                val filteredByDept = api.logistics.getEasystaffLogistics(
                    academicYearOfferId = currentYear,
                    departmentCode = firstActivity.departmentCode
                )
                logger.info("testGetEasystaffLogistics: filtered by departmentCode result size=${filteredByDept.size}")
                assertTrue(filteredByDept.isNotEmpty(), "Filtered by departmentCode should return non-empty list")
            }

            if (firstActivity?.courseOfStudyCode != null) {
                logger.info("testGetEasystaffLogistics: calling getEasystaffLogistics() with courseOfStudyList=${firstActivity.courseOfStudyCode}")
                val filteredByCds = api.logistics.getEasystaffLogistics(
                    academicYearOfferId = currentYear,
                    courseOfStudyList = firstActivity.courseOfStudyCode
                )
                logger.info("testGetEasystaffLogistics: filtered by courseOfStudyList result size=${filteredByCds.size}")
                assertTrue(filteredByCds.isNotEmpty(), "Filtered by courseOfStudyList should return non-empty list")
            }
        } else {
            logger.info("testGetEasystaffLogistics: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getEasystaffLogistics(academicYearOfferId = java.time.Year.now().value)
            }
            logger.info("testGetEasystaffLogistics: Esse3Exception thrown as expected")
        }
    }

    @Disabled("easystaff integration is disabled")
    @Test
    fun testGetEasystaffStructure() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetEasystaffStructure: calling getEasystaffStructure() with academicYearOfferId=$currentYear and defaults")
            val defaultResult = api.logistics.getEasystaffStructure(academicYearOfferId = currentYear)
            logger.info("testGetEasystaffStructure: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getEasystaffStructure() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetEasystaffStructure: courseOfStudyCode=${item.courseOfStudyCode}, courseOfStudyDescription=${item.courseOfStudyDescription}, departmentCode=${item.departmentCode}, courseTypeCode=${item.courseTypeCode}")
            }

            val firstItem = defaultResult.first()
            if (firstItem.departmentCode != null) {
                logger.info("testGetEasystaffStructure: calling getEasystaffStructure() with departmentCode=${firstItem.departmentCode}")
                val filteredByDept = api.logistics.getEasystaffStructure(
                    academicYearOfferId = currentYear,
                    departmentCode = firstItem.departmentCode
                )
                logger.info("testGetEasystaffStructure: filtered by departmentCode result size=${filteredByDept.size}")
                assertTrue(filteredByDept.isNotEmpty(), "Filtered by departmentCode should return non-empty list")
            }

            if (firstItem.courseOfStudyCode != null) {
                logger.info("testGetEasystaffStructure: calling getEasystaffStructure() with courseOfStudyList=${firstItem.courseOfStudyCode}")
                val filteredByCds = api.logistics.getEasystaffStructure(
                    academicYearOfferId = currentYear,
                    courseOfStudyList = firstItem.courseOfStudyCode
                )
                logger.info("testGetEasystaffStructure: filtered by courseOfStudyList result size=${filteredByCds.size}")
                assertTrue(filteredByCds.isNotEmpty(), "Filtered by courseOfStudyList should return non-empty list")
            }
        } else {
            logger.info("testGetEasystaffStructure: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getEasystaffStructure(academicYearOfferId = java.time.Year.now().value)
            }
            logger.info("testGetEasystaffStructure: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLogistics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetLogistics: calling getLogistics() with defaults")
            val defaultResult = api.logistics.getLogistics()
            logger.info("testGetLogistics: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLogistics() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetLogistics: partitionKey.activityLogId=${item.partitionKey.activityLogId}, siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, startDate=${item.startDate}, endDate=${item.endDate}")
            }

            logger.info("testGetLogistics: calling getLogistics() with pagination start=0, limit=5")
            val paginatedResult = api.logistics.getLogistics(start = 0, limit = 5)
            logger.info("testGetLogistics: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            logger.info("testGetLogistics: calling getLogistics() with academicYearOfferId=$currentYear")
            val filteredByYear = api.logistics.getLogistics(academicYearOfferId = currentYear)
            logger.info("testGetLogistics: filtered by academicYearOfferId result size=${filteredByYear.size}")

            val firstItem = defaultResult.first()
            if (firstItem.teachingLanguageCode != null) {
                logger.info("testGetLogistics: calling getLogistics() with teachingLanguageCode=${firstItem.teachingLanguageCode}")
                val filteredByLang = api.logistics.getLogistics(
                    teachingLanguageCode = firstItem.teachingLanguageCode,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLogistics: filtered by teachingLanguageCode result size=${filteredByLang.size}")
                assertTrue(filteredByLang.isNotEmpty(), "Filtered by teachingLanguageCode should return non-empty list")
            }

            if (firstItem.siteDescription != null) {
                logger.info("testGetLogistics: calling getLogistics() with siteDescription=${firstItem.siteDescription}")
                val filteredBySite = api.logistics.getLogistics(
                    siteDescription = firstItem.siteDescription,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLogistics: filtered by siteDescription result size=${filteredBySite.size}")
                assertTrue(filteredBySite.isNotEmpty(), "Filtered by siteDescription should return non-empty list")
            }

            logger.info("testGetLogistics: calling getLogistics() with order param")
            val orderedResult = api.logistics.getLogistics(order = "dataModLog", start = 0, limit = 5)
            logger.info("testGetLogistics: ordered result size=${orderedResult.size}")
        } else {
            logger.info("testGetLogistics: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getLogistics()
            }
            logger.info("testGetLogistics: Esse3Exception thrown as expected")
        }
    }

    @Disabled("easystaff integration is disabled")
    @Test
    fun testGetCancellableCoverage() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCancellableCoverage: calling getCancellableCoverage() with defaults")
            val result = api.logistics.getCancellableCoverage()
            logger.info("testGetCancellableCoverage: returnCode=${result.returnCode}, message=${result.message}")
            assertNotNull(result, "getCancellableCoverage() should return a non-null result")

            val currentYear = java.time.Year.now().value
            logger.info("testGetCancellableCoverage: calling getCancellableCoverage() with academicYearOfferId=$currentYear")
            val filteredByYear = api.logistics.getCancellableCoverage(academicYearOfferId = currentYear)
            logger.info("testGetCancellableCoverage: filtered returnCode=${filteredByYear.returnCode}, message=${filteredByYear.message}")
        } else {
            logger.info("testGetCancellableCoverage: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getCancellableCoverage()
            }
            logger.info("testGetCancellableCoverage: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutSyllabusTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutSyllabusTeachingActivity: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutSyllabusTeachingActivity: user lacks TEACHER/TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.putSyllabusTeachingActivity(
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3SyllabusActivityPatch(
                        academicYearOfferId = java.time.Year.now().value.toLong(),
                        courseOfStudyCode = "TEST",
                        academicYearOrderId = 1,
                        studyPlanCode = "TEST",
                        activityCode = "TEST"
                    )
                )
            }
            logger.info("testPutSyllabusTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTeachingActivityLogWithSyllabus() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetTeachingActivityLogWithSyllabus: fetching logistics to derive an activityLogId")
            val logistics = api.logistics.getLogistics(start = 0, limit = 5)
            assertTrue(logistics.isNotEmpty(), "getLogistics() should return non-empty list to derive activityLogId")
            val activityLogId = logistics.first().partitionKey.activityLogId
            assertNotNull(activityLogId, "activityLogId should not be null")
            logger.info("testGetTeachingActivityLogWithSyllabus: derived activityLogId=$activityLogId")

            logger.info("testGetTeachingActivityLogWithSyllabus: calling getTeachingActivityLogWithSyllabus() with activityLogId=$activityLogId and defaults")
            val defaultResult = api.logistics.getTeachingActivityLogWithSyllabus(activityLogId = activityLogId)
            logger.info("testGetTeachingActivityLogWithSyllabus: default result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetTeachingActivityLogWithSyllabus: partitionKey.activityLogId=${item.partitionKey?.activityLogId}, siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, syllabusCount=${item.syllabusTeachingActivity.size}")
            }

            val currentYear = java.time.Year.now().value
            logger.info("testGetTeachingActivityLogWithSyllabus: calling with academicYearOfferId=$currentYear")
            val filteredByYear = api.logistics.getTeachingActivityLogWithSyllabus(
                activityLogId = activityLogId,
                academicYearOfferId = currentYear
            )
            logger.info("testGetTeachingActivityLogWithSyllabus: filtered by year result size=${filteredByYear.size}")
        } else {
            logger.info("testGetTeachingActivityLogWithSyllabus: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getTeachingActivityLogWithSyllabus(activityLogId = 1L)
            }
            logger.info("testGetTeachingActivityLogWithSyllabus: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTeachingUnitLogWithDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetTeachingUnitLogWithDetails: fetching logistics to derive an activityLogId")
            val logistics = api.logistics.getLogistics(start = 0, limit = 5)
            assertTrue(logistics.isNotEmpty(), "getLogistics() should return non-empty list to derive activityLogId")
            val activityLogId = logistics.first().partitionKey.activityLogId
            assertNotNull(activityLogId, "activityLogId should not be null")
            logger.info("testGetTeachingUnitLogWithDetails: derived activityLogId=$activityLogId")

            logger.info("testGetTeachingUnitLogWithDetails: calling getTeachingUnitLogWithDetails() with activityLogId=$activityLogId and defaults")
            val defaultResult = api.logistics.getTeachingUnitLogWithDetails(activityLogId = activityLogId)
            logger.info("testGetTeachingUnitLogWithDetails: default result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetTeachingUnitLogWithDetails: teachingUnitLogId=${item.teachingUnitLogId}, teachingLoadCount=${item.teachingLoad.size}, syllabusCount=${item.syllabusTeachingUnit.size}")
            }

            val currentYear = java.time.Year.now().value
            logger.info("testGetTeachingUnitLogWithDetails: calling with academicYearOfferId=$currentYear")
            val filteredByYear = api.logistics.getTeachingUnitLogWithDetails(
                activityLogId = activityLogId,
                academicYearOfferId = currentYear
            )
            logger.info("testGetTeachingUnitLogWithDetails: filtered by year result size=${filteredByYear.size}")
        } else {
            logger.info("testGetTeachingUnitLogWithDetails: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getTeachingUnitLogWithDetails(activityLogId = 1L)
            }
            logger.info("testGetTeachingUnitLogWithDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullLogistics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetFullLogistics: fetching logistics to derive an activityLogId")
            val logistics = api.logistics.getLogistics(start = 0, limit = 5)
            assertTrue(logistics.isNotEmpty(), "getLogistics() should return non-empty list to derive activityLogId")
            val activityLogId = logistics.first().partitionKey.activityLogId
            assertNotNull(activityLogId, "activityLogId should not be null")
            logger.info("testGetFullLogistics: derived activityLogId=$activityLogId")

            logger.info("testGetFullLogistics: calling getFullLogistics() with activityLogId=$activityLogId and defaults")
            val defaultResult = api.logistics.getFullLogistics(activityLogId = activityLogId)
            logger.info("testGetFullLogistics: default result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetFullLogistics: siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, unitLogCount=${item.teachingUnitLogWithDetails.size}, syllabusCount=${item.syllabusTeachingActivity.size}")
            }

            val currentYear = java.time.Year.now().value
            logger.info("testGetFullLogistics: calling with academicYearOfferId=$currentYear")
            val filteredByYear = api.logistics.getFullLogistics(
                activityLogId = activityLogId,
                academicYearOfferId = currentYear
            )
            logger.info("testGetFullLogistics: filtered by year result size=${filteredByYear.size}")
        } else {
            logger.info("testGetFullLogistics: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getFullLogistics(activityLogId = 1L)
            }
            logger.info("testGetFullLogistics: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullLogisticsByTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetFullLogisticsByTeachingActivity: calling getFullLogisticsByTeachingActivity() with academicYearOfferId=$currentYear and defaults")
            val defaultResult = api.logistics.getFullLogisticsByTeachingActivity(academicYearOfferId = currentYear)
            logger.info("testGetFullLogisticsByTeachingActivity: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getFullLogisticsByTeachingActivity() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetFullLogisticsByTeachingActivity: siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, unitLogCount=${item.teachingUnitLogWithDetails.size}")
            }

            logger.info("testGetFullLogisticsByTeachingActivity: calling with teachingActivityPublicationFlag=1")
            val filteredByFlag = api.logistics.getFullLogisticsByTeachingActivity(
                academicYearOfferId = currentYear,
                teachingActivityPublicationFlag = 1
            )
            logger.info("testGetFullLogisticsByTeachingActivity: filtered by publication flag result size=${filteredByFlag.size}")
        } else {
            logger.info("testGetFullLogisticsByTeachingActivity: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getFullLogisticsByTeachingActivity(academicYearOfferId = java.time.Year.now().value)
            }
            logger.info("testGetFullLogisticsByTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLogisticsByLecturer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLogisticsByLecturer: calling getLogisticsByLecturer() with defaults")
            val defaultResult = api.logistics.getLogisticsByLecturer()
            logger.info("testGetLogisticsByLecturer: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLogisticsByLecturer() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetLogisticsByLecturer: lecturerId=${item.lecturerId}, courseOfStudyCode=${item.courseOfStudyCode}, activityCode=${item.activityCode}, teachingUnitCode=${item.teachingUnitCode}")
            }

            logger.info("testGetLogisticsByLecturer: calling getLogisticsByLecturer() with pagination start=0, limit=5")
            val paginatedResult = api.logistics.getLogisticsByLecturer(start = 0, limit = 5)
            logger.info("testGetLogisticsByLecturer: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            val firstItem = defaultResult.first()
            if (firstItem.lecturerMatricola != null) {
                logger.info("testGetLogisticsByLecturer: calling getLogisticsByLecturer() with lecturerMatricola=${firstItem.lecturerMatricola}")
                val filteredByMatricola = api.logistics.getLogisticsByLecturer(
                    lecturerMatricola = firstItem.lecturerMatricola,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLogisticsByLecturer: filtered by matricola result size=${filteredByMatricola.size}")
                assertTrue(filteredByMatricola.isNotEmpty(), "Filtered by lecturerMatricola should return non-empty list")
            }

            if (firstItem.lecturerSurname != null) {
                logger.info("testGetLogisticsByLecturer: calling getLogisticsByLecturer() with lecturerSurname=${firstItem.lecturerSurname}")
                val filteredBySurname = api.logistics.getLogisticsByLecturer(
                    lecturerSurname = firstItem.lecturerSurname,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLogisticsByLecturer: filtered by surname result size=${filteredBySurname.size}")
                assertTrue(filteredBySurname.isNotEmpty(), "Filtered by lecturerSurname should return non-empty list")
            }

            if (firstItem.academicYearOfferId != null) {
                logger.info("testGetLogisticsByLecturer: calling getLogisticsByLecturer() with academicYearOfferId=${firstItem.academicYearOfferId}")
                val filteredByYear = api.logistics.getLogisticsByLecturer(
                    academicYearOfferId = firstItem.academicYearOfferId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLogisticsByLecturer: filtered by academicYearOfferId result size=${filteredByYear.size}")
                assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearOfferId should return non-empty list")
            }
        } else {
            logger.info("testGetLogisticsByLecturer: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getLogisticsByLecturer()
            }
            logger.info("testGetLogisticsByLecturer: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullLogisticsByOdStreamed() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetFullLogisticsByOdStreamed: fetching logistics by teaching activity to derive courseOfStudyOfferId")
            val logisticsByAd = api.logistics.getFullLogisticsByTeachingActivity(academicYearOfferId = currentYear)
            assertTrue(logisticsByAd.isNotEmpty(), "getFullLogisticsByTeachingActivity() should return non-empty list to derive courseOfStudyOfferId")
            val firstItem = logisticsByAd.first()
            val partitionKey = firstItem.partitionKey
            assertNotNull(partitionKey, "partitionKey should not be null")
            val activityLogId = partitionKey.activityLogId
            assertNotNull(activityLogId, "activityLogId should not be null")

            val physicalKey = firstItem.physicalTeachingActivityKey
            val courseOfStudyOfferId = physicalKey?.courseOfStudyId ?: studentProfile.degreeCourseId
            logger.info("testGetFullLogisticsByOdStreamed: using academicYearOfferId=$currentYear, courseOfStudyOfferId=$courseOfStudyOfferId")

            logger.info("testGetFullLogisticsByOdStreamed: calling getFullLogisticsByOdStreamed() with defaults")
            val defaultResult = api.logistics.getFullLogisticsByOdStreamed(
                academicYearOfferId = currentYear,
                courseOfStudyOfferId = courseOfStudyOfferId
            )
            logger.info("testGetFullLogisticsByOdStreamed: result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetFullLogisticsByOdStreamed: siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, unitLogCount=${item.teachingUnitLogWithDetails.size}")
            }
        } else {
            logger.info("testGetFullLogisticsByOdStreamed: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getFullLogisticsByOdStreamed(
                    academicYearOfferId = java.time.Year.now().value,
                    courseOfStudyOfferId = studentProfile.degreeCourseId
                )
            }
            logger.info("testGetFullLogisticsByOdStreamed: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullLogisticsByOd() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val currentYear = java.time.Year.now().value
            logger.info("testGetFullLogisticsByOd: fetching logistics by teaching activity to derive courseOfStudyOfferId")
            val logisticsByAd = api.logistics.getFullLogisticsByTeachingActivity(academicYearOfferId = currentYear)
            assertTrue(logisticsByAd.isNotEmpty(), "getFullLogisticsByTeachingActivity() should return non-empty list to derive courseOfStudyOfferId")
            val firstItem = logisticsByAd.first()
            val physicalKey = firstItem.physicalTeachingActivityKey
            val courseOfStudyOfferId = physicalKey?.courseOfStudyId ?: studentProfile.degreeCourseId
            logger.info("testGetFullLogisticsByOd: using academicYearOfferId=$currentYear, courseOfStudyOfferId=$courseOfStudyOfferId")

            logger.info("testGetFullLogisticsByOd: calling getFullLogisticsByOd() with defaults")
            val defaultResult = api.logistics.getFullLogisticsByOd(
                academicYearOfferId = currentYear,
                courseOfStudyOfferId = courseOfStudyOfferId
            )
            logger.info("testGetFullLogisticsByOd: result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetFullLogisticsByOd: siteDescription=${item.siteDescription}, teachingLanguageCode=${item.teachingLanguageCode}, unitLogCount=${item.teachingUnitLogWithDetails.size}")
            }
        } else {
            logger.info("testGetFullLogisticsByOd: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getFullLogisticsByOd(
                    academicYearOfferId = java.time.Year.now().value,
                    courseOfStudyOfferId = studentProfile.degreeCourseId
                )
            }
            logger.info("testGetFullLogisticsByOd: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDeletedLogistics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDeletedLogistics: calling getDeletedLogistics() with defaults")
            val defaultResult = api.logistics.getDeletedLogistics()
            logger.info("testGetDeletedLogistics: default result size=${defaultResult.size}")
            for (item in defaultResult.take(3)) {
                logger.info("testGetDeletedLogistics: activityLogId=${item.activityLogId}, logModificationDate=${item.logModificationDate}")
            }

            logger.info("testGetDeletedLogistics: calling getDeletedLogistics() with pagination start=0, limit=5")
            val paginatedResult = api.logistics.getDeletedLogistics(start = 0, limit = 5)
            logger.info("testGetDeletedLogistics: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetDeletedLogistics: calling getDeletedLogistics() with order param")
            val orderedResult = api.logistics.getDeletedLogistics(order = "dataModLog", start = 0, limit = 5)
            logger.info("testGetDeletedLogistics: ordered result size=${orderedResult.size}")
        } else {
            logger.info("testGetDeletedLogistics: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getDeletedLogistics()
            }
            logger.info("testGetDeletedLogistics: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAllClassrooms() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetAllClassrooms: calling getAllClassrooms() with defaults")
            val defaultResult = api.logistics.getAllClassrooms()
            logger.info("testGetAllClassrooms: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getAllClassrooms() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetAllClassrooms: classroomId=${item.classroomId}, classroomCode=${item.classroomCode}, buildingId=${item.buildingId}, buildingCode=${item.buildingCode}, classroomDescription=${item.classroomDescription}, capacity=${item.capacity}")
            }

            val firstItem = defaultResult.first()
            logger.info("testGetAllClassrooms: calling getAllClassrooms() with classroomCode=${firstItem.classroomCode}")
            val filteredByCode = api.logistics.getAllClassrooms(classroomCode = firstItem.classroomCode)
            logger.info("testGetAllClassrooms: filtered by classroomCode result size=${filteredByCode.size}")
            assertTrue(filteredByCode.isNotEmpty(), "Filtered by classroomCode should return non-empty list")

            logger.info("testGetAllClassrooms: calling getAllClassrooms() with externalClassroomCode=${firstItem.externalClassroomCode}")
            val filteredByExtCode = api.logistics.getAllClassrooms(externalClassroomCode = firstItem.externalClassroomCode)
            logger.info("testGetAllClassrooms: filtered by externalClassroomCode result size=${filteredByExtCode.size}")
            assertTrue(filteredByExtCode.isNotEmpty(), "Filtered by externalClassroomCode should return non-empty list")

            logger.info("testGetAllClassrooms: calling getAllClassrooms() with order param")
            val orderedResult = api.logistics.getAllClassrooms(order = "aulaCod")
            logger.info("testGetAllClassrooms: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should not be empty")
        } else {
            logger.info("testGetAllClassrooms: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getAllClassrooms()
            }
            logger.info("testGetAllClassrooms: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBuildings() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBuildings: calling getBuildings() with defaults")
            val defaultResult = api.logistics.getBuildings()
            logger.info("testGetBuildings: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getBuildings() default should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetBuildings: buildingId=${item.buildingId}, buildingCode=${item.buildingCode}, buildingDescription=${item.buildingDescription}, street=${item.street}, municipalityDescription=${item.municipalityDescription}")
            }

            logger.info("testGetBuildings: calling getBuildings() with order param")
            val orderedResult = api.logistics.getBuildings(order = "edificioCod")
            logger.info("testGetBuildings: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should not be empty")
        } else {
            logger.info("testGetBuildings: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getBuildings()
            }
            logger.info("testGetBuildings: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBuilding() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBuilding: fetching buildings list to derive a buildingId")
            val buildings = api.logistics.getBuildings()
            assertTrue(buildings.isNotEmpty(), "getBuildings() should return non-empty list to derive buildingId")
            val buildingId = buildings.first().buildingId
            assertNotNull(buildingId, "buildingId should not be null")
            logger.info("testGetBuilding: derived buildingId=$buildingId")

            logger.info("testGetBuilding: calling getBuilding() with buildingId=$buildingId and defaults")
            val result = api.logistics.getBuilding(buildingId = buildingId)
            logger.info("testGetBuilding: buildingId=${result.buildingId}, buildingCode=${result.buildingCode}, buildingDescription=${result.buildingDescription}, street=${result.street}, municipalityDescription=${result.municipalityDescription}, webUrl=${result.webUrl}")
            assertNotNull(result.buildingId, "Building buildingId should not be null")
            assertNotNull(result.buildingCode, "Building buildingCode should not be null")
        } else {
            logger.info("testGetBuilding: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getBuilding(buildingId = 1L)
            }
            logger.info("testGetBuilding: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetClassrooms() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetClassrooms: fetching buildings list to derive a buildingId")
            val buildings = api.logistics.getBuildings()
            assertTrue(buildings.isNotEmpty(), "getBuildings() should return non-empty list to derive buildingId")
            val buildingId = buildings.first().buildingId
            assertNotNull(buildingId, "buildingId should not be null")
            logger.info("testGetClassrooms: derived buildingId=$buildingId")

            logger.info("testGetClassrooms: calling getClassrooms() with buildingId=$buildingId and defaults")
            val defaultResult = api.logistics.getClassrooms(buildingId = buildingId)
            logger.info("testGetClassrooms: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getClassrooms() should return non-empty list")
            for (item in defaultResult.take(3)) {
                logger.info("testGetClassrooms: classroomId=${item.classroomId}, classroomCode=${item.classroomCode}, classroomDescription=${item.classroomDescription}, capacity=${item.capacity}, buildingCode=${item.buildingCode}")
            }

            logger.info("testGetClassrooms: calling getClassrooms() with order param")
            val orderedResult = api.logistics.getClassrooms(buildingId = buildingId, order = "aulaCod")
            logger.info("testGetClassrooms: ordered result size=${orderedResult.size}")
        } else {
            logger.info("testGetClassrooms: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getClassrooms(buildingId = 1L)
            }
            logger.info("testGetClassrooms: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetClassroom() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetClassroom: fetching buildings list to derive a buildingId")
            val buildings = api.logistics.getBuildings()
            assertTrue(buildings.isNotEmpty(), "getBuildings() should return non-empty list to derive buildingId")
            val buildingId = buildings.first().buildingId
            assertNotNull(buildingId, "buildingId should not be null")
            logger.info("testGetClassroom: derived buildingId=$buildingId")

            logger.info("testGetClassroom: fetching classrooms to derive a classroomId")
            val classrooms = api.logistics.getClassrooms(buildingId = buildingId)
            assertTrue(classrooms.isNotEmpty(), "getClassrooms() should return non-empty list to derive classroomId")
            val classroomId = classrooms.first().classroomId
            assertNotNull(classroomId, "classroomId should not be null")
            logger.info("testGetClassroom: derived classroomId=$classroomId")

            logger.info("testGetClassroom: calling getClassroom() with classroomId=$classroomId, buildingId=$buildingId and defaults")
            val result = api.logistics.getClassroom(classroomId = classroomId, buildingId = buildingId)
            logger.info("testGetClassroom: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getClassroom() should return non-empty list")
            val firstClassroom = result.first()
            logger.info("testGetClassroom: classroomId=${firstClassroom.classroomId}, classroomCode=${firstClassroom.classroomCode}, classroomDescription=${firstClassroom.classroomDescription}, capacity=${firstClassroom.capacity}, authorizationFlag=${firstClassroom.authorizationFlag}")
            assertNotNull(firstClassroom.classroomId, "Classroom classroomId should not be null")
        } else {
            logger.info("testGetClassroom: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.getClassroom(classroomId = 1L, buildingId = 1L)
            }
            logger.info("testGetClassroom: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testSyncSystemLog() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSyncSystemLog: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testSyncSystemLog: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.logistics.syncSystemLog()
            }
            logger.info("testSyncSystemLog: Esse3Exception thrown as expected")
        }
    }
}
