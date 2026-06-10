package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
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
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Questionari sheet: the career-triggered activity list, the
 * open/dismiss/retry detail flow (independent detailStatus), failure isolation between the
 * list and the detail, and the per-activity questionnaire fetch keyed by activityChoiceId.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuestionnairesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getQuestionnaireActivities: GetQuestionnaireActivitiesUseCase = mockk()
    private val getActivityQuestionnaires: GetActivityQuestionnairesUseCase = mockk()
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

    private fun viewModel(): QuestionnairesViewModel {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(any()) } returns emptyList()
        return QuestionnairesViewModel(getQuestionnaireActivities, getActivityQuestionnaires, observeActiveAccount)
    }

    @Test
    fun `activities reach Loaded on the career-triggered fetch`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } returns listOf(activity(1L))

        val vm = QuestionnairesViewModel(getQuestionnaireActivities, getActivityQuestionnaires, observeActiveAccount)

        assertThat((vm.activities.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `a list fetch failure sets Failed and leaves activities NotYetLoaded`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } throws boom

        val vm = QuestionnairesViewModel(getQuestionnaireActivities, getActivityQuestionnaires, observeActiveAccount)

        assertThat(vm.activities.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat((vm.syncStatus.value as SyncStatus.Failed).cause).isEqualTo(boom)
    }

    @Test
    fun `openActivity selects it and loads its detail keyed by choice id`() = runTest {
        val vm = viewModel()
        coEvery { getActivityQuestionnaires(careerId, 1L) } returns detail(1L)

        vm.openActivity(activity(1L))

        assertThat(vm.selectedActivity.value).isEqualTo(activity(1L))
        assertThat(vm.activityDetail.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat(vm.detailStatus.value).isEqualTo(SyncStatus.Idle)
        coVerify { getActivityQuestionnaires(careerId, 1L) }
    }

    @Test
    fun `a detail failure sets detailStatus Failed without touching the list sync`() = runTest {
        val boom = IOException("offline")
        val vm = viewModel()
        coEvery { getActivityQuestionnaires(careerId, 1L) } throws boom

        vm.openActivity(activity(1L))

        assertThat((vm.detailStatus.value as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        assertThat(vm.activityDetail.value).isEqualTo(Loadable.NotYetLoaded)
    }

    @Test
    fun `dismissActivity clears the selection and resets detail`() = runTest {
        val vm = viewModel()
        coEvery { getActivityQuestionnaires(careerId, 1L) } returns detail(1L)
        vm.openActivity(activity(1L))

        vm.dismissActivity()

        assertThat(vm.selectedActivity.value).isNull()
        assertThat(vm.activityDetail.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat(vm.detailStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `retryDetail re-fetches the currently selected activity`() = runTest {
        val vm = viewModel()
        coEvery { getActivityQuestionnaires(careerId, 1L) } throws IOException("first")
        vm.openActivity(activity(1L))

        coEvery { getActivityQuestionnaires(careerId, 1L) } returns detail(1L)
        vm.retryDetail()

        assertThat(vm.activityDetail.value).isInstanceOf(Loadable.Loaded::class.java)
        coVerify(atLeast = 2) { getActivityQuestionnaires(careerId, 1L) }
    }

    @Test
    fun `retryDetail with no selection is a no-op`() = runTest {
        val vm = viewModel()

        vm.retryDetail()

        coVerify(exactly = 0) { getActivityQuestionnaires(any(), any()) }
    }

    @Test
    fun `refresh re-fetches the list and clears a prior failure`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getQuestionnaireActivities(careerId) } throws IOException("offline")
        val vm = QuestionnairesViewModel(getQuestionnaireActivities, getActivityQuestionnaires, observeActiveAccount)

        coEvery { getQuestionnaireActivities(careerId) } returns listOf(activity(1L))
        vm.refresh()

        assertThat((vm.activities.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
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
