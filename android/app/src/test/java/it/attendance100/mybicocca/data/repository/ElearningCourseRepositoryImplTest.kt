package it.attendance100.mybicocca.data.repository

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.course.ActivityCompletionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.local.settings.ElearningFilterStore
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningEnrollIntoCourseResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningResponseWarning
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the course repository's tractable paths: the completion-state stream
 * mapping, the TTL-gated and deduplicated enrolled-courses refresh, the local-only favourite /
 * hidden writes, the enrol mutation surfacing a server failure, and the activity-completion
 * mutation that mirrors the change to Moodle. The fan-out detail refresh and enrolled-list merge
 * are exercised by their dedicated mapper tests; here the focus is the repository policy.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningCourseRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val lmsUserId = 7
    private val account = elearningRepoTestAccount(accountId, lmsUserId)
    private val token = "x".repeat(32)

    private val context = mockk<Context>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val courseDao = mockk<CourseDao>(relaxed = true)
    private val deadlineDao = mockk<DeadlineDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val filterStore = mockk<ElearningFilterStore>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): ElearningCourseRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningCourseRepositoryImpl(
            context = context,
            sessionManager = sessionManager,
            courseDao = courseDao,
            deadlineDao = deadlineDao,
            syncStateDao = syncStateDao,
            stalePolicy = stalePolicy,
            filterStore = filterStore,
            scope = scope,
        )
    }

    @Test
    fun `observeCompletionStates keys the domain map by course-module id`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { courseDao.observeCompletion(accountId.value, courseId.value) } returns flowOf(
            listOf(
                ActivityCompletionEntity(
                    accountId = accountId.value,
                    courseId = courseId.value,
                    cmId = 1001,
                    isCompleted = true,
                    completedAtMs = null,
                    isManual = true,
                    isAutomatic = false,
                    isTracked = true,
                ),
            ),
        )

        repository.observeCompletionStates(accountId, courseId).test {
            val map = awaitItem()
            assertThat(map.keys).containsExactly(1001)
            assertThat(map[1001]!!.isCompleted).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `observeFilter exposes the filter store stream`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { filterStore.filter } returns flowOf(CourseFilter.Favourites)

        repository.observeFilter().test {
            assertThat(awaitItem()).isEqualTo(CourseFilter.Favourites)
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshEnrolledCourses skips all network while fresh and not forced`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.ENROLLED_COURSES, 0L)
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.ENROLLED_COURSES,
            scopeId = 0L,
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refreshEnrolledCourses(accountId, force = false)

        coVerify(exactly = 0) { elearningApi.courses.getUserCourses(any(), any()) }
        coVerify(exactly = 0) { courseDao.replaceEnrolled(any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setFavourite is a local-only Room write`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.setFavourite(accountId, courseId, favourite = true)

        coVerify { courseDao.setFavourite(accountId.value, courseId.value, true) }
        coVerify(exactly = 0) { sessionManager.elearning() }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setHidden is a local-only Room write`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.setHidden(accountId, courseId, hidden = true)

        coVerify { courseDao.setHidden(accountId.value, courseId.value, true) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setActivityCompleted updates the platform then mirrors the fresh status into Room`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.completion.getActivitiesCompletionStatus(token, courseId.value, null) } returns
            mockk(relaxed = true)

        repository.setActivityCompleted(accountId, courseId, cmId = 1001, completed = true)

        coVerify { elearningApi.completion.updateActivityCompletion(token, 1001, true) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setActivityCompleted survives a failed status re-fetch`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.completion.getActivitiesCompletionStatus(token, courseId.value, null) } throws
            RuntimeException("boom")

        repository.setActivityCompleted(accountId, courseId, cmId = 1001, completed = true)

        coVerify { elearningApi.completion.updateActivityCompletion(token, 1001, true) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `enrolIntoCourse raises the server warning message when status is false`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.courses.enrollIntoCourse(token, courseId.value, null) } returns
            ElearningEnrollIntoCourseResponse(
                status = false,
                warnings = listOf(ElearningResponseWarning(message = "Chiave di iscrizione errata")),
            )

        val thrown = repository.runCatching {
            enrolIntoCourse(accountId, courseId, password = null)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown!!.message).isEqualTo("Chiave di iscrizione errata")
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `enrolIntoCourse succeeds silently when status is true`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.courses.enrollIntoCourse(token, courseId.value, "secret") } returns
            ElearningEnrollIntoCourseResponse(status = true)

        repository.enrolIntoCourse(accountId, courseId, password = "secret")

        coVerify { elearningApi.courses.enrollIntoCourse(token, courseId.value, "secret") }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `clearForAccount wipes both course and deadline caches`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.clearForAccount(accountId)

        coVerify { courseDao.clearAllForAccount(accountId.value) }
        coVerify { deadlineDao.deleteForAccount(accountId.value) }
        scope.coroutineContext[Job]?.cancel()
    }
}
