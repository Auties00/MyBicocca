package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.library.BookLibrarySeatUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryBookingByTokenUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryReservationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConfirmLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConsumeLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetAvailableSeatsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibrariesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryAgreementsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryBookingConstraintsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryLiveStatusUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryWeekHoursUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryZonesUseCase
import it.attendance100.mybicocca.domain.usecase.library.LogoutLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryLinkedEmailUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObservePendingLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.RefreshLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RequestLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.VerifyLibraryPresenceUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI coverage for the Biblioteca sheet body: the home page shows the verify-email prompt before the
 * institutional email is linked, tapping the footer leaves the home page (toward login), and the
 * bookable libraries list renders its loaded cards once a logged-in session opens the directory. The
 * screen is driven by a real [LibraryViewModel] over MockK-faked use cases (reusing the Wave 1
 * construction), anchored on [LibraryTestTags], and wrapped in the shared test environment (the
 * app-wide CompositionLocals plus the production theme) via [setBicoccaContent].
 *
 * The home page pins its "Verifica"/"Prenota" footer below a fixed 400dp verify/empty surface so the
 * sheet cannot stretch to full height; the tall-screen [Config.qualifiers] gives the headless window
 * enough height to lay out the whole sheet body so that footer is on-screen and hittable rather than
 * clipped below the fold (matching the sibling Appuntamenti page test).
 */
@RunWith(AndroidJUnit4::class)
class LibraryPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val getLibraries: GetLibrariesUseCase = mockk()
    private val getLiveStatus: GetLibraryLiveStatusUseCase = mockk(relaxed = true)
    private val getWeekHours: GetLibraryWeekHoursUseCase = mockk(relaxed = true)
    private val getZones: GetLibraryZonesUseCase = mockk(relaxed = true)
    private val getAgreements: GetLibraryAgreementsUseCase = mockk(relaxed = true)
    private val getConstraints: GetLibraryBookingConstraintsUseCase = mockk(relaxed = true)
    private val getAvailableSeats: GetAvailableSeatsUseCase = mockk(relaxed = true)
    private val bookSeat: BookLibrarySeatUseCase = mockk(relaxed = true)
    private val observeReservations: ObserveLibraryReservationsUseCase = mockk()
    private val refreshReservations: RefreshLibraryReservationsUseCase = mockk(relaxed = true)
    private val cancelReservation: CancelLibraryReservationUseCase = mockk(relaxed = true)
    private val verifyPresenceUseCase: VerifyLibraryPresenceUseCase = mockk()
    private val observeLinkedEmail: ObserveLibraryLinkedEmailUseCase = mockk()
    private val requestEmailValidation: RequestLibraryEmailValidationUseCase = mockk()
    private val confirmEmailValidation: ConfirmLibraryEmailValidationUseCase = mockk()
    private val logout: LogoutLibraryUseCase = mockk(relaxed = true)
    private val cancelBookingByToken: CancelLibraryBookingByTokenUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observePendingLibraryAction: ObservePendingLibraryActionUseCase = mockk()
    private val consumeLibraryAction: ConsumeLibraryActionUseCase = mockk(relaxed = true)

    private val reservationsFlow = MutableStateFlow<List<LibraryReservation>>(emptyList())

    private fun library() = Library(
        id = "lib-1",
        slug = "centrale",
        name = "Biblioteca Centrale",
        secondaryName = null,
        address = null,
        latitude = null,
        longitude = null,
        pictureUrl = null,
        phone = null,
        email = null,
        websiteUrl = null,
        bookable = true,
        liveStatus = null,
    )

    private fun viewModel(linkedEmail: String? = null): LibraryViewModel {
        coEvery { getLibraries() } returns listOf(library())
        every { observeReservations() } returns reservationsFlow
        coEvery { refreshReservations() } returns Unit
        every { observeLinkedEmail() } returns flowOf(linkedEmail)
        every { observeActiveAccount() } returns flowOf(null)
        every { observePendingLibraryAction() } returns MutableStateFlow(null)
        return LibraryViewModel(
            getLibraries, getLiveStatus, getWeekHours, getZones, getAgreements, getConstraints,
            getAvailableSeats, bookSeat, observeReservations, refreshReservations, cancelReservation,
            verifyPresenceUseCase, observeLinkedEmail, requestEmailValidation, confirmEmailValidation,
            logout, cancelBookingByToken, observeActiveAccount, observePendingLibraryAction,
            consumeLibraryAction,
        )
    }

    private fun setScreen(vm: LibraryViewModel) {
        compose.setBicoccaContent {
            LibraryPage(viewModel = vm)
        }
    }

    @Test
    fun the_home_page_shows_the_verify_email_prompt_when_not_logged_in() {
        setScreen(viewModel())

        compose.onNodeWithTag(LibraryTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_VERIFY_PROMPT).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_FOOTER).assertIsDisplayed()
    }

    @Test
    fun tapping_the_footer_leaves_the_home_page() {
        setScreen(viewModel())

        compose.onNodeWithTag(LibraryTestTags.HOME_FOOTER).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.HOME_PAGE).assertDoesNotExist()
    }

    @Test
    fun opening_the_directory_renders_the_bookable_library_cards() {
        val vm = viewModel(linkedEmail = "mario.rossi@campus.unimib.it")
        vm.openLibraries()
        setScreen(vm)

        compose.onNodeWithTag(LibraryTestTags.library("lib-1")).assertIsDisplayed()
    }
}
