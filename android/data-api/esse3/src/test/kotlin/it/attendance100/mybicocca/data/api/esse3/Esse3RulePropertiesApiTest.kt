package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3RulePropertiesApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3RulePropertiesApiTest::class.java.name)
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
    fun testGetPropaedeuticityRegulationsDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPropaedeuticityRegulationsDefaults: calling getPropaedeuticityRegulations() with defaults")
            val result = api.ruleProperties.getPropaedeuticityRegulations()
            logger.info("testGetPropaedeuticityRegulationsDefaults: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getPropaedeuticityRegulations() default should return non-empty list")
            for (regulation in result) {
                logger.info(
                    "testGetPropaedeuticityRegulationsDefaults: proposalRuleId=${regulation.proposalRuleId}, " +
                        "facultyId=${regulation.facultyId}, facultyCode=${regulation.facultyCode}, " +
                        "courseOfStudyId=${regulation.courseOfStudyId}, courseOfStudyCode=${regulation.courseOfStudyCode}, " +
                        "courseOfStudyDescription=${regulation.courseOfStudyDescription}, " +
                        "courseTypeCode=${regulation.courseTypeCode}, cohort=${regulation.cohort}, " +
                        "academicYearOrderId=${regulation.academicYearOrderId}, state=${regulation.state}"
                )
            }
        } else {
            logger.info("testGetPropaedeuticityRegulationsDefaults: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.ruleProperties.getPropaedeuticityRegulations()
            }
            logger.info("testGetPropaedeuticityRegulationsDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPropaedeuticityRegulationsPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPropaedeuticityRegulationsPagination: calling getPropaedeuticityRegulations() with start=0, limit=5")
            val paginatedResult = api.ruleProperties.getPropaedeuticityRegulations(
                start = 0,
                limit = 5
            )
            logger.info("testGetPropaedeuticityRegulationsPagination: result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            for (regulation in paginatedResult) {
                logger.info(
                    "testGetPropaedeuticityRegulationsPagination: proposalRuleId=${regulation.proposalRuleId}, " +
                        "courseOfStudyCode=${regulation.courseOfStudyCode}, cohort=${regulation.cohort}"
                )
            }
        } else {
            logger.info("testGetPropaedeuticityRegulationsPagination: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.ruleProperties.getPropaedeuticityRegulations(start = 0, limit = 5)
            }
            logger.info("testGetPropaedeuticityRegulationsPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPropaedeuticityRegulationsWithOptionalParams() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: fetching full list first to extract filter values")
            val allRegulations = api.ruleProperties.getPropaedeuticityRegulations(start = 0, limit = 5)
            assertTrue(allRegulations.isNotEmpty(), "Need at least one regulation to test optional params")
            val firstRegulation = allRegulations.first()

            if (firstRegulation.facultyId != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by facultyId=${firstRegulation.facultyId}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    facultyId = firstRegulation.facultyId
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by facultyId result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by facultyId should return non-empty list")
            }

            if (firstRegulation.courseOfStudyId != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by courseOfStudyId=${firstRegulation.courseOfStudyId}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    courseOfStudyId = firstRegulation.courseOfStudyId
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by courseOfStudyId result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseOfStudyId should return non-empty list")
            }

            if (firstRegulation.courseTypeCode != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by courseTypeCode=${firstRegulation.courseTypeCode}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    courseTypeCode = firstRegulation.courseTypeCode
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by courseTypeCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseTypeCode should return non-empty list")
            }

            if (firstRegulation.cohort != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by cohort=${firstRegulation.cohort}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    cohort = firstRegulation.cohort?.toLong()
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by cohort result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by cohort should return non-empty list")
            }

            if (firstRegulation.facultyCode != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by facultyCode=${firstRegulation.facultyCode}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    facultyCode = firstRegulation.facultyCode
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by facultyCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by facultyCode should return non-empty list")
            }

            if (firstRegulation.courseOfStudyCode != null) {
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtering by courseOfStudyCode=${firstRegulation.courseOfStudyCode}")
                val filtered = api.ruleProperties.getPropaedeuticityRegulations(
                    courseOfStudyCode = firstRegulation.courseOfStudyCode
                )
                logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: filtered by courseOfStudyCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseOfStudyCode should return non-empty list")
            }

            logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: calling with fields param")
            val resultWithFields = api.ruleProperties.getPropaedeuticityRegulations(
                start = 0,
                limit = 5,
                fields = "regpropId,cdsCod,cdsDes"
            )
            logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: result with fields size=${resultWithFields.size}")
        } else {
            logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.ruleProperties.getPropaedeuticityRegulations(start = 0, limit = 5)
            }
            logger.info("testGetPropaedeuticityRegulationsWithOptionalParams: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPropaedeuticityRegulationWithDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPropaedeuticityRegulationWithDetails: fetching regulations list to get a proposalRuleId")
            val regulations = api.ruleProperties.getPropaedeuticityRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test detail endpoint")
            val proposalRuleId = regulations.first().proposalRuleId
            assertNotNull(proposalRuleId, "proposalRuleId should not be null for the first regulation")

            logger.info("testGetPropaedeuticityRegulationWithDetails: calling getPropaedeuticityRegulationWithDetails(proposalRuleId=$proposalRuleId)")
            val result = api.ruleProperties.getPropaedeuticityRegulationWithDetails(
                proposalRuleId = proposalRuleId
            )
            logger.info(
                "testGetPropaedeuticityRegulationWithDetails: proposalRuleId=${result.proposalRuleId}, " +
                    "facultyId=${result.facultyId}, facultyCode=${result.facultyCode}, " +
                    "courseOfStudyId=${result.courseOfStudyId}, courseOfStudyCode=${result.courseOfStudyCode}, " +
                    "courseOfStudyDescription=${result.courseOfStudyDescription}, " +
                    "cohort=${result.cohort}, state=${result.state}, " +
                    "constraints size=${result.constraints.size}"
            )
            assertNotNull(result.proposalRuleId, "Returned proposalRuleId should not be null")

            for (constraint in result.constraints) {
                logger.info(
                    "testGetPropaedeuticityRegulationWithDetails: constraint proposalRuleWinnerId=${constraint.proposalRuleWinnerId}, " +
                        "constraintType=${constraint.constraintType}, courseYear=${constraint.courseYear}, " +
                        "sectorCode=${constraint.sectorCode}, orProposalRules size=${constraint.orProposalRules.size}"
                )
            }

            logger.info("testGetPropaedeuticityRegulationWithDetails: calling with fields param")
            val resultWithFields = api.ruleProperties.getPropaedeuticityRegulationWithDetails(
                proposalRuleId = proposalRuleId,
                fields = "regpropId,cdsCod,cdsDes"
            )
            logger.info("testGetPropaedeuticityRegulationWithDetails: result with fields proposalRuleId=${resultWithFields.proposalRuleId}")

            logger.info("testGetPropaedeuticityRegulationWithDetails: calling with optionalFields param")
            val resultWithOptFields = api.ruleProperties.getPropaedeuticityRegulationWithDetails(
                proposalRuleId = proposalRuleId,
                optionalFields = "vincoli"
            )
            logger.info(
                "testGetPropaedeuticityRegulationWithDetails: result with optionalFields proposalRuleId=${resultWithOptFields.proposalRuleId}, " +
                    "constraints size=${resultWithOptFields.constraints.size}"
            )
        } else {
            logger.info("testGetPropaedeuticityRegulationWithDetails: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.ruleProperties.getPropaedeuticityRegulationWithDetails(proposalRuleId = 1)
            }
            logger.info("testGetPropaedeuticityRegulationWithDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRegulationConstraints() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetRegulationConstraints: fetching regulations list to derive path params")
            val regulations = api.ruleProperties.getPropaedeuticityRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test constraints endpoint")
            val proposalRuleId = regulations.first().proposalRuleId
            assertNotNull(proposalRuleId, "proposalRuleId should not be null for the first regulation")

            logger.info("testGetRegulationConstraints: fetching details for proposalRuleId=$proposalRuleId to get a proposalRuleWinnerId")
            val detail = api.ruleProperties.getPropaedeuticityRegulationWithDetails(
                proposalRuleId = proposalRuleId
            )
            logger.info("testGetRegulationConstraints: detail constraints size=${detail.constraints.size}")

            if (detail.constraints.isNotEmpty()) {
                val firstConstraint = detail.constraints.first()
                val proposalRuleWinnerId = firstConstraint.proposalRuleWinnerId
                assertNotNull(proposalRuleWinnerId, "proposalRuleWinnerId should not be null for the first constraint")

                logger.info("testGetRegulationConstraints: calling getRegulationConstraints(proposalRuleId=$proposalRuleId, proposalRuleWinnerId=$proposalRuleWinnerId)")
                val constraints = api.ruleProperties.getRegulationConstraints(
                    proposalRuleId = proposalRuleId,
                    proposalRuleWinnerId = proposalRuleWinnerId
                )
                logger.info("testGetRegulationConstraints: result size=${constraints.size}")
                assertTrue(constraints.isNotEmpty(), "getRegulationConstraints() should return non-empty list")
                for (constraint in constraints) {
                    logger.info(
                        "testGetRegulationConstraints: proposalRuleWinnerId=${constraint.proposalRuleWinnerId}, " +
                            "proposalRuleId=${constraint.proposalRuleId}, constraintType=${constraint.constraintType}, " +
                            "courseYear=${constraint.courseYear}, sectorCode=${constraint.sectorCode}, " +
                            "orProposalRules size=${constraint.orProposalRules.size}"
                    )
                }

                logger.info("testGetRegulationConstraints: calling with fields param")
                val constraintsWithFields = api.ruleProperties.getRegulationConstraints(
                    proposalRuleId = proposalRuleId,
                    proposalRuleWinnerId = proposalRuleWinnerId,
                    fields = "regpropVincId,regpropId"
                )
                logger.info("testGetRegulationConstraints: result with fields size=${constraintsWithFields.size}")

                logger.info("testGetRegulationConstraints: calling with optionalFields param")
                val constraintsWithOptFields = api.ruleProperties.getRegulationConstraints(
                    proposalRuleId = proposalRuleId,
                    proposalRuleWinnerId = proposalRuleWinnerId,
                    optionalFields = "regolePropOR"
                )
                logger.info("testGetRegulationConstraints: result with optionalFields size=${constraintsWithOptFields.size}")
            } else {
                logger.info("testGetRegulationConstraints: no constraints found for proposalRuleId=$proposalRuleId, skipping sub-resource test")
            }
        } else {
            logger.info("testGetRegulationConstraints: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.ruleProperties.getRegulationConstraints(
                    proposalRuleId = 1,
                    proposalRuleWinnerId = 1
                )
            }
            logger.info("testGetRegulationConstraints: Esse3Exception thrown as expected")
        }
    }
}
