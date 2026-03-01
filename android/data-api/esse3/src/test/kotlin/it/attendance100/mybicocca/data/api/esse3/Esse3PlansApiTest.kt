package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3PlansApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetCareerSegments() {
        try {
            val segments = api.plans.getCareerSegments()
            assertNotNull(segments)
            logger.info("getCareerSegments (plans) returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("  segment: matId=${segment.matId}, cdsCod=${segment.courseOfStudyCode}, stuId=${segment.studentId}")
                assertNotNull(segment.matId, "matId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCareerSegments: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCareerSegmentsByFiscalCode() {
        try {
            val fiscalCode = session.fiscalCode
            if (fiscalCode == null) {
                logger.warning("No fiscal code available in session, skipping testGetCareerSegmentsByFiscalCode")
                return
            }
            val segments = api.plans.getCareerSegments(fiscalCode = fiscalCode)
            assertNotNull(segments)
            logger.info("getCareerSegments by fiscalCode returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("  segment: matId=${segment.matId}, cdsCod=${segment.courseOfStudyCode}")
                assertNotNull(segment.matId, "matId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCareerSegmentsByFiscalCode: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetPlansStatistics() {
        try {
            val stats = api.plans.getPlansStatistics()
            assertNotNull(stats)
            logger.info("getPlansStatistics returned ${stats.size} statistics entries")
            for (stat in stats) {
                logger.info("  stat: tipPiano=${stat.planType}, staPianoCod=${stat.studyPlanStatusCode}, num=${stat.number}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetPlansStatistics: insufficient permissions (requires TECHNICAL_USER) - ${e.message}")
        }
    }

    @Test
    suspend fun testGetPlansStatisticsWithFilters() {
        try {
            val currentYear = 2024
            val stats = api.plans.getPlansStatistics(minimumCohort = currentYear - 5)
            assertNotNull(stats)
            logger.info("getPlansStatistics with minimumCohort=${currentYear - 5} returned ${stats.size} statistics entries")
            for (stat in stats) {
                logger.info("  stat: tipPiano=${stat.planType}, staPianoCod=${stat.studyPlanStatusCode}, num=${stat.number}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetPlansStatisticsWithFilters: insufficient permissions (requires TECHNICAL_USER) - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetPlansStatisticsWithFilters: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentPlanHeaders() {
        try {
            val studentId = studentProfile.studentId
            val headers = api.plans.getStudentPlanHeaders(studentId)
            assertNotNull(headers)
            logger.info("getStudentPlanHeaders returned ${headers.size} plan headers for studentId=$studentId")
            for (header in headers) {
                logger.info("  header: pianoId=${header.planId}, stato=${header.state}, statoDes=${header.stateDescription}, tipoPiano=${header.planType}")
                assertNotNull(header.planId, "planId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetStudentPlanHeaders: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentPlanHeadersWithStateFilter() {
        try {
            val studentId = studentProfile.studentId
            val headers = api.plans.getStudentPlanHeaders(studentId, planState = listOf("P"))
            assertNotNull(headers)
            logger.info("getStudentPlanHeaders with planState=P returned ${headers.size} headers for studentId=$studentId")
            for (header in headers) {
                logger.info("  header: pianoId=${header.planId}, stato=${header.state}")
                assertEquals("P", header.state, "All returned headers should be in state P (published)")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetStudentPlanHeadersWithStateFilter: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetStudentPlanHeadersWithStateFilter: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentPlan() {
        try {
            val studentId = studentProfile.studentId
            val headers = api.plans.getStudentPlanHeaders(studentId)
            assertNotNull(headers)

            if (headers.isEmpty()) {
                logger.warning("No plan headers found for studentId=$studentId, skipping getStudentPlan test")
                return
            }

            for (header in headers) {
                val planId = header.planId?.toLong() ?: continue
                val plan = api.plans.getStudentPlan(studentId, planId)
                assertNotNull(plan)
                logger.info("  getStudentPlan: studentId=$studentId, planId=$planId, stato=${plan.state}, statoDes=${plan.stateDescription}")
                assertNotNull(plan.state, "plan state should not be null")
                logger.info("  plan activities: ${plan.activity.size}, rules: ${plan.rules.size}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetStudentPlan: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetPlanPrint() {
        try {
            val studentId = studentProfile.studentId
            val headers = api.plans.getStudentPlanHeaders(studentId)
            assertNotNull(headers)

            if (headers.isEmpty()) {
                logger.warning("No plan headers found for studentId=$studentId, skipping getPlanPrint test")
                return
            }

            for (header in headers) {
                val planId = header.planId?.toLong() ?: continue
                try {
                    val print = api.plans.getPlanPrint(studentId, planId)
                    assertNotNull(print)
                    logger.info("  getPlanPrint: studentId=$studentId, planId=$planId, print length=${print.length}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getPlanPrint validation error for planId=$planId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetPlanPrint: insufficient permissions - ${e.message}")
        }
    }

    @Test
    @Disabled("Destructive: creates a new study plan for the student - not easily reversible")
    suspend fun testPostStudentPlan() {
        // NOTE: Disabled because posting a study plan modifies the student's academic plan.
        // This requires specific plan data and approval workflows to be tested safely.
    }
}
