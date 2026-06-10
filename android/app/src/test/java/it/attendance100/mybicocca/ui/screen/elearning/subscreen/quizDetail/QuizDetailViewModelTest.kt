package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

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
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.LoadAttemptPageUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.LoadAttemptReviewUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizBestGradeUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.RefreshQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.SaveQuizDraftUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.StartQuizAttemptUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.SubmitQuizAttemptUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.AttemptUiState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuizDetailOneShotEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the quiz detail ViewModel: the attempts-refresh sync status, the attempt state machine
 * (start to in-progress, answer/flag mutation, submit to reviewing), and the failure one-shots.
 */
class QuizDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val quizId = QuizId(50)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun build(
        activeAccount: Account? = account,
        refreshAttempts: RefreshQuizAttemptsUseCase = mockk(relaxed = true),
        startAttempt: StartQuizAttemptUseCase = mockk(relaxed = true),
        loadPage: LoadAttemptPageUseCase = mockk(relaxed = true),
        saveDraft: SaveQuizDraftUseCase = mockk(relaxed = true),
        submitAttempt: SubmitQuizAttemptUseCase = mockk(relaxed = true),
        loadReview: LoadAttemptReviewUseCase = mockk(relaxed = true),
    ): QuizDetailViewModel = QuizDetailViewModel(
        key = SheetRoute.QuizDetail(quizId = quizId.value, courseId = 3),
        savedState = SavedStateHandle(),
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(activeAccount)
        },
        observeQuiz = mockk<ObserveQuizUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf<Loadable<Quiz>>(Loadable.NotYetLoaded)
        },
        observeAttempts = mockk<ObserveQuizAttemptsUseCase> {
            every { this@mockk.invoke(any(), any()) } returns
                flowOf<Loadable<List<QuizAttempt>>>(Loadable.Loaded(emptyList()))
        },
        observeBestGrade = mockk<ObserveQuizBestGradeUseCase> {
            every { this@mockk.invoke(any(), any()) } returns
                flowOf<Loadable<BestGrade?>>(Loadable.Loaded(null))
        },
        refreshAttempts = refreshAttempts,
        startAttempt = startAttempt,
        loadAttemptPage = loadPage,
        saveDraft = saveDraft,
        submitAttempt = submitAttempt,
        loadReview = loadReview,
    )

    @Test
    fun `init refresh success leaves syncStatus Idle`() = runTest {
        val refresh = mockk<RefreshQuizAttemptsUseCase>()
        coEvery { refresh.invoke(accountId, quizId) } returns Unit
        val vm = build(refreshAttempts = refresh)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `init refresh failure sets Failed and emits RefreshFailed`() = runTest {
        val cause = IOException("offline")
        val refresh = mockk<RefreshQuizAttemptsUseCase>()
        coEvery { refresh.invoke(accountId, quizId) } throws cause
        val vm = build(refreshAttempts = refresh)
        vm.oneShotEvents.test {
            assertThat(awaitItem()).isEqualTo(QuizDetailOneShotEvent.RefreshFailed(cause))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Failed(cause))
    }

    @Test
    fun `onStartAttempt loads the first page and lands in progress`() = runTest {
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } returns AttemptId(7)
        val page = mockk<AttemptPage>(relaxed = true)
        val loadPage = mockk<LoadAttemptPageUseCase>()
        coEvery { loadPage.invoke(accountId, AttemptId(7), 0) } returns page
        val vm = build(startAttempt = start, loadPage = loadPage)
        vm.oneShotEvents.test {
            vm.onStartAttempt()
            assertThat(awaitItem()).isEqualTo(QuizDetailOneShotEvent.AttemptStarted(AttemptId(7)))
            cancelAndIgnoreRemainingEvents()
        }
        val state = vm.attemptUi.value
        assertThat(state).isInstanceOf(AttemptUiState.InProgress::class.java)
        assertThat((state as AttemptUiState.InProgress).attemptId).isEqualTo(AttemptId(7))
    }

    @Test
    fun `onStartAttempt failure returns to Idle and emits RefreshFailed`() = runTest {
        val cause = IOException("locked")
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } throws cause
        val vm = build(startAttempt = start)
        vm.oneShotEvents.test {
            vm.onStartAttempt()
            assertThat(awaitItem()).isEqualTo(QuizDetailOneShotEvent.RefreshFailed(cause))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.attemptUi.value).isEqualTo(AttemptUiState.Idle)
    }

    @Test
    fun `setAnswer adds and replaces a slot answer`() = runTest {
        val vm = startedVm()
        vm.setAnswer(slot = 1, fields = mapOf("a" to "x"))
        assertThat(answersOf(vm)).hasSize(1)
        vm.setAnswer(slot = 1, fields = mapOf("a" to "y"))
        assertThat(answersOf(vm)).hasSize(1)
        assertThat(answersOf(vm).first().fields).containsExactly("a", "y")
    }

    @Test
    fun `toggleFlag adds then removes the slot`() = runTest {
        val vm = startedVm()
        vm.toggleFlag(2)
        assertThat(flaggedOf(vm)).containsExactly(2)
        vm.toggleFlag(2)
        assertThat(flaggedOf(vm)).isEmpty()
    }

    @Test
    fun `setAnswer is a no-op when not in progress`() = runTest {
        val vm = build()
        vm.setAnswer(slot = 1, fields = mapOf("a" to "x"))
        assertThat(vm.attemptUi.value).isEqualTo(AttemptUiState.Idle)
    }

    @Test
    fun `onSubmit success transitions to Reviewing`() = runTest {
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } returns AttemptId(7)
        val page = mockk<AttemptPage>(relaxed = true)
        val loadPage = mockk<LoadAttemptPageUseCase>()
        coEvery { loadPage.invoke(accountId, AttemptId(7), 0) } returns page
        val submit = mockk<SubmitQuizAttemptUseCase>(relaxed = true)
        val review = mockk<AttemptReview>(relaxed = true)
        val loadReview = mockk<LoadAttemptReviewUseCase>()
        coEvery { loadReview.invoke(accountId, AttemptId(7)) } returns review
        val vm = build(startAttempt = start, loadPage = loadPage, submitAttempt = submit, loadReview = loadReview)
        vm.onStartAttempt()
        vm.onSubmit()
        val state = vm.attemptUi.value
        assertThat(state).isInstanceOf(AttemptUiState.Reviewing::class.java)
        coVerify { submit.invoke(accountId, AttemptId(7), any(), false) }
    }

    @Test
    fun `onSubmit failure restores the in-progress state and emits RefreshFailed`() = runTest {
        val cause = IOException("net")
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } returns AttemptId(7)
        val page = mockk<AttemptPage>(relaxed = true)
        val loadPage = mockk<LoadAttemptPageUseCase>()
        coEvery { loadPage.invoke(accountId, AttemptId(7), 0) } returns page
        val submit = mockk<SubmitQuizAttemptUseCase>()
        coEvery { submit.invoke(any(), any(), any(), any()) } throws cause
        val vm = build(startAttempt = start, loadPage = loadPage, submitAttempt = submit)
        vm.onStartAttempt()
        vm.oneShotEvents.test {
            assertThat(awaitItem()).isEqualTo(QuizDetailOneShotEvent.AttemptStarted(AttemptId(7)))
            vm.onSubmit()
            assertThat(awaitItem()).isEqualTo(QuizDetailOneShotEvent.RefreshFailed(cause))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.attemptUi.value).isInstanceOf(AttemptUiState.InProgress::class.java)
    }

    @Test
    fun `viewReview opens the review of a finished attempt`() = runTest {
        val review = mockk<AttemptReview>(relaxed = true)
        val loadReview = mockk<LoadAttemptReviewUseCase>()
        coEvery { loadReview.invoke(accountId, AttemptId(3)) } returns review
        val vm = build(loadReview = loadReview)
        vm.viewReview(AttemptId(3))
        assertThat(vm.attemptUi.value).isInstanceOf(AttemptUiState.Reviewing::class.java)
    }

    @Test
    fun `closeAttempt returns to Idle`() = runTest {
        val vm = startedVm()
        vm.closeAttempt()
        assertThat(vm.attemptUi.value).isEqualTo(AttemptUiState.Idle)
    }

    @Test
    fun `pullToRefresh re-runs the attempts refresh`() = runTest {
        val refresh = mockk<RefreshQuizAttemptsUseCase>(relaxed = true)
        val vm = build(refreshAttempts = refresh)
        vm.pullToRefresh()
        coVerify(atLeast = 2) { refresh.invoke(accountId, quizId) }
    }

    private fun startedVm(): QuizDetailViewModel {
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } returns AttemptId(7)
        val page = mockk<AttemptPage>(relaxed = true)
        val loadPage = mockk<LoadAttemptPageUseCase>()
        coEvery { loadPage.invoke(accountId, AttemptId(7), 0) } returns page
        return build(startAttempt = start, loadPage = loadPage).also { it.onStartAttempt() }
    }

    private fun answersOf(vm: QuizDetailViewModel) =
        (vm.attemptUi.value as AttemptUiState.InProgress).answers

    private fun flaggedOf(vm: QuizDetailViewModel) =
        (vm.attemptUi.value as AttemptUiState.InProgress).flagged
}
