package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.LoadSubmissionFormUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveAssignmentUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ProbeSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ReadSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshSubmissionStatusUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RemoveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SaveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SubmitAssignmentForGradingUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentDetailOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.state.AssignmentPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the compito detail ViewModel: the in-sheet pager navigation, the submission-editor load,
 * the file-budget enforcement of addFiles, the drafts-vs-no-drafts confirm-submit branch, the
 * submitting gate, and the lifecycle one-shot outcomes.
 */
class AssignmentDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val assignmentId = AssignmentId(7)
    private val courseId = CourseId(3)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun form(
        onlineTextEnabled: Boolean = true,
        fileEnabled: Boolean = true,
        maxFiles: Int = 3,
        submissionDraftsEnabled: Boolean = true,
        requiresSubmissionStatement: Boolean = false,
        existingOnlineText: String? = null,
        existingFiles: List<Assignment.AttachmentRef> = emptyList(),
    ): SubmissionForm = SubmissionForm(
        onlineTextEnabled = onlineTextEnabled,
        fileEnabled = fileEnabled,
        maxFiles = maxFiles,
        maxSizeBytes = 0,
        acceptedFileTypes = emptyList(),
        requiresSubmissionStatement = requiresSubmissionStatement,
        submissionStatement = null,
        submissionDraftsEnabled = submissionDraftsEnabled,
        canEdit = true,
        canSubmit = true,
        existingOnlineText = existingOnlineText,
        existingFiles = existingFiles,
    )

    private fun build(
        activeAccount: Account? = account,
        observeAssignment: ObserveAssignmentUseCase = mockk {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.NotYetLoaded)
        },
        loadForm: LoadSubmissionFormUseCase = mockk(relaxed = true),
        save: SaveSubmissionUseCase = mockk(relaxed = true),
        submit: SubmitAssignmentForGradingUseCase = mockk(relaxed = true),
        remove: RemoveSubmissionUseCase = mockk(relaxed = true),
        probe: ProbeSubmissionFileUseCase = mockk(relaxed = true),
        read: ReadSubmissionFileUseCase = mockk(relaxed = true),
        refreshStatus: RefreshSubmissionStatusUseCase = mockk(relaxed = true),
        refreshCourse: RefreshCourseAssignmentsUseCase = mockk(relaxed = true),
    ): AssignmentDetailViewModel = AssignmentDetailViewModel(
        key = AppRoute.AssignmentDetail(assignId = assignmentId.value, courseId = courseId.value),
        savedState = SavedStateHandle(),
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(activeAccount)
        },
        observeAssignment = observeAssignment,
        refreshSubmissionStatus = refreshStatus,
        refreshCourseAssignments = refreshCourse,
        loadSubmissionForm = loadForm,
        saveSubmission = save,
        submitAssignmentForGrading = submit,
        removeSubmissionUseCase = remove,
        probeSubmissionFile = probe,
        readSubmissionFile = read,
    )

    @Test
    fun `backStack starts at Detail`() = runTest {
        val vm = build()
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
    }

    @Test
    fun `openFile emits OpenFile`() = runTest {
        val vm = build()
        vm.oneShotEvents.test {
            vm.openFile("https://file", "a.pdf")
            assertThat(awaitItem()).isEqualTo(
                AssignmentDetailOneShotEvent.OpenFile("https://file", "a.pdf"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openCompose pushes Compose and loads the editor model`() = runTest {
        val loaded = form(existingOnlineText = "hello")
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(accountId, courseId, assignmentId) } returns loaded
        val vm = build(loadForm = loadForm)
        vm.openCompose()
        assertThat(vm.backStack.value.last()).isEqualTo(AssignmentPage.Compose)
        assertThat(vm.submissionForm.value).isEqualTo(Loadable.Loaded(loaded))
        assertThat(vm.draftText.value).isEqualTo("hello")
    }

    @Test
    fun `openCompose failure pops back and emits ActionFailed`() = runTest {
        val cause = IOException("locked")
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(accountId, courseId, assignmentId) } throws cause
        val vm = build(loadForm = loadForm)
        vm.oneShotEvents.test {
            vm.openCompose()
            val event = awaitItem()
            assertThat(event).isInstanceOf(AssignmentDetailOneShotEvent.ActionFailed::class.java)
            assertThat((event as AssignmentDetailOneShotEvent.ActionFailed).cause).isEqualTo(cause)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
    }

    @Test
    fun `back pops one page but never below the root`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form()
        val vm = build(loadForm = loadForm)
        vm.openCompose()
        vm.back()
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
        vm.back()
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
    }

    @Test
    fun `addFiles caps additions to the remaining file budget`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form(maxFiles = 2)
        val probe = mockk<ProbeSubmissionFileUseCase>()
        coEvery { probe.invoke(any()) } answers {
            SubmissionFileMetadata(fileName = firstArg<String>(), mimeType = null, sizeBytes = null)
        }
        val vm = build(loadForm = loadForm, probe = probe)
        vm.openCompose()
        vm.addFiles(listOf("u1", "u2", "u3"))
        assertThat(vm.pickedFiles.value.map { it.uri }).containsExactly("u1", "u2").inOrder()
    }

    @Test
    fun `addFiles deduplicates already-picked uris`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form(maxFiles = 5)
        val probe = mockk<ProbeSubmissionFileUseCase>()
        coEvery { probe.invoke(any()) } answers {
            SubmissionFileMetadata(fileName = firstArg<String>(), mimeType = null, sizeBytes = null)
        }
        val vm = build(loadForm = loadForm, probe = probe)
        vm.openCompose()
        vm.addFiles(listOf("u1"))
        vm.addFiles(listOf("u1", "u2"))
        assertThat(vm.pickedFiles.value.map { it.uri }).containsExactly("u1", "u2").inOrder()
    }

    @Test
    fun `removeFile drops the picked uri`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form(maxFiles = 5)
        val probe = mockk<ProbeSubmissionFileUseCase>()
        coEvery { probe.invoke(any()) } answers {
            SubmissionFileMetadata(fileName = firstArg<String>(), mimeType = null, sizeBytes = null)
        }
        val vm = build(loadForm = loadForm, probe = probe)
        vm.openCompose()
        vm.addFiles(listOf("u1", "u2"))
        vm.removeFile("u1")
        assertThat(vm.pickedFiles.value.map { it.uri }).containsExactly("u2")
    }

    @Test
    fun `confirmSubmit with drafts enabled saves then submits for grading`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns
            form(fileEnabled = false, submissionDraftsEnabled = true)
        val save = mockk<SaveSubmissionUseCase>(relaxed = true)
        val submit = mockk<SubmitAssignmentForGradingUseCase>(relaxed = true)
        val vm = build(loadForm = loadForm, save = save, submit = submit)
        vm.openCompose()
        vm.oneShotEvents.test {
            vm.confirmSubmit()
            assertThat(awaitItem()).isEqualTo(AssignmentDetailOneShotEvent.SubmissionSent)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { save.invoke(accountId, assignmentId, any(), any(), any()) }
        coVerify { submit.invoke(accountId, assignmentId, true) }
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
    }

    @Test
    fun `confirmSubmit without drafts only saves`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns
            form(fileEnabled = false, submissionDraftsEnabled = false)
        val save = mockk<SaveSubmissionUseCase>(relaxed = true)
        val submit = mockk<SubmitAssignmentForGradingUseCase>(relaxed = true)
        val vm = build(loadForm = loadForm, save = save, submit = submit)
        vm.openCompose()
        vm.oneShotEvents.test {
            vm.confirmSubmit()
            assertThat(awaitItem()).isEqualTo(AssignmentDetailOneShotEvent.SubmissionSent)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { submit.invoke(any(), any(), any()) }
    }

    @Test
    fun `confirmSubmit passes statement acceptance through when required`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns
            form(fileEnabled = false, requiresSubmissionStatement = true)
        val submit = mockk<SubmitAssignmentForGradingUseCase>(relaxed = true)
        val vm = build(loadForm = loadForm, submit = submit)
        vm.openCompose()
        vm.setStatementAccepted(true)
        vm.confirmSubmit()
        coVerify { submit.invoke(accountId, assignmentId, true) }
    }

    @Test
    fun `confirmSubmit is a no-op without a loaded form`() = runTest {
        val save = mockk<SaveSubmissionUseCase>(relaxed = true)
        val vm = build(save = save)
        vm.confirmSubmit()
        coVerify(exactly = 0) { save.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `saveDraft failure emits ActionFailed and keeps the editor open`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form(fileEnabled = false)
        val save = mockk<SaveSubmissionUseCase>()
        coEvery { save.invoke(any(), any(), any(), any(), any()) } throws IOException("net")
        val vm = build(loadForm = loadForm, save = save)
        vm.openCompose()
        vm.oneShotEvents.test {
            vm.saveDraft()
            assertThat(awaitItem()).isInstanceOf(AssignmentDetailOneShotEvent.ActionFailed::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.backStack.value.last()).isEqualTo(AssignmentPage.Compose)
        assertThat(vm.submitting.value).isFalse()
    }

    @Test
    fun `removeSubmission resets navigation and emits SubmissionRemoved`() = runTest {
        val remove = mockk<RemoveSubmissionUseCase>(relaxed = true)
        val vm = build(remove = remove)
        vm.oneShotEvents.test {
            vm.removeSubmission()
            assertThat(awaitItem()).isEqualTo(AssignmentDetailOneShotEvent.SubmissionRemoved)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
    }

    @Test
    fun `removeSubmission failure emits ActionFailed`() = runTest {
        val remove = mockk<RemoveSubmissionUseCase>()
        coEvery { remove.invoke(any(), any()) } throws IOException("net")
        val vm = build(remove = remove)
        vm.oneShotEvents.test {
            vm.removeSubmission()
            assertThat(awaitItem()).isInstanceOf(AssignmentDetailOneShotEvent.ActionFailed::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init refresh failure sets Failed and emits RefreshFailed`() = runTest {
        val cause = IOException("offline")
        val refreshStatus = mockk<RefreshSubmissionStatusUseCase>()
        coEvery { refreshStatus.invoke(any(), any()) } throws cause
        val vm = build(refreshStatus = refreshStatus)
        vm.oneShotEvents.test {
            assertThat(awaitItem()).isEqualTo(AssignmentDetailOneShotEvent.RefreshFailed(cause))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Failed(cause))
    }

    @Test
    fun `resetNavigation clears editor state back to root`() = runTest {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(any(), any(), any()) } returns form(existingOnlineText = "draft")
        val vm = build(loadForm = loadForm)
        vm.openCompose()
        vm.setText("edited")
        vm.resetNavigation()
        assertThat(vm.backStack.value).containsExactly(AssignmentPage.Detail)
        assertThat(vm.draftText.value).isEmpty()
        assertThat(vm.submissionForm.value).isEqualTo(Loadable.NotYetLoaded)
    }
}
