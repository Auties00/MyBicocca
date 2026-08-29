package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

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
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetRefundsUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

/**
 * State and behaviour coverage for the Rimborsi sheet. The screen is driven by a real
 * [RefundsViewModel] over MockK-faked use cases (the Wave-1 construction), the refund fetch stubbed
 * to the snapshot each test needs. State tests assert exactly one [RefundsTestTags] state marker;
 * the behaviour test taps a refund row and verifies the open-detail callback fires with the row's
 * stable key. Wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class RefundsListPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getRefunds: GetRefundsUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): RefundsViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return RefundsViewModel(getRefunds, observeActiveAccount)
    }

    private fun setScreen(vm: RefundsViewModel, onOpenDetail: (Long) -> Unit = {}) {
        compose.setBicoccaContent {
                RefundsListPage(viewModel = vm, onOpenDetail = onOpenDetail)

        }
        compose.waitForIdle()
    }

    @Test
    fun a_never_completing_fetch_keeps_the_loading_marker_on_screen() {
        coEvery { getRefunds(careerId) } coAnswers { CompletableDeferred<List<Refund>>().await() }
        setScreen(viewModel())

        compose.onNodeWithTag(RefundsTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(RefundsTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun loaded_refunds_render_the_list_with_a_row_per_refund() {
        coEvery { getRefunds(careerId) } returns listOf(refund(invoiceId = 7L))
        setScreen(viewModel())

        compose.onNodeWithTag(RefundsTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(RefundsTestTags.row(7L)).assertIsDisplayed()
    }

    @Test
    fun an_empty_career_renders_the_empty_marker() {
        coEvery { getRefunds(careerId) } returns emptyList()
        setScreen(viewModel())

        compose.onNodeWithTag(RefundsTestTags.STATE_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_fetch_failure_renders_the_error_marker() {
        coEvery { getRefunds(careerId) } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(RefundsTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_a_refund_row_opens_its_detail_with_the_row_key() {
        coEvery { getRefunds(careerId) } returns listOf(refund(invoiceId = 7L))
        val onOpenDetail = mockk<(Long) -> Unit>(relaxed = true)
        setScreen(viewModel(), onOpenDetail)

        compose.onNodeWithTag(RefundsTestTags.row(7L)).performClick()
        compose.waitForIdle()

        verify { onOpenDetail(7L) }
    }

    private fun refund(invoiceId: Long?): Refund = Refund(
        invoiceId = invoiceId,
        academicYear = 2024,
        amount = 100.0,
        description = "Rimborso",
        reasonCode = null,
        mandateNumber = null,
        refunded = true,
        note = null,
        collectedBy = null,
        issueDate = null,
        processingDate = null,
        paymentDate = null,
        creditDate = null,
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
