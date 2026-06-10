package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.LoadSubmissionFormUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ObserveAssignmentUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ProbeSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.ReadSubmissionFileUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshCourseAssignmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RefreshSubmissionStatusUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.RemoveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SaveSubmissionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.assignment.SubmitAssignmentForGradingUseCase
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the compito (assignment) detail sheet's overview entry. A real
 * [AssignmentDetailViewModel] over faked use cases renders the overview from a [Loadable]
 * assignment: an unresolved assignment shows the loading marker, a not-submitted assignment within
 * the window shows the overview with a compose CTA whose tap loads the submission editor. The sheet
 * is anchored on [AssignmentDetailTestTags] and wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class AssignmentDetailPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val accountId = AccountId("acc-1")
    private val assignmentId = AssignmentId(7)
    private val courseId = CourseId(3)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun assignment(status: SubmissionStatus = SubmissionStatus.NotSubmitted): Assignment =
        Assignment(
            id = assignmentId,
            courseId = courseId,
            cmId = null,
            name = "Compito 1",
            intro = null,
            introFiles = emptyList(),
            dueDate = null,
            allowSubmissionsFrom = null,
            cutoffDate = null,
            gradingDueDate = null,
            maxAttempts = null,
            allowedExtensions = emptyList(),
            allowDrafts = true,
            submissionStatus = status,
            pageUrl = null,
        )

    private fun form(): SubmissionForm = SubmissionForm(
        onlineTextEnabled = true,
        fileEnabled = false,
        maxFiles = 3,
        maxSizeBytes = 0,
        acceptedFileTypes = emptyList(),
        requiresSubmissionStatement = false,
        submissionStatement = null,
        submissionDraftsEnabled = true,
        canEdit = true,
        canSubmit = true,
        existingOnlineText = null,
        existingFiles = emptyList(),
    )

    private fun build(
        observeAssignment: ObserveAssignmentUseCase,
        loadForm: LoadSubmissionFormUseCase = mockk(relaxed = true),
    ): AssignmentDetailViewModel = AssignmentDetailViewModel(
        key = AppRoute.AssignmentDetail(assignId = assignmentId.value, courseId = courseId.value),
        savedState = SavedStateHandle(),
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(account)
        },
        observeAssignment = observeAssignment,
        refreshSubmissionStatus = mockk(relaxed = true),
        refreshCourseAssignments = mockk<RefreshCourseAssignmentsUseCase>(relaxed = true),
        loadSubmissionForm = loadForm,
        saveSubmission = mockk<SaveSubmissionUseCase>(relaxed = true),
        submitAssignmentForGrading = mockk<SubmitAssignmentForGradingUseCase>(relaxed = true),
        removeSubmissionUseCase = mockk<RemoveSubmissionUseCase>(relaxed = true),
        probeSubmissionFile = mockk<ProbeSubmissionFileUseCase>(relaxed = true),
        readSubmissionFile = mockk<ReadSubmissionFileUseCase>(relaxed = true),
    )

    private fun setPage(viewModel: AssignmentDetailViewModel) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                AssignmentDetailPage(
                    assignId = assignmentId.value,
                    courseId = courseId.value,
                    onOpenFile = { _, _, _, _, _ -> },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun observe(loadable: Loadable<Assignment>): ObserveAssignmentUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(loadable)
    }

    @Test
    fun an_unresolved_assignment_shows_the_loading_marker() {
        val vm = build(observeAssignment = observe(Loadable.NotYetLoaded))
        setPage(vm)

        compose.onNodeWithTag(AssignmentDetailTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AssignmentDetailTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun a_loaded_assignment_shows_the_overview() {
        val vm = build(observeAssignment = observe(Loadable.Loaded(assignment())))
        setPage(vm)

        compose.onNodeWithTag(AssignmentDetailTestTags.OVERVIEW).assertIsDisplayed()
        compose.onNodeWithTag(AssignmentDetailTestTags.COMPOSE_ACTION).assertIsDisplayed()
    }

    @Test
    fun tapping_the_compose_action_loads_the_submission_editor() {
        val loadForm = mockk<LoadSubmissionFormUseCase>()
        coEvery { loadForm.invoke(accountId, courseId, assignmentId) } returns form()
        val vm = build(observeAssignment = observe(Loadable.Loaded(assignment())), loadForm = loadForm)
        setPage(vm)

        compose.onNodeWithTag(AssignmentDetailTestTags.COMPOSE_ACTION).performClick()
        compose.waitForIdle()

        coVerify { loadForm.invoke(accountId, courseId, assignmentId) }
    }

    @Test
    fun a_submitted_assignment_exposes_the_remove_action() {
        val submitted = SubmissionStatus.Submitted(
            submittedAt = null,
            files = emptyList(),
            onlineText = "done",
        )
        val vm = build(observeAssignment = observe(Loadable.Loaded(assignment(submitted))))
        setPage(vm)

        compose.onNodeWithTag(AssignmentDetailTestTags.REMOVE_ACTION).assertIsDisplayed()
    }
}
