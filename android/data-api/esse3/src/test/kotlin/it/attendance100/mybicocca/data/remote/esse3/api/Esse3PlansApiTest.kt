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

class Esse3PlansApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3PlansApiTest::class.java.name)
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
    fun testGetCareerSegmentsDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegmentsDefaults: calling getCareerSegments() with defaults")
            val result = api.plans.getCareerSegments()
            logger.info("testGetCareerSegmentsDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getCareerSegments() default should return non-empty list")
            for (segment in result) {
                logger.info("testGetCareerSegmentsDefaults: studentId=${segment.studentId}, courseOfStudyCode=${segment.courseOfStudyCode}, courseOfStudyId=${segment.courseOfStudyId}, studentStatusCode=${segment.studentStatusCode}, courseYear=${segment.courseYear}")
            }
        } else {
            logger.info("testGetCareerSegmentsDefaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getCareerSegments()
            }
            logger.info("testGetCareerSegmentsDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegmentsPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegmentsPagination: calling getCareerSegments() with start=0, limit=5")
            val result = api.plans.getCareerSegments(start = 0, limit = 5)
            logger.info("testGetCareerSegmentsPagination: result size=${result.size}")
            assertTrue(result.size <= 5, "Paginated result size should be <= 5")
            for (segment in result) {
                logger.info("testGetCareerSegmentsPagination: studentId=${segment.studentId}, matId=${segment.matId}, enrollmentId=${segment.enrollmentId}")
            }
        } else {
            logger.info("testGetCareerSegmentsPagination: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getCareerSegments(start = 0, limit = 5)
            }
            logger.info("testGetCareerSegmentsPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegmentsWithStudentId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetCareerSegmentsWithStudentId: calling getCareerSegments() with courseOfStudyStudentId=$studentId")
            val result = api.plans.getCareerSegments(courseOfStudyStudentId = studentProfile.degreeCourseId)
            logger.info("testGetCareerSegmentsWithStudentId: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getCareerSegments() with courseOfStudyStudentId should return non-empty list")
            for (segment in result) {
                logger.info("testGetCareerSegmentsWithStudentId: studentId=${segment.studentId}, courseOfStudyCode=${segment.courseOfStudyCode}, studyPlanCode=${segment.studyPlanCode}, academicYearOrderId=${segment.academicYearOrderId}")
            }
        } else {
            logger.info("testGetCareerSegmentsWithStudentId: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getCareerSegments(courseOfStudyStudentId = studentProfile.degreeCourseId)
            }
            logger.info("testGetCareerSegmentsWithStudentId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerSegmentsWithOptionalParams() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerSegmentsWithOptionalParams: calling getCareerSegments() with fields param")
            val resultWithFields = api.plans.getCareerSegments(fields = "stuId,matId,cdsCod,cdsId")
            logger.info("testGetCareerSegmentsWithOptionalParams: result with fields size=${resultWithFields.size}")
            assertTrue(resultWithFields.isNotEmpty(), "getCareerSegments() with fields should return non-empty list")

            logger.info("testGetCareerSegmentsWithOptionalParams: calling getCareerSegments() with order param")
            val resultWithOrder = api.plans.getCareerSegments(order = "+stuId")
            logger.info("testGetCareerSegmentsWithOptionalParams: result with order size=${resultWithOrder.size}")
            assertTrue(resultWithOrder.isNotEmpty(), "getCareerSegments() with order should return non-empty list")

            val defaultResult = api.plans.getCareerSegments()
            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.courseOfStudyId != null) {
                    logger.info("testGetCareerSegmentsWithOptionalParams: calling getCareerSegments() with courseOfStudyStudentId=${first.courseOfStudyId}")
                    val filteredResult = api.plans.getCareerSegments(courseOfStudyStudentId = first.courseOfStudyId)
                    logger.info("testGetCareerSegmentsWithOptionalParams: filtered result size=${filteredResult.size}")
                    assertTrue(filteredResult.isNotEmpty(), "getCareerSegments() with courseOfStudyStudentId should return non-empty list")
                }

                if (first.studentStatusCode != null) {
                    logger.info("testGetCareerSegmentsWithOptionalParams: calling getCareerSegments() with studentStatusCode=${first.studentStatusCode}")
                    val filteredResult = api.plans.getCareerSegments(studentStatusCode = first.studentStatusCode)
                    logger.info("testGetCareerSegmentsWithOptionalParams: filtered result size=${filteredResult.size}")
                    assertTrue(filteredResult.isNotEmpty(), "getCareerSegments() with studentStatusCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetCareerSegmentsWithOptionalParams: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getCareerSegments()
            }
            logger.info("testGetCareerSegmentsWithOptionalParams: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPlansStatistics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPlansStatistics: calling getPlansStatistics() with defaults")
            val result = api.plans.getPlansStatistics()
            logger.info("testGetPlansStatistics: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getPlansStatistics() default should return non-empty list")
            for (stat in result) {
                logger.info("testGetPlansStatistics: planType=${stat.planType}, statusFlag=${stat.statusFlag}, choiceRegulationType=${stat.choiceRegulationType}, number=${stat.number}, studyPlanStatusCode=${stat.studyPlanStatusCode}, studyPlanStatusDescription=${stat.studyPlanStatusDescription}")
            }
        } else {
            logger.info("testGetPlansStatistics: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getPlansStatistics()
            }
            logger.info("testGetPlansStatistics: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentPlanHeadersDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetStudentPlanHeadersDefaults: calling getStudentPlanHeaders() with studentId=$studentId")
            val result = api.plans.getStudentPlanHeaders(studentId = studentId)
            logger.info("testGetStudentPlanHeadersDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getStudentPlanHeaders() default should return non-empty list")
            for (header in result) {
                logger.info("testGetStudentPlanHeadersDefaults: studentId=${header.studentId}, planId=${header.planId}, state=${header.state}, stateDescription=${header.stateDescription}, planType=${header.planType}, cohort=${header.cohort}, courseOfStudyStudentCode=${header.courseOfStudyStudentCode}, courseOfStudyStudentDescription=${header.courseOfStudyStudentDescription}")
            }
        } else {
            logger.info("testGetStudentPlanHeadersDefaults: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getStudentPlanHeaders(studentId = studentProfile.studentId)
            }
            logger.info("testGetStudentPlanHeadersDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentPlanHeadersWithOptionalParams() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetStudentPlanHeadersWithOptionalParams: calling getStudentPlanHeaders() with fields param")
            val resultWithFields = api.plans.getStudentPlanHeaders(
                studentId = studentId,
                fields = "stuId,pianoId,stato,statoDes"
            )
            logger.info("testGetStudentPlanHeadersWithOptionalParams: result with fields size=${resultWithFields.size}")

            logger.info("testGetStudentPlanHeadersWithOptionalParams: calling getStudentPlanHeaders() with order param")
            val resultWithOrder = api.plans.getStudentPlanHeaders(
                studentId = studentId,
                order = "+pianoId"
            )
            logger.info("testGetStudentPlanHeadersWithOptionalParams: result with order size=${resultWithOrder.size}")

            logger.info("testGetStudentPlanHeadersWithOptionalParams: calling getStudentPlanHeaders() with planState filter")
            val resultWithPlanState = api.plans.getStudentPlanHeaders(
                studentId = studentId,
                planState = listOf("A", "V")
            )
            logger.info("testGetStudentPlanHeadersWithOptionalParams: result with planState size=${resultWithPlanState.size}")
        } else {
            logger.info("testGetStudentPlanHeadersWithOptionalParams: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getStudentPlanHeaders(studentId = studentProfile.studentId)
            }
            logger.info("testGetStudentPlanHeadersWithOptionalParams: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentPlan() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetStudentPlan: first retrieving plan headers to get a planId for studentId=$studentId")
            val headers = api.plans.getStudentPlanHeaders(studentId = studentId)
            logger.info("testGetStudentPlan: found ${headers.size} plan headers")
            assertTrue(headers.isNotEmpty(), "Need at least one plan header to test getStudentPlan()")

            val planId = headers.first().planId
            assertNotNull(planId, "planId should not be null in the first plan header")
            logger.info("testGetStudentPlan: calling getStudentPlan() with studentId=$studentId, planId=$planId")
            val plan = api.plans.getStudentPlan(studentId = studentId, planId = planId.toLong())
            logger.info("testGetStudentPlan: studentId=${plan.studentId}, planId=${plan.planId}, state=${plan.state}, stateDescription=${plan.stateDescription}, planType=${plan.planType}, cohort=${plan.cohort}")
            logger.info("testGetStudentPlan: courseOfStudyStudentCode=${plan.courseOfStudyStudentCode}, courseOfStudyStudentDescription=${plan.courseOfStudyStudentDescription}")
            logger.info("testGetStudentPlan: rules size=${plan.rules.size}, activities size=${plan.activity.size}")
            assertNotNull(plan.studentId, "studentId should not be null in the study plan")
            assertNotNull(plan.planId, "planId should not be null in the study plan")

            for ((index, rule) in plan.rules.withIndex()) {
                logger.info("testGetStudentPlan: rule[$index] choicePlanId=${rule.choicePlanId}, description=${rule.description}, orderNumber=${rule.orderNumber}")
            }
            for ((index, activity) in plan.activity.take(5).withIndex()) {
                logger.info("testGetStudentPlan: activity[$index] itemId=${activity.itemId}, activityTranscriptCode=${activity.activityTranscriptCode}, activityTranscriptDescription=${activity.activityTranscriptDescription}, weight=${activity.weight}, courseYear=${activity.courseYear}")
            }
        } else {
            logger.info("testGetStudentPlan: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getStudentPlanHeaders(studentId = studentProfile.studentId)
            }
            logger.info("testGetStudentPlan: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentPlanWithOptionalParams() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            val headers = api.plans.getStudentPlanHeaders(studentId = studentId)
            assertTrue(headers.isNotEmpty(), "Need at least one plan header to test getStudentPlan() with optional params")

            val planId = headers.first().planId
            assertNotNull(planId, "planId should not be null in the first plan header")

            logger.info("testGetStudentPlanWithOptionalParams: calling getStudentPlan() with fields param")
            val resultWithFields = api.plans.getStudentPlan(
                studentId = studentId,
                planId = planId.toLong(),
                fields = "stuId,pianoId,stato,statoDes,cdsStuCod"
            )
            logger.info("testGetStudentPlanWithOptionalParams: result with fields studentId=${resultWithFields.studentId}, planId=${resultWithFields.planId}")
            assertNotNull(resultWithFields.studentId, "studentId should not be null with fields param")

            logger.info("testGetStudentPlanWithOptionalParams: calling getStudentPlan() with optionalFields param")
            val resultWithOptFields = api.plans.getStudentPlan(
                studentId = studentId,
                planId = planId.toLong(),
                optionalFields = "regole,attivita"
            )
            logger.info("testGetStudentPlanWithOptionalParams: result with optionalFields rules size=${resultWithOptFields.rules.size}, activities size=${resultWithOptFields.activity.size}")
        } else {
            logger.info("testGetStudentPlanWithOptionalParams: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.getStudentPlanHeaders(studentId = studentProfile.studentId)
            }
            logger.info("testGetStudentPlanWithOptionalParams: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPlanPrint() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TEACHER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId
            logger.info("testGetPlanPrint: first retrieving plan headers to get a planId for studentId=$studentId")
            val headers = api.plans.getStudentPlanHeaders(studentId = studentId)
            logger.info("testGetPlanPrint: found ${headers.size} plan headers")
            assertTrue(headers.isNotEmpty(), "Need at least one plan header to test getPlanPrint()")

            val planId = headers.first().planId
            assertNotNull(planId, "planId should not be null in the first plan header")

            logger.info("testGetPlanPrint: calling getPlanPrint() with studentId=$studentId, planId=$planId")
            val stream = api.plans.getPlanPrint(studentId = studentId, planId = planId.toLong())
            assertNotNull(stream, "getPlanPrint() should return a non-null ByteReadChannel")
            logger.info("testGetPlanPrint: successfully received stream response for plan print")
        } else {
            logger.info("testGetPlanPrint: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                val headers = api.plans.getStudentPlanHeaders(studentId = studentProfile.studentId)
                val planId = headers.firstOrNull()?.planId ?: 1L
                api.plans.getPlanPrint(studentId = studentProfile.studentId, planId = planId.toLong())
            }
            logger.info("testGetPlanPrint: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostStudentPlan() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostStudentPlan: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostStudentPlan: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.plans.postStudentPlan(
                    studentId = studentProfile.studentId,
                    body = it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PostPlanBody(
                        type = "S",
                        state = "B",
                        implementationFlag = false,
                        cancelValidPlanFlag = false,
                        choiceRegulationType = 0
                    )
                )
            }
            logger.info("testPostStudentPlan: Esse3Exception thrown as expected")
        }
    }
}
