package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttemptDataItem
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetUserAttemptsRequest.AttemptStatusFilter
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptAnswer
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptId
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptPage
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.domain.model.elearning.quiz.BestGrade
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizAttempt
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningQuizRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val quizDao: QuizDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningQuizRepository {

    private val courseRefreshInFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Quiz>>> =
        quizDao.observeForCourse(accountId.value, courseId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Quiz>> }
            .flowOn(Dispatchers.Default)

    override fun observe(accountId: AccountId, quizId: QuizId): Flow<Loadable<Quiz>> =
        quizDao.observe(accountId.value, quizId.value)
            .map { row -> if (row == null) Loadable.NotYetLoaded as Loadable<Quiz> else Loadable.Loaded(row.toDomain()) }
            .flowOn(Dispatchers.Default)

    override fun observeAttempts(accountId: AccountId, quizId: QuizId): Flow<Loadable<List<QuizAttempt>>> =
        quizDao.observeAttempts(accountId.value, quizId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<QuizAttempt>> }
            .flowOn(Dispatchers.Default)

    override fun observeBestGrade(accountId: AccountId, quizId: QuizId): Flow<Loadable<BestGrade?>> =
        quizDao.observeBestGrade(accountId.value, quizId.value)
            .map { row -> Loadable.Loaded(row?.toDomain()) as Loadable<BestGrade?> }
            .flowOn(Dispatchers.Default)

    override fun observeDraftAnswers(accountId: AccountId, attemptId: AttemptId): Flow<List<AttemptAnswer>> =
        quizDao.observeAnswers(accountId.value, attemptId.value)
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        val key = RefreshKey(accountId, courseId)
        val deferred = courseRefreshInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshForCourse(accountId, courseId, force)
            }.also { d -> d.invokeOnCompletion { courseRefreshInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_QUIZZES, courseId.value.toLong())) return
        val (api, token) = sessionManager.elearning()
        val response = api.quizzes.getQuizzesForCourse(token, courseId.value)
        val rows = response.quizzes.map { it.toEntity(accountId) }
        quizDao.replaceForCourse(accountId.value, courseId.value, rows)
        stamp(accountId, ElearningSyncScope.COURSE_QUIZZES, courseId.value.toLong())
    }

    override suspend fun refreshAttempts(accountId: AccountId, quizId: QuizId) {
        val (api, token) = sessionManager.elearning()
        val attemptsResp = api.quizzes.getUserAttempts(
            wsToken = token,
            quizId = quizId.value,
            userId = null,
            status = AttemptStatusFilter.ALL,
            includePreviews = false,
        )
        val rows = attemptsResp.attempts.map { it.toEntity(accountId) }
        quizDao.replaceAttempts(accountId.value, quizId.value, rows)
        runCatching {
            val best = api.quizzes.getUserBestGrade(token, quizId.value, null)
            quizDao.upsertBestGrade(best.toEntity(accountId, quizId))
        }
        stamp(accountId, ElearningSyncScope.QUIZ_ATTEMPTS, quizId.value.toLong())
    }

    override suspend fun startAttempt(accountId: AccountId, quizId: QuizId, force: Boolean): AttemptId {
        val (api, token) = sessionManager.elearning()
        val response = api.quizzes.startAttempt(
            wsToken = token,
            quizId = quizId.value,
            preflightData = emptyList(),
            forceNew = force,
        )
        val row = response.attempt.toEntity(accountId)
        quizDao.upsertAttempts(listOf(row))
        return AttemptId(response.attempt.id)
    }

    override suspend fun loadAttemptPage(
        accountId: AccountId,
        attemptId: AttemptId,
        page: Int,
    ): AttemptPage {
        val (api, token) = sessionManager.elearning()
        val resp = api.quizzes.getAttemptData(token, attemptId.value, page, emptyList())
        // Reconcile attempt state in cache.
        quizDao.upsertAttempts(listOf(resp.attempt.toEntity(accountId)))
        return resp.toDomain(attemptId, page)
    }

    override suspend fun saveDraft(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
    ) {
        // Persist locally so navigation away doesn't lose progress.
        quizDao.upsertAnswers(answers.map { it.toEntity(accountId, attemptId) })
        val items = answers.toApiItems()
        val (api, token) = sessionManager.elearning()
        api.quizzes.saveAttempt(
            wsToken = token,
            attemptId = attemptId.value,
            data = items,
            preflightData = emptyList(),
        )
    }

    override suspend fun submitAttempt(
        accountId: AccountId,
        attemptId: AttemptId,
        answers: List<AttemptAnswer>,
        timeUp: Boolean,
    ) {
        val items = answers.toApiItems()
        val (api, token) = sessionManager.elearning()
        api.quizzes.processAttempt(
            wsToken = token,
            attemptId = attemptId.value,
            data = items,
            finishAttempt = true,
            timeUp = timeUp,
            preflightData = emptyList(),
        )
        quizDao.deleteAnswers(accountId.value, attemptId.value)
        // Refresh attempts so UI reflects Finished state and best grade.
        // We don't know the quizId from attemptId alone; the VM is expected to call refreshAttempts.
    }

    override suspend fun loadReview(accountId: AccountId, attemptId: AttemptId): AttemptReview {
        val (api, token) = sessionManager.elearning()
        val resp = api.quizzes.getAttemptReview(token, attemptId.value, null)
        quizDao.upsertAttempts(listOf(resp.attempt.toEntity(accountId)))
        return resp.toDomain(attemptId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        quizDao.clearAllForAccount(accountId.value)
    }

    private fun List<AttemptAnswer>.toApiItems(): List<ElearningAttemptDataItem> =
        flatMap { answer ->
            answer.fields.map { (name, value) -> ElearningAttemptDataItem(name = name, value = value) }
        }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private data class RefreshKey(val accountId: AccountId, val courseId: CourseId)
}
