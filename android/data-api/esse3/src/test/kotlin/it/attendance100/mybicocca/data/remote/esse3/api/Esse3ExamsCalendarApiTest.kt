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

class Esse3ExamsCalendarApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3ExamsCalendarApiTest::class.java.name)
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
    fun testGetLecturerAuthorizations() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerAuthorizations: calling getLecturerAuthorizations() with defaults")
            val defaultResult = api.examsCalendar.getLecturerAuthorizations()
            logger.info("testGetLecturerAuthorizations: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getLecturerAuthorizations() default should return non-empty list")
            for (auth in defaultResult) {
                logger.info("testGetLecturerAuthorizations: lecturerId=${auth.lecturerId}, courseOfStudyId=${auth.courseOfStudyId}, activityId=${auth.activityId}")
                assertNotNull(auth.lecturerId, "lecturerId should not be null")
            }

            logger.info("testGetLecturerAuthorizations: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 5)
            logger.info("testGetLecturerAuthorizations: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetLecturerAuthorizations: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerAuthorizations()
            }
        }
    }

    @Test
    fun testGetLecturerAuthorizationsByLecturer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerAuthorizationsByLecturer: fetching authorizations to derive lecturerId")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization to derive lecturerId")
            val lecturerId = authorizations.first().lecturerId
            logger.info("testGetLecturerAuthorizationsByLecturer: using lecturerId=$lecturerId")

            val result = api.examsCalendar.getLecturerAuthorizationsByLecturer(lecturerId = lecturerId)
            logger.info("testGetLecturerAuthorizationsByLecturer: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getLecturerAuthorizationsByLecturer() should return non-empty list")
            for (auth in result) {
                logger.info("testGetLecturerAuthorizationsByLecturer: courseOfStudyId=${auth.courseOfStudyId}, activityId=${auth.activityId}")
            }
        } else {
            logger.info("testGetLecturerAuthorizationsByLecturer: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerAuthorizationsByLecturer(lecturerId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallsByLecturerAuthorization() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallsByLecturerAuthorization: fetching authorizations to derive lecturerId")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization to derive lecturerId")
            val lecturerId = authorizations.first().lecturerId
            logger.info("testGetExamCallsByLecturerAuthorization: using lecturerId=$lecturerId")

            val result = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31"
            )
            logger.info("testGetExamCallsByLecturerAuthorization: result size=${result.size}")
            for (call in result) {
                logger.info("testGetExamCallsByLecturerAuthorization: callId=${call.callId}, activityDescription=${call.activityDescription}, state=${call.state}")
            }

            logger.info("testGetExamCallsByLecturerAuthorization: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 5
            )
            logger.info("testGetExamCallsByLecturerAuthorization: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetExamCallsByLecturerAuthorization: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallsByLecturerAuthorization(
                    lecturerId = 1L,
                    minCallDate = "2024-01-01",
                    maxCallDate = "2026-12-31"
                )
            }
        }
    }

    @Test
    fun testGetLecturerAuthorizationsCourseTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerAuthorizationsCourseTeachingActivity: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization to derive path params")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()
            logger.info("testGetLecturerAuthorizationsCourseTeachingActivity: lecturerId=$lecturerId, courseOfStudyId=$courseOfStudyId, activityId=$activityId")

            val result = api.examsCalendar.getLecturerAuthorizationsCourseTeachingActivity(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                lecturerId = lecturerId
            )
            logger.info("testGetLecturerAuthorizationsCourseTeachingActivity: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getLecturerAuthorizationsCourseTeachingActivity() should return non-empty list")
            for (item in result) {
                logger.info("testGetLecturerAuthorizationsCourseTeachingActivity: academicYearTeacherAuthorizationId=${item.academicYearTeacherAuthorizationId}")
            }
        } else {
            logger.info("testGetLecturerAuthorizationsCourseTeachingActivity: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerAuthorizationsCourseTeachingActivity(
                    courseOfStudyId = 1L,
                    activityId = 1L,
                    lecturerId = 1L
                )
            }
        }
    }

    @Test
    fun testGetLecturerAuthorization() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerAuthorization: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization to derive path params")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()
            val aaAbilDocId = auth.academicYearTeacherAuthorizationId.toLong()
            logger.info("testGetLecturerAuthorization: lecturerId=$lecturerId, courseOfStudyId=$courseOfStudyId, activityId=$activityId, aaAbilDocId=$aaAbilDocId")

            val result = api.examsCalendar.getLecturerAuthorization(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                lecturerId = lecturerId,
                academicYearOfferTeacherAuthorizationId = aaAbilDocId
            )
            logger.info("testGetLecturerAuthorization: lecturerId=${result.lecturerId}, courseOfStudyId=${result.courseOfStudyId}")
            assertNotNull(result.lecturerId, "lecturerId should not be null")
        } else {
            logger.info("testGetLecturerAuthorization: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerAuthorization(
                    courseOfStudyId = 1L,
                    activityId = 1L,
                    lecturerId = 1L,
                    academicYearOfferTeacherAuthorizationId = 1L
                )
            }
        }
    }

    @Test
    fun testGetActivitiesForExamCalls() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetActivitiesForExamCalls: calling getActivitiesForExamCalls() with defaults")
            val defaultResult = api.examsCalendar.getActivitiesForExamCalls()
            logger.info("testGetActivitiesForExamCalls: default result size=${defaultResult.size}")
            for (activity in defaultResult) {
                logger.info("testGetActivitiesForExamCalls: activityCode=${activity.activityCode}, activityDescription=${activity.activityDescription}, matId=${activity.matId}")
            }

            logger.info("testGetActivitiesForExamCalls: calling with matId from student profile")
            val matIdResult = api.examsCalendar.getActivitiesForExamCalls(matId = studentProfile.matId)
            logger.info("testGetActivitiesForExamCalls: matId result size=${matIdResult.size}")

            logger.info("testGetActivitiesForExamCalls: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.getActivitiesForExamCalls(start = 0, limit = 5)
            logger.info("testGetActivitiesForExamCalls: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetActivitiesForExamCalls: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getActivitiesForExamCalls()
            }
        }
    }

    @Test
    fun testGetExamCalls() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCalls: fetching activities to derive courseOfStudyId and activityId")
            val activities = api.examsCalendar.getActivitiesForExamCalls(start = 0, limit = 5)
            if (activities.isEmpty()) {
                logger.info("testGetExamCalls: no activities found, skipping detailed assertions")
                return@runTest
            }
            val activity = activities.first()
            val courseOfStudyId = activity.courseOfStudyDefaultCallId
            val activityId = activity.activityExamDefinitionId.toLong()
            logger.info("testGetExamCalls: using courseOfStudyId=$courseOfStudyId, activityId=$activityId")

            val defaultResult = api.examsCalendar.getExamCalls(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId
            )
            logger.info("testGetExamCalls: default result size=${defaultResult.size}")
            for (call in defaultResult) {
                logger.info("testGetExamCalls: callId=${call.callId}, state=${call.state}, activityDescription=${call.activityDescription}, callStartDate=${call.callStartDate}")
            }

            logger.info("testGetExamCalls: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.getExamCalls(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                start = 0,
                limit = 5
            )
            logger.info("testGetExamCalls: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetExamCalls: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCalls(courseOfStudyId = 1L, activityId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostExamCall() = runTest {
    }

    @Test
    fun testGetExamCall() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCall: fetching activities to derive path params")
            val activities = api.examsCalendar.getActivitiesForExamCalls(start = 0, limit = 5)
            if (activities.isEmpty()) {
                logger.info("testGetExamCall: no activities found, skipping")
                return@runTest
            }
            val activity = activities.first()
            val courseOfStudyId = activity.courseOfStudyDefaultCallId
            val activityId = activity.activityExamDefinitionId.toLong()
            logger.info("testGetExamCall: fetching exam calls for courseOfStudyId=$courseOfStudyId, activityId=$activityId")

            val calls = api.examsCalendar.getExamCalls(courseOfStudyId = courseOfStudyId, activityId = activityId, start = 0, limit = 1)
            if (calls.isEmpty()) {
                logger.info("testGetExamCall: no exam calls found, skipping")
                return@runTest
            }
            val callId = calls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCall: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCall: using callId=$callId")

            val result = api.examsCalendar.getExamCall(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetExamCall: callId=${result.callId}, state=${result.state}, activityDescription=${result.activityDescription}")
            assertNotNull(result.callId, "callId should not be null")
            assertNotNull(result.courseOfStudyId, "courseOfStudyId should not be null")
        } else {
            logger.info("testGetExamCall: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCall(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutExamCall() = runTest {
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteExamCall() = runTest {
    }

    @Test
    fun testGetLecturersExamCall() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturersExamCall: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetLecturersExamCall: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetLecturersExamCall: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetLecturersExamCall: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetLecturersExamCall: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getLecturersExamCall(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetLecturersExamCall: result size=${result.size}")
            for (teacher in result) {
                logger.info("testGetLecturersExamCall: lecturerId=${teacher.lecturerId}, lecturerSurname=${teacher.lecturerSurname}, roleCode=${teacher.roleCode}")
            }
        } else {
            logger.info("testGetLecturersExamCall: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturersExamCall(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    fun testGetLecturerExamCall() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerExamCall: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetLecturerExamCall: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetLecturerExamCall: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetLecturerExamCall: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetLecturerExamCall: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId, lecturerId=$lecturerId")

            val result = api.examsCalendar.getLecturerExamCall(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                lecturerId = lecturerId
            )
            logger.info("testGetLecturerExamCall: lecturerId=${result.lecturerId}, roleCode=${result.roleCode}, lecturerSurname=${result.lecturerSurname}")
            assertNotNull(result.lecturerId, "lecturerId should not be null")
        } else {
            logger.info("testGetLecturerExamCall: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerExamCall(courseOfStudyId = 1L, activityId = 1L, callId = 1L, lecturerId = 1L)
            }
        }
    }

    @Test
    fun testGetCommonExamsExamCall() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCommonExamsExamCall: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetCommonExamsExamCall: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetCommonExamsExamCall: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetCommonExamsExamCall: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetCommonExamsExamCall: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getCommonExamsExamCall(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetCommonExamsExamCall: result size=${result.size}")
            for (common in result) {
                logger.info("testGetCommonExamsExamCall: childCourseOfStudyId=${common.childCourseOfStudyId}, childActivityId=${common.childActivityId}")
            }
        } else {
            logger.info("testGetCommonExamsExamCall: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getCommonExamsExamCall(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallEnrolledList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallEnrolledList: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetExamCallEnrolledList: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetExamCallEnrolledList: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallEnrolledList: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallEnrolledList: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getExamCallEnrolledList(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetExamCallEnrolledList: result size=${result.size}")
            for (enrolled in result) {
                logger.info("testGetExamCallEnrolledList: studentId=${enrolled.studentId}, matricola=${enrolled.matricola}, studentSurname=${enrolled.studentSurname}")
            }
        } else {
            logger.info("testGetExamCallEnrolledList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallEnrolledList(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostExamCallEnrolledList() = runTest {
    }

    @Test
    fun testGetEnrolledExamCall() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEnrolledExamCall: fetching bookings by matId to derive path params")
            val bookings = api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            if (bookings.isEmpty()) {
                logger.info("testGetEnrolledExamCall: no bookings found, skipping")
                return@runTest
            }
            val booking = bookings.first()
            val courseOfStudyId = booking.courseOfStudyId
                ?: run {
                    logger.info("testGetEnrolledExamCall: courseOfStudyId is null, skipping")
                    return@runTest
                }
            val activityId = booking.activityId?.toLong()
                ?: run {
                    logger.info("testGetEnrolledExamCall: activityId is null, skipping")
                    return@runTest
                }
            val callId = booking.callId?.toLong()
                ?: run {
                    logger.info("testGetEnrolledExamCall: callId is null, skipping")
                    return@runTest
                }
            val studentId = booking.studentId?.toLong()
                ?: run {
                    logger.info("testGetEnrolledExamCall: studentId is null, skipping")
                    return@runTest
                }
            logger.info("testGetEnrolledExamCall: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId, studentId=$studentId")

            val result = api.examsCalendar.getEnrolledExamCall(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                studentId = studentId
            )
            logger.info("testGetEnrolledExamCall: applicationListId=${result.applicationListId}, matricola=${result.matricola}")
            assertNotNull(result.applicationListId, "applicationListId should not be null")
        } else {
            logger.info("testGetEnrolledExamCall: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getEnrolledExamCall(courseOfStudyId = 1L, activityId = 1L, callId = 1L, studentId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutModifyBooking() = runTest {
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteExamCallEnrolledList() = runTest {
    }

    @Test
    fun testGetPresenceCertificate() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPresenceCertificate: fetching bookings by matId to derive path params")
            val bookings = api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            if (bookings.isEmpty()) {
                logger.info("testGetPresenceCertificate: no bookings found, skipping")
                return@runTest
            }
            val booking = bookings.first()
            val courseOfStudyId = booking.courseOfStudyId
                ?: run {
                    logger.info("testGetPresenceCertificate: courseOfStudyId is null, skipping")
                    return@runTest
                }
            val activityId = booking.activityId?.toLong()
                ?: run {
                    logger.info("testGetPresenceCertificate: activityId is null, skipping")
                    return@runTest
                }
            val callId = booking.callId?.toLong()
                ?: run {
                    logger.info("testGetPresenceCertificate: callId is null, skipping")
                    return@runTest
                }
            val studentId = booking.studentId?.toLong()
                ?: run {
                    logger.info("testGetPresenceCertificate: studentId is null, skipping")
                    return@runTest
                }
            logger.info("testGetPresenceCertificate: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId, studentId=$studentId")

            val result = api.examsCalendar.getPresenceCertificate(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                studentId = studentId
            )
            logger.info("testGetPresenceCertificate: received ByteReadChannel stream, isClosedForRead=${result.isClosedForRead}")
            assertNotNull(result, "Presence certificate stream should not be null")
        } else {
            logger.info("testGetPresenceCertificate: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getPresenceCertificate(courseOfStudyId = 1L, activityId = 1L, callId = 1L, studentId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutApplicationListOutcome() = runTest {
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutAcknowledgmentOfReceipt() = runTest {
    }

    @Test
    fun testGetBookingStatino() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBookingStatino: fetching bookings by matId to derive path params")
            val bookings = api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            if (bookings.isEmpty()) {
                logger.info("testGetBookingStatino: no bookings found, skipping")
                return@runTest
            }
            val booking = bookings.first()
            val courseOfStudyId = booking.courseOfStudyId
                ?: run {
                    logger.info("testGetBookingStatino: courseOfStudyId is null, skipping")
                    return@runTest
                }
            val activityId = booking.activityId?.toLong()
                ?: run {
                    logger.info("testGetBookingStatino: activityId is null, skipping")
                    return@runTest
                }
            val callId = booking.callId?.toLong()
                ?: run {
                    logger.info("testGetBookingStatino: callId is null, skipping")
                    return@runTest
                }
            val studentId = booking.studentId?.toLong()
                ?: run {
                    logger.info("testGetBookingStatino: studentId is null, skipping")
                    return@runTest
                }
            logger.info("testGetBookingStatino: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId, studentId=$studentId")

            val result = api.examsCalendar.getBookingStatino(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                studentId = studentId
            )
            logger.info("testGetBookingStatino: received ByteReadChannel stream, isClosedForRead=${result.isClosedForRead}")
            assertNotNull(result, "Booking statino stream should not be null")
        } else {
            logger.info("testGetBookingStatino: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getBookingStatino(courseOfStudyId = 1L, activityId = 1L, callId = 1L, studentId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutExamCallPublication() = runTest {
    }

    @Test
    fun testGetExamCallSessions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallSessions: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetExamCallSessions: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetExamCallSessions: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallSessions: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallSessions: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getExamCallSessions(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetExamCallSessions: result size=${result.size}")
            for (session in result) {
                logger.info("testGetExamCallSessions: academicYearSessionId=${session.academicYearSessionId}, sessionId=${session.sessionId}, sessionDescription=${session.sessionDescription}")
            }
        } else {
            logger.info("testGetExamCallSessions: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallSessions(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallSession() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallSession: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetExamCallSession: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetExamCallSession: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallSession: callId is null, skipping")
                    return@runTest
                }

            logger.info("testGetExamCallSession: fetching sessions for callId=$callId")
            val sessions = api.examsCalendar.getExamCallSessions(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            if (sessions.isEmpty()) {
                logger.info("testGetExamCallSession: no sessions found, skipping")
                return@runTest
            }
            val session = sessions.first()
            val academicYearSessionId = session.academicYearSessionId?.toLong()
                ?: run {
                    logger.info("testGetExamCallSession: academicYearSessionId is null, skipping")
                    return@runTest
                }
            val sessionId = session.sessionId?.toLong()
                ?: run {
                    logger.info("testGetExamCallSession: sessionId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallSession: using academicYearSessionId=$academicYearSessionId, sessionId=$sessionId")

            val result = api.examsCalendar.getExamCallSession(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                academicYearSessionId = academicYearSessionId,
                sessionId = sessionId
            )
            logger.info("testGetExamCallSession: academicYearSessionId=${result.academicYearSessionId}, sessionId=${result.sessionId}, sessionDescription=${result.sessionDescription}")
            assertNotNull(result.academicYearSessionId, "academicYearSessionId should not be null")
        } else {
            logger.info("testGetExamCallSession: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallSession(courseOfStudyId = 1L, activityId = 1L, callId = 1L, academicYearSessionId = 1L, sessionId = 1L)
            }
        }
    }

    @Test
    fun testGetTagsByBooking() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetTagsByBooking: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetTagsByBooking: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetTagsByBooking: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetTagsByBooking: callId is null, skipping")
                    return@runTest
                }

            logger.info("testGetTagsByBooking: fetching enrolled list to derive activityChoiceId")
            val enrolled = api.examsCalendar.getExamCallEnrolledList(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            if (enrolled.isEmpty()) {
                logger.info("testGetTagsByBooking: no enrolled students found, skipping")
                return@runTest
            }
            val activityChoiceId = enrolled.first().activityChoiceId
                ?: run {
                    logger.info("testGetTagsByBooking: activityChoiceId is null, skipping")
                    return@runTest
                }
            logger.info("testGetTagsByBooking: using activityChoiceId=$activityChoiceId")

            val result = api.examsCalendar.getTagsByBooking(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                activityChoiceId = activityChoiceId
            )
            logger.info("testGetTagsByBooking: result size=${result.size}")
            for (tag in result) {
                logger.info("testGetTagsByBooking: tagCode=${tag.tagCode}, tagDescription=${tag.tagDescription}")
            }
        } else {
            logger.info("testGetTagsByBooking: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getTagsByBooking(courseOfStudyId = 1L, activityId = 1L, callId = 1L, activityChoiceId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallTypes: fetching activities to derive path params")
            val activities = api.examsCalendar.getActivitiesForExamCalls(start = 0, limit = 5)
            if (activities.isEmpty()) {
                logger.info("testGetExamCallTypes: no activities found, skipping")
                return@runTest
            }
            val activity = activities.first()
            val courseOfStudyId = activity.courseOfStudyDefaultCallId
            val activityId = activity.activityExamDefinitionId.toLong()

            val calls = api.examsCalendar.getExamCalls(courseOfStudyId = courseOfStudyId, activityId = activityId, start = 0, limit = 1)
            if (calls.isEmpty()) {
                logger.info("testGetExamCallTypes: no exam calls found, skipping")
                return@runTest
            }
            val callId = calls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallTypes: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallTypes: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getExamCallTypes(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetExamCallTypes: result size=${result.size}")
            for (examType in result) {
                logger.info("testGetExamCallTypes: examTypeCode=${examType.examTypeCode}, examTypeDescription=${examType.examTypeDescription}, actorCode=${examType.actorCode}")
            }
        } else {
            logger.info("testGetExamCallTypes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallTypes(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallShifts() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallShifts: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetExamCallShifts: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetExamCallShifts: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallShifts: callId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallShifts: using courseOfStudyId=$courseOfStudyId, activityId=$activityId, callId=$callId")

            val result = api.examsCalendar.getExamCallShifts(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            logger.info("testGetExamCallShifts: result size=${result.size}")
            for (shift in result) {
                logger.info("testGetExamCallShifts: callLogId=${shift.callLogId}, description=${shift.description}, graduationDateTime=${shift.graduationDateTime}")
            }
        } else {
            logger.info("testGetExamCallShifts: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallShifts(courseOfStudyId = 1L, activityId = 1L, callId = 1L)
            }
        }
    }

    @Test
    fun testGetExamCallShift() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExamCallShift: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetExamCallShift: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetExamCallShift: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetExamCallShift: callId is null, skipping")
                    return@runTest
                }

            logger.info("testGetExamCallShift: fetching shifts for callId=$callId")
            val shifts = api.examsCalendar.getExamCallShifts(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            if (shifts.isEmpty()) {
                logger.info("testGetExamCallShift: no shifts found, skipping")
                return@runTest
            }
            val callLogId = shifts.first().callLogId?.toLong()
                ?: run {
                    logger.info("testGetExamCallShift: callLogId is null, skipping")
                    return@runTest
                }
            logger.info("testGetExamCallShift: using callLogId=$callLogId")

            val result = api.examsCalendar.getExamCallShift(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                callLogId = callLogId
            )
            logger.info("testGetExamCallShift: callLogId=${result.callLogId}, description=${result.description}, graduationDateTime=${result.graduationDateTime}")
            assertNotNull(result.callLogId, "callLogId should not be null")
        } else {
            logger.info("testGetExamCallShift: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getExamCallShift(courseOfStudyId = 1L, activityId = 1L, callId = 1L, callLogId = 1L)
            }
        }
    }

    @Test
    fun testGetLecturersShift() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturersShift: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetLecturersShift: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetLecturersShift: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetLecturersShift: callId is null, skipping")
                    return@runTest
                }

            logger.info("testGetLecturersShift: fetching shifts for callId=$callId")
            val shifts = api.examsCalendar.getExamCallShifts(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            if (shifts.isEmpty()) {
                logger.info("testGetLecturersShift: no shifts found, skipping")
                return@runTest
            }
            val callLogId = shifts.first().callLogId?.toLong()
                ?: run {
                    logger.info("testGetLecturersShift: callLogId is null, skipping")
                    return@runTest
                }
            logger.info("testGetLecturersShift: using callLogId=$callLogId")

            val result = api.examsCalendar.getLecturersShift(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                callLogId = callLogId
            )
            logger.info("testGetLecturersShift: result size=${result.size}")
            for (teacher in result) {
                logger.info("testGetLecturersShift: lecturerId=${teacher.lecturerId}, lecturerSurname=${teacher.lecturerSurname}, roleCode=${teacher.roleCode}")
            }
        } else {
            logger.info("testGetLecturersShift: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturersShift(courseOfStudyId = 1L, activityId = 1L, callId = 1L, callLogId = 1L)
            }
        }
    }

    @Test
    fun testGetLecturerShift() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLecturerShift: fetching authorizations to derive path params")
            val authorizations = api.examsCalendar.getLecturerAuthorizations(start = 0, limit = 1)
            assertTrue(authorizations.isNotEmpty(), "Need at least one authorization")
            val auth = authorizations.first()
            val lecturerId = auth.lecturerId
            val courseOfStudyId = auth.courseOfStudyId
            val activityId = auth.activityId.toLong()

            logger.info("testGetLecturerShift: fetching exam calls by lecturer authorization")
            val examCalls = api.examsCalendar.getExamCallsByLecturerAuthorization(
                lecturerId = lecturerId,
                minCallDate = "2024-01-01",
                maxCallDate = "2026-12-31",
                start = 0,
                limit = 1
            )
            if (examCalls.isEmpty()) {
                logger.info("testGetLecturerShift: no exam calls found, skipping")
                return@runTest
            }
            val callId = examCalls.first().callId?.toLong()
                ?: run {
                    logger.info("testGetLecturerShift: callId is null, skipping")
                    return@runTest
                }

            logger.info("testGetLecturerShift: fetching shifts for callId=$callId")
            val shifts = api.examsCalendar.getExamCallShifts(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId
            )
            if (shifts.isEmpty()) {
                logger.info("testGetLecturerShift: no shifts found, skipping")
                return@runTest
            }
            val callLogId = shifts.first().callLogId?.toLong()
                ?: run {
                    logger.info("testGetLecturerShift: callLogId is null, skipping")
                    return@runTest
                }
            logger.info("testGetLecturerShift: using callLogId=$callLogId, lecturerId=$lecturerId")

            val result = api.examsCalendar.getLecturerShift(
                courseOfStudyId = courseOfStudyId,
                activityId = activityId,
                callId = callId,
                callLogId = callLogId,
                lecturerId = lecturerId
            )
            logger.info("testGetLecturerShift: lecturerId=${result.lecturerId}, roleCode=${result.roleCode}, lecturerSurname=${result.lecturerSurname}")
            assertNotNull(result.lecturerId, "lecturerId should not be null")
        } else {
            logger.info("testGetLecturerShift: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getLecturerShift(courseOfStudyId = 1L, activityId = 1L, callId = 1L, callLogId = 1L, lecturerId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutShiftPublication() = runTest {
    }

    @Test
    fun testGetEsacomByAcademicYearId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEsacomByAcademicYearId: calling getEsacomByAcademicYearId with academicYearId=2025")
            val defaultResult = api.examsCalendar.getEsacomByAcademicYearId(academicYearId = 2025)
            logger.info("testGetEsacomByAcademicYearId: default result size=${defaultResult.size}")
            for (esacom in defaultResult) {
                logger.info("testGetEsacomByAcademicYearId: courseOfStudyGraduationId=${esacom.courseOfStudyGraduationId}, activityExamId=${esacom.activityExamId}")
            }

            logger.info("testGetEsacomByAcademicYearId: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.getEsacomByAcademicYearId(academicYearId = 2025, start = 0, limit = 5)
            logger.info("testGetEsacomByAcademicYearId: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetEsacomByAcademicYearId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getEsacomByAcademicYearId(academicYearId = 2025)
            }
        }
    }

    @Test
    fun testGetEsacomParent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEsacomParent: fetching esacom to derive path params")
            val esacomList = api.examsCalendar.getEsacomByAcademicYearId(academicYearId = 2025, start = 0, limit = 1)
            if (esacomList.isEmpty()) {
                logger.info("testGetEsacomParent: no esacom found, skipping")
                return@runTest
            }
            val esacom = esacomList.first()
            val courseOfStudyGraduationId = esacom.courseOfStudyGraduationId
                ?: run {
                    logger.info("testGetEsacomParent: courseOfStudyGraduationId is null, skipping")
                    return@runTest
                }
            val activityExamId = esacom.activityExamId?.toLong()
                ?: run {
                    logger.info("testGetEsacomParent: activityExamId is null, skipping")
                    return@runTest
                }
            logger.info("testGetEsacomParent: using courseOfStudyGraduationId=$courseOfStudyGraduationId, activityExamId=$activityExamId")

            val result = api.examsCalendar.getEsacomParent(
                academicYearId = 2025,
                courseOfStudyGraduationId = courseOfStudyGraduationId,
                activityExamId = activityExamId
            )
            logger.info("testGetEsacomParent: result size=${result.size}")
            for (item in result) {
                logger.info("testGetEsacomParent: childCourseOfStudyId=${item.childCourseOfStudyId}, childActivityId=${item.childActivityId}")
            }
        } else {
            logger.info("testGetEsacomParent: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getEsacomParent(academicYearId = 2025, courseOfStudyGraduationId = 1L, activityExamId = 1L)
            }
        }
    }

    @Test
    fun testGetEsacomChild() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetEsacomChild: fetching esacom to derive path params")
            val esacomList = api.examsCalendar.getEsacomByAcademicYearId(academicYearId = 2025, start = 0, limit = 1)
            if (esacomList.isEmpty()) {
                logger.info("testGetEsacomChild: no esacom found, skipping")
                return@runTest
            }
            val esacom = esacomList.first()
            val courseOfStudyGraduationId = esacom.courseOfStudyGraduationId
                ?: run {
                    logger.info("testGetEsacomChild: courseOfStudyGraduationId is null, skipping")
                    return@runTest
                }
            val activityExamId = esacom.activityExamId?.toLong()
                ?: run {
                    logger.info("testGetEsacomChild: activityExamId is null, skipping")
                    return@runTest
                }
            val childCourseOfStudyId = esacom.childCourseOfStudyId
                ?: run {
                    logger.info("testGetEsacomChild: childCourseOfStudyId is null, skipping")
                    return@runTest
                }
            val childActivityId = esacom.childActivityId
                ?: run {
                    logger.info("testGetEsacomChild: childActivityId is null, skipping")
                    return@runTest
                }
            logger.info("testGetEsacomChild: using courseOfStudyGraduationId=$courseOfStudyGraduationId, activityExamId=$activityExamId, childCourseOfStudyId=$childCourseOfStudyId, childActivityId=$childActivityId")

            val result = api.examsCalendar.getEsacomChild(
                academicYearId = 2025,
                courseOfStudyGraduationId = courseOfStudyGraduationId,
                activityExamId = activityExamId,
                childCourseOfStudyId = childCourseOfStudyId,
                childActivityId = childActivityId
            )
            logger.info("testGetEsacomChild: courseOfStudyGraduationId=${result.courseOfStudyGraduationId}, mutualFlag=${result.mutualFlag}")
            assertNotNull(result.courseOfStudyGraduationId, "courseOfStudyGraduationId should not be null")
        } else {
            logger.info("testGetEsacomChild: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getEsacomChild(academicYearId = 2025, courseOfStudyGraduationId = 1L, activityExamId = 1L, childCourseOfStudyId = 1L, childActivityId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutEsacomChild() = runTest {
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testDeleteEsacomChild() = runTest {
    }

    @Test
    fun testGetBookingsByMatId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBookingsByMatId: calling getBookingsByMatId with matId=${studentProfile.enrollmentId}")
            val defaultResult = api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            logger.info("testGetBookingsByMatId: default result size=${defaultResult.size}")
            for (booking in defaultResult) {
                logger.info("testGetBookingsByMatId: applicationListId=${booking.applicationListId}, callId=${booking.callId}, studentActivityDescription=${booking.studentActivityDescription}")
            }
        } else {
            logger.info("testGetBookingsByMatId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            }
        }
    }

    @Test
    fun testGetBooking() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetBooking: fetching bookings by matId to derive applicationListId")
            val bookings = api.examsCalendar.getBookingsByMatId(matId = studentProfile.matId)
            if (bookings.isEmpty()) {
                logger.info("testGetBooking: no bookings found, skipping")
                return@runTest
            }
            val booking = bookings.first()
            val applicationListId = booking.applicationListId
                ?: run {
                    logger.info("testGetBooking: applicationListId is null, skipping")
                    return@runTest
                }
            logger.info("testGetBooking: using matId=${studentProfile.enrollmentId}, applicationListId=$applicationListId")

            val result = api.examsCalendar.getBooking(
                matId = studentProfile.matId,
                applicationListId = applicationListId
            )
            logger.info("testGetBooking: applicationListId=${result.applicationListId}, matricola=${result.matricola}, examCallDescription=${result.examCallDescription}")
            assertNotNull(result.applicationListId, "applicationListId should not be null")
        } else {
            logger.info("testGetBooking: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getBooking(matId = studentProfile.matId, applicationListId = 1L)
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutAcknowledgmentOfReceiptApplicationList() = runTest {
    }

    @Test
    fun testGetSessions() = runTest {
        logger.info("testGetSessions: calling getSessions() with defaults")
        val defaultResult = api.examsCalendar.getSessions()
        logger.info("testGetSessions: default result size=${defaultResult.size}")
        assertTrue(defaultResult.isNotEmpty(), "getSessions() default should return non-empty list")
        for (session in defaultResult) {
            logger.info("testGetSessions: academicYearSessionId=${session.academicYearSessionId}, sessionId=${session.sessionId}, description=${session.description}, startDate=${session.startDate}")
        }

        logger.info("testGetSessions: calling with pagination start=0, limit=5")
        val paginatedResult = api.examsCalendar.getSessions(start = 0, limit = 5)
        logger.info("testGetSessions: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        logger.info("testGetSessions: calling with courseOfStudyId from student profile")
        val filteredResult = api.examsCalendar.getSessions(courseOfStudyId = studentProfile.degreeCourseId)
        logger.info("testGetSessions: filtered by courseOfStudyId result size=${filteredResult.size}")

        logger.info("testGetSessions: calling with date range filter")
        val dateFilteredResult = api.examsCalendar.getSessions(minStartDate = "2025-01-01", maxEndDate = "2026-12-31")
        logger.info("testGetSessions: date filtered result size=${dateFilteredResult.size}")
    }

    @Test
    fun testGetSessionsByAcademicYearSession() = runTest {
        logger.info("testGetSessionsByAcademicYearSession: fetching sessions to derive academicYearSessionId")
        val sessions = api.examsCalendar.getSessions(start = 0, limit = 1)
        assertTrue(sessions.isNotEmpty(), "Need at least one session to derive academicYearSessionId")
        val academicYearSessionId = sessions.first().academicYearSessionId?.toLong()
            ?: run {
                logger.info("testGetSessionsByAcademicYearSession: academicYearSessionId is null, skipping")
                return@runTest
            }
        logger.info("testGetSessionsByAcademicYearSession: using academicYearSessionId=$academicYearSessionId")

        val defaultResult = api.examsCalendar.getSessionsByAcademicYearSession(academicYearSessionId = academicYearSessionId)
        logger.info("testGetSessionsByAcademicYearSession: default result size=${defaultResult.size}")
        assertTrue(defaultResult.isNotEmpty(), "getSessionsByAcademicYearSession() should return non-empty list")
        for (session in defaultResult) {
            logger.info("testGetSessionsByAcademicYearSession: sessionId=${session.sessionId}, courseOfStudyId=${session.courseOfStudyId}, description=${session.description}")
        }

        logger.info("testGetSessionsByAcademicYearSession: calling with pagination start=0, limit=5")
        val paginatedResult = api.examsCalendar.getSessionsByAcademicYearSession(academicYearSessionId = academicYearSessionId, start = 0, limit = 5)
        logger.info("testGetSessionsByAcademicYearSession: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
    }

    @Test
    fun testGetSessionsByAcademicYearSessionAndCourseOfStudy() = runTest {
        logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: fetching sessions to derive path params")
        val sessions = api.examsCalendar.getSessions(courseOfStudyId = studentProfile.degreeCourseId, start = 0, limit = 1)
        if (sessions.isEmpty()) {
            logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: no sessions found for courseOfStudyId, skipping")
            return@runTest
        }
        val academicYearSessionId = sessions.first().academicYearSessionId?.toLong()
            ?: run {
                logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: academicYearSessionId is null, skipping")
                return@runTest
            }
        logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: using academicYearSessionId=$academicYearSessionId, courseOfStudyId=${studentProfile.degreeCourseId}")

        val result = api.examsCalendar.getSessionsByAcademicYearSessionAndCourseOfStudy(
            academicYearSessionId = academicYearSessionId,
            courseOfStudyId = studentProfile.degreeCourseId
        )
        logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: result size=${result.size}")
        assertTrue(result.isNotEmpty(), "getSessionsByAcademicYearSessionAndCourseOfStudy() should return non-empty list")
        for (session in result) {
            logger.info("testGetSessionsByAcademicYearSessionAndCourseOfStudy: sessionId=${session.sessionId}, description=${session.description}, startDate=${session.startDate}")
        }
    }

    @Test
    fun testExportSystemLog() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testExportSystemLog: calling exportSystemLog() with defaults")
            val defaultResult = api.examsCalendar.exportSystemLog()
            logger.info("testExportSystemLog: default result size=${defaultResult.size}")
            for (export in defaultResult) {
                logger.info("testExportSystemLog: processingId=${export.processingId}, description=${export.description}")
            }

            logger.info("testExportSystemLog: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.exportSystemLog(start = 0, limit = 5)
            logger.info("testExportSystemLog: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testExportSystemLog: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.exportSystemLog()
            }
        }
    }

    @Test
    fun testExportSystemLogByProcessingId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testExportSystemLogByProcessingId: fetching system logs to derive processingId")
            val logs = api.examsCalendar.exportSystemLog(start = 0, limit = 1)
            if (logs.isEmpty()) {
                logger.info("testExportSystemLogByProcessingId: no system logs found, skipping")
                return@runTest
            }
            val processingId = logs.first().processingId
                ?: run {
                    logger.info("testExportSystemLogByProcessingId: processingId is null, skipping")
                    return@runTest
                }
            logger.info("testExportSystemLogByProcessingId: using processingId=$processingId")

            val result = api.examsCalendar.exportSystemLogByProcessingId(processingId = processingId)
            logger.info("testExportSystemLogByProcessingId: processingId=${result.processingId}, description=${result.description}")
            assertNotNull(result.processingId, "processingId should not be null")
        } else {
            logger.info("testExportSystemLogByProcessingId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.exportSystemLogByProcessingId(processingId = 1L)
            }
        }
    }

    @Test
    fun testExportSystemLogEvents() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testExportSystemLogEvents: fetching system logs to derive processingId")
            val logs = api.examsCalendar.exportSystemLog(start = 0, limit = 1)
            if (logs.isEmpty()) {
                logger.info("testExportSystemLogEvents: no system logs found, skipping")
                return@runTest
            }
            val processingId = logs.first().processingId
                ?: run {
                    logger.info("testExportSystemLogEvents: processingId is null, skipping")
                    return@runTest
                }
            logger.info("testExportSystemLogEvents: using processingId=$processingId")

            val defaultResult = api.examsCalendar.exportSystemLogEvents(processingId = processingId)
            logger.info("testExportSystemLogEvents: default result size=${defaultResult.size}")
            for (event in defaultResult) {
                logger.info("testExportSystemLogEvents: courseOfStudyId=${event.courseOfStudyId}, activityId=${event.activityId}, description=${event.description}")
            }

            logger.info("testExportSystemLogEvents: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.exportSystemLogEvents(processingId = processingId, start = 0, limit = 5)
            logger.info("testExportSystemLogEvents: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testExportSystemLogEvents: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.exportSystemLogEvents(processingId = 1L)
            }
        }
    }

    @Test
    fun testExportSystemLogSessions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testExportSystemLogSessions: fetching system logs to derive processingId")
            val logs = api.examsCalendar.exportSystemLog(start = 0, limit = 1)
            if (logs.isEmpty()) {
                logger.info("testExportSystemLogSessions: no system logs found, skipping")
                return@runTest
            }
            val processingId = logs.first().processingId
                ?: run {
                    logger.info("testExportSystemLogSessions: processingId is null, skipping")
                    return@runTest
                }
            logger.info("testExportSystemLogSessions: using processingId=$processingId")

            val defaultResult = api.examsCalendar.exportSystemLogSessions(processingId = processingId)
            logger.info("testExportSystemLogSessions: default result size=${defaultResult.size}")
            for (session in defaultResult) {
                logger.info("testExportSystemLogSessions: sessionKey=${session.sessionKey}, description=${session.description}")
            }

            logger.info("testExportSystemLogSessions: calling with pagination start=0, limit=5")
            val paginatedResult = api.examsCalendar.exportSystemLogSessions(processingId = processingId, start = 0, limit = 5)
            logger.info("testExportSystemLogSessions: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testExportSystemLogSessions: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.exportSystemLogSessions(processingId = 1L)
            }
        }
    }

    @Test
    fun testGetCommitmentsByDate() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCommitmentsByDate: calling getCommitmentsByDate() with defaults")
            val defaultResult = api.examsCalendar.getCommitmentsByDate()
            logger.info("testGetCommitmentsByDate: default result size=${defaultResult.size}")
            for (commitment in defaultResult) {
                logger.info("testGetCommitmentsByDate: commitmentCode=${commitment.commitmentCode}, courseOfStudyId=${commitment.courseOfStudyId}, callStartDate=${commitment.callStartDate}")
            }

            logger.info("testGetCommitmentsByDate: calling with referenceDate=2026-03-27")
            val dateResult = api.examsCalendar.getCommitmentsByDate(referenceDate = "2026-03-27")
            logger.info("testGetCommitmentsByDate: date result size=${dateResult.size}")
        } else {
            logger.info("testGetCommitmentsByDate: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.examsCalendar.getCommitmentsByDate()
            }
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testImportSystemLog() = runTest {
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testUpdateCommitment() = runTest {
    }
}
