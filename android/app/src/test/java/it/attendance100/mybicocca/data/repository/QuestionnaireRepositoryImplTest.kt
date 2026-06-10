package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.questionnaire.ActivityQuestionnairesEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireActivityEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireCacheDao
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireUnitEntity
import it.attendance100.mybicocca.data.mapper.questionnaire.toEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitLogStudyPlanWebList
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TeachingUnitWithQuestionnaire
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRowWithQuestionnaireStatus
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Live-first triad for the two VAL_DID questionnaire list reads. `getActivities` maps the
 * libretto rows and `getActivityQuestionnaires` the teaching-unit evaluation; both write
 * through to [QuestionnaireCacheDao] on success, read the offline mirror on [IOException],
 * and surface the error (rethrow / null parent) when the mirror is empty.
 */
class QuestionnaireRepositoryImplTest {

    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val cacheDao: QuestionnaireCacheDao = mockk(relaxed = true)
    private val esse3Api: Esse3Api = mockk(relaxed = true)

    private val careerId = RepositoryTestFixtures.careerId
    private val matId = RepositoryTestFixtures.ENROLLMENT_TRAIT_ID
    private val activityChoiceId = 314L

    private lateinit var repository: QuestionnaireRepositoryImpl

    @Before
    fun setUp() {
        every { sessionManager.activeAccount } returns MutableStateFlow(RepositoryTestFixtures.account())
        coEvery { sessionManager.esse3() } returns esse3Api
        repository = QuestionnaireRepositoryImpl(sessionManager, cacheDao)
    }

    @Test
    fun `getActivities success maps domain skipping linkState zero and writes through`() = runTest {
        coEvery { esse3Api.questionnaires.getRecordBookQuestionnaires(matId, "P") } returns
            listOf(
                activityRow(choiceId = 1L, linkState = 3),
                activityRow(choiceId = 2L, linkState = 0),
            )

        val activities = repository.getActivities(careerId)

        assertThat(activities).hasSize(1)
        assertThat(activities.single().activityChoiceId).isEqualTo(1L)
        assertThat(activities.single().status).isEqualTo(QuestionnaireActivityStatus.ToCompile)
        coVerify(exactly = 1) { cacheDao.replaceActivities(careerId.value, any()) }
    }

    @Test
    fun `getActivities offline with cache returns the mirrored rows`() = runTest {
        coEvery { esse3Api.questionnaires.getRecordBookQuestionnaires(matId, "P") } throws IOException("offline")
        coEvery { cacheDao.getActivities(careerId.value) } returns listOf(cachedActivityEntity())

        val activities = repository.getActivities(careerId)

        assertThat(activities).hasSize(1)
        assertThat(activities.single().activityChoiceId).isEqualTo(1L)
        coVerify(exactly = 0) { cacheDao.replaceActivities(any(), any()) }
    }

