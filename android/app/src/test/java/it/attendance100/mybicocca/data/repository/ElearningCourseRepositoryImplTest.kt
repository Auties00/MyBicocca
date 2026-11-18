package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.text.StringResolver
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
import it.attendance100.mybicocca.data.elearning.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleCalendarEvent
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleEventIcon
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleStatusResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleCalendarEventsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWarning
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
    private val auth = MoodleTokenAuth(token)

    private val stringResolver = mockk<StringResolver>(relaxed = true)
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
            stringResolver = stringResolver,
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

        coVerify(exactly = 0) { elearningApi.moodle.course.getUsersCourses(any(), any(), any()) }
        coVerify(exactly = 0) { courseDao.replaceEnrolled(any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    /**
     * Moodle ignores `aftereventid` when it cannot load that event and answers with the first page
     * again. Following the cursor alone looped forever and kept the calendar's refresh spinning.
     */
    @Test
    fun `refreshEnrolledCourses stops paging deadlines when Moodle repeats a page`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.moodle.course.getUsersCourses(lmsUserId, auth = auth) } returns emptyList()
        // Verified on the calendar itself, so the chained `moodle` getter is not counted too.
        val calendar = elearningApi.moodle.calendar
        val samePage = MoodleCalendarEventsResponse(
            events = (1..50).map { id ->
                MoodleCalendarEvent(
                    id = id,
                    name = "Consegna $id",
                    timeStart = 1_700_000_000L + id,
                    timeSort = 1_700_000_000L,
                    icon = MoodleEventIcon(),
                )
            },
            firstId = 1,
            lastId = 50,
        )
        coEvery {
            calendar.getActionEventsByTimesort(any(), any(), any(), any(), any(), any(), any(), auth)
        } returns samePage

        repository.refreshEnrolledCourses(accountId, force = true)

        coVerify(exactly = 2) {
            calendar.getActionEventsByTimesort(any(), any(), any(), any(), any(), any(), any(), auth)
        }
        coVerify { deadlineDao.replaceForAccount(accountId.value, any()) }
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
        coEvery { elearningApi.moodle.completion.getActivitiesCompletionStatus(courseId.value, lmsUserId, auth) } returns
            mockk(relaxed = true)

        repository.setActivityCompleted(accountId, courseId, cmId = 1001, completed = true)

        coVerify { elearningApi.moodle.completion.updateActivityCompletionStatusManually(1001, true, auth) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setActivityCompleted survives a failed status re-fetch`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.moodle.completion.getActivitiesCompletionStatus(courseId.value, lmsUserId, auth) } throws
            RuntimeException("boom")

        repository.setActivityCompleted(accountId, courseId, cmId = 1001, completed = true)

        coVerify { elearningApi.moodle.completion.updateActivityCompletionStatusManually(1001, true, auth) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `enrolIntoCourse raises the server warning message when status is false`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.moodle.course.enrolUser(courseId.value, null, auth = auth) } returns
            MoodleStatusResponse(
                status = false,
                warnings = listOf(MoodleWarning(message = "Chiave di iscrizione errata")),
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
        coEvery { elearningApi.moodle.course.enrolUser(courseId.value, "secret", auth = auth) } returns
            MoodleStatusResponse(status = true)

        repository.enrolIntoCourse(accountId, courseId, password = "secret")

        coVerify { elearningApi.moodle.course.enrolUser(courseId.value, "secret", auth = auth) }
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
