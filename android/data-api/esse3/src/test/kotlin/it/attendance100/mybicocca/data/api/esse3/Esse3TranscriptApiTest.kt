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

class Esse3TranscriptApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3TranscriptApiTest::class.java.name)
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
    fun testGetCareerSegments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegments: calling getCareerSegments() with defaults")
            val defaultResult = api.transcript.getCareerSegments()
            logger.info("testGetCareerSegments: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCareerSegments() default should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetCareerSegments: matId=${item.matId}, studentId=${item.studentId}, courseOfStudyCode=${item.courseOfStudyCode}, studentStatusCode=${item.studentStatusCode}, enrollmentId=${item.enrollmentId}")
            }

            logger.info("testGetCareerSegments: calling getCareerSegments() with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getCareerSegments(start = 0, limit = 5)
            logger.info("testGetCareerSegments: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            val firstItem = defaultResult.first()
            if (firstItem.enrollmentId != null) {
                logger.info("testGetCareerSegments: calling getCareerSegments() with enrollmentId=${firstItem.enrollmentId}")
                val filteredByEnrollment = api.transcript.getCareerSegments(matricola = firstItem.enrollmentId.toString())
                logger.info("testGetCareerSegments: filtered by enrollmentId size=${filteredByEnrollment.size}")
                assertTrue(filteredByEnrollment.isNotEmpty(), "Filtered by enrollmentId should return non-empty list")
            }

            if (firstItem.courseOfStudyId != null) {
                logger.info("testGetCareerSegments: calling getCareerSegments() with courseOfStudyStudentId=${firstItem.courseOfStudyId}")
                val filteredByCds = api.transcript.getCareerSegments(courseOfStudyStudentId = firstItem.courseOfStudyId)
                logger.info("testGetCareerSegments: filtered by courseOfStudyStudentId size=${filteredByCds.size}")
                assertTrue(filteredByCds.isNotEmpty(), "Filtered by courseOfStudyStudentId should return non-empty list")
            }

            if (firstItem.studentStatusCode != null) {
                logger.info("testGetCareerSegments: calling getCareerSegments() with studentStatusCode=${firstItem.studentStatusCode}")
                val filteredByStatus = api.transcript.getCareerSegments(studentStatusCode = firstItem.studentStatusCode)
                logger.info("testGetCareerSegments: filtered by studentStatusCode size=${filteredByStatus.size}")
                assertTrue(filteredByStatus.isNotEmpty(), "Filtered by studentStatusCode should return non-empty list")
            }
        } else {
            logger.info("testGetCareerSegments: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getCareerSegments()
            }
            logger.info("testGetCareerSegments: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentClassByTeachingActivityStudyPlanOrderNew() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: calling with required params")
            val result = api.transcript.getStudentClassByTeachingActivityStudyPlanOrderNew(
                academicYearOfferLogId = 2024,
                teachingActivityLogCode = "TEST",
                courseOfStudyLogCode = "TEST",
                academicYearOrderLogId = 1,
                studyPlanLogCode = "GEN"
            )
            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: result size=${result.size}")
            for (item in result) {
                logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: personId=${item.personId}, matricola=${item.matricola}, activityChoiceId=${item.activityChoiceId}, choiceStatusCode=${item.choiceStatusCode}")
            }

            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: calling with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getStudentClassByTeachingActivityStudyPlanOrderNew(
                academicYearOfferLogId = 2024,
                teachingActivityLogCode = "TEST",
                courseOfStudyLogCode = "TEST",
                academicYearOrderLogId = 1,
                studyPlanLogCode = "GEN",
                start = 0,
                limit = 5
            )
            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getStudentClassByTeachingActivityStudyPlanOrderNew(
                    academicYearOfferLogId = 2024,
                    teachingActivityLogCode = "TEST",
                    courseOfStudyLogCode = "TEST",
                    academicYearOrderLogId = 1,
                    studyPlanLogCode = "GEN"
                )
            }
            logger.info("testGetStudentClassByTeachingActivityStudyPlanOrderNew: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentClassByTeachingActivityLogId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentClassByTeachingActivityLogId: calling with activityLogId=1")
            val result = api.transcript.getStudentClassByTeachingActivityLogId(activityLogId = 1)
            logger.info("testGetStudentClassByTeachingActivityLogId: result size=${result.size}")
            for (item in result) {
                logger.info("testGetStudentClassByTeachingActivityLogId: personId=${item.personId}, matricola=${item.matricola}, activityChoiceId=${item.activityChoiceId}")
            }

            logger.info("testGetStudentClassByTeachingActivityLogId: calling with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getStudentClassByTeachingActivityLogId(
                activityLogId = 1,
                start = 0,
                limit = 5
            )
            logger.info("testGetStudentClassByTeachingActivityLogId: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetStudentClassByTeachingActivityLogId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getStudentClassByTeachingActivityLogId(activityLogId = 1)
            }
            logger.info("testGetStudentClassByTeachingActivityLogId: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutMassiveAttendance() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutMassiveAttendance: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutMassiveAttendance: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.putMassiveAttendance(
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3BulkAttendanceParameters(
                        activityCode = "TEST"
                    )
                )
            }
            logger.info("testPutMassiveAttendance: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutMassiveDetections() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutMassiveDetections: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutMassiveDetections: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.putMassiveDetections(
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3BulkPresenceReleaseParameters(
                        partialCode = "TEST",
                        domicilePartialCode = "TEST",
                        invoicePartialCode = "TEST",
                        academicYearOfferId = 2024,
                        studyPlanCode = "GEN",
                        academicYearOrderId = 1,
                        courseOfStudyCode = "TEST",
                        activityCode = "TEST",
                        lecturerFiscalCodeCheck = "TEST",
                        lecturerFiscalCodeDetection = "TEST",
                        operation = "TEST"
                    )
                )
            }
            logger.info("testPutMassiveDetections: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment to test getCareerSegment()")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetCareerSegment: using matId=$matId")

            logger.info("testGetCareerSegment: calling getCareerSegment(matId=$matId) with defaults")
            val result = api.transcript.getCareerSegment(matId = matId)
            logger.info("testGetCareerSegment: matId=${result.matId}, studentId=${result.studentId}, courseOfStudyCode=${result.courseOfStudyCode}, studentStatusCode=${result.studentStatusCode}")
            assertNotNull(result.matId, "matId should not be null")

            logger.info("testGetCareerSegment: calling getCareerSegment() with optionalFields")
            val resultWithOptFields = api.transcript.getCareerSegment(
                matId = matId,
                optionalFields = "facCod"
            )
            logger.info("testGetCareerSegment: with optionalFields matId=${resultWithOptFields.matId}, facultyCode=${resultWithOptFields.facultyCode}")
        } else {
            logger.info("testGetCareerSegment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getCareerSegment(matId = studentProfile.matId)
            }
            logger.info("testGetCareerSegment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookExamCalls() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookExamCalls: using matId=$matId")

            logger.info("testGetRecordBookExamCalls: calling getRecordBookExamCalls(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookExamCalls(matId = matId)
            logger.info("testGetRecordBookExamCalls: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookExamCalls: examCallId=${item.examCallId}, activityDescription=${item.activityDescription}, callStartDate=${item.callStartDate}, callOpeningState=${item.callOpeningState}, activityChoiceId=${item.activityChoiceId}")
            }

            logger.info("testGetRecordBookExamCalls: calling getRecordBookExamCalls() with actorCode=STU")
            val resultWithActor = api.transcript.getRecordBookExamCalls(
                matId = matId,
                actorCode = "STU"
            )
            logger.info("testGetRecordBookExamCalls: result with actorCode size=${resultWithActor.size}")
        } else {
            logger.info("testGetRecordBookExamCalls: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookExamCalls(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookExamCalls: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookAverages() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookAverages: using matId=$matId")

            logger.info("testGetRecordBookAverages: calling getRecordBookAverages(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookAverages(matId = matId)
            logger.info("testGetRecordBookAverages: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookAverages: base=${item.base}, averageTypeCode=${item.averageTypeCode}, average=${item.average}, okType=${item.okType}")
            }
        } else {
            logger.info("testGetRecordBookAverages: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookAverages(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookAverages: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookAverage() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookAverage: using matId=$matId")

            val averages = api.transcript.getRecordBookAverages(matId = matId)
            if (averages.isNotEmpty()) {
                val firstAvg = averages.first()
                val base = firstAvg.base.toString()
                val type = firstAvg.averageTypeCode.value
                logger.info("testGetRecordBookAverage: calling getRecordBookAverage(matId=$matId, base=$base, type=$type)")
                val result = api.transcript.getRecordBookAverage(matId = matId, base = base, type = type)
                logger.info("testGetRecordBookAverage: result size=${result.size}")
                for (item in result) {
                    logger.info("testGetRecordBookAverage: base=${item.base}, averageTypeCode=${item.averageTypeCode}, average=${item.average}")
                }
            } else {
                logger.info("testGetRecordBookAverage: no averages found, calling with base=30 type=A")
                val result = api.transcript.getRecordBookAverage(matId = matId, base = "30", type = "A")
                logger.info("testGetRecordBookAverage: result size=${result.size}")
            }
        } else {
            logger.info("testGetRecordBookAverage: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookAverage(matId = studentProfile.matId, base = "30", type = "A")
            }
            logger.info("testGetRecordBookAverage: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookPartitions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookPartitions: using matId=$matId")

            logger.info("testGetRecordBookPartitions: calling getRecordBookPartitions(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookPartitions(matId = matId)
            logger.info("testGetRecordBookPartitions: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookPartitions: activityPartitionId=${item.activityPartitionId}, activityChoiceId=${item.activityChoiceId}, matId=${item.matId}, lecturerSurnameTitle=${item.lecturerSurnameTitle}")
            }

            logger.info("testGetRecordBookPartitions: calling getRecordBookPartitions() with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getRecordBookPartitions(matId = matId, start = 0, limit = 5)
            logger.info("testGetRecordBookPartitions: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetRecordBookPartitions: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookPartitions(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookPartitions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBookingsByMatId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetBookingsByMatId: using matId=$matId")

            logger.info("testGetBookingsByMatId: calling getBookingsByMatId(matId=$matId) with defaults")
            val defaultResult = api.transcript.getBookingsByMatId(matId = matId)
            logger.info("testGetBookingsByMatId: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetBookingsByMatId: applicationListId=${item.applicationListId}, activityChoiceId=${item.activityChoiceId}, matId=${item.matId}, studentActivityCode=${item.studentActivityCode}")
            }

            logger.info("testGetBookingsByMatId: calling getBookingsByMatId() with actorCode=STU")
            val resultWithActor = api.transcript.getBookingsByMatId(matId = matId, actorCode = "STU")
            logger.info("testGetBookingsByMatId: result with actorCode size=${resultWithActor.size}")
        } else {
            logger.info("testGetBookingsByMatId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getBookingsByMatId(matId = studentProfile.matId)
            }
            logger.info("testGetBookingsByMatId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookTests() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookTests: using matId=$matId")

            logger.info("testGetRecordBookTests: calling getRecordBookTests(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookTests(matId = matId)
            logger.info("testGetRecordBookTests: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookTests: activityRegulationId=${item.activityRegulationId}, activityChoiceId=${item.activityChoiceId}, matId=${item.matId}, regulationStatusCode=${item.regulationStatusCode}")
            }

            logger.info("testGetRecordBookTests: calling getRecordBookTests() with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getRecordBookTests(matId = matId, start = 0, limit = 5)
            logger.info("testGetRecordBookTests: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetRecordBookTests: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookTests(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookTests: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRows() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookRows: using matId=$matId")

            logger.info("testGetRecordBookRows: calling getRecordBookRows(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookRows(matId = matId)
            logger.info("testGetRecordBookRows: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getRecordBookRows() should return non-empty list")
            for (item in defaultResult) {
                logger.info("testGetRecordBookRows: activityChoiceId=${item.activityChoiceId}, activityDescription=${item.activityDescription}, activityCode=${item.activityCode}, weight=${item.weight}, state=${item.state}, courseYear=${item.courseYear}")
            }

            if (defaultResult.first().activityCode != null) {
                val activityCode = defaultResult.first().activityCode!!
                logger.info("testGetRecordBookRows: calling getRecordBookRows() with activityCode=$activityCode")
                val filteredResult = api.transcript.getRecordBookRows(matId = matId, activityCode = activityCode)
                logger.info("testGetRecordBookRows: filtered result size=${filteredResult.size}")
                assertTrue(filteredResult.isNotEmpty(), "Filtered by activityCode should return non-empty list")
            }
        } else {
            logger.info("testGetRecordBookRows: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRows(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookRows: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostRecordBookRow: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostRecordBookRow: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.postRecordBookRow(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3ActivitiesToInsert(
                        courseYear = 1,
                        activityType = "OBB"
                    )
                )
            }
            logger.info("testPostRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetRecordBookRow: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetRecordBookRow: calling getRecordBookRow() with defaults")
            val result = api.transcript.getRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetRecordBookRow: activityChoiceId=${result.activityChoiceId}, activityDescription=${result.activityDescription}, activityCode=${result.activityCode}, weight=${result.weight}, state=${result.state}")
            assertNotNull(result.activityChoiceId, "activityChoiceId should not be null")

            logger.info("testGetRecordBookRow: calling getRecordBookRow() with optionalFields")
            val resultWithOptFields = api.transcript.getRecordBookRow(
                matId = matId,
                activityChoiceId = activityChoiceId,
                optionalFields = "chiaveADContestualizzata"
            )
            logger.info("testGetRecordBookRow: with optionalFields activityChoiceId=${resultWithOptFields.activityChoiceId}, contextualizedTeachingActivityKey=${resultWithOptFields.contextualizedTeachingActivityKey}")
        } else {
            logger.info("testGetRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteRecordBookRow: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteRecordBookRow: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.deleteRecordBookRow(
                    matId = studentProfile.matId,
                    activityChoiceId = 1
                )
            }
            logger.info("testDeleteRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPatchRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPatchRecordBookRow: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPatchRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.patchRecordBookRow(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PatchTranscriptRow(
                        missionState = "NONE"
                    )
                )
            }
            logger.info("testPatchRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExamCallsByRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetExamCallsByRecordBookRow: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetExamCallsByRecordBookRow: calling getExamCallsByRecordBookRow() with defaults")
            val defaultResult = api.transcript.getExamCallsByRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetExamCallsByRecordBookRow: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetExamCallsByRecordBookRow: examCallId=${item.examCallId}, callStartDate=${item.callStartDate}, activityDescription=${item.activityDescription}, callOpeningState=${item.callOpeningState}")
            }

            logger.info("testGetExamCallsByRecordBookRow: calling with actorCode=STU")
            val resultWithActor = api.transcript.getExamCallsByRecordBookRow(
                matId = matId,
                activityChoiceId = activityChoiceId,
                actorCode = "STU"
            )
            logger.info("testGetExamCallsByRecordBookRow: result with actorCode size=${resultWithActor.size}")
        } else {
            logger.info("testGetExamCallsByRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getExamCallsByRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetExamCallsByRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testSetManualAttendance() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSetManualAttendance: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testSetManualAttendance: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.setManualAttendance(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3SingleAttendanceParameters(
                        choiceStatusCode = "F"
                    )
                )
            }
            logger.info("testSetManualAttendance: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowPartitions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetRecordBookRowPartitions: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetRecordBookRowPartitions: calling getRecordBookRowPartitions() with defaults")
            val defaultResult = api.transcript.getRecordBookRowPartitions(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetRecordBookRowPartitions: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookRowPartitions: activityPartitionId=${item.activityPartitionId}, lecturerSurnameTitle=${item.lecturerSurnameTitle}, teachingLanguageDescription=${item.teachingLanguageDescription}")
            }
        } else {
            logger.info("testGetRecordBookRowPartitions: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowPartitions(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetRecordBookRowPartitions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowPartition() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId

            val partitions = api.transcript.getRecordBookRowPartitions(matId = matId, activityChoiceId = activityChoiceId)
            if (partitions.isNotEmpty()) {
                val activityPartitionId = partitions.first().activityPartitionId
                logger.info("testGetRecordBookRowPartition: using matId=$matId, activityChoiceId=$activityChoiceId, activityPartitionId=$activityPartitionId")

                logger.info("testGetRecordBookRowPartition: calling getRecordBookRowPartition()")
                val result = api.transcript.getRecordBookRowPartition(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    activityPartitionId = activityPartitionId
                )
                logger.info("testGetRecordBookRowPartition: activityPartitionId=${result.activityPartitionId}, lecturerSurnameTitle=${result.lecturerSurnameTitle}, teachingLanguageDescription=${result.teachingLanguageDescription}")
                assertNotNull(result.activityPartitionId, "activityPartitionId should not be null")
            } else {
                logger.info("testGetRecordBookRowPartition: no partitions found for activityChoiceId=$activityChoiceId, skipping single partition test")
            }
        } else {
            logger.info("testGetRecordBookRowPartition: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowPartition(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    activityPartitionId = 1
                )
            }
            logger.info("testGetRecordBookRowPartition: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBookingsByTeachingActivityChoiceId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetBookingsByTeachingActivityChoiceId: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetBookingsByTeachingActivityChoiceId: calling getBookingsByTeachingActivityChoiceId() with defaults")
            val defaultResult = api.transcript.getBookingsByTeachingActivityChoiceId(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetBookingsByTeachingActivityChoiceId: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetBookingsByTeachingActivityChoiceId: applicationListId=${item.applicationListId}, activityChoiceId=${item.activityChoiceId}, studentActivityCode=${item.studentActivityCode}")
            }

            logger.info("testGetBookingsByTeachingActivityChoiceId: calling with actorCode=STU")
            val resultWithActor = api.transcript.getBookingsByTeachingActivityChoiceId(
                matId = matId,
                activityChoiceId = activityChoiceId,
                actorCode = "STU"
            )
            logger.info("testGetBookingsByTeachingActivityChoiceId: result with actorCode size=${resultWithActor.size}")
        } else {
            logger.info("testGetBookingsByTeachingActivityChoiceId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getBookingsByTeachingActivityChoiceId(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetBookingsByTeachingActivityChoiceId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBookingByTeachingActivityChoiceId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId

            val bookings = api.transcript.getBookingsByTeachingActivityChoiceId(matId = matId, activityChoiceId = activityChoiceId)
            if (bookings.isNotEmpty()) {
                val applicationListId = bookings.first().applicationListId
                    ?: throw IllegalStateException("First booking has no applicationListId")
                logger.info("testGetBookingByTeachingActivityChoiceId: using matId=$matId, activityChoiceId=$activityChoiceId, applicationListId=$applicationListId")

                logger.info("testGetBookingByTeachingActivityChoiceId: calling getBookingByTeachingActivityChoiceId()")
                val result = api.transcript.getBookingByTeachingActivityChoiceId(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    applicationListId = applicationListId
                )
                logger.info("testGetBookingByTeachingActivityChoiceId: applicationListId=${result.applicationListId}, activityChoiceId=${result.activityChoiceId}, matId=${result.matId}")
                assertNotNull(result.applicationListId, "applicationListId should not be null")
            } else {
                logger.info("testGetBookingByTeachingActivityChoiceId: no bookings found for activityChoiceId=$activityChoiceId, skipping single booking test")
            }
        } else {
            logger.info("testGetBookingByTeachingActivityChoiceId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getBookingByTeachingActivityChoiceId(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    applicationListId = 1
                )
            }
            logger.info("testGetBookingByTeachingActivityChoiceId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPresenceCertificateByApplicationListId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val bookings = api.transcript.getBookingsByMatId(matId = matId)
            if (bookings.isNotEmpty()) {
                val booking = bookings.first()
                val activityChoiceId = booking.activityChoiceId
                    ?: throw IllegalStateException("Booking has no activityChoiceId")
                val applicationListId = booking.applicationListId
                    ?: throw IllegalStateException("Booking has no applicationListId")
                logger.info("testGetPresenceCertificateByApplicationListId: using matId=$matId, activityChoiceId=$activityChoiceId, applicationListId=$applicationListId")

                logger.info("testGetPresenceCertificateByApplicationListId: calling getPresenceCertificateByApplicationListId()")
                val result = api.transcript.getPresenceCertificateByApplicationListId(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    applicationListId = applicationListId
                )
                logger.info("testGetPresenceCertificateByApplicationListId: received ByteReadChannel stream, isClosedForRead=${result.isClosedForRead}")
                assertNotNull(result, "Presence certificate stream should not be null")
            } else {
                logger.info("testGetPresenceCertificateByApplicationListId: no bookings found, skipping stream test")
            }
        } else {
            logger.info("testGetPresenceCertificateByApplicationListId: user lacks STUDENT permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getPresenceCertificateByApplicationListId(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    applicationListId = 1
                )
            }
            logger.info("testGetPresenceCertificateByApplicationListId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetBookingStatinoByApplicationListId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val bookings = api.transcript.getBookingsByMatId(matId = matId)
            if (bookings.isNotEmpty()) {
                val booking = bookings.first()
                val activityChoiceId = booking.activityChoiceId
                    ?: throw IllegalStateException("Booking has no activityChoiceId")
                val applicationListId = booking.applicationListId
                    ?: throw IllegalStateException("Booking has no applicationListId")
                logger.info("testGetBookingStatinoByApplicationListId: using matId=$matId, activityChoiceId=$activityChoiceId, applicationListId=$applicationListId")

                logger.info("testGetBookingStatinoByApplicationListId: calling getBookingStatinoByApplicationListId()")
                val result = api.transcript.getBookingStatinoByApplicationListId(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    applicationListId = applicationListId
                )
                logger.info("testGetBookingStatinoByApplicationListId: received ByteReadChannel stream, isClosedForRead=${result.isClosedForRead}")
                assertNotNull(result, "Booking statino stream should not be null")
            } else {
                logger.info("testGetBookingStatinoByApplicationListId: no bookings found, skipping stream test")
            }
        } else {
            logger.info("testGetBookingStatinoByApplicationListId: user lacks STUDENT permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getBookingStatinoByApplicationListId(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    applicationListId = 1
                )
            }
            logger.info("testGetBookingStatinoByApplicationListId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCheckProposalRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetCheckProposalRecordBookRow: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetCheckProposalRecordBookRow: calling getCheckProposalRecordBookRow() with defaults")
            val result = api.transcript.getCheckProposalRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetCheckProposalRecordBookRow: outcome=${result.outcome}")
            assertNotNull(result, "Prerequisites check result should not be null")

            logger.info("testGetCheckProposalRecordBookRow: calling getCheckProposalRecordBookRow() with referenceDate=2024-01-01")
            val resultWithDate = api.transcript.getCheckProposalRecordBookRow(
                matId = matId,
                activityChoiceId = activityChoiceId,
                referenceDate = "2024-01-01"
            )
            logger.info("testGetCheckProposalRecordBookRow: with referenceDate outcome=${resultWithDate.outcome}")
        } else {
            logger.info("testGetCheckProposalRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getCheckProposalRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetCheckProposalRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowTests() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetRecordBookRowTests: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetRecordBookRowTests: calling getRecordBookRowTests() with defaults")
            val defaultResult = api.transcript.getRecordBookRowTests(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetRecordBookRowTests: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookRowTests: activityRegulationId=${item.activityRegulationId}, regulationStatusCode=${item.regulationStatusCode}, callDate=${item.callDate}, finalOutcome=${item.finalOutcome}")
            }
        } else {
            logger.info("testGetRecordBookRowTests: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowTests(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetRecordBookRowTests: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowTest() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId

            val tests = api.transcript.getRecordBookRowTests(matId = matId, activityChoiceId = activityChoiceId)
            if (tests.isNotEmpty()) {
                val activityRegulationId = tests.first().activityRegulationId
                logger.info("testGetRecordBookRowTest: using matId=$matId, activityChoiceId=$activityChoiceId, activityRegulationId=$activityRegulationId")

                logger.info("testGetRecordBookRowTest: calling getRecordBookRowTest()")
                val result = api.transcript.getRecordBookRowTest(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    activityRegulationId = activityRegulationId
                )
                logger.info("testGetRecordBookRowTest: activityRegulationId=${result.activityRegulationId}, regulationStatusCode=${result.regulationStatusCode}, callDate=${result.callDate}")
                assertNotNull(result.activityRegulationId, "activityRegulationId should not be null")
            } else {
                logger.info("testGetRecordBookRowTest: no tests found for activityChoiceId=$activityChoiceId, skipping single test")
            }
        } else {
            logger.info("testGetRecordBookRowTest: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowTest(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    activityRegulationId = 1
                )
            }
            logger.info("testGetRecordBookRowTest: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutRecordBookRowRecognition() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutRecordBookRowRecognition: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutRecordBookRowRecognition: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.putRecordBookRowRecognition(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3RecognitionParameters(
                        searchId = 1,
                        academicYearAttendanceId = 2024,
                        requestTypeCode = "RIC"
                    )
                )
            }
            logger.info("testPutRecordBookRowRecognition: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteRecordBookRecognitionRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeleteRecordBookRecognitionRow: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testDeleteRecordBookRecognitionRow: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.deleteRecordBookRecognitionRow(
                    matId = studentProfile.matId,
                    activityChoiceId = 1
                )
            }
            logger.info("testDeleteRecordBookRecognitionRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowDetections() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetRecordBookRowDetections: using matId=$matId, activityChoiceId=$activityChoiceId, choiceReleaseId=1")

            logger.info("testGetRecordBookRowDetections: calling getRecordBookRowDetections() with defaults")
            val defaultResult = api.transcript.getRecordBookRowDetections(
                matId = matId,
                activityChoiceId = activityChoiceId,
                choiceReleaseId = 1
            )
            logger.info("testGetRecordBookRowDetections: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookRowDetections: choiceReleaseDetailId=${item.choiceReleaseDetailId}, startDateTime=${item.startDateTime}, duration=${item.duration}, state=${item.state}")
            }
        } else {
            logger.info("testGetRecordBookRowDetections: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowDetections(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    choiceReleaseId = 1
                )
            }
            logger.info("testGetRecordBookRowDetections: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowSegments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetRecordBookRowSegments: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetRecordBookRowSegments: calling getRecordBookRowSegments() with defaults")
            val defaultResult = api.transcript.getRecordBookRowSegments(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetRecordBookRowSegments: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookRowSegments: segmentChoiceId=${item.segmentChoiceId}, activityChoiceId=${item.activityChoiceId}, matId=${item.matId}, attributes.weight=${item.attributes.weight}")
            }
        } else {
            logger.info("testGetRecordBookRowSegments: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowSegments(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetRecordBookRowSegments: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRowSegment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val careerSegments = api.transcript.getCareerSegments()
            assertTrue(careerSegments.isNotEmpty(), "Need at least one career segment")
            val matId = careerSegments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId

            val rowSegments = api.transcript.getRecordBookRowSegments(matId = matId, activityChoiceId = activityChoiceId)
            if (rowSegments.isNotEmpty()) {
                val segmentChoiceId = rowSegments.first().segmentChoiceId
                logger.info("testGetRecordBookRowSegment: using matId=$matId, activityChoiceId=$activityChoiceId, segmentChoiceId=$segmentChoiceId")

                logger.info("testGetRecordBookRowSegment: calling getRecordBookRowSegment()")
                val result = api.transcript.getRecordBookRowSegment(
                    matId = matId,
                    activityChoiceId = activityChoiceId,
                    segmentChoiceId = segmentChoiceId
                )
                logger.info("testGetRecordBookRowSegment: segmentChoiceId=${result.segmentChoiceId}, attributes.weight=${result.attributes.weight}, attributes.sectorCode=${result.attributes.sectorCode}")
                assertNotNull(result.segmentChoiceId, "segmentChoiceId should not be null")
            } else {
                logger.info("testGetRecordBookRowSegment: no segments found for activityChoiceId=$activityChoiceId, skipping single segment test")
            }
        } else {
            logger.info("testGetRecordBookRowSegment: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookRowSegment(
                    matId = studentProfile.matId,
                    activityChoiceId = 1,
                    segmentChoiceId = 1
                )
            }
            logger.info("testGetRecordBookRowSegment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetSyllabusTeachingActivityRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetSyllabusTeachingActivityRecordBookRow: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetSyllabusTeachingActivityRecordBookRow: calling getSyllabusTeachingActivityRecordBookRow() with defaults")
            val defaultResult = api.transcript.getSyllabusTeachingActivityRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetSyllabusTeachingActivityRecordBookRow: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetSyllabusTeachingActivityRecordBookRow: matId=${item.matId}, activityChoiceId=${item.activityChoiceId}, teachingActivityPublicationFlag=${item.teachingActivityPublicationFlag}, trainingObjectives=${item.trainingObjectives?.take(80)}")
            }
        } else {
            logger.info("testGetSyllabusTeachingActivityRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getSyllabusTeachingActivityRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetSyllabusTeachingActivityRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetSyllabusTeachingUnitRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")

            val rows = api.transcript.getRecordBookRows(matId = matId)
            assertTrue(rows.isNotEmpty(), "Need at least one record book row")
            val activityChoiceId = rows.first().activityChoiceId
            logger.info("testGetSyllabusTeachingUnitRecordBookRow: using matId=$matId, activityChoiceId=$activityChoiceId")

            logger.info("testGetSyllabusTeachingUnitRecordBookRow: calling getSyllabusTeachingUnitRecordBookRow() with defaults")
            val defaultResult = api.transcript.getSyllabusTeachingUnitRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetSyllabusTeachingUnitRecordBookRow: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetSyllabusTeachingUnitRecordBookRow: matId=${item.matId}, activityChoiceId=${item.activityChoiceId}, teachingUnitLogId=${item.teachingUnitLogId}, teachingUnitDescription=${item.teachingUnitDescription}")
            }
        } else {
            logger.info("testGetSyllabusTeachingUnitRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getSyllabusTeachingUnitRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1)
            }
            logger.info("testGetSyllabusTeachingUnitRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookSegments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookSegments: using matId=$matId")

            logger.info("testGetRecordBookSegments: calling getRecordBookSegments(matId=$matId) with defaults")
            val defaultResult = api.transcript.getRecordBookSegments(matId = matId)
            logger.info("testGetRecordBookSegments: default result size=${defaultResult.size}")
            for (item in defaultResult) {
                logger.info("testGetRecordBookSegments: segmentChoiceId=${item.segmentChoiceId}, activityChoiceId=${item.activityChoiceId}, matId=${item.matId}, attributes.weight=${item.attributes.weight}, attributes.sectorCode=${item.attributes.sectorCode}")
            }

            logger.info("testGetRecordBookSegments: calling getRecordBookSegments() with pagination start=0, limit=5")
            val paginatedResult = api.transcript.getRecordBookSegments(matId = matId, start = 0, limit = 5)
            logger.info("testGetRecordBookSegments: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetRecordBookSegments: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookSegments(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookSegments: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookStats() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val segments = api.transcript.getCareerSegments()
            assertTrue(segments.isNotEmpty(), "Need at least one career segment")
            val matId = segments.first().matId
                ?: throw IllegalStateException("First career segment has no matId")
            logger.info("testGetRecordBookStats: using matId=$matId")

            logger.info("testGetRecordBookStats: calling getRecordBookStats(matId=$matId) with defaults")
            val result = api.transcript.getRecordBookStats(matId = matId)
            logger.info("testGetRecordBookStats: matId=${result.matId}, passedTeachingActivityNumber=${result.passedTeachingActivityNumber}, passedMeasurementUnitWeight=${result.passedMeasurementUnitWeight}, bookletTeachingActivityNumber=${result.bookletTeachingActivityNumber}, averages.size=${result.averages.size}")
            assertNotNull(result, "Stats result should not be null")

            logger.info("testGetRecordBookStats: calling getRecordBookStats() with optionalFields")
            val resultWithOptFields = api.transcript.getRecordBookStats(
                matId = matId,
                optionalFields = "gruppoVoto"
            )
            logger.info("testGetRecordBookStats: with optionalFields gradeGroup=${resultWithOptFields.gradeGroup}")

            logger.info("testGetRecordBookStats: calling getRecordBookStats() with date range filters")
            val resultWithDates = api.transcript.getRecordBookStats(
                matId = matId,
                initialAveragesReferenceDate = "2020-01-01",
                finalAveragesReferenceDate = "2026-12-31"
            )
            logger.info("testGetRecordBookStats: with date range matId=${resultWithDates.matId}, averages.size=${resultWithDates.averages.size}")
        } else {
            logger.info("testGetRecordBookStats: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.transcript.getRecordBookStats(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookStats: Esse3Exception thrown as expected")
        }
    }
}
