package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptAnswerEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizDao
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttemptDataItem
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetQuizzesResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningQuiz
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the quiz repository: the TTL-gated, deduplicated course refresh, and the
 * draft/submit write ordering — draft answers land in Room before the remote autosave so leaving
 * never loses progress, and submitting clears them after the remote process call. Also covers the
 * flat name/value flattening of attempt answers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningQuizRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val quizId = QuizId(100)
    private val attemptId = AttemptId(7000)
    private val lmsUserId = 7
    private val account = elearningRepoTestAccount(accountId, lmsUserId)
    private val token = "x".repeat(32)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val quizDao = mockk<QuizDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): ElearningQuizRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningQuizRepositoryImpl(sessionManager, quizDao, syncStateDao, stalePolicy, scope)
    }

    @Test
    fun `observeForCourse maps rows to Loaded`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { quizDao.observeForCourse(accountId.value, courseId.value) } returns flowOf(
            listOf(quizRow(1)),
        )

        repository.observeForCourse(accountId, courseId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `observe maps null row to NotYetLoaded`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { quizDao.observe(accountId.value, quizId.value) } returns flowOf(null)

        repository.observe(accountId, quizId).test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse writes quizzes through and stamps`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.quizzes.getQuizzesForCourse(token, courseId.value) } returns
            ElearningGetQuizzesResponse(
                quizzes = listOf(
                    ElearningQuiz(id = 100, courseId = courseId.value, courseModuleId = 9, name = "Quiz 1"),
                ),
            )

        repository.refreshForCourse(accountId, courseId, force = false)

        val rows = slot<List<QuizEntity>>()
        coVerify { quizDao.replaceForCourse(accountId.value, courseId.value, capture(rows)) }
        assertThat(rows.captured.first().quizId).isEqualTo(100)
        coVerify {
            syncStateDao.upsertState(match { it.scope == ElearningSyncScope.COURSE_QUIZZES })
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse dedups concurrent callers`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        val gate = CompletableDeferred<Unit>()
        coEvery { elearningApi.quizzes.getQuizzesForCourse(token, courseId.value) } coAnswers {
            gate.await()
            ElearningGetQuizzesResponse(quizzes = emptyList())
        }

        val first = launch { repository.refreshForCourse(accountId, courseId, force = false) }
        runCurrent()
        val second = launch { repository.refreshForCourse(accountId, courseId, force = false) }
        runCurrent()

        gate.complete(Unit)
        joinAll(first, second)

        coVerify(exactly = 1) { elearningApi.quizzes.getQuizzesForCourse(token, courseId.value) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshForCourse skips network while fresh and not forced`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.COURSE_QUIZZES, courseId.value.toLong())
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.COURSE_QUIZZES,
            scopeId = courseId.value.toLong(),
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refreshForCourse(accountId, courseId, force = false)

        coVerify(exactly = 0) { elearningApi.quizzes.getQuizzesForCourse(any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `saveDraft persists answers to Room before the remote autosave`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        val answers = listOf(
            AttemptAnswer(slot = 1, fields = mapOf("q1:answer" to "Paris", "q1:sequencecheck" to "2")),
        )

        repository.saveDraft(accountId, attemptId, answers)

        val rows = slot<List<QuizAttemptAnswerEntity>>()
        coVerify { quizDao.upsertAnswers(capture(rows)) }
        assertThat(rows.captured.first().attemptId).isEqualTo(attemptId.value)
        assertThat(rows.captured.first().slot).isEqualTo(1)
        val items = slot<List<ElearningAttemptDataItem>>()
        coVerify { elearningApi.quizzes.saveAttempt(token, attemptId.value, capture(items), emptyList()) }
        assertThat(items.captured.map { it.name }).containsExactly("q1:answer", "q1:sequencecheck")
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `submitAttempt processes the flattened items and clears the draft answers`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        val answers = listOf(
            AttemptAnswer(slot = 1, fields = mapOf("q1:answer" to "A")),
            AttemptAnswer(slot = 2, fields = mapOf("q2:answer" to "B")),
        )

        repository.submitAttempt(accountId, attemptId, answers, timeUp = false)

        val items = slot<List<ElearningAttemptDataItem>>()
        coVerify {
            elearningApi.quizzes.processAttempt(token, attemptId.value, capture(items), true, false, emptyList())
        }
        assertThat(items.captured).hasSize(2)
        coVerify { quizDao.deleteAnswers(accountId.value, attemptId.value) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `observeDraftAnswers maps cached rows to domain`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { quizDao.observeAnswers(accountId.value, attemptId.value) } returns flowOf(
            listOf(
                QuizAttemptAnswerEntity(
                    accountId = accountId.value,
                    attemptId = attemptId.value,
                    slot = 3,
                    fieldsJson = """{"q3:answer":"yes"}""",
                ),
            ),
        )

        repository.observeDraftAnswers(accountId, attemptId).test {
            val answers = awaitItem()
            assertThat(answers).hasSize(1)
            assertThat(answers.first().slot).isEqualTo(3)
            assertThat(answers.first().fields).containsEntry("q3:answer", "yes")
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.clearForAccount(accountId)

        coVerify { quizDao.clearAllForAccount(accountId.value) }
        scope.coroutineContext[Job]?.cancel()
    }

    private fun quizRow(id: Int) = QuizEntity(
        accountId = accountId.value,
        quizId = id,
        courseId = courseId.value,
        cmId = 9,
        name = "Quiz $id",
        intro = null,
        timeOpenMs = null,
        timeCloseMs = null,
        timeLimitSeconds = null,
        gracePeriodSeconds = null,
        maxAttempts = null,
        passGrade = null,
        sumGrades = null,
        maxGrade = null,
        preferredBehaviour = null,
        reviewBeforeBitmask = null,
        reviewAfterBitmask = null,
    )
}
