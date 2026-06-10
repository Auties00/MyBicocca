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
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.grade.CourseGradeOverviewEntity
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
import it.attendance100.mybicocca.data.local.elearning.grade.GradeItemEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseGrade
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetCourseGradesResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetGradeItemsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGradeItem
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUserGrades
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItemType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Behaviour coverage for the grade repository: a cache-as-SSOT repo where `observe*` streams hot
 * from Room and `refresh*` writes the mapped API rows through to the DAO. Tests assert the
 * write-through, the per-account user-grade filter, the staleness skip, and the in-flight
 * deduplication, with the API client and DAOs mocked at their boundaries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningGradeRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val lmsUserId = 7

    private val account = elearningRepoTestAccount(accountId, lmsUserId)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val gradeDao = mockk<GradeDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val token = "x".repeat(32)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): ElearningGradeRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningGradeRepositoryImpl(
            sessionManager = sessionManager,
            gradeDao = gradeDao,
            syncStateDao = syncStateDao,
            stalePolicy = stalePolicy,
            scope = scope,
        )
    }

    @Test
    fun `observeCourseGradeItems maps cached rows to Loaded domain list`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { gradeDao.observeCourseGradeItems(accountId.value, courseId.value) } returns flowOf(
            listOf(
                GradeItemEntity(
                    accountId = accountId.value,
                    courseId = courseId.value,
                    itemId = 100L,
                    name = "Esame scritto",
                    typeRaw = "course",
                    activityType = null,
                    grade = 28.0,
                    maxGrade = 30.0,
                    percentage = 93.3,
                    gradeFormatted = "28,00",
                    feedback = null,
                    gradedAtMs = null,
                    sortOrder = 0,
                ),
            ),
        )

        repository.observeCourseGradeItems(accountId, courseId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value.first().id).isEqualTo(100L)
            assertThat(loaded.value.first().type).isEqualTo(GradeItemType.Course)
            assertThat(loaded.value.first().grade).isEqualTo(28.0)
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `observeAllCourseGrades wraps empty Room emission in Loaded not NotYetLoaded`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        every { gradeDao.observeAllCourseGrades(accountId.value) } returns flowOf(emptyList())

        repository.observeAllCourseGrades(accountId).test {
            val loaded = awaitItem()
            assertThat(loaded).isInstanceOf(Loadable.Loaded::class.java)
            assertThat((loaded as Loadable.Loaded).value).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `refreshCourseGradeItems writes the active user's items through and stamps`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.grades.getGradeItems(token, courseId.value, lmsUserId, null) } returns
            ElearningGetGradeItemsResponse(
                userGrades = listOf(
                    ElearningUserGrades(
                        courseId = courseId.value,
                        userId = 999,
                        gradeItems = listOf(ElearningGradeItem(id = 1, itemName = "Other user")),
                    ),
                    ElearningUserGrades(
                        courseId = courseId.value,
                        userId = lmsUserId,
                        gradeItems = listOf(
                            ElearningGradeItem(id = 50, itemName = "Voto finale", itemType = "course", gradeRaw = 30.0),
                        ),
                    ),
                ),
            )

        repository.refreshCourseGradeItems(accountId, courseId, force = false)

        val rows = slot<List<GradeItemEntity>>()
        coVerify { gradeDao.replaceCourseGradeItems(accountId.value, courseId.value, capture(rows)) }
        assertThat(rows.captured).hasSize(1)
        assertThat(rows.captured.first().itemId).isEqualTo(50L)
        assertThat(rows.captured.first().name).isEqualTo("Voto finale")
        coVerify {
            syncStateDao.upsertState(match { it.scope == ElearningSyncScope.COURSE_GRADES && it.scopeId == courseId.value.toLong() })
        }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `refreshCourseGradeItems skips network while fresh and not forced`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.COURSE_GRADES, courseId.value.toLong())
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.COURSE_GRADES,
            scopeId = courseId.value.toLong(),
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refreshCourseGradeItems(accountId, courseId, force = false)

        coVerify(exactly = 0) { elearningApi.grades.getGradeItems(any(), any(), any(), any()) }
        coVerify(exactly = 0) { gradeDao.replaceCourseGradeItems(any(), any(), any()) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `refreshCourseGradeItems with force fetches even when cache is fresh`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            syncStateDao.getState(accountId.value, ElearningSyncScope.COURSE_GRADES, courseId.value.toLong())
        } returns ElearningSyncStateEntity(
            accountId = accountId.value,
            scope = ElearningSyncScope.COURSE_GRADES,
            scopeId = courseId.value.toLong(),
            lastRefreshedAtMs = System.currentTimeMillis(),
        )
        coEvery { elearningApi.grades.getGradeItems(token, courseId.value, lmsUserId, null) } returns
            ElearningGetGradeItemsResponse(userGrades = emptyList())

        repository.refreshCourseGradeItems(accountId, courseId, force = true)

        coVerify { elearningApi.grades.getGradeItems(token, courseId.value, lmsUserId, null) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `refreshAllCourseGrades stores overview rows with no course name and stamps`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.grades.getCourseGrades(token, lmsUserId) } returns
            ElearningGetCourseGradesResponse(
                grades = listOf(ElearningCourseGrade(courseId = 11, grade = "27,00", rawGrade = "27.0")),
            )

        repository.refreshAllCourseGrades(accountId, force = false)

        val rows = slot<List<CourseGradeOverviewEntity>>()
        coVerify { gradeDao.replaceAllCourseGrades(accountId.value, capture(rows)) }
        assertThat(rows.captured).hasSize(1)
        assertThat(rows.captured.first().courseName).isEmpty()
        assertThat(rows.captured.first().grade).isEqualTo(27.0)
        coVerify {
            syncStateDao.upsertState(match { it.scope == ElearningSyncScope.ALL_COURSE_GRADES && it.scopeId == 0L })
        }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `refreshCourseGradeItems dedups concurrent callers into one network pass`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        val gate = CompletableDeferred<Unit>()
        coEvery { elearningApi.grades.getGradeItems(token, courseId.value, lmsUserId, null) } coAnswers {
            gate.await()
            ElearningGetGradeItemsResponse(userGrades = emptyList())
        }

        val first = launch { repository.refreshCourseGradeItems(accountId, courseId, force = false) }
        runCurrent()
        val second = launch { repository.refreshCourseGradeItems(accountId, courseId, force = false) }
        runCurrent()

        gate.complete(Unit)
        joinAll(first, second)

        coVerify(exactly = 1) { elearningApi.grades.getGradeItems(token, courseId.value, lmsUserId, null) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.clearForAccount(accountId)

        coVerify { gradeDao.clearAllForAccount(accountId.value) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}
