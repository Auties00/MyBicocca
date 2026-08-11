package it.attendance100.mybicocca.ui.screen.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.settings.BadgeCardTheme
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetPrerequisiteStatusUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveGradeRollupUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptRowsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptStatsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.RefreshTranscriptUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

/**
 * Coverage for the profile body ViewModel: transcript rows filtered to study-plan activities,
 * the independent sync status with its derived isRefreshing/errorMessage, the
 * NotYetLoaded -> Loaded data path, the lazily filled prerequisite map (passed rows skipped),
 * the forced refresh on pull, and clearError.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)
    private val accountId = AccountId("acc-1")

    private val account = Account(
        id = accountId,
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

    private fun row(
        id: Long,
        state: TranscriptRowState,
        inStudyPlan: Boolean = true,
    ) = TranscriptRow(
        id = id,
        careerId = careerId,
        activityCode = "E3101Q$id",
        activityName = "Attivita $id",
        courseYear = 1,
        credits = 6f,
        state = state,
        grade = if (state == TranscriptRowState.Passed) 28 else null,
        cumLaude = false,
        examDate = if (state == TranscriptRowState.Passed) LocalDate.of(2025, 1, 10) else null,
        academicYear = 2024,
        inStudyPlan = inStudyPlan,
        examType = "Scritto",
        bookableCallsCount = 0,
    )

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observeStats: ObserveTranscriptStatsUseCase = mockk()
    private val observeGradeRollup: ObserveGradeRollupUseCase = mockk()
    private val observeRows: ObserveTranscriptRowsUseCase = mockk()
    private val refreshTranscript: RefreshTranscriptUseCase = mockk(relaxed = true)
    private val getPrerequisiteStatus: GetPrerequisiteStatusUseCase = mockk()
    private val getUserPhoto: GetUserPhotoUseCase = mockk()
    private val observeBadgeCardTheme: ObserveBadgeCardThemeUseCase = mockk()

    private fun viewModel(
        rows: Loadable<List<TranscriptRow>> = Loadable.Loaded(emptyList()),
        stats: Loadable<TranscriptStats> = Loadable.NotYetLoaded,
        rollup: Loadable<GradeRollup> = Loadable.NotYetLoaded,
        activeAccount: Account? = account,
        prerequisiteStatuses: Map<Long, PrerequisiteStatus> = emptyMap(),
        badgeCardTheme: BadgeCardTheme = BadgeCardTheme.Default,
    ): ProfileViewModel {
        every { observeActiveAccount() } returns flowOf(activeAccount)
        every { observeStats(any()) } returns flowOf(stats)
        every { observeGradeRollup(any()) } returns flowOf(rollup)
        every { observeRows(any()) } returns flowOf(rows)
        every { observeBadgeCardTheme() } returns flowOf(badgeCardTheme)
        coEvery { getUserPhoto(any()) } returns "/tmp/photo.jpg"
        coEvery { getPrerequisiteStatus(any(), any()) } returns PrerequisiteStatus.Unknown
        prerequisiteStatuses.forEach { (activityId, status) ->
            coEvery { getPrerequisiteStatus(careerId, activityId) } returns status
        }
        return ProfileViewModel(
            observeActiveAccount,
            observeStats,
            observeGradeRollup,
            observeRows,
            refreshTranscript,
            getPrerequisiteStatus,
            getUserPhoto,
            observeBadgeCardTheme,
        )
    }

    @Test
    fun `account and activeCareer derive from the active account`() = runTest {
        val vm = viewModel()

        assertThat(vm.account.value).isEqualTo(account)
        vm.activeCareer.test {
            assertThat(awaitItem()?.id).isEqualTo(careerId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transcriptRows drops supernumerary rows not in the study plan`() = runTest {
        val rows = listOf(
            row(1, TranscriptRowState.Passed, inStudyPlan = true),
            row(2, TranscriptRowState.Planned, inStudyPlan = false),
        )
        val vm = viewModel(rows = Loadable.Loaded(rows))

        vm.transcriptRows.test {
            val loaded = awaitItem()
            assertThat(loaded).isInstanceOf(Loadable.Loaded::class.java)
            val value = (loaded as Loadable.Loaded).value
            assertThat(value).hasSize(1)
            assertThat(value.single().id).isEqualTo(1L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init runs a non-forced refresh and stays Idle on success`() = runTest {
        val vm = viewModel()

        coVerify { refreshTranscript(careerId, false) }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `refresh forces a re-sync`() = runTest {
        val vm = viewModel()

        vm.refresh()

        coVerify { refreshTranscript(careerId, true) }
    }

    @Test
    fun `refresh failure sets Failed with derived error message and isRefreshing false`() = runTest {
        val boom = IOException("offline")
        coEvery { refreshTranscript(any(), true) } throws boom
        val vm = viewModel()

        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat(vm.isRefreshing.value).isFalse()
        vm.errorMessage.test {
            assertThat((awaitItem() as it.attendance100.mybicocca.core.text.UiText.StringResource).resId).isEqualTo(
                it.attendance100.mybicocca.R.string.profile_refresh_error
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError returns the sync status to Idle after a failure`() = runTest {
        coEvery { refreshTranscript(any(), true) } throws IOException("offline")
        val vm = viewModel()
        vm.refresh()
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)

        vm.clearError()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        vm.errorMessage.test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError is a no-op when not in a failure state`() = runTest {
        val vm = viewModel()

        vm.clearError()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `prerequisite statuses are fetched for pending rows and skip passed ones`() = runTest {
        val rows = listOf(
            row(1, TranscriptRowState.Passed),
            row(2, TranscriptRowState.Planned),
            row(3, TranscriptRowState.Frequented),
        )
        val vm = viewModel(
            rows = Loadable.Loaded(rows),
            prerequisiteStatuses = mapOf(
                2L to PrerequisiteStatus.NotSatisfied,
                3L to PrerequisiteStatus.Satisfied,
            ),
        )

        vm.prerequisiteStatuses.test {
            assertThat(awaitItem()).containsExactly(
                2L, PrerequisiteStatus.NotSatisfied,
                3L, PrerequisiteStatus.Satisfied,
            )
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { getPrerequisiteStatus(careerId, 1L) }
    }

    @Test
    fun `badgeCardTheme streams the persisted finish`() = runTest {
        val vm = viewModel(badgeCardTheme = BadgeCardTheme.White)

        vm.badgeCardTheme.test {
            assertThat(awaitItem()).isEqualTo(BadgeCardTheme.White)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `requestHypotheticalCalculator fires a one-shot open request`() = runTest {
        val vm = viewModel()

        vm.openCalculatorRequests.test {
            vm.requestHypotheticalCalculator()
            assertThat(awaitItem()).isEqualTo(Unit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stats stays NotYetLoaded with no active career`() = runTest {
        val vm = viewModel(activeAccount = null)

        vm.stats.test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
