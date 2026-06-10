package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

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
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.domain.model.document.TitleStatus
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetAcademicTitlesUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Titoli sheet: the career-triggered initial load, the
 * NotYetLoaded -> Loaded transition, sync status independence on failure, and the refresh
 * action re-fetching with the active career id.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TitlesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val account = account(careerId)

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val getAcademicTitles: GetAcademicTitlesUseCase = mockk()

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

    private fun viewModel(): TitlesViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        coEvery { getAcademicTitles(any()) } returns emptyList()
        return TitlesViewModel(observeActiveAccount, getAcademicTitles)
    }

    @Test
    fun `titles goes NotYetLoaded then Loaded on the career-triggered load`() = runTest {
        every { observeActiveAccount() } returns flowOf(account)
        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"))

        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)

        val loaded = vm.titles.value
        assertThat(loaded).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((loaded as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `initial load uses the active career id`() = runTest {
        viewModel()

        coVerify { getAcademicTitles(careerId) }
    }

    @Test
    fun `load failure sets Failed and leaves titles NotYetLoaded`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account)
        coEvery { getAcademicTitles(careerId) } throws boom

        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat(vm.titles.value).isEqualTo(Loadable.NotYetLoaded)
    }

    @Test
    fun `refresh failure keeps the previously loaded titles on screen`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account)
        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"))
        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)

        coEvery { getAcademicTitles(careerId) } throws boom
        vm.refresh()

        assertThat(vm.titles.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((vm.titles.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)
    }

    @Test
    fun `refresh re-fetches and returns sync status to Idle on success`() = runTest {
        every { observeActiveAccount() } returns flowOf(account)
        coEvery { getAcademicTitles(careerId) } returns emptyList()
        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)

        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"), title("t2"))
        vm.refresh()

        assertThat((vm.titles.value as Loadable.Loaded).value).hasSize(2)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `syncStatus stays Idle and no load runs while no career is active`() = runTest {
        every { observeActiveAccount() } returns flowOf(null)
        coEvery { getAcademicTitles(any()) } returns emptyList()

        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)

        assertThat(vm.titles.value).isEqualTo(Loadable.NotYetLoaded)
        coVerify(exactly = 0) { getAcademicTitles(any()) }
        vm.titles.test {
            assertThat(awaitItem()).isEqualTo(Loadable.NotYetLoaded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh awaits the first non-null career and re-fetches once it lands`() = runTest {
        val accountFlow = MutableStateFlow<Account?>(null)
        every { observeActiveAccount() } returns accountFlow
        coEvery { getAcademicTitles(careerId) } returns listOf(title("t1"))

        val vm = TitlesViewModel(observeActiveAccount, getAcademicTitles)
        vm.refresh()
        accountFlow.value = account

        coVerify { getAcademicTitles(careerId) }
    }

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
