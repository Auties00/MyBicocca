package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.AcceptExamResultUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamResultsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.RejectExamResultUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

/**
 * State + behaviour coverage for the Esiti sheet. The page is driven by a real
 * [ExamResultsViewModel] built over MockK-faked use cases (reusing the Wave 1 unit-test
 * construction); passing an explicit instance bypasses the screen's `viewModel` default. Anchored
 * on [ExamResultsTestTags] and wrapped via [setBicoccaContent], which installs the app-wide
 * CompositionLocals (HapticManager, AppSnackbar controller, DeviceType) the page reads. Covers the all-empty and
 * error root markers, the populated feed, opening a pending outcome's detail, and that accepting it
 * runs the accept use case.
 */
@RunWith(AndroidJUnit4::class)
class ExamResultsPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getExamResults: GetExamResultsUseCase = mockk()
    private val acceptExamResult: AcceptExamResultUseCase = mockk(relaxed = true)
    private val rejectExamResult: RejectExamResultUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun setPage() {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        val vm = ExamResultsViewModel(getExamResults, acceptExamResult, rejectExamResult, observeActiveAccount)
        compose.setBicoccaContent {
            ExamResultsPage(viewModel = vm)
        }
        compose.waitForIdle()
    }

    @Test
    fun an_empty_feed_renders_the_all_empty_marker() {
        coEvery { getExamResults(any()) } returns emptyList()
        setPage()

        compose.onNodeWithTag(ExamResultsTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(ExamResultsTestTags.STATE_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_first_load_failure_renders_the_error_marker() {
        coEvery { getExamResults(any()) } throws IOException("offline")
        setPage()

        compose.onNodeWithTag(ExamResultsTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun a_pending_outcome_renders_the_content_feed_with_its_row() {
        val pending = pendingResult(applicationListId = 700L)
        coEvery { getExamResults(any()) } returns listOf(pending)
        setPage()

        compose.onNodeWithTag(ExamResultsTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(ExamResultsTestTags.item(identity(pending))).assertIsDisplayed()
    }

    @Test
    fun tapping_a_pending_outcome_opens_its_detail_with_the_decision_actions() {
        val pending = pendingResult(applicationListId = 700L)
        coEvery { getExamResults(any()) } returns listOf(pending)
        setPage()

        compose.onNodeWithTag(ExamResultsTestTags.item(identity(pending))).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(ExamResultsTestTags.ACCEPT_BUTTON).assertIsDisplayed()
        val feedGone = compose.onAllNodesWithTag(ExamResultsTestTags.STATE_CONTENT)
            .fetchSemanticsNodes().isEmpty()
        assert(feedGone)
    }

    @Test
    fun accepting_a_pending_outcome_runs_the_accept_use_case() {
        val pending = pendingResult(applicationListId = 700L)
        coEvery { getExamResults(any()) } returns listOf(pending)
        setPage()

        compose.onNodeWithTag(ExamResultsTestTags.item(identity(pending))).performClick()
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithTag(ExamResultsTestTags.ACCEPT_BUTTON)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag(ExamResultsTestTags.ACCEPT_BUTTON).performClick()
        compose.waitForIdle()

        coVerify { acceptExamResult(careerId, 700L) }
    }

    /** Mirrors the private `ExamResult.identity()` keying used by the page's list items. */
    private fun identity(result: ExamResult): String =
        result.applicationListId?.toString()
            ?: "${result.key.courseOfStudyId}-${result.key.activityId}-${result.key.callId}"

    private fun pendingResult(applicationListId: Long?): ExamResult = ExamResult(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3),
        applicationListId = applicationListId,
        publicationId = 4242L,
        activityDescription = "Algoritmi",
        examDateTime = null,
        grade = ExamGrade.Numeric(28),
        acknowledgment = AcknowledgmentStatus.NotViewed,
        publishedNote = null,
        acknowledgmentDeadline = LocalDate.now().plusDays(7),
    )

    private companion object {
        fun account(careerId: CareerId): Account = Account(
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
}
