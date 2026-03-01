package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3RulePropertiesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetPropaedeuticityRegulations() {
        val regulations = api.ruleProperties.getPropaedeuticityRegulations()
        assertNotNull(regulations)
        logger.info("getPropaedeuticityRegulations: found ${regulations.size} regulations")

        for (regulation in regulations.take(5)) {
            assertNotNull(regulation)
            assertNotNull(regulation.proposalRuleId, "proposalRuleId should not be null")
            logger.info(
                "  regulation: id=${regulation.proposalRuleId}, " +
                    "cdsId=${regulation.courseOfStudyId}, state=${regulation.state}"
            )
        }
    }

    @Test
    suspend fun testGetPropaedeuticityRegulationsWithFilters() {
        val courseOfStudyId = studentProfile.degreeCourseId

        // Filter by courseOfStudyId
        val byCourse = api.ruleProperties.getPropaedeuticityRegulations(courseOfStudyId = courseOfStudyId)
        assertNotNull(byCourse)
        logger.info("getPropaedeuticityRegulations (cdsId=$courseOfStudyId): found ${byCourse.size} regulations")

        // Filter with limit
        val limited = api.ruleProperties.getPropaedeuticityRegulations(limit = 5, start = 0)
        assertNotNull(limited)
        assertTrue(limited.size <= 5, "Should return at most 5 results")
        logger.info("getPropaedeuticityRegulations (limit=5): found ${limited.size} regulations")

        // Filter by state
        try {
            val published = api.ruleProperties.getPropaedeuticityRegulations(proposalRuleState = "P")
            assertNotNull(published)
            logger.info("getPropaedeuticityRegulations (state=P): found ${published.size} regulations")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping state filter: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping state filter: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetPropaedeuticityRegulationWithDetails() {
        val regulations = api.ruleProperties.getPropaedeuticityRegulations(limit = 10)
        if (regulations.isEmpty()) {
            logger.warning("No propaedeuticity regulations found, skipping detail tests")
            return
        }

        for (regulation in regulations) {
            val id = regulation.proposalRuleId ?: continue
            try {
                val withDetails = api.ruleProperties.getPropaedeuticityRegulationWithDetails(id)
                assertNotNull(withDetails)
                assertTrue(
                    withDetails.proposalRuleId == id,
                    "getPropaedeuticityRegulationWithDetails($id): returned id should match"
                )
                logger.info(
                    "getPropaedeuticityRegulationWithDetails($id): " +
                        "state=${withDetails.state}, constraints=${withDetails.constraints.size}"
                )

                for (constraint in withDetails.constraints.take(3)) {
                    assertNotNull(constraint)
                    logger.info(
                        "  constraint: winnerId=${constraint.proposalRuleWinnerId}, " +
                            "type=${constraint.constraintType}, ors=${constraint.orProposalRules.size}"
                    )
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getPropaedeuticityRegulationWithDetails($id): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetRegulationConstraints() {
        val regulations = api.ruleProperties.getPropaedeuticityRegulations(limit = 10)
        if (regulations.isEmpty()) {
            logger.warning("No propaedeuticity regulations found, skipping constraint tests")
            return
        }

        for (regulation in regulations) {
            val id = regulation.proposalRuleId ?: continue
            try {
                val withDetails = api.ruleProperties.getPropaedeuticityRegulationWithDetails(id)
                val firstConstraint = withDetails.constraints.firstOrNull()
                val winnerId = firstConstraint?.proposalRuleWinnerId ?: continue

                val constraints = api.ruleProperties.getRegulationConstraints(id, winnerId)
                assertNotNull(constraints)
                logger.info("getRegulationConstraints($id, $winnerId): found ${constraints.size} constraints")

                for (constraint in constraints) {
                    assertNotNull(constraint)
                    logger.info(
                        "  constraint: winnerId=${constraint.proposalRuleWinnerId}, " +
                            "type=${constraint.constraintType}, year=${constraint.courseYear}"
                    )
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getRegulationConstraints($id): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getRegulationConstraints($id): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetPropaedeuticityRegulationsForStudentCourse() {
        val courseOfStudyId = studentProfile.degreeCourseId
        val courseOfStudyCode = api.structure.getCourseOfStudy(courseOfStudyId).courseOfStudyCode

        if (courseOfStudyCode != null) {
            val regulations = api.ruleProperties.getPropaedeuticityRegulations(
                courseOfStudyCode = courseOfStudyCode
            )
            assertNotNull(regulations)
            logger.info(
                "getPropaedeuticityRegulations (cdsCod=$courseOfStudyCode): found ${regulations.size} regulations"
            )
            for (reg in regulations) {
                assertNotNull(reg)
                logger.info("  reg: id=${reg.proposalRuleId}, state=${reg.state}, cohort=${reg.cohort}")
            }
        }
    }

    @Test
    suspend fun testGetPropaedeuticityRegulationsWithOrderAndOptionalFields() {
        try {
            val regulations = api.ruleProperties.getPropaedeuticityRegulations(
                limit = 5,
                order = "regpropId"
            )
            assertNotNull(regulations)
            logger.info("getPropaedeuticityRegulations (order=regpropId, limit=5): found ${regulations.size} regulations")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping ordered query: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping ordered query: validation error - ${e.message}")
        }
    }
}
