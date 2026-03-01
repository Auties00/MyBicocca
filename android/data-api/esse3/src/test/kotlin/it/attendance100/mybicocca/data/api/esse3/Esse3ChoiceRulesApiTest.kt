package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3ChoiceRulesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetChoiceRegulations() {
        val regulations = api.choiceRules.getChoiceRegulations()
        assertNotNull(regulations)
        assertTrue(regulations.isNotEmpty(), "Choice regulations should not be empty")
        logger.info("getChoiceRegulations: found ${regulations.size} regulations")

        for (regulation in regulations.take(5)) {
            assertNotNull(regulation)
            assertNotNull(regulation.choiceRegulationId, "choiceRegulationId should not be null")
            logger.info(
                "  regulation: id=${regulation.choiceRegulationId}, " +
                    "cdsId=${regulation.courseOfStudyId}, state=${regulation.state}"
            )
        }
    }

    @Test
    suspend fun testGetChoiceRegulationsWithFilters() {
        val courseOfStudyId = studentProfile.degreeCourseId

        // Filter by courseOfStudyId
        val byCourse = api.choiceRules.getChoiceRegulations(courseOfStudyId = courseOfStudyId)
        assertNotNull(byCourse)
        logger.info("getChoiceRegulations (cdsId=$courseOfStudyId): found ${byCourse.size} regulations")

        // Filter with limit
        val limited = api.choiceRules.getChoiceRegulations(limit = 5, start = 0)
        assertNotNull(limited)
        assertTrue(limited.size <= 5, "Should return at most 5 results")
        logger.info("getChoiceRegulations (limit=5): found ${limited.size} regulations")
    }

    @Test
    suspend fun testGetChoiceRegulationWithSchemas() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 5)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val id = regulation.choiceRegulationId ?: continue
            try {
                val withSchemas = api.choiceRules.getChoiceRegulationWithSchemas(id)
                assertNotNull(withSchemas)
                assertTrue(withSchemas.choiceRegulationId == id, "choiceRegulationId should match")
                logger.info(
                    "getChoiceRegulationWithSchemas($id): state=${withSchemas.state}, " +
                        "schemas=${withSchemas.planSchemas.size}, windows=${withSchemas.compilationWindows.size}"
                )
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRegulationWithSchemas($id): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetChoiceRegulationWithDetails() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val id = regulation.choiceRegulationId ?: continue
            try {
                val withDetails = api.choiceRules.getChoiceRegulationWithDetails(id)
                assertNotNull(withDetails)
                logger.info("getChoiceRegulationWithDetails($id): ok")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRegulationWithDetails($id): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetChoiceRegulationWindows() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 5)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val id = regulation.choiceRegulationId ?: continue
            try {
                val windows = api.choiceRules.getChoiceRegulationWindows(id)
                assertNotNull(windows)
                logger.info("getChoiceRegulationWindows($id): found ${windows.size} windows")

                for (window in windows) {
                    assertNotNull(window)
                    val windowId = window.finalRegulationChoiceId ?: continue
                    logger.info("  window: id=$windowId, regsceId=${window.choiceRegulationId}")

                    val single = api.choiceRules.getChoiceRegulationWindow(id, windowId)
                    assertNotNull(single)
                    assertTrue(
                        single.finalRegulationChoiceId == windowId,
                        "getChoiceRegulationWindow windowId should match"
                    )
                    logger.info("  getChoiceRegulationWindow($id, $windowId): ok")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRegulationWindows($id): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetChoiceRulesAndDetails() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val rules = api.choiceRules.getChoiceRules(regId)
                assertNotNull(rules)
                logger.info("getChoiceRules($regId): found ${rules.size} rules")

                for (rule in rules) {
                    assertNotNull(rule)
                    val choiceId = rule.choiceId ?: continue
                    logger.info(
                        "  rule: choiceId=$choiceId, sceId=${rule.choiceRuleId}, " +
                            "des=${rule.description}, type=${rule.choiceType}"
                    )

                    val withDetails = api.choiceRules.getChoiceRuleWithDetails(regId, choiceId)
                    assertNotNull(withDetails)
                    assertTrue(withDetails.choiceId == choiceId, "getChoiceRuleWithDetails choiceId should match")
                    logger.info("  getChoiceRuleWithDetails($regId, $choiceId): ok")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRules($regId): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetStudyPlanSchemasAndDetails() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val schemas = api.choiceRules.getStudyPlanSchemas(regId)
                assertNotNull(schemas)
                logger.info("getStudyPlanSchemas($regId): found ${schemas.size} schemas")

                for (schema in schemas) {
                    assertNotNull(schema)
                    val schemaId = schema.schemaId ?: continue
                    logger.info("  schema: id=$schemaId, code=${schema.schemaCode}, des=${schema.schemaDescription}")

                    val withDetails = api.choiceRules.getStudyPlanSchemaWithDetails(regId, schemaId)
                    assertNotNull(withDetails)
                    assertTrue(withDetails.schemaId == schemaId, "getStudyPlanSchemaWithDetails schemaId should match")
                    logger.info("  getStudyPlanSchemaWithDetails($regId, $schemaId): ok")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getStudyPlanSchemas($regId): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetPlansStatsByChoiceRegulationId() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val id = regulation.choiceRegulationId ?: continue
            try {
                val stats = api.choiceRules.getPlansStatsByChoiceRegulationId(id)
                assertNotNull(stats)
                logger.info(
                    "getPlansStatsByChoiceRegulationId($id): " +
                        "plans=${stats.planNumber}, validStd=${stats.validStandardPlansNumber}"
                )
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getPlansStatsByChoiceRegulationId($id): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetPlansStatsByChoiceId() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val rules = api.choiceRules.getChoiceRules(regId)
                for (rule in rules) {
                    val choiceId = rule.choiceId ?: continue
                    try {
                        val stats = api.choiceRules.getPlansStatsByChoiceId(regId, choiceId)
                        assertNotNull(stats)
                        logger.info(
                            "getPlansStatsByChoiceId($regId, $choiceId): " +
                                "plans=${stats.planNumber}, validStd=${stats.validStandardPlansNumber}"
                        )
                    } catch (_: Esse3NotAuthorizedException) {
                        logger.warning("Skipping getPlansStatsByChoiceId($regId,$choiceId): insufficient permissions")
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRules($regId) for stats: insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetPlansStatsBySchemaId() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val schemas = api.choiceRules.getStudyPlanSchemas(regId)
                for (schema in schemas) {
                    val schemaId = schema.schemaId ?: continue
                    try {
                        val stats = api.choiceRules.getPlansStatsBySchemaId(regId, schemaId)
                        assertNotNull(stats)
                        logger.info(
                            "getPlansStatsBySchemaId($regId, $schemaId): " +
                                "plans=${stats.planNumber}, validStd=${stats.validStandardPlansNumber}"
                        )
                    } catch (_: Esse3NotAuthorizedException) {
                        logger.warning("Skipping getPlansStatsBySchemaId($regId,$schemaId): insufficient permissions")
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getStudyPlanSchemas($regId) for stats: insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetLinkedPlansByChoiceId() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val rules = api.choiceRules.getChoiceRules(regId)
                for (rule in rules.take(2)) {
                    val choiceId = rule.choiceId ?: continue
                    try {
                        val plans = api.choiceRules.getLinkedPlansByChoiceId(regId, choiceId, limit = 10)
                        assertNotNull(plans)
                        logger.info("getLinkedPlansByChoiceId($regId, $choiceId): found ${plans.size} plans")
                        for (plan in plans.take(3)) {
                            assertNotNull(plan)
                            logger.info("  plan: matId=${plan.matId}, state=${plan.state}")
                        }
                    } catch (_: Esse3NotAuthorizedException) {
                        logger.warning("Skipping getLinkedPlansByChoiceId($regId,$choiceId): insufficient permissions")
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRules($regId): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetLinkedPlansByTeachingActivityChoiceId() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 3)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val regId = regulation.choiceRegulationId ?: continue
            try {
                val rules = api.choiceRules.getChoiceRules(regId)
                for (rule in rules.take(2)) {
                    val choiceId = rule.choiceId ?: continue
                    try {
                        val ruleDetails = api.choiceRules.getChoiceRuleWithDetails(regId, choiceId)
                        // Find first activity in blocks to use as teachingActivityChoiceId
                        val firstBlock = ruleDetails.blocks.firstOrNull()
                        val firstActivity = firstBlock?.activity?.firstOrNull()
                        val activityChoiceId = firstActivity?.teachingActivityChoiceId ?: continue

                        val plans = api.choiceRules.getLinkedPlansByTeachingActivityChoiceId(
                            regId, choiceId, activityChoiceId, limit = 10
                        )
                        assertNotNull(plans)
                        logger.info(
                            "getLinkedPlansByTeachingActivityChoiceId($regId,$choiceId,$activityChoiceId): " +
                                "found ${plans.size} plans"
                        )
                        for (plan in plans.take(3)) {
                            assertNotNull(plan)
                            logger.info("  plan: matId=${plan.matId}, state=${plan.state}")
                        }
                    } catch (_: Esse3NotAuthorizedException) {
                        logger.warning(
                            "Skipping getLinkedPlansByTeachingActivityChoiceId($regId,$choiceId): " +
                                "insufficient permissions"
                        )
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getChoiceRules($regId): insufficient permissions - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetPropaedeuticityRegulationByChoiceRegulation() {
        val regulations = api.choiceRules.getChoiceRegulations(limit = 5)
        assertTrue(regulations.isNotEmpty(), "Need at least one regulation for this test")

        for (regulation in regulations) {
            val id = regulation.choiceRegulationId ?: continue
            try {
                val prereq = api.choiceRules.getPropaedeuticityRegulationByChoiceRegulation(id)
                assertNotNull(prereq)
                logger.info(
                    "getPropaedeuticityRegulationByChoiceRegulation($id): " +
                        "regpropId=${prereq.proposalRuleId}, state=${prereq.state}"
                )
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning(
                    "Skipping getPropaedeuticityRegulationByChoiceRegulation($id): insufficient permissions - ${e.message}"
                )
            } catch (e: Esse3ValidationException) {
                logger.warning(
                    "Skipping getPropaedeuticityRegulationByChoiceRegulation($id): validation error - ${e.message}"
                )
            }
        }
    }

    @Test
    @Disabled("Mutation: patches choice regulation state - requires TECHNICAL_USER permissions")
    suspend fun testPatchChoiceRegulation() {
        // NOTE: Disabled because it mutates server-side data and requires TECHNICAL_USER permissions.
    }

    @Test
    @Disabled("Mutation: updates linked plans count - requires TECHNICAL_USER permissions")
    suspend fun testPutLinkedPlansCount() {
        // NOTE: Disabled because it mutates server-side data and requires TECHNICAL_USER permissions.
    }
}
