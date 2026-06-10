package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetEnrollmentHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetRenewalWebUrlUseCase
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

/**
 * State and behaviour coverage for the Iscrizioni sheet. The screen is driven by a real
 * [EnrollmentsViewModel] over MockK-faked use cases (the Wave-1 construction), the history fetch
 * stubbed to the snapshot each test needs. State tests assert exactly one [EnrollmentsTestTags]
 * state marker; the behaviour test taps the pinned renewal footer and verifies it resolves the
 * Esse3 web URL through the view model. Wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class EnrollmentsTimelinePageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getEnrollmentHistory: GetEnrollmentHistoryUseCase = mockk()
    private val getRenewalWebUrl: GetRenewalWebUrlUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): EnrollmentsViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return EnrollmentsViewModel(observeActiveAccount, getEnrollmentHistory, getRenewalWebUrl)
    }

    private fun setScreen(vm: EnrollmentsViewModel, onOpenDetail: (EnrollmentId) -> Unit = {}) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                EnrollmentsTimelinePage(viewModel = vm, onOpenDetail = onOpenDetail)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun a_never_completing_fetch_keeps_the_loading_marker_on_screen() {
        coEvery { getEnrollmentHistory(careerId) } coAnswers { CompletableDeferred<EnrollmentHistory>().await() }
        setScreen(viewModel())

        compose.onNodeWithTag(EnrollmentsTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(EnrollmentsTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun a_renewable_history_renders_a_row_per_year_and_the_renewal_footer() {
        coEvery { getEnrollmentHistory(careerId) } returns
            EnrollmentHistory(years = listOf(enrollment(id = 5L)), renewal = RenewalState.Renewable(2025))
        setScreen(viewModel())

        compose.onNodeWithTag(EnrollmentsTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(EnrollmentsTestTags.row(5L)).assertIsDisplayed()
        compose.onNodeWithTag(EnrollmentsTestTags.RENEW_BUTTON).assertIsDisplayed()
    }

    @Test
    fun a_fetch_failure_renders_the_error_marker() {
        coEvery { getEnrollmentHistory(careerId) } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(EnrollmentsTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_renew_resolves_the_Esse3_web_url_through_the_view_model() {
        coEvery { getEnrollmentHistory(careerId) } returns
            EnrollmentHistory(years = listOf(enrollment(id = 5L)), renewal = RenewalState.Renewable(2025))
        every { getRenewalWebUrl(careerId) } returns "https://s3w.si.unimib.it/renew"
        setScreen(viewModel())

        compose.onNodeWithTag(EnrollmentsTestTags.RENEW_BUTTON).performClick()
        compose.waitForIdle()

        verify { getRenewalWebUrl(careerId) }
    }

    private fun enrollment(id: Long): AnnualEnrollment = AnnualEnrollment(
        id = EnrollmentId(id),
        academicYear = 2024,
        courseYear = 1,
        outOfCourseYears = 0,
        type = EnrollmentType.InProgress,
        typeDescription = "In corso",
        status = EnrollmentStatus.Active,
        statusReasonCode = null,
        conditional = false,
        reconstructed = false,
        partTime = null,
        suspension = null,
        awaitingDegree = false,
        degreeAwardDate = null,
        studentTypeDescription = null,
        exemptionDescription = null,
        incomeBandId = null,
        canteenBandId = null,
        meritBandId = null,
        meritNote = null,
        enrollmentNote = null,
        disabilityPercentage = null,
        disabilityTypeDescription = null,
        courseDescription = "Informatica",
        courseTypeDescription = null,
        degreeClassCode = null,
        degreeClassDescription = null,
        orientationDescription = null,
        addressDescription = null,
        studyOrderDescription = null,
        minimumCredits = null,
        courseDuration = null,
        teachingLanguage = null,
        regulationCode = null,
        universityDescription = null,
        siteDescription = null,
        enrollmentDate = null,
        insertionDate = null,
        modificationDate = null,
    )

    private fun account(careerId: CareerId): Account = Account(
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
}
