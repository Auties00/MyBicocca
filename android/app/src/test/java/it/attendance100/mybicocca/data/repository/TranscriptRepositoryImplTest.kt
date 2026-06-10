package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.transcript.GradeRollupProjection
import it.attendance100.mybicocca.data.local.transcript.TranscriptDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PrerequisitesCheck
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptRow
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptStats
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3TranscriptTest
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the transcript repository: the observe (hot Room Flow wrapped in
 * Loadable) vs refresh (live fetch, atomic Room swap, TTL gate, per-career de-dup) split, the
 * course-detail fan-out (prerequisite skip for passed activities, bookable-count fallback to 0),
 * and the passed-course-name cache that triggers a best-effort refresh when empty.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TranscriptRepositoryImplTest {

    private val careerId: CareerId = RepositoryTestFixtures.careerId
    private val matId: Long = RepositoryTestFixtures.ENROLLMENT_TRAIT_ID
    private val account: Account = RepositoryTestFixtures.account()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val esse3 = mockk<Esse3Api>(relaxed = true)
    private val dao = mockk<TranscriptDao>(relaxed = true)
    private val syncStateDao = mockk<TranscriptSyncStateDao>(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): TranscriptRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.esse3() } returns esse3
        return TranscriptRepositoryImpl(sessionManager, dao, syncStateDao, stalePolicy, scope)
    }

    private fun rowEntity(id: Long, name: String, state: TranscriptRowState) = TranscriptRowEntity(
        id = id,
        careerId = careerId.value,
        activityCode = "E3101Q$id",
        activityName = name,
        courseYear = 1,
        credits = 6f,
        state = state.name,
        grade = if (state == TranscriptRowState.Passed) 28 else null,
        cumLaude = false,
        examDate = "2024-06-01",
        academicYear = 2024,
        inStudyPlan = true,
        examType = "Scritto",
        bookableCallsCount = 0,
    )

    @Test
    fun `observeRows wraps the Room rows in Loaded`() = runTest {
        val repository = newRepository(this)
        every { dao.observeRows(careerId.value) } returns flowOf(
            listOf(rowEntity(1L, "Analisi", TranscriptRowState.Passed)),
        )

        repository.observeRows(careerId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value).hasSize(1)
            assertThat(loaded.value.first().activityName).isEqualTo("Analisi")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeStats maps a null stats row to NotYetLoaded`() = runTest {
        val repository = newRepository(this)
        every { dao.observeStats(careerId.value) } returns flowOf(null)

        repository.observeStats(careerId).test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeStats maps a present stats row to Loaded`() = runTest {
        val repository = newRepository(this)
        every { dao.observeStats(careerId.value) } returns flowOf(
            TranscriptStatsEntity(
                careerId = careerId.value,
                passedCredits = 60f,
                totalCreditsRequired = 180f,
                arithmeticAverage = 27.5f,
                weightedAverage = 28f,
                passedExamCount = 5,
                plannedExamCount = 20,
                maxGrade = 30,
                cumLaudeAvailable = true,
            ),
        )

        repository.observeStats(careerId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value.passedCredits).isEqualTo(60f)
            assertThat(loaded.value.arithmeticAverage).isEqualTo(27.5f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeGradeRollup maps a null projection to NotYetLoaded`() = runTest {
        val repository = newRepository(this)
        every { dao.observeGradeRollup(careerId.value) } returns flowOf(null)

        repository.observeGradeRollup(careerId).test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeGradeRollup folds null sums in the projection to zero`() = runTest {
        val repository = newRepository(this)
        every { dao.observeGradeRollup(careerId.value) } returns flowOf(
            GradeRollupProjection(gradedExamCount = 0, gradeSum = null, weightedGradeSum = null, gradedCreditsSum = null),
        )

        repository.observeGradeRollup(careerId).test {
            val loaded = awaitItem() as Loadable.Loaded
            assertThat(loaded.value.gradeSum).isEqualTo(0L)
            assertThat(loaded.value.weightedGradeSum).isEqualTo(0.0)
            assertThat(loaded.value.gradedCreditsSum).isEqualTo(0f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh forced fetches and swaps the transcript into Room`() = runTest {
        val repository = newRepository(this)
        coEvery { esse3.transcript.getRecordBookRows(matId) } returns emptyList()
        coEvery { esse3.transcript.getRecordBookStats(matId) } returns Esse3TranscriptStats()
        coEvery { esse3.transcript.getRecordBookAverages(matId) } returns emptyList()

        repository.refresh(careerId, force = true)

        coVerify { esse3.transcript.getRecordBookRows(matId) }
        coVerify { dao.replaceAll(careerId.value, any(), any()) }
        coVerify { syncStateDao.upsertState(match { it.careerId == careerId.value }) }
    }

    @Test
    fun `refresh skips the network while the cache is fresh and not forced`() = runTest {
        val repository = newRepository(this)
        coEvery { syncStateDao.getState(careerId.value) } returns TranscriptSyncStateEntity(
            careerId = careerId.value,
            lastRefreshedAtMs = System.currentTimeMillis(),
        )

        repository.refresh(careerId, force = false)

        coVerify(exactly = 0) { esse3.transcript.getRecordBookRows(any()) }
        coVerify(exactly = 0) { dao.replaceAll(any(), any(), any()) }
    }

    @Test
    fun `refresh no-ops for a career absent from the active account`() = runTest {
        val repository = newRepository(this)

        repository.refresh(CareerId(999_999L), force = true)

        coVerify(exactly = 0) { esse3.transcript.getRecordBookRows(any()) }
    }

    @Test
    fun `refresh merges the dedicated averages when the inline ones are empty`() = runTest {
        val repository = newRepository(this)
        coEvery { esse3.transcript.getRecordBookRows(matId) } returns emptyList()
        coEvery { esse3.transcript.getRecordBookStats(matId) } returns Esse3TranscriptStats(averages = emptyList())
        coEvery { esse3.transcript.getRecordBookAverages(matId) } returns emptyList()

        repository.refresh(careerId, force = true)

        coVerify { esse3.transcript.getRecordBookAverages(matId) }
    }

    @Test
    fun `getCourseDetail skips the prerequisite call for an already-passed activity`() = runTest {
        val repository = newRepository(this)
        val transcriptApi = esse3.transcript
        coEvery { transcriptApi.getRecordBookRowTests(matId, 11L) } returns emptyList()
        val row = mockk<Esse3TranscriptRow>(relaxed = true)
        every { row.bookableCallsNumber } returns 3
        coEvery { transcriptApi.getRecordBookRow(matId, 11L) } returns row

        val detail = repository.getCourseDetail(careerId, activityChoiceId = 11L, alreadyPassed = true)

        assertThat(detail.prerequisites).isEqualTo(PrerequisiteStatus.Unknown)
        assertThat(detail.bookableCallsCount).isEqualTo(3)
        coVerify { transcriptApi.getRecordBookRowTests(matId, 11L) }
        coVerify { transcriptApi.getRecordBookRow(matId, 11L) }
        coVerify(exactly = 0) { transcriptApi.getCheckProposalRecordBookRow(matId, 11L, any()) }
    }

    @Test
    fun `getCourseDetail checks the prerequisite for a not-yet-passed activity`() = runTest {
        val repository = newRepository(this)
        coEvery { esse3.transcript.getRecordBookRowTests(matId, 11L) } returns emptyList<Esse3TranscriptTest>()
        coEvery { esse3.transcript.getCheckProposalRecordBookRow(matId, 11L) } returns
            Esse3PrerequisitesCheck(outcome = 1)
        val row = mockk<Esse3TranscriptRow>(relaxed = true)
        every { row.bookableCallsNumber } returns 1
        coEvery { esse3.transcript.getRecordBookRow(matId, 11L) } returns row

        val detail = repository.getCourseDetail(careerId, activityChoiceId = 11L, alreadyPassed = false)

        assertThat(detail.prerequisites).isEqualTo(PrerequisiteStatus.Satisfied)
    }

    @Test
    fun `getCourseDetail falls back to zero bookable calls when the row re-read fails`() = runTest {
        val repository = newRepository(this)
        coEvery { esse3.transcript.getRecordBookRowTests(matId, 11L) } returns emptyList()
        coEvery { esse3.transcript.getRecordBookRow(matId, 11L) } throws RuntimeException("boom")

        val detail = repository.getCourseDetail(careerId, activityChoiceId = 11L, alreadyPassed = true)

        assertThat(detail.bookableCallsCount).isEqualTo(0)
    }

    @Test
    fun `getPrerequisiteStatus degrades to Unknown on a failed check`() = runTest {
        val repository = newRepository(this)
        coEvery { esse3.transcript.getCheckProposalRecordBookRow(matId, 11L) } throws RuntimeException("422")

        val status = repository.getPrerequisiteStatus(careerId, activityChoiceId = 11L)

        assertThat(status).isEqualTo(PrerequisiteStatus.Unknown)
    }

    @Test
    fun `getPrerequisiteStatus is Unknown for a career absent from the active account`() = runTest {
        val repository = newRepository(this)

        val status = repository.getPrerequisiteStatus(CareerId(999_999L), activityChoiceId = 11L)

        assertThat(status).isEqualTo(PrerequisiteStatus.Unknown)
        coVerify(exactly = 0) { esse3.transcript.getCheckProposalRecordBookRow(any(), any(), any()) }
    }

    @Test
    fun `getPassedCourseNames returns the normalized cached names without refreshing`() = runTest {
        val repository = newRepository(this)
        coEvery { dao.getPassedActivityNames(careerId.value) } returns
            listOf("  Algebra  Lineare ", "", "Analisi")

        val names = repository.getPassedCourseNames(careerId)

        assertThat(names).containsExactly("ALGEBRA LINEARE", "ANALISI")
    }

    @Test
    fun `getPassedCourseNames refreshes once when the cache is empty then re-reads`() = runTest {
        val repository = newRepository(this)
        coEvery { dao.getPassedActivityNames(careerId.value) } returnsMany listOf(
            emptyList(),
            listOf("Fisica"),
        )
        coEvery { esse3.transcript.getRecordBookRows(matId) } returns emptyList()
        coEvery { esse3.transcript.getRecordBookStats(matId) } returns Esse3TranscriptStats()
        coEvery { esse3.transcript.getRecordBookAverages(matId) } returns emptyList()

        val names = repository.getPassedCourseNames(careerId)

        assertThat(names).containsExactly("FISICA")
        coVerify { esse3.transcript.getRecordBookRows(matId) }
    }
}
