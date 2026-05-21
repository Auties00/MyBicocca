package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Answer
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TagsList
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3QuestionnairesApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3QuestionnairesApiTest::class.java.name)
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
    @Disabled("Irreversible mutating operation")
    fun testSavePageAnswers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSavePageAnswers: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testSavePageAnswers: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.savePageAnswers(
                    studentId = studentProfile.studentId,
                    questionnaireId = "TEST",
                    questionComponentId = 1L,
                    pageId = 1L,
                    body = listOf(
                        Esse3Answer(applicationId = 1, answerId = 1, responseBody = "test")
                    ),
                    eventComponentId = "TEST_EVENT"
                )
            }
            logger.info("testSavePageAnswers: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testSetNewSurvey() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSetNewSurvey: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testSetNewSurvey: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.setNewSurvey(
                    questionnaireId = "TEST",
                    activityChoiceId = 1L,
                    studentId = studentProfile.studentId,
                    body = Esse3TagsList(tags = null),
                    eventComponentId = "TEST_EVENT",
                    questionConfigId = 1L
                )
            }
            logger.info("testSetNewSurvey: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutQuestionnaireComponentConfirm() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutQuestionnaireComponentConfirm: user has required permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutQuestionnaireComponentConfirm: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.putQuestionnaireComponentConfirm(
                    activityChoiceId = 1L,
                    questionnaireId = "TEST",
                    questionComponentId = 1L,
                    studentId = studentProfile.studentId,
                    questionConfigId = 1L,
                    userComponentId = 1L,
                    eventComponentId = "TEST_EVENT"
                )
            }
            logger.info("testPutQuestionnaireComponentConfirm: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookQuestionnaires() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val matId = studentProfile.matId
            logger.info("testGetRecordBookQuestionnaires: calling getRecordBookQuestionnaires() with matId=$matId and defaults")
            val defaultResult = api.questionnaires.getRecordBookQuestionnaires(matId = matId)
            logger.info("testGetRecordBookQuestionnaires: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getRecordBookQuestionnaires() default should return non-empty list")
            for (row in defaultResult.take(3)) {
                logger.info("testGetRecordBookQuestionnaires: row matId=${row.matId}, activityChoiceId=${row.activityChoiceId}, activityDescription=${row.activityDescription}, state=${row.state}, stateDescription=${row.stateDescription}, linkState=${row.linkState}")
            }

            logger.info("testGetRecordBookQuestionnaires: calling getRecordBookQuestionnaires() with questFilter param")
            val filteredResult = api.questionnaires.getRecordBookQuestionnaires(
                matId = matId,
                questionFilter = "COMPILABILI"
            )
            logger.info("testGetRecordBookQuestionnaires: filtered result size=${filteredResult.size}")
            for (row in filteredResult.take(3)) {
                logger.info("testGetRecordBookQuestionnaires: filtered row activityChoiceId=${row.activityChoiceId}, activityDescription=${row.activityDescription}, linkState=${row.linkState}")
            }

            logger.info("testGetRecordBookQuestionnaires: calling getRecordBookQuestionnaires() with order param")
            val orderedResult = api.questionnaires.getRecordBookQuestionnaires(
                matId = matId,
                order = "+ord"
            )
            logger.info("testGetRecordBookQuestionnaires: ordered result size=${orderedResult.size}")
        } else {
            logger.info("testGetRecordBookQuestionnaires: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getRecordBookQuestionnaires(matId = studentProfile.matId)
            }
            logger.info("testGetRecordBookQuestionnaires: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRecordBookRow() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val matId = studentProfile.matId
            logger.info("testGetRecordBookRow: fetching record book to obtain an activityChoiceId")
            val recordBook = api.questionnaires.getRecordBookQuestionnaires(matId = matId)
            assertTrue(recordBook.isNotEmpty(), "Record book should have at least one row to test getRecordBookRow()")
            val activityChoiceId = recordBook.first().activityChoiceId
            logger.info("testGetRecordBookRow: using activityChoiceId=$activityChoiceId")

            logger.info("testGetRecordBookRow: calling getRecordBookRow() with defaults")
            val result = api.questionnaires.getRecordBookRow(matId = matId, activityChoiceId = activityChoiceId)
            logger.info("testGetRecordBookRow: result matId=${result.matId}, activityChoiceId=${result.activityChoiceId}, activityDescription=${result.activityDescription}, state=${result.state}, linkState=${result.linkState}")
            assertNotNull(result.activityDescription, "activityDescription should not be null")
            assertTrue(result.matId == matId, "Returned matId should match the requested matId")
            assertTrue(result.activityChoiceId == activityChoiceId, "Returned activityChoiceId should match the requested activityChoiceId")

            logger.info("testGetRecordBookRow: calling getRecordBookRow() with questFilter param")
            val filteredResult = api.questionnaires.getRecordBookRow(
                matId = matId,
                activityChoiceId = activityChoiceId,
                questionFilter = "COMPILABILI"
            )
            logger.info("testGetRecordBookRow: filtered result activityDescription=${filteredResult.activityDescription}, linkState=${filteredResult.linkState}")
        } else {
            logger.info("testGetRecordBookRow: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getRecordBookRow(matId = studentProfile.matId, activityChoiceId = 1L)
            }
            logger.info("testGetRecordBookRow: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTeachingUnitQuestionnaireEvaluation() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val matId = studentProfile.matId
            logger.info("testGetTeachingUnitQuestionnaireEvaluation: fetching record book to find a row with questionnaire link")
            val recordBook = api.questionnaires.getRecordBookQuestionnaires(
                matId = matId,
                questionFilter = "COMPILABILI"
            )
            if (recordBook.isEmpty()) {
                logger.info("testGetTeachingUnitQuestionnaireEvaluation: no compilable questionnaires found, skipping active test")
                return@runTest
            }
            val row = recordBook.firstOrNull { (it.linkState ?: 0) > 0 }
            if (row == null) {
                logger.info("testGetTeachingUnitQuestionnaireEvaluation: no rows with active link state found, skipping active test")
                return@runTest
            }
            val activityChoiceId = row.activityChoiceId
            logger.info("testGetTeachingUnitQuestionnaireEvaluation: using activityChoiceId=$activityChoiceId")

            val result = api.questionnaires.getTeachingUnitQuestionnaireEvaluation(
                activityChoiceId = activityChoiceId,
                eventComponentId = "VAL_DID"
            )
            logger.info("testGetTeachingUnitQuestionnaireEvaluation: result activityChoiceId=${result.activityChoiceId}, state=${result.state}, questionnaireDescription=${result.questionnaireDescription}, questionnaireId=${result.questionnaireId}, teachingUnitLogWebStudyPlanList size=${result.teachingUnitLogWebStudyPlanList.size}")
            for (unit in result.teachingUnitLogWebStudyPlanList.take(3)) {
                logger.info("testGetTeachingUnitQuestionnaireEvaluation: unit teachingUnitCode=${unit.teachingUnitCode}, teachingUnitDescription=${unit.teachingUnitDescription}, domicilePartialCode=${unit.domicilePartialCode}")
            }
        } else {
            logger.info("testGetTeachingUnitQuestionnaireEvaluation: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getTeachingUnitQuestionnaireEvaluation(
                    activityChoiceId = 1L,
                    eventComponentId = "VAL_DID"
                )
            }
            logger.info("testGetTeachingUnitQuestionnaireEvaluation: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPage() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPage: this endpoint requires an active questionnaire session with valid IDs; skipping live call without valid context")
            logger.info("testGetPage: endpoint requires studentId, activityChoiceId, questionnaireId, questionComponentId, pageId, userComponentId from an active survey session")
        } else {
            logger.info("testGetPage: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getPage(
                    studentId = studentProfile.studentId,
                    activityChoiceId = 1L,
                    questionnaireId = "TEST",
                    questionComponentId = 1L,
                    pageId = 1L,
                    userComponentId = 1L
                )
            }
            logger.info("testGetPage: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetNextPage() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetNextPage: this endpoint requires an active questionnaire session with valid IDs; skipping live call without valid context")
            logger.info("testGetNextPage: endpoint requires studentId, activityChoiceId, questionnaireId, questionComponentId, pageId, userComponentId from an active survey session")
        } else {
            logger.info("testGetNextPage: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getNextPage(
                    studentId = studentProfile.studentId,
                    activityChoiceId = 1L,
                    questionnaireId = "TEST",
                    questionComponentId = 1L,
                    pageId = 1L,
                    userComponentId = 1L
                )
            }
            logger.info("testGetNextPage: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPreviousPage() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPreviousPage: this endpoint requires an active questionnaire session with valid IDs; skipping live call without valid context")
            logger.info("testGetPreviousPage: endpoint requires studentId, activityChoiceId, questionnaireId, questionComponentId, pageId, userComponentId from an active survey session")
        } else {
            logger.info("testGetPreviousPage: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getPreviousPage(
                    studentId = studentProfile.studentId,
                    activityChoiceId = 1L,
                    questionnaireId = "TEST",
                    questionComponentId = 1L,
                    pageId = 1L,
                    userComponentId = 1L
                )
            }
            logger.info("testGetPreviousPage: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetComponentSummary() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetComponentSummary: this endpoint requires an active questionnaire session with valid IDs; skipping live call without valid context")
            logger.info("testGetComponentSummary: endpoint requires studentId, activityChoiceId, questionComponentId, questionnaireId, questionConfigId, userComponentId, eventComponentId from an active survey session")
        } else {
            logger.info("testGetComponentSummary: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getComponentSummary(
                    studentId = studentProfile.studentId,
                    activityChoiceId = 1L,
                    questionComponentId = 1L,
                    questionnaireId = "TEST",
                    questionConfigId = 1L,
                    userComponentId = 1L,
                    eventComponentId = "TEST_EVENT"
                )
            }
            logger.info("testGetComponentSummary: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUserComponentEventInfoAvaDoc() = runTest {
        logger.info("testGetUserComponentEventInfoAvaDoc: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetUserComponentEventInfoAvaDoc: calling getUserComponentEventInfoAvaDoc() with questionnaireId=AVA_DOC, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getUserComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId
        )
        logger.info("testGetUserComponentEventInfoAvaDoc: default result size=${defaultResult.size}")

        logger.info("testGetUserComponentEventInfoAvaDoc: calling getUserComponentEventInfoAvaDoc() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getUserComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetUserComponentEventInfoAvaDoc: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetUserComponentEventInfoAvaDoc: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, questionnaireDescription=${entry.questionnaireDescription}, lecturerSurname=${entry.lecturerSurname}, lecturerName=${entry.lecturerName}, activityCode=${entry.activityCode}, activityDescription=${entry.activityDescription}")
        }

        logger.info("testGetUserComponentEventInfoAvaDoc: calling getUserComponentEventInfoAvaDoc() with order param")
        val orderedResult = api.questionnaires.getUserComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId,
            start = 0,
            limit = 5,
            order = "+docenteCognome"
        )
        logger.info("testGetUserComponentEventInfoAvaDoc: ordered result size=${orderedResult.size}")
    }

    @Test
    fun testGetQuestionnaireComponentEventInfoAvaDoc() = runTest {
        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: calling getQuestionnaireComponentEventInfoAvaDoc() with questionnaireId=AVA_DOC, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getQuestionnaireComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId
        )
        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: default result size=${defaultResult.size}")

        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: calling getQuestionnaireComponentEventInfoAvaDoc() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getQuestionnaireComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, activityCode=${entry.activityCode}, activityDescription=${entry.activityDescription}, lecturerSurname=${entry.lecturerSurname}")
        }

        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: calling getQuestionnaireComponentEventInfoAvaDoc() with order param")
        val orderedResult = api.questionnaires.getQuestionnaireComponentEventInfoAvaDoc(
            questionnaireId = "AVA_DOC",
            academicYearId = academicYearId,
            start = 0,
            limit = 5,
            order = "+adCod"
        )
        logger.info("testGetQuestionnaireComponentEventInfoAvaDoc: ordered result size=${orderedResult.size}")
    }

    @Test
    fun testGetUserComponentEventInfoGeneralQuestionnaire() = runTest {
        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: calling getUserComponentEventInfoGeneralQuestionnaire() with questionnaireId=GEN_QUEST, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getUserComponentEventInfoGeneralQuestionnaire(
            questionnaireId = "GEN_QUEST",
            academicYearId = academicYearId
        )
        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: default result size=${defaultResult.size}")

        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: calling getUserComponentEventInfoGeneralQuestionnaire() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getUserComponentEventInfoGeneralQuestionnaire(
            questionnaireId = "GEN_QUEST",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, courseOfStudyCode=${entry.courseOfStudyCode}, courseOfStudyDescription=${entry.courseOfStudyDescription}")
        }

        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: calling getUserComponentEventInfoGeneralQuestionnaire() with order param")
        val orderedResult = api.questionnaires.getUserComponentEventInfoGeneralQuestionnaire(
            questionnaireId = "GEN_QUEST",
            academicYearId = academicYearId,
            start = 0,
            limit = 5,
            order = "+cdsCod"
        )
        logger.info("testGetUserComponentEventInfoGeneralQuestionnaire: ordered result size=${orderedResult.size}")
    }

    @Test
    fun testGetQuestionnaireComponentEventInfoGeneralQuestionnaire() = runTest {
        logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: calling getQuestionnaireComponentEventInfoGeneralQuestionnaire() with questionnaireId=GEN_QUEST, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getQuestionnaireComponentEventInfoGeneralQuestionnaire(
            questionnaireId = "GEN_QUEST",
            academicYearId = academicYearId
        )
        logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: default result size=${defaultResult.size}")

        logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: calling getQuestionnaireComponentEventInfoGeneralQuestionnaire() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getQuestionnaireComponentEventInfoGeneralQuestionnaire(
            questionnaireId = "GEN_QUEST",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetQuestionnaireComponentEventInfoGeneralQuestionnaire: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, courseOfStudyCode=${entry.courseOfStudyCode}")
        }
    }

    @Test
    fun testGetUserComponentEventInfoPostLogin() = runTest {
        logger.info("testGetUserComponentEventInfoPostLogin: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetUserComponentEventInfoPostLogin: calling getUserComponentEventInfoPostLogin() with questionnaireId=POST_LOGIN, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getUserComponentEventInfoPostLogin(
            questionnaireId = "POST_LOGIN",
            academicYearId = academicYearId
        )
        logger.info("testGetUserComponentEventInfoPostLogin: default result size=${defaultResult.size}")

        logger.info("testGetUserComponentEventInfoPostLogin: calling getUserComponentEventInfoPostLogin() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getUserComponentEventInfoPostLogin(
            questionnaireId = "POST_LOGIN",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetUserComponentEventInfoPostLogin: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetUserComponentEventInfoPostLogin: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, studentId=${entry.studentId}, courseOfStudyCode=${entry.courseOfStudyCode}")
        }

        logger.info("testGetUserComponentEventInfoPostLogin: calling getUserComponentEventInfoPostLogin() with order param")
        val orderedResult = api.questionnaires.getUserComponentEventInfoPostLogin(
            questionnaireId = "POST_LOGIN",
            academicYearId = academicYearId,
            start = 0,
            limit = 5,
            order = "+cdsCod"
        )
        logger.info("testGetUserComponentEventInfoPostLogin: ordered result size=${orderedResult.size}")
    }

    @Test
    fun testGetQuestionnaireComponentEventInfoPostLogin() = runTest {
        logger.info("testGetQuestionnaireComponentEventInfoPostLogin: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetQuestionnaireComponentEventInfoPostLogin: calling getQuestionnaireComponentEventInfoPostLogin() with questionnaireId=POST_LOGIN, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getQuestionnaireComponentEventInfoPostLogin(
            questionnaireId = "POST_LOGIN",
            academicYearId = academicYearId
        )
        logger.info("testGetQuestionnaireComponentEventInfoPostLogin: default result size=${defaultResult.size}")

        logger.info("testGetQuestionnaireComponentEventInfoPostLogin: calling getQuestionnaireComponentEventInfoPostLogin() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getQuestionnaireComponentEventInfoPostLogin(
            questionnaireId = "POST_LOGIN",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetQuestionnaireComponentEventInfoPostLogin: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetQuestionnaireComponentEventInfoPostLogin: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, courseOfStudyCode=${entry.courseOfStudyCode}")
        }
    }

    @Test
    fun testGetUserComponentEventInfoDidacticEvaluation() = runTest {
        logger.info("testGetUserComponentEventInfoDidacticEvaluation: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetUserComponentEventInfoDidacticEvaluation: calling getUserComponentEventInfoDidacticEvaluation() with questionnaireId=VAL_DID, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getUserComponentEventInfoDidacticEvaluation(
            questionnaireId = "VAL_DID",
            academicYearId = academicYearId
        )
        logger.info("testGetUserComponentEventInfoDidacticEvaluation: default result size=${defaultResult.size}")

        logger.info("testGetUserComponentEventInfoDidacticEvaluation: calling getUserComponentEventInfoDidacticEvaluation() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getUserComponentEventInfoDidacticEvaluation(
            questionnaireId = "VAL_DID",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetUserComponentEventInfoDidacticEvaluation: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetUserComponentEventInfoDidacticEvaluation: entry userComponentId=${entry.userComponentId}, questionnaireCode=${entry.questionnaireCode}, activityCode=${entry.activityCode}, activityDescription=${entry.activityDescription}, lecturerSurname=${entry.lecturerSurname}, lecturerName=${entry.lecturerName}")
        }

        logger.info("testGetUserComponentEventInfoDidacticEvaluation: calling getUserComponentEventInfoDidacticEvaluation() with order param")
        val orderedResult = api.questionnaires.getUserComponentEventInfoDidacticEvaluation(
            questionnaireId = "VAL_DID",
            academicYearId = academicYearId,
            start = 0,
            limit = 5,
            order = "+adCod"
        )
        logger.info("testGetUserComponentEventInfoDidacticEvaluation: ordered result size=${orderedResult.size}")
    }

    @Test
    fun testGetQuestionnaireComponentEventInfoDidacticEvaluation() = runTest {
        logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: ANY permission required, calling with defaults")
        val currentYear = java.time.Year.now().value
        val academicYearId = currentYear - 1

        logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: calling getQuestionnaireComponentEventInfoDidacticEvaluation() with questionnaireId=VAL_DID, academicYearId=$academicYearId and defaults")
        val defaultResult = api.questionnaires.getQuestionnaireComponentEventInfoDidacticEvaluation(
            questionnaireId = "VAL_DID",
            academicYearId = academicYearId
        )
        logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: default result size=${defaultResult.size}")

        logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: calling getQuestionnaireComponentEventInfoDidacticEvaluation() with pagination start=0, limit=5")
        val paginatedResult = api.questionnaires.getQuestionnaireComponentEventInfoDidacticEvaluation(
            questionnaireId = "VAL_DID",
            academicYearId = academicYearId,
            start = 0,
            limit = 5
        )
        logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: paginated result size=${paginatedResult.size}")
        assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

        for (entry in paginatedResult.take(3)) {
            logger.info("testGetQuestionnaireComponentEventInfoDidacticEvaluation: entry questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, activityCode=${entry.activityCode}, activityDescription=${entry.activityDescription}, lecturerSurname=${entry.lecturerSurname}")
        }
    }

    @Test
    fun testGetCompiledQuestionnaires() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompiledQuestionnaires: user has TECHNICAL_USER permission, calling getCompiledQuestionnaires()")
            val result = api.questionnaires.getCompiledQuestionnaires(questionComponentId = 1L)
            logger.info("testGetCompiledQuestionnaires: result size=${result.size}")
            for (entry in result.take(3)) {
                logger.info("testGetCompiledQuestionnaires: entry questionnaireId=${entry.questionnaireId}, questionComponentId=${entry.questionComponentId}, questionnaireCode=${entry.questionnaireCode}, questionnaireDescription=${entry.questionnaireDescription}, questionStateCode=${entry.questionStateCode}")
            }
        } else {
            logger.info("testGetCompiledQuestionnaires: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getCompiledQuestionnaires(questionComponentId = 1L)
            }
            logger.info("testGetCompiledQuestionnaires: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInternshipQuestionnaires() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInternshipQuestionnaires: user has TECHNICAL_USER permission, calling getInternshipQuestionnaires()")
            val result = api.questionnaires.getInternshipQuestionnaires(domicileInternshipId = 1L)
            logger.info("testGetInternshipQuestionnaires: result internshipConfig size=${result.internshipConfig.size}, domicileInternshipCompletionTag size=${result.domicileInternshipCompletionTag.size}")
        } else {
            logger.info("testGetInternshipQuestionnaires: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.getInternshipQuestionnaires(domicileInternshipId = 1L)
            }
            logger.info("testGetInternshipQuestionnaires: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testSetVisibility() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSetVisibility: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testSetVisibility: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.questionnaires.setVisibility(
                    userId = 1L,
                    questionComponentId = 1L,
                    visibleKind = "TEST",
                    visibleValue = true
                )
            }
            logger.info("testSetVisibility: Esse3Exception thrown as expected")
        }
    }
}
