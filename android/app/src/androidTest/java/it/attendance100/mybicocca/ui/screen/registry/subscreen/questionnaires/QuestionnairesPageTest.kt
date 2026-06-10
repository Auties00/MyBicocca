package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetActivityQuestionnairesUseCase
import it.attendance100.mybicocca.domain.usecase.questionnaire.GetQuestionnaireActivitiesUseCase
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * UI coverage for the Questionari sheet body: the root pages through its loading / empty / activity
 * list states off the activities stream, and tapping an activity row pushes its units page. The
 * screen is driven by a real [QuestionnairesViewModel] over MockK-faked use cases (reusing the Wave 1
 * construction), anchored on [QuestionnairesTestTags], and wrapped in the production [BicoccaTheme].
 * Compila is never tapped — it mounts a hiltViewModel child the unit-test harness cannot create.
 */
@RunWith(AndroidJUnit4::class)
class QuestionnairesPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getQuestionnaireActivities: GetQuestionnaireActivitiesUseCase = mockk()
    private val getActivityQuestionnaires: GetActivityQuestionnairesUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun activity(choiceId: Long): QuestionnaireActivity = QuestionnaireActivity(
        activityChoiceId = choiceId,
        activityCode = "E3101Q123",
        activityName = "Analisi",
        credits = 6f,
        courseYear = 1,
        attendanceYear = 2023,
        status = QuestionnaireActivityStatus.ToCompile,
    )

    private fun detail(choiceId: Long): ActivityQuestionnaires = ActivityQuestionnaires(
        activityChoiceId = choiceId,
        questionnaireId = 1,
        questionnaireConfigId = 2,
        questionnaireName = "VAL_DID",
        anonymous = true,
        units = emptyList(),
    )

    private fun viewModel(): QuestionnairesViewModel =
        QuestionnairesViewModel(getQuestionnaireActivities, getActivityQuestionnaires, observeActiveAccount)

    private fun setScreen(vm: QuestionnairesViewModel) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                QuestionnairesPage(viewModel = vm)
            }
        }
    }

    @Test
    fun the_root_shows_the_loading_state_while_no_career_has_resolved() {
        every { observeActiveAccount() } returns emptyFlow()

        setScreen(viewModel())

        compose.onNodeWithTag(QuestionnairesTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(QuestionnairesTestTags.ROOT_LOADING).assertIsDisplayed()
    }

    @Test
    fun the_root_shows_the_empty_state_when_no_evaluation_is_open() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } returns emptyList()

        setScreen(viewModel())

        compose.onNodeWithTag(QuestionnairesTestTags.ROOT_EMPTY).assertIsDisplayed()
    }

    @Test
    fun the_root_lists_the_evaluation_activities_when_present() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } returns listOf(activity(1L))

        setScreen(viewModel())

        compose.onNodeWithTag(QuestionnairesTestTags.ACTIVITY_LIST).assertIsDisplayed()
        compose.onNodeWithTag(QuestionnairesTestTags.activity(1L)).assertIsDisplayed()
    }

    @Test
    fun tapping_an_activity_row_pushes_its_units_page() {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } returns listOf(activity(1L))
        coEvery { getActivityQuestionnaires(careerId, 1L) } returns detail(1L)

        setScreen(viewModel())

        compose.onNodeWithTag(QuestionnairesTestTags.activity(1L)).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(QuestionnairesTestTags.UNITS_PAGE).assertIsDisplayed()
    }

    private fun account(): Account = Account(
        id = AccountId("acc-1"),
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = listOf(
                Career(
                    id = careerId,
                    enrollmentTraitId = 1L,
                    programId = 2L,
                    easyStaffProgramCode = "E32",
                    academicYearEnrollmentId = 3L,
                    studentNumber = "900001",
                    description = "Informatica",
                    academicYear = 2024,
                    status = CareerStatus.ACTIVE,
                ),
            ),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )
}
