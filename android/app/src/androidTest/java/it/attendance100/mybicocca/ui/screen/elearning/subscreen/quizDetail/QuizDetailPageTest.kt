package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptState
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.LoadAttemptPageUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizBestGradeUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.ObserveQuizUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.RefreshQuizAttemptsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.quiz.StartQuizAttemptUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the quiz detail sheet's overview entry. A real
 * [QuizDetailViewModel] over faked use cases renders the overview from observed Room state: an
 * unresolved quiz shows the loading marker, a loaded quiz shows the overview with the start CTA,
 * and tapping it starts an attempt; a loaded quiz with attempts exposes the history action whose
 * tap morphs the sheet onto the keyed attempt list. The page reads the app-wide snackbar
 * controller, so the test renders through `setBicoccaContent`, which installs the app-wide
 * CompositionLocals plus the production theme. Anchored on [QuizDetailTestTags].
 */
@RunWith(AndroidJUnit4::class)
class QuizDetailPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val accountId = AccountId("acc-1")
    private val quizId = QuizId(50)
    private val courseId = 3

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun quiz(): Quiz = Quiz(
        id = quizId,
        courseId = CourseId(courseId),
        cmId = null,
        name = "Quiz 1",
        intro = null,
        timeOpen = null,
        timeClose = null,
        timeLimitSeconds = null,
        gracePeriodSeconds = null,
        maxAttempts = null,
        passGrade = null,
        sumGrades = 10.0,
        maxGrade = 10.0,
        preferredBehaviour = null,
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )

    private fun attempt(id: Int): QuizAttempt = QuizAttempt(
        id = AttemptId(id),
        quizId = quizId,
        userId = 1,
        attemptNumber = id,
        state = AttemptState.Finished,
        sumGrades = 8.0,
        timeStart = null,
        timeFinish = null,
        timeModified = null,
        layout = "1,0",
        previewMode = false,
    )

    private fun build(
        quizLoadable: Loadable<Quiz>,
        attempts: List<QuizAttempt> = emptyList(),
        bestGrade: BestGrade? = null,
        startAttempt: StartQuizAttemptUseCase = mockk(relaxed = true),
        loadPage: LoadAttemptPageUseCase = mockk(relaxed = true),
    ): QuizDetailViewModel = QuizDetailViewModel(
        key = SheetRoute.QuizDetail(quizId = quizId.value, courseId = courseId),
        savedState = SavedStateHandle(),
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(account)
        },
        observeQuiz = mockk<ObserveQuizUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(quizLoadable)
        },
        observeAttempts = mockk<ObserveQuizAttemptsUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(attempts))
        },
        observeBestGrade = mockk<ObserveQuizBestGradeUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(bestGrade))
        },
        refreshAttempts = mockk<RefreshQuizAttemptsUseCase>(relaxed = true),
        startAttempt = startAttempt,
        loadAttemptPage = loadPage,
        saveDraft = mockk(relaxed = true),
        submitAttempt = mockk(relaxed = true),
        loadReview = mockk(relaxed = true),
    )

    private fun setPage(viewModel: QuizDetailViewModel) {
        compose.setBicoccaContent {
            QuizDetailPage(
                quizId = quizId.value,
                courseId = courseId,
                viewModel = viewModel,
            )
        }
    }

    @Test
    fun an_unresolved_quiz_shows_the_loading_marker() {
        val vm = build(quizLoadable = Loadable.NotYetLoaded)
        setPage(vm)

        compose.onNodeWithTag(QuizDetailTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(QuizDetailTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun a_loaded_quiz_shows_the_overview_with_the_primary_action() {
        val vm = build(quizLoadable = Loadable.Loaded(quiz()))
        setPage(vm)

        compose.onNodeWithTag(QuizDetailTestTags.OVERVIEW).assertIsDisplayed()
        compose.onNodeWithTag(QuizDetailTestTags.PRIMARY_ACTION).assertIsDisplayed()
    }

    @Test
    fun tapping_the_primary_action_starts_an_attempt() {
        val start = mockk<StartQuizAttemptUseCase>()
        coEvery { start.invoke(accountId, quizId, any()) } returns AttemptId(7)
        val page = mockk<AttemptPage>(relaxed = true)
        val loadPage = mockk<LoadAttemptPageUseCase>()
        coEvery { loadPage.invoke(accountId, AttemptId(7), 0) } returns page
        val vm = build(quizLoadable = Loadable.Loaded(quiz()), startAttempt = start, loadPage = loadPage)
        setPage(vm)

        compose.onNodeWithTag(QuizDetailTestTags.PRIMARY_ACTION).performClick()
        compose.waitForIdle()

        coVerify { start.invoke(accountId, quizId, any()) }
    }

    @Test
    fun the_history_action_opens_the_attempts_list_with_keyed_rows() {
        val vm = build(quizLoadable = Loadable.Loaded(quiz()), attempts = listOf(attempt(1), attempt(2)))
        setPage(vm)

        compose.onNodeWithTag(QuizDetailTestTags.HISTORY_ACTION).assertIsDisplayed()
        compose.onNodeWithTag(QuizDetailTestTags.HISTORY_ACTION).performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag(QuizDetailTestTags.HISTORY_LIST)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.waitForIdle()

        compose.onNodeWithTag(QuizDetailTestTags.HISTORY_LIST).assertIsDisplayed()
        compose.onNodeWithTag(QuizDetailTestTags.attempt(1)).assertIsDisplayed()
        compose.onNodeWithTag(QuizDetailTestTags.attempt(2)).assertIsDisplayed()
    }
}
