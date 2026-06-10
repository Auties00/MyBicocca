package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanPrintUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state.StudyPlanEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Percorso e piano sheet: the plan/path fan-out, the
 * Loaded(null) "no plan" distinction, the path degrading silently to Loaded(null) without
 * failing sync, the editable gate riding StudyPath.editingOpen, and the printPlan one-shot
 * PDF / error events.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getStudyPlan: GetStudyPlanUseCase = mockk()
    private val getStudyPath: GetStudyPathUseCase = mockk()
    private val getStudyPlanPrint: GetStudyPlanPrintUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun plan(): StudyPlan = StudyPlan(
        planId = 555L,
        studentId = 101L,
        type = StudyPlanType.Standard,
        statusDescription = "Approvato",
        lastUpdated = null,
        choiceRegulationId = 10L,
        schemaId = 20L,
        courses = emptyList(),
    )

    private fun path(editingOpen: Boolean): StudyPath = StudyPath(
        percorso = null,
        orientamento = null,
        profilo = null,
        partTime = null,
        choiceRegulationId = 10L,
        currentSchemaId = 20L,
        options = emptyList(),
        editingOpen = editingOpen,
        choiceAvailable = false,
    )

    private fun viewModel(): StudyPlanViewModel {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(any()) } returns plan()
        coEvery { getStudyPath(any()) } returns path(editingOpen = false)
        return StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)
    }

    @Test
    fun `plan and path both reach Loaded and sync settles Idle`() = runTest {
        val vm = viewModel()

        assertThat(vm.plan.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((vm.plan.value as Loadable.Loaded).value).isEqualTo(plan())
        assertThat(vm.studyPath.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `no plan resolves to Loaded null, distinct from NotYetLoaded`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns null
        coEvery { getStudyPath(careerId) } returns path(editingOpen = false)

        val vm = StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

        assertThat(vm.plan.value).isEqualTo(Loadable.Loaded(null))
    }

    @Test
    fun `a plan failure sets Failed and does not blank the path`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } throws boom
        coEvery { getStudyPath(careerId) } returns path(editingOpen = true)

        val vm = StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

        assertThat((vm.syncStatus.value as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat(vm.studyPath.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat(vm.plan.value).isEqualTo(Loadable.NotYetLoaded)
    }

    @Test
    fun `a path failure degrades to Loaded null while the plan still loads`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns plan()
        coEvery { getStudyPath(careerId) } throws IOException("path offline")

        val vm = StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

        assertThat(vm.studyPath.value).isEqualTo(Loadable.Loaded(null))
        assertThat(vm.plan.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        assertThat(vm.editable.value).isFalse()
    }

    @Test
    fun `editable mirrors the path editing window`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns plan()
        coEvery { getStudyPath(careerId) } returns path(editingOpen = true)

        val vm = StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

        assertThat(vm.editable.value).isTrue()
    }

    @Test
    fun `printPlan emits an OpenPdf event with the plan-id filename`() = runTest {
        val vm = viewModel()
        coEvery { getStudyPlanPrint(careerId, 555L) } returns byteArrayOf(1, 2, 3)

        vm.events.test {
            vm.printPlan()
            val event = awaitItem()
            assertThat(event).isInstanceOf(StudyPlanEvent.OpenPdf::class.java)
            assertThat((event as StudyPlanEvent.OpenPdf).fileName).isEqualTo("piano_di_studi_555.pdf")
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.actionInProgress.value).isFalse()
    }

    @Test
    fun `printPlan emits a message when the print fails`() = runTest {
        val vm = viewModel()
        coEvery { getStudyPlanPrint(careerId, 555L) } throws IOException("no pdf")

        vm.events.test {
            vm.printPlan()
            assertThat(awaitItem()).isInstanceOf(StudyPlanEvent.ShowMessage::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.actionInProgress.value).isFalse()
    }

    @Test
    fun `printPlan is a no-op when there is no plan id`() = runTest {
        every { observeActiveAccount() } returns flowOf(account())
        coEvery { getStudyPlan(careerId) } returns null
        coEvery { getStudyPath(careerId) } returns path(editingOpen = false)
        val vm = StudyPlanViewModel(getStudyPlan, getStudyPath, getStudyPlanPrint, observeActiveAccount)

        vm.events.test {
            vm.printPlan()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeCareerId exposes the selected career for the no-plan edit entry`() = runTest {
        val vm = viewModel()

        assertThat(vm.activeCareerId.value).isEqualTo(careerId)
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
