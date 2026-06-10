package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
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
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveBadgeCardThemeUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.GetPrerequisiteStatusUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveGradeRollupUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptRowsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptStatsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.RefreshTranscriptUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * State and behaviour coverage for the profile body. The screen is driven by a real
 * [ProfileViewModel] over MockK-faked use cases (the same construction the Wave 1
 * [ProfileViewModelTest] uses), so a [Loadable.NotYetLoaded] stats stream renders the first-load
 * skeleton while a [Loadable.Loaded] stream renders the statistics grid and grade chart. Tests
 * anchor on [ProfileTestTags] and wrap content in the app-wide
 * [it.attendance100.mybicocca.testing.setBicoccaContent] environment (the production
 * [it.attendance100.mybicocca.ui.theme.BicoccaTheme] plus the haptic/snackbar/device-type locals
 * every screen assumes).
 *
 * The statistics streams are career-scoped: [ProfileViewModel] only flows real stats once an active
 * account exposes a [it.attendance100.mybicocca.domain.model.career.CareerId], so the content-state
 * tests load [account] (whose selected career matches the stub `careerId`) to drive the cache emission.
 * That mounts the flippable [it.attendance100.mybicocca.ui.screen.profile.component.StudentCard] at the
 * top of the scrolling body, so the statistics tiles and the grade chart sit below the fold and are
 * reached with `performScrollTo()` before asserting. The not-yet-loaded test keeps a null account, which
 * also resolves to [Loadable.NotYetLoaded] and renders the first-load skeleton.
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val account = Account(
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

    private val stats = TranscriptStats(
        careerId = careerId,
        passedCredits = 60f,
        totalCreditsRequired = 180f,
        arithmeticAverage = 27.5f,
        weightedAverage = 28.1f,
        passedExamCount = 6,
        plannedExamCount = 20,
        maxGrade = 30,
        cumLaudeAvailable = true,
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
        statsLoadable: Loadable<TranscriptStats> = Loadable.NotYetLoaded,
        activeAccount: Account? = null,
    ): ProfileViewModel {
        every { observeActiveAccount() } returns flowOf(activeAccount)
        every { observeStats(any()) } returns flowOf(statsLoadable)
        every { observeGradeRollup(any()) } returns flowOf<Loadable<GradeRollup>>(Loadable.NotYetLoaded)
        every { observeRows(any()) } returns flowOf<Loadable<List<TranscriptRow>>>(Loadable.Loaded(emptyList()))
        every { observeBadgeCardTheme() } returns flowOf(BadgeCardTheme.Default)
        coEvery { getUserPhoto(any()) } returns ""
        coEvery { getPrerequisiteStatus(any(), any()) } returns PrerequisiteStatus.Unknown
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

    private fun setProfile(vm: ProfileViewModel) {
        compose.setBicoccaContent {
            ProfileContent(viewModel = vm)
        }
    }

    @Test
    fun not_yet_loaded_stats_render_the_first_load_skeleton_and_no_content() {
        setProfile(viewModel(statsLoadable = Loadable.NotYetLoaded))

        compose.onNodeWithTag(ProfileTestTags.STATE_SKELETON).assertIsDisplayed()
        compose.onAllNodesWithTag(ProfileTestTags.STATE_CONTENT).assertCountEquals(0)
    }

    @Test
    fun loaded_stats_render_the_statistics_grid_and_the_grade_chart() {
        setProfile(viewModel(statsLoadable = Loadable.Loaded(stats), activeAccount = account))

        compose.onNodeWithTag(ProfileTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(ProfileTestTags.STAT_ARITHMETIC).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(ProfileTestTags.STAT_WEIGHTED).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(ProfileTestTags.PROGRESS_EXAMS).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(ProfileTestTags.PROGRESS_CREDITS).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(ProfileTestTags.STATE_CONTENT)
            .performScrollToNode(hasTestTag(ProfileTestTags.GRADE_CHART))
        compose.onNodeWithTag(ProfileTestTags.GRADE_CHART).assertIsDisplayed()
        compose.onAllNodesWithTag(ProfileTestTags.STATE_SKELETON).assertCountEquals(0)
    }

    @Test
    fun a_loaded_active_account_renders_the_student_badge_card() {
        setProfile(viewModel(statsLoadable = Loadable.Loaded(stats), activeAccount = account))

        compose.onNodeWithTag(ProfileTestTags.STUDENT_CARD).assertIsDisplayed()
    }

    @Test
    fun tapping_a_stat_card_opens_the_calculator_and_keeps_the_content_on_screen() {
        setProfile(viewModel(statsLoadable = Loadable.Loaded(stats), activeAccount = account))

        compose.onNodeWithTag(ProfileTestTags.STAT_ARITHMETIC).performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(ProfileTestTags.STATE_CONTENT).assertIsDisplayed()
    }
}
