package it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.DownloadCertificateUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetCertificatesUseCase
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

/**
 * State and behaviour coverage for the Certificati sheet. The view model is Context-bound
 * (`@ApplicationContext`) but Robolectric supplies a real application [Context], so it constructs
 * and renders here; only the disk-cache existence check runs on load (no FileProvider). The screen
 * is driven by a real [CertificatesViewModel] over MockK-faked use cases, the list fetch stubbed to
 * the snapshot each test needs. State tests assert exactly one [CertificatesTestTags] state marker;
 * the behaviour test taps a certificate row, verifies the download use case runs, and asserts the
 * in-sheet result page appears (download stubbed to fail so no FileProvider open is attempted).
 * Wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class CertificatesPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val careerId = CareerId(101L)
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val getCertificates: GetCertificatesUseCase = mockk()
    private val downloadCertificate: DownloadCertificateUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): CertificatesViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return CertificatesViewModel(context, getCertificates, downloadCertificate, observeActiveAccount)
    }

    private fun setScreen(vm: CertificatesViewModel) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                CertificatesPage(viewModel = vm)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun a_never_completing_fetch_keeps_the_loading_marker_on_screen() {
        coEvery { getCertificates() } coAnswers { CompletableDeferred<List<Certificate>>().await() }
        setScreen(viewModel())

        compose.onNodeWithTag(CertificatesTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(CertificatesTestTags.STATE_LOADING).assertIsDisplayed()
    }

    @Test
    fun loaded_certificates_render_the_list_with_a_tile_per_certificate() {
        coEvery { getCertificates() } returns listOf(certificate("c1"))
        setScreen(viewModel())

        compose.onNodeWithTag(CertificatesTestTags.STATE_CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(CertificatesTestTags.tile("c1")).assertIsDisplayed()
    }

    @Test
    fun an_empty_list_renders_the_empty_marker() {
        coEvery { getCertificates() } returns emptyList()
        setScreen(viewModel())

        compose.onNodeWithTag(CertificatesTestTags.STATE_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_fetch_failure_renders_the_error_marker() {
        coEvery { getCertificates() } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(CertificatesTestTags.STATE_ERROR).assertIsDisplayed()
    }

    @Test
    fun tapping_a_certificate_runs_the_download_and_a_failure_surfaces_the_result_page() {
        coEvery { getCertificates() } returns listOf(certificate("c1"))
        coEvery { downloadCertificate(CertificateId("c1")) } throws IOException("offline")
        setScreen(viewModel())

        compose.onNodeWithTag(CertificatesTestTags.tile("c1")).performClick()
        compose.waitForIdle()

        coVerify { downloadCertificate(CertificateId("c1")) }
        compose.onNodeWithTag(CertificatesTestTags.RESULT_PAGE).assertIsDisplayed()
    }

    private fun certificate(id: String): Certificate = Certificate(
        id = CertificateId(id),
        description = "Autodichiarazione Iscrizione",
        type = CertificateType.Enrolment,
        solarYear = 2024,
        digitallySigned = false,
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
