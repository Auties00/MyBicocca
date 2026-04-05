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

class Esse3ChoiceRulesApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3ChoiceRulesApiTest::class.java.name)
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
    fun testGetChoiceRegulationsDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationsDefaults: calling getChoiceRegulations() with defaults")
            val result = api.choiceRules.getChoiceRegulations()
            logger.info("testGetChoiceRegulationsDefaults: result size=${result.size}")
            assertNotNull(result, "getChoiceRegulations() default result should not be null")
            assertTrue(result.isNotEmpty(), "getChoiceRegulations() default should return non-empty list")
            for (regulation in result) {
                assertNotNull(regulation.choiceRegulationId, "choiceRegulationId should not be null")
                logger.info(
                    "testGetChoiceRegulationsDefaults: choiceRegulationId=${regulation.choiceRegulationId}, " +
                        "facultyId=${regulation.facultyId}, facultyCode=${regulation.facultyCode}, " +
                        "courseOfStudyId=${regulation.courseOfStudyId}, courseOfStudyCode=${regulation.courseOfStudyCode}, " +
                        "courseOfStudyDescription=${regulation.courseOfStudyDescription}, " +
                        "courseTypeCode=${regulation.courseTypeCode}, cohort=${regulation.cohort}, " +
                        "academicYearOrderId=${regulation.academicYearOrderId}, " +
                        "academicYearRevisionId=${regulation.academicYearRevisionId}, " +
                        "state=${regulation.state}, lastModificationDate=${regulation.lastModificationDate}"
                )
            }
        } else {
            logger.info("testGetChoiceRegulationsDefaults: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulations()
            }
            logger.info("testGetChoiceRegulationsDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationsPagination() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationsPagination: calling getChoiceRegulations() with start=0, limit=5")
            val paginatedResult = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            logger.info("testGetChoiceRegulationsPagination: result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            for (regulation in paginatedResult) {
                logger.info(
                    "testGetChoiceRegulationsPagination: choiceRegulationId=${regulation.choiceRegulationId}, " +
                        "courseOfStudyCode=${regulation.courseOfStudyCode}, cohort=${regulation.cohort}"
                )
            }
        } else {
            logger.info("testGetChoiceRegulationsPagination: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            }
            logger.info("testGetChoiceRegulationsPagination: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationsWithOptionalParams() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationsWithOptionalParams: fetching full list first to extract filter values")
            val allRegulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(allRegulations.isNotEmpty(), "Need at least one regulation to test optional params")
            val firstRegulation = allRegulations.first()

            if (firstRegulation.courseOfStudyId != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by courseOfStudyId=${firstRegulation.courseOfStudyId}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    courseOfStudyId = firstRegulation.courseOfStudyId
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by courseOfStudyId result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseOfStudyId should return non-empty list")
            }

            if (firstRegulation.facultyId != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by facultyId=${firstRegulation.facultyId}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    facultyId = firstRegulation.facultyId
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by facultyId result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by facultyId should return non-empty list")
            }

            if (firstRegulation.courseTypeCode != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by courseTypeCode=${firstRegulation.courseTypeCode}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    courseTypeCode = firstRegulation.courseTypeCode
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by courseTypeCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseTypeCode should return non-empty list")
            }

            if (firstRegulation.cohort != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by cohort=${firstRegulation.cohort}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    cohort = firstRegulation.cohort?.toLong()
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by cohort result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by cohort should return non-empty list")
            }

            if (firstRegulation.facultyCode != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by facultyCode=${firstRegulation.facultyCode}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    facultyCode = firstRegulation.facultyCode
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by facultyCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by facultyCode should return non-empty list")
            }

            if (firstRegulation.courseOfStudyCode != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by courseOfStudyCode=${firstRegulation.courseOfStudyCode}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    courseOfStudyCode = firstRegulation.courseOfStudyCode
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by courseOfStudyCode result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by courseOfStudyCode should return non-empty list")
            }

            if (firstRegulation.academicYearOrderId != null) {
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtering by academicYearOrderId=${firstRegulation.academicYearOrderId}")
                val filtered = api.choiceRules.getChoiceRegulations(
                    academicYearOrderId = firstRegulation.academicYearOrderId
                )
                logger.info("testGetChoiceRegulationsWithOptionalParams: filtered by academicYearOrderId result size=${filtered.size}")
                assertTrue(filtered.isNotEmpty(), "Filtering by academicYearOrderId should return non-empty list")
            }

            logger.info("testGetChoiceRegulationsWithOptionalParams: calling with fields param")
            val resultWithFields = api.choiceRules.getChoiceRegulations(
                start = 0,
                limit = 5,
                fields = "regsceId,cdsCod,cdsDes"
            )
            logger.info("testGetChoiceRegulationsWithOptionalParams: result with fields size=${resultWithFields.size}")
        } else {
            logger.info("testGetChoiceRegulationsWithOptionalParams: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            }
            logger.info("testGetChoiceRegulationsWithOptionalParams: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationWithSchemas() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationWithSchemas: fetching regulations list to get a choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test detail endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetChoiceRegulationWithSchemas: calling getChoiceRegulationWithSchemas(choiceRegulationId=$choiceRegulationId)")
            val result = api.choiceRules.getChoiceRegulationWithSchemas(choiceRegulationId)
            logger.info(
                "testGetChoiceRegulationWithSchemas: choiceRegulationId=${result.choiceRegulationId}, " +
                    "facultyId=${result.facultyId}, facultyCode=${result.facultyCode}, " +
                    "courseOfStudyId=${result.courseOfStudyId}, courseOfStudyCode=${result.courseOfStudyCode}, " +
                    "courseOfStudyDescription=${result.courseOfStudyDescription}, " +
                    "state=${result.state}, " +
                    "compilationWindows size=${result.compilationWindows.size}, " +
                    "planSchemas size=${result.planSchemas.size}"
            )
            assertNotNull(result.choiceRegulationId, "Returned choiceRegulationId should not be null")
            assertTrue(result.choiceRegulationId == choiceRegulationId, "choiceRegulationId should match the requested id")

            for (window in result.compilationWindows) {
                logger.info(
                    "testGetChoiceRegulationWithSchemas: window finalRegulationChoiceId=${window.finalRegulationChoiceId}, " +
                        "windowType=${window.windowType}, startDate=${window.startDate}, endDate=${window.endDate}"
                )
            }

            for (schema in result.planSchemas) {
                logger.info(
                    "testGetChoiceRegulationWithSchemas: schema schemaId=${schema.schemaId}, " +
                        "schemaCode=${schema.schemaCode}, schemaDescription=${schema.schemaDescription}"
                )
            }

            logger.info("testGetChoiceRegulationWithSchemas: calling with fields param")
            val resultWithFields = api.choiceRules.getChoiceRegulationWithSchemas(
                choiceRegulationId = choiceRegulationId,
                fields = "regsceId,cdsCod,cdsDes"
            )
            logger.info("testGetChoiceRegulationWithSchemas: result with fields choiceRegulationId=${resultWithFields.choiceRegulationId}")

            logger.info("testGetChoiceRegulationWithSchemas: calling with optionalFields param")
            val resultWithOptFields = api.choiceRules.getChoiceRegulationWithSchemas(
                choiceRegulationId = choiceRegulationId,
                optionalFields = "schemiDiPiano,finestreDiCompilazione"
            )
            logger.info(
                "testGetChoiceRegulationWithSchemas: result with optionalFields " +
                    "planSchemas size=${resultWithOptFields.planSchemas.size}, " +
                    "compilationWindows size=${resultWithOptFields.compilationWindows.size}"
            )
        } else {
            logger.info("testGetChoiceRegulationWithSchemas: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulationWithSchemas(choiceRegulationId = 1)
            }
            logger.info("testGetChoiceRegulationWithSchemas: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationWindows() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationWindows: fetching regulations list to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test windows endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetChoiceRegulationWindows: calling getChoiceRegulationWindows(choiceRegulationId=$choiceRegulationId)")
            val windows = api.choiceRules.getChoiceRegulationWindows(choiceRegulationId)
            logger.info("testGetChoiceRegulationWindows: result size=${windows.size}")
            assertNotNull(windows, "getChoiceRegulationWindows() result should not be null")

            for (window in windows) {
                logger.info(
                    "testGetChoiceRegulationWindows: finalRegulationChoiceId=${window.finalRegulationChoiceId}, " +
                        "choiceRegulationId=${window.choiceRegulationId}, " +
                        "windowType=${window.windowType}, " +
                        "startDate=${window.startDate}, endDate=${window.endDate}"
                )
            }

            logger.info("testGetChoiceRegulationWindows: calling with fields param")
            val resultWithFields = api.choiceRules.getChoiceRegulationWindows(
                choiceRegulationId = choiceRegulationId,
                fields = "finregsceId,regsceId,tipoFinestra"
            )
            logger.info("testGetChoiceRegulationWindows: result with fields size=${resultWithFields.size}")
        } else {
            logger.info("testGetChoiceRegulationWindows: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulationWindows(choiceRegulationId = 1)
            }
            logger.info("testGetChoiceRegulationWindows: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationWindow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationWindow: fetching regulations and windows to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test window detail endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val windows = api.choiceRules.getChoiceRegulationWindows(choiceRegulationId)
                if (windows.isEmpty()) {
                    logger.info("testGetChoiceRegulationWindow: no windows for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstWindow = windows.first()
                val finalRegulationChoiceId = firstWindow.finalRegulationChoiceId
                assertNotNull(finalRegulationChoiceId, "finalRegulationChoiceId should not be null")

                logger.info("testGetChoiceRegulationWindow: calling getChoiceRegulationWindow(choiceRegulationId=$choiceRegulationId, finalRegulationChoiceId=$finalRegulationChoiceId)")
                val result = api.choiceRules.getChoiceRegulationWindow(choiceRegulationId, finalRegulationChoiceId)
                logger.info(
                    "testGetChoiceRegulationWindow: finalRegulationChoiceId=${result.finalRegulationChoiceId}, " +
                        "choiceRegulationId=${result.choiceRegulationId}, " +
                        "windowType=${result.windowType}, " +
                        "startDate=${result.startDate}, endDate=${result.endDate}"
                )
                assertNotNull(result.finalRegulationChoiceId, "Returned finalRegulationChoiceId should not be null")
                assertTrue(result.finalRegulationChoiceId == finalRegulationChoiceId, "finalRegulationChoiceId should match")

                logger.info("testGetChoiceRegulationWindow: calling with fields param")
                val resultWithFields = api.choiceRules.getChoiceRegulationWindow(
                    choiceRegulationId = choiceRegulationId,
                    finalRegulationChoiceId = finalRegulationChoiceId,
                    fields = "finregsceId,regsceId"
                )
                logger.info("testGetChoiceRegulationWindow: result with fields finalRegulationChoiceId=${resultWithFields.finalRegulationChoiceId}")
                found = true
                break
            }

            if (!found) {
                logger.info("testGetChoiceRegulationWindow: no regulation with windows found, test inconclusive")
            }
        } else {
            logger.info("testGetChoiceRegulationWindow: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulationWindow(choiceRegulationId = 1, finalRegulationChoiceId = 1)
            }
            logger.info("testGetChoiceRegulationWindow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRulesDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRulesDefaults: fetching regulations list to derive choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test choice rules endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetChoiceRulesDefaults: calling getChoiceRules(choiceRegulationId=$choiceRegulationId)")
            val rules = api.choiceRules.getChoiceRules(choiceRegulationId)
            logger.info("testGetChoiceRulesDefaults: result size=${rules.size}")
            assertNotNull(rules, "getChoiceRules() result should not be null")

            for (rule in rules) {
                logger.info(
                    "testGetChoiceRulesDefaults: choiceId=${rule.choiceId}, " +
                        "choiceRegulationId=${rule.choiceRegulationId}, " +
                        "choiceRuleId=${rule.choiceRuleId}, " +
                        "orderNumber=${rule.orderNumber}, " +
                        "description=${rule.description}, " +
                        "choiceType=${rule.choiceType}, " +
                        "teachingUnitType=${rule.teachingUnitType}, " +
                        "minTeachingUnit=${rule.minTeachingUnit}, " +
                        "maxTeachingUnit=${rule.maxTeachingUnit}, " +
                        "schemas size=${rule.schemas.size}"
                )
            }

            logger.info("testGetChoiceRulesDefaults: calling with fields param")
            val resultWithFields = api.choiceRules.getChoiceRules(
                choiceRegulationId = choiceRegulationId,
                fields = "sceltaId,regsceId,des"
            )
            logger.info("testGetChoiceRulesDefaults: result with fields size=${resultWithFields.size}")

            logger.info("testGetChoiceRulesDefaults: calling with optionalFields param")
            val resultWithOptFields = api.choiceRules.getChoiceRules(
                choiceRegulationId = choiceRegulationId,
                optionalFields = "schemi"
            )
            logger.info("testGetChoiceRulesDefaults: result with optionalFields size=${resultWithOptFields.size}")
        } else {
            logger.info("testGetChoiceRulesDefaults: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRules(choiceRegulationId = 1)
            }
            logger.info("testGetChoiceRulesDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRuleWithDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRuleWithDetails: fetching regulations and rules to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test choice rule detail endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val rules = api.choiceRules.getChoiceRules(choiceRegulationId)
                if (rules.isEmpty()) {
                    logger.info("testGetChoiceRuleWithDetails: no rules for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstRule = rules.first()
                val choiceId = firstRule.choiceId
                assertNotNull(choiceId, "choiceId should not be null for the first rule")

                logger.info("testGetChoiceRuleWithDetails: calling getChoiceRuleWithDetails(choiceRegulationId=$choiceRegulationId, choiceId=$choiceId)")
                val result = api.choiceRules.getChoiceRuleWithDetails(choiceRegulationId, choiceId)
                logger.info(
                    "testGetChoiceRuleWithDetails: choiceId=${result.choiceId}, " +
                        "choiceRegulationId=${result.choiceRegulationId}, " +
                        "choiceRuleId=${result.choiceRuleId}, " +
                        "description=${result.description}, " +
                        "choiceType=${result.choiceType}, " +
                        "schemas size=${result.schemas.size}, " +
                        "blocks size=${result.blocks.size}, " +
                        "conditions size=${result.conditions.size}, " +
                        "orFilters size=${result.orFilters.size}"
                )
                assertNotNull(result.choiceId, "Returned choiceId should not be null")
                assertTrue(result.choiceId == choiceId, "choiceId should match the requested id")

                for (block in result.blocks) {
                    logger.info(
                        "testGetChoiceRuleWithDetails: block blockId=${block.blockId}, " +
                            "languageCode=${block.languageCode}, " +
                            "activities size=${block.activity.size}, " +
                            "conditions size=${block.conditions.size}"
                    )
                }

                logger.info("testGetChoiceRuleWithDetails: calling with fields param")
                val resultWithFields = api.choiceRules.getChoiceRuleWithDetails(
                    choiceRegulationId = choiceRegulationId,
                    choiceId = choiceId,
                    fields = "sceltaId,regsceId,des"
                )
                logger.info("testGetChoiceRuleWithDetails: result with fields choiceId=${resultWithFields.choiceId}")
                found = true
                break
            }

            if (!found) {
                logger.info("testGetChoiceRuleWithDetails: no regulation with rules found, test inconclusive")
            }
        } else {
            logger.info("testGetChoiceRuleWithDetails: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRuleWithDetails(choiceRegulationId = 1, choiceId = 1)
            }
            logger.info("testGetChoiceRuleWithDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudyPlanSchemasDefaults() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudyPlanSchemasDefaults: fetching regulations list to derive choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test schemas endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetStudyPlanSchemasDefaults: calling getStudyPlanSchemas(choiceRegulationId=$choiceRegulationId)")
            val schemas = api.choiceRules.getStudyPlanSchemas(choiceRegulationId)
            logger.info("testGetStudyPlanSchemasDefaults: result size=${schemas.size}")
            assertNotNull(schemas, "getStudyPlanSchemas() result should not be null")

            for (schema in schemas) {
                logger.info(
                    "testGetStudyPlanSchemasDefaults: schemaId=${schema.schemaId}, " +
                        "schemaCode=${schema.schemaCode}, " +
                        "schemaDescription=${schema.schemaDescription}, " +
                        "schemaDescriptionEnglish=${schema.schemaDescriptionEnglish}, " +
                        "studyPlanId=${schema.studyPlanId}, " +
                        "studyPlanCode=${schema.studyPlanCode}, " +
                        "statutoryFlag=${schema.statutoryFlag}, " +
                        "webViewFlag=${schema.webViewFlag}"
                )
            }

            logger.info("testGetStudyPlanSchemasDefaults: calling with fields param")
            val resultWithFields = api.choiceRules.getStudyPlanSchemas(
                choiceRegulationId = choiceRegulationId,
                fields = "schemaId,schemaCod,schemaDes"
            )
            logger.info("testGetStudyPlanSchemasDefaults: result with fields size=${resultWithFields.size}")
        } else {
            logger.info("testGetStudyPlanSchemasDefaults: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getStudyPlanSchemas(choiceRegulationId = 1)
            }
            logger.info("testGetStudyPlanSchemasDefaults: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudyPlanSchemaWithDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudyPlanSchemaWithDetails: fetching regulations and schemas to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test schema detail endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val schemas = api.choiceRules.getStudyPlanSchemas(choiceRegulationId)
                if (schemas.isEmpty()) {
                    logger.info("testGetStudyPlanSchemaWithDetails: no schemas for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstSchema = schemas.first()
                val schemaId = firstSchema.schemaId
                assertNotNull(schemaId, "schemaId should not be null for the first schema")

                logger.info("testGetStudyPlanSchemaWithDetails: calling getStudyPlanSchemaWithDetails(choiceRegulationId=$choiceRegulationId, schemaId=$schemaId)")
                val result = api.choiceRules.getStudyPlanSchemaWithDetails(choiceRegulationId, schemaId)
                logger.info(
                    "testGetStudyPlanSchemaWithDetails: schemaId=${result.schemaId}, " +
                        "schemaCode=${result.schemaCode}, " +
                        "schemaDescription=${result.schemaDescription}, " +
                        "studyPlanId=${result.studyPlanId}, " +
                        "studyPlanCode=${result.studyPlanCode}, " +
                        "choiceRules size=${result.choiceRules.size}"
                )
                assertNotNull(result.schemaId, "Returned schemaId should not be null")
                assertTrue(result.schemaId == schemaId, "schemaId should match the requested id")

                for (choiceRule in result.choiceRules) {
                    logger.info(
                        "testGetStudyPlanSchemaWithDetails: choiceRule choiceId=${choiceRule.choiceId}, " +
                            "description=${choiceRule.description}, " +
                            "choiceType=${choiceRule.choiceType}, " +
                            "blocks size=${choiceRule.blocks.size}"
                    )
                }

                logger.info("testGetStudyPlanSchemaWithDetails: calling with fields param")
                val resultWithFields = api.choiceRules.getStudyPlanSchemaWithDetails(
                    choiceRegulationId = choiceRegulationId,
                    schemaId = schemaId,
                    fields = "schemaId,schemaCod,schemaDes"
                )
                logger.info("testGetStudyPlanSchemaWithDetails: result with fields schemaId=${resultWithFields.schemaId}")
                found = true
                break
            }

            if (!found) {
                logger.info("testGetStudyPlanSchemaWithDetails: no regulation with schemas found, test inconclusive")
            }
        } else {
            logger.info("testGetStudyPlanSchemaWithDetails: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getStudyPlanSchemaWithDetails(choiceRegulationId = 1, schemaId = 1)
            }
            logger.info("testGetStudyPlanSchemaWithDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPlansStatsByChoiceRegulationId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPlansStatsByChoiceRegulationId: fetching regulations list to derive choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test plans stats endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetPlansStatsByChoiceRegulationId: calling getPlansStatsByChoiceRegulationId(choiceRegulationId=$choiceRegulationId)")
            val stats = api.choiceRules.getPlansStatsByChoiceRegulationId(choiceRegulationId)
            logger.info(
                "testGetPlansStatsByChoiceRegulationId: " +
                    "validStandardPlansNumber=${stats.validStandardPlansNumber}, " +
                    "validIndividualPlansNumber=${stats.validIndividualPlansNumber}, " +
                    "planNumber=${stats.planNumber}"
            )
            assertNotNull(stats, "getPlansStatsByChoiceRegulationId() result should not be null")
        } else {
            logger.info("testGetPlansStatsByChoiceRegulationId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getPlansStatsByChoiceRegulationId(choiceRegulationId = 1)
            }
            logger.info("testGetPlansStatsByChoiceRegulationId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPlansStatsByChoiceId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPlansStatsByChoiceId: fetching regulations and rules to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test plans stats by choice id endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val rules = api.choiceRules.getChoiceRules(choiceRegulationId)
                if (rules.isEmpty()) {
                    logger.info("testGetPlansStatsByChoiceId: no rules for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstRule = rules.first()
                val choiceId = firstRule.choiceId
                assertNotNull(choiceId, "choiceId should not be null for the first rule")

                logger.info("testGetPlansStatsByChoiceId: calling getPlansStatsByChoiceId(choiceRegulationId=$choiceRegulationId, choiceId=$choiceId)")
                val stats = api.choiceRules.getPlansStatsByChoiceId(choiceRegulationId, choiceId)
                logger.info(
                    "testGetPlansStatsByChoiceId: " +
                        "validStandardPlansNumber=${stats.validStandardPlansNumber}, " +
                        "validIndividualPlansNumber=${stats.validIndividualPlansNumber}, " +
                        "planNumber=${stats.planNumber}"
                )
                assertNotNull(stats, "getPlansStatsByChoiceId() result should not be null")
                found = true
                break
            }

            if (!found) {
                logger.info("testGetPlansStatsByChoiceId: no regulation with rules found, test inconclusive")
            }
        } else {
            logger.info("testGetPlansStatsByChoiceId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getPlansStatsByChoiceId(choiceRegulationId = 1, choiceId = 1)
            }
            logger.info("testGetPlansStatsByChoiceId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPlansStatsBySchemaId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPlansStatsBySchemaId: fetching regulations and schemas to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test plans stats by schema id endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val schemas = api.choiceRules.getStudyPlanSchemas(choiceRegulationId)
                if (schemas.isEmpty()) {
                    logger.info("testGetPlansStatsBySchemaId: no schemas for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstSchema = schemas.first()
                val schemaId = firstSchema.schemaId
                assertNotNull(schemaId, "schemaId should not be null for the first schema")

                logger.info("testGetPlansStatsBySchemaId: calling getPlansStatsBySchemaId(choiceRegulationId=$choiceRegulationId, schemaId=$schemaId)")
                val stats = api.choiceRules.getPlansStatsBySchemaId(choiceRegulationId, schemaId)
                logger.info(
                    "testGetPlansStatsBySchemaId: " +
                        "validStandardPlansNumber=${stats.validStandardPlansNumber}, " +
                        "validIndividualPlansNumber=${stats.validIndividualPlansNumber}, " +
                        "planNumber=${stats.planNumber}"
                )
                assertNotNull(stats, "getPlansStatsBySchemaId() result should not be null")
                found = true
                break
            }

            if (!found) {
                logger.info("testGetPlansStatsBySchemaId: no regulation with schemas found, test inconclusive")
            }
        } else {
            logger.info("testGetPlansStatsBySchemaId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getPlansStatsBySchemaId(choiceRegulationId = 1, schemaId = 1)
            }
            logger.info("testGetPlansStatsBySchemaId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLinkedPlansByChoiceId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLinkedPlansByChoiceId: fetching regulations and rules to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test linked plans by choice id endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val rules = api.choiceRules.getChoiceRules(choiceRegulationId)
                if (rules.isEmpty()) {
                    logger.info("testGetLinkedPlansByChoiceId: no rules for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                val firstRule = rules.first()
                val choiceId = firstRule.choiceId
                assertNotNull(choiceId, "choiceId should not be null for the first rule")

                logger.info("testGetLinkedPlansByChoiceId: calling getLinkedPlansByChoiceId(choiceRegulationId=$choiceRegulationId, choiceId=$choiceId) with defaults")
                val defaultResult = api.choiceRules.getLinkedPlansByChoiceId(choiceRegulationId, choiceId)
                logger.info("testGetLinkedPlansByChoiceId: default result size=${defaultResult.size}")
                assertNotNull(defaultResult, "getLinkedPlansByChoiceId() result should not be null")

                for (plan in defaultResult.take(5)) {
                    logger.info(
                        "testGetLinkedPlansByChoiceId: studentId=${plan.studentId}, " +
                            "planId=${plan.planId}, state=${plan.state}, " +
                            "stateDescription=${plan.stateDescription}, " +
                            "matId=${plan.matId}, choiceRegulationId=${plan.choiceRegulationId}"
                    )
                }

                logger.info("testGetLinkedPlansByChoiceId: calling with pagination start=0, limit=5")
                val paginatedResult = api.choiceRules.getLinkedPlansByChoiceId(
                    choiceRegulationId = choiceRegulationId,
                    choiceId = choiceId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetLinkedPlansByChoiceId: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

                found = true
                break
            }

            if (!found) {
                logger.info("testGetLinkedPlansByChoiceId: no regulation with rules found, test inconclusive")
            }
        } else {
            logger.info("testGetLinkedPlansByChoiceId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getLinkedPlansByChoiceId(choiceRegulationId = 1, choiceId = 1)
            }
            logger.info("testGetLinkedPlansByChoiceId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLinkedPlansByTeachingActivityChoiceId() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetLinkedPlansByTeachingActivityChoiceId: fetching regulations, rules and details to derive path params")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test linked plans by teaching activity choice id endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                val rules = api.choiceRules.getChoiceRules(choiceRegulationId)
                if (rules.isEmpty()) {
                    logger.info("testGetLinkedPlansByTeachingActivityChoiceId: no rules for choiceRegulationId=$choiceRegulationId, skipping")
                    continue
                }

                for (rule in rules.take(3)) {
                    val choiceId = rule.choiceId ?: continue
                    val ruleDetails = api.choiceRules.getChoiceRuleWithDetails(choiceRegulationId, choiceId)
                    val firstBlock = ruleDetails.blocks.firstOrNull() ?: continue
                    val firstActivity = firstBlock.activity.firstOrNull() ?: continue
                    val teachingActivityChoiceId = firstActivity.teachingActivityChoiceId ?: continue

                    logger.info(
                        "testGetLinkedPlansByTeachingActivityChoiceId: calling getLinkedPlansByTeachingActivityChoiceId(" +
                            "choiceRegulationId=$choiceRegulationId, choiceId=$choiceId, " +
                            "teachingActivityChoiceId=$teachingActivityChoiceId) with defaults"
                    )
                    val defaultResult = api.choiceRules.getLinkedPlansByTeachingActivityChoiceId(
                        choiceRegulationId, choiceId, teachingActivityChoiceId
                    )
                    logger.info("testGetLinkedPlansByTeachingActivityChoiceId: default result size=${defaultResult.size}")
                    assertNotNull(defaultResult, "getLinkedPlansByTeachingActivityChoiceId() result should not be null")

                    for (plan in defaultResult.take(5)) {
                        logger.info(
                            "testGetLinkedPlansByTeachingActivityChoiceId: studentId=${plan.studentId}, " +
                                "planId=${plan.planId}, state=${plan.state}, " +
                                "stateDescription=${plan.stateDescription}, " +
                                "matId=${plan.matId}, teachingActivityChoiceId=${plan.teachingActivityChoiceId}"
                        )
                    }

                    logger.info("testGetLinkedPlansByTeachingActivityChoiceId: calling with pagination start=0, limit=5")
                    val paginatedResult = api.choiceRules.getLinkedPlansByTeachingActivityChoiceId(
                        choiceRegulationId = choiceRegulationId,
                        choiceId = choiceId,
                        teachingActivityChoiceId = teachingActivityChoiceId,
                        start = 0,
                        limit = 5
                    )
                    logger.info("testGetLinkedPlansByTeachingActivityChoiceId: paginated result size=${paginatedResult.size}")
                    assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

                    found = true
                    break
                }

                if (found) break
            }

            if (!found) {
                logger.info("testGetLinkedPlansByTeachingActivityChoiceId: no regulation with rules and activities found, test inconclusive")
            }
        } else {
            logger.info("testGetLinkedPlansByTeachingActivityChoiceId: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getLinkedPlansByTeachingActivityChoiceId(
                    choiceRegulationId = 1,
                    choiceId = 1,
                    teachingActivityChoiceId = 1
                )
            }
            logger.info("testGetLinkedPlansByTeachingActivityChoiceId: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetChoiceRegulationWithDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetChoiceRegulationWithDetails: fetching regulations list to derive choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 3)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test full detail endpoint")
            val choiceRegulationId = regulations.first().choiceRegulationId
            assertNotNull(choiceRegulationId, "choiceRegulationId should not be null for the first regulation")

            logger.info("testGetChoiceRegulationWithDetails: calling getChoiceRegulationWithDetails(choiceRegulationId=$choiceRegulationId)")
            val result = api.choiceRules.getChoiceRegulationWithDetails(choiceRegulationId)
            logger.info(
                "testGetChoiceRegulationWithDetails: choiceRegulationId=${result.choiceRegulationId}, " +
                    "facultyId=${result.facultyId}, facultyCode=${result.facultyCode}, " +
                    "courseOfStudyId=${result.courseOfStudyId}, courseOfStudyCode=${result.courseOfStudyCode}, " +
                    "courseOfStudyDescription=${result.courseOfStudyDescription}, " +
                    "state=${result.state}, " +
                    "compilationWindows size=${result.compilationWindows.size}, " +
                    "planSchemas size=${result.planSchemas.size}"
            )
            assertNotNull(result.choiceRegulationId, "Returned choiceRegulationId should not be null")

            for (schema in result.planSchemas) {
                logger.info(
                    "testGetChoiceRegulationWithDetails: planSchema schemaId=${schema.schemaId}, " +
                        "schemaCode=${schema.schemaCode}, " +
                        "choiceRules size=${schema.choiceRules.size}"
                )
            }

            logger.info("testGetChoiceRegulationWithDetails: calling with fields param")
            val resultWithFields = api.choiceRules.getChoiceRegulationWithDetails(
                choiceRegulationId = choiceRegulationId,
                fields = "regsceId,cdsCod,cdsDes"
            )
            logger.info("testGetChoiceRegulationWithDetails: result with fields choiceRegulationId=${resultWithFields.choiceRegulationId}")
        } else {
            logger.info("testGetChoiceRegulationWithDetails: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getChoiceRegulationWithDetails(choiceRegulationId = 1)
            }
            logger.info("testGetChoiceRegulationWithDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPropaedeuticityRegulationByChoiceRegulation() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: fetching regulations list to derive choiceRegulationId")
            val regulations = api.choiceRules.getChoiceRegulations(start = 0, limit = 5)
            assertTrue(regulations.isNotEmpty(), "Need at least one regulation to test propaedeuticity endpoint")

            var found = false
            for (regulation in regulations) {
                val choiceRegulationId = regulation.choiceRegulationId ?: continue
                try {
                    logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: calling getPropaedeuticityRegulationByChoiceRegulation(choiceRegulationId=$choiceRegulationId)")
                    val result = api.choiceRules.getPropaedeuticityRegulationByChoiceRegulation(choiceRegulationId)
                    logger.info(
                        "testGetPropaedeuticityRegulationByChoiceRegulation: proposalRuleId=${result.proposalRuleId}, " +
                            "facultyId=${result.facultyId}, facultyCode=${result.facultyCode}, " +
                            "courseOfStudyId=${result.courseOfStudyId}, courseOfStudyCode=${result.courseOfStudyCode}, " +
                            "state=${result.state}, " +
                            "constraints size=${result.constraints.size}"
                    )
                    assertNotNull(result, "getPropaedeuticityRegulationByChoiceRegulation() result should not be null")

                    for (constraint in result.constraints) {
                        logger.info(
                            "testGetPropaedeuticityRegulationByChoiceRegulation: constraint " +
                                "proposalRuleWinnerId=${constraint.proposalRuleWinnerId}, " +
                                "constraintType=${constraint.constraintType}, " +
                                "courseYear=${constraint.courseYear}, " +
                                "sectorCode=${constraint.sectorCode}, " +
                                "orProposalRules size=${constraint.orProposalRules.size}"
                        )
                    }

                    logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: calling with fields param")
                    val resultWithFields = api.choiceRules.getPropaedeuticityRegulationByChoiceRegulation(
                        choiceRegulationId = choiceRegulationId,
                        fields = "regpropId,cdsCod,cdsDes"
                    )
                    logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: result with fields proposalRuleId=${resultWithFields.proposalRuleId}")
                    found = true
                    break
                } catch (e: Esse3Exception) {
                    logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: validation error for choiceRegulationId=$choiceRegulationId - ${e.message}, trying next")
                }
            }

            if (!found) {
                logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: no regulation with propaedeuticity found, test inconclusive")
            }
        } else {
            logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.getPropaedeuticityRegulationByChoiceRegulation(choiceRegulationId = 1)
            }
            logger.info("testGetPropaedeuticityRegulationByChoiceRegulation: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPatchChoiceRegulation() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPatchChoiceRegulation: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPatchChoiceRegulation: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.patchChoiceRegulation(
                    choiceRegulationId = 1,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateChoiceRegulation()
                )
            }
            logger.info("testPatchChoiceRegulation: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutLinkedPlansCount() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutLinkedPlansCount: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutLinkedPlansCount: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.choiceRules.putLinkedPlansCount(
                    choiceRegulationId = 1,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3PlansCountsFilters()
                )
            }
            logger.info("testPutLinkedPlansCount: Esse3Exception thrown as expected")
        }
    }
}
