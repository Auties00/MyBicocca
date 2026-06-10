package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

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
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetAcademicTitlesUseCase
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

/**
 * State and behaviour coverage for the Titoli sheet. The screen is driven by a real
 * [TitlesViewModel] over MockK-faked use cases (the Wave-1 construction), the titles fetch stubbed
 * to the snapshot each test needs. State tests assert exactly one [TitlesTestTags] state marker; the
 * behaviour test taps a title row and verifies the open-detail callback fires with the title id.
 * Wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class TitlesListPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)

    private val getAcademicTitles: GetAcademicTitlesUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): TitlesViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return TitlesViewModel(observeActiveAccount, getAcademicTitles)
    }

    private fun setScreen(vm: TitlesViewModel, onOpenDetail: (String) -> Unit = {}) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                TitlesListPage(viewModel = vm, onOpenDetail = onOpenDetail)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun a_never_completing_fetch_keeps_the_loading_marker_on_screen() {
        coEvery { getAcademicTitles(careerId) } coAnswers { CompletableDeferred<List<AcademicTitle>>().await() }
        setScreen(viewModel())

        compose.onNodeWithTag(TitlesTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(TitlesTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun loaded_titles_render_the_grouped_list_with_a_row_per_title() {
        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"))
        setScreen(viewModel())

        compose.onNodeWithTag(TitlesTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(TitlesTestTags.row("t1")).assertIsDisplayed()
    }

    @Test
    fun an_empty_career_renders_the_empty_marker() {
        coEvery { getAcademicTitles(careerId) } returns emptyList()
        setScreen(viewModel())

        compose.onNodeWithTag(TitlesTestTags.STATE_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_fetch_failure_renders_the_error_marker() {
        coEvery { getAcademicTitles(careerId) } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(TitlesTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_a_title_row_opens_its_detail_with_the_title_id() {
        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"))
        val onOpenDetail = mockk<(String) -> Unit>(relaxed = true)
        setScreen(viewModel(), onOpenDetail)

        compose.onNodeWithTag(TitlesTestTags.row("t1")).performClick()
        compose.waitForIdle()

        verify { onOpenDetail("t1") }
    }

    private fun title(id: String): AcademicTitle = AcademicTitle(
        id = id,
        category = TitleCategory.Italian,
        status = TitleStatus.Awarded,
        typeDescription = "Laurea",
        subject = "Informatica",
        institution = "Bicocca",
        country = null,
        year = "2023/2024",
        grade = "110/110",
        cumLaude = true,
        valueDeclarationFiled = false,
        attributes = emptyList(),
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