    @Test
    fun `getActivities offline with empty cache rethrows`() = runTest {
        coEvery { esse3Api.questionnaires.getRecordBookQuestionnaires(matId, "P") } throws IOException("offline")
        coEvery { cacheDao.getActivities(careerId.value) } returns emptyList()

        val thrown = runCatching { repository.getActivities(careerId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getActivityQuestionnaires success maps domain and writes through`() = runTest {
        coEvery {
            esse3Api.questionnaires.getTeachingUnitQuestionnaireEvaluation(activityChoiceId, "EV_VAL_DID")
        } returns teachingUnitDto()

        val result = repository.getActivityQuestionnaires(careerId, activityChoiceId)

        assertThat(result.activityChoiceId).isEqualTo(activityChoiceId)
        assertThat(result.units).hasSize(1)
        assertThat(result.units.single().tags).isEqualTo("TAG-1")
        coVerify(exactly = 1) {
            cacheDao.replaceActivityQuestionnaires(careerId.value, activityChoiceId, any(), any())
        }
    }

    @Test
    fun `getActivityQuestionnaires errors when Esse3 omits the activity id`() = runTest {
        coEvery {
            esse3Api.questionnaires.getTeachingUnitQuestionnaireEvaluation(activityChoiceId, "EV_VAL_DID")
        } returns Esse3TeachingUnitWithQuestionnaire(activityChoiceId = null)

        val thrown = runCatching { repository.getActivityQuestionnaires(careerId, activityChoiceId) }
            .exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `getActivityQuestionnaires offline with cache reconstructs from parent and units`() = runTest {
        coEvery {
            esse3Api.questionnaires.getTeachingUnitQuestionnaireEvaluation(activityChoiceId, "EV_VAL_DID")
        } throws IOException("offline")
        coEvery { cacheDao.getActivityQuestionnaires(careerId.value, activityChoiceId) } returns
            cachedParentEntity()
        coEvery { cacheDao.getUnits(careerId.value, activityChoiceId) } returns listOf(cachedUnitEntity())

        val result = repository.getActivityQuestionnaires(careerId, activityChoiceId)

        assertThat(result.activityChoiceId).isEqualTo(activityChoiceId)
        assertThat(result.units).hasSize(1)
        assertThat(result.units.single().tags).isEqualTo("TAG-1")
        coVerify(exactly = 0) {
            cacheDao.replaceActivityQuestionnaires(any(), any(), any(), any())
        }
    }

    @Test
    fun `getActivityQuestionnaires offline with no cached parent rethrows`() = runTest {
        coEvery {
            esse3Api.questionnaires.getTeachingUnitQuestionnaireEvaluation(activityChoiceId, "EV_VAL_DID")
        } throws IOException("offline")
        coEvery { cacheDao.getActivityQuestionnaires(careerId.value, activityChoiceId) } returns null

        val thrown = runCatching { repository.getActivityQuestionnaires(careerId, activityChoiceId) }
            .exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    private fun activityRow(choiceId: Long, linkState: Int) =
        Esse3TranscriptRowWithQuestionnaireStatus(
            activityChoiceId = choiceId,
            activityDescription = "Analisi Matematica",
            activityCode = "E3101Q123",
            courseYear = 1,
            weight = 6f,
            state = Esse3State.Passed,
            linkState = linkState,
        )

    private fun teachingUnitDto() = Esse3TeachingUnitWithQuestionnaire(
        activityChoiceId = activityChoiceId,
        questionnaireId = 5,
        questionConfigId = 9,
        questionnaireDescription = "Valutazione della didattica",
        anonymousFlag = 1,
        teachingUnitLogWebStudyPlanList = listOf(
            Esse3TeachingUnitLogStudyPlanWebList(
                tagsValidationDid = "TAG-1",
                teachingUnitDescription = "Modulo A",
                linkState = 0,
            ),
        ),
    )

    private fun cachedActivityEntity(): QuestionnaireActivityEntity =
        QuestionnaireActivity(
            activityChoiceId = 1L,
            activityCode = "E3101Q123",
            activityName = "Analisi Matematica",
            credits = 6f,
            courseYear = 1,
            attendanceYear = 2024,
            status = QuestionnaireActivityStatus.ToCompile,
        ).toEntity(careerId, 0)

    private fun cachedParentEntity(): ActivityQuestionnairesEntity =
        ActivityQuestionnaires(
            activityChoiceId = activityChoiceId,
            questionnaireId = 5,
            questionnaireConfigId = 9,
            questionnaireName = "Valutazione della didattica",
            anonymous = true,
            units = emptyList(),
        ).toEntity(careerId)

    private fun cachedUnitEntity(): QuestionnaireUnitEntity =
        QuestionnaireUnit(
            teachingUnitName = "Modulo A",
            lecturerName = null,
            partitionName = null,
            completed = false,
            tags = "TAG-1",
        ).toEntity(careerId, activityChoiceId, 0)
}
