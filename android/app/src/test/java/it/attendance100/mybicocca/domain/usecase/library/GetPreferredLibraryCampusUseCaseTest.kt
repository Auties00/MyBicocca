package it.attendance100.mybicocca.domain.usecase.library

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.library.LibraryCampus
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.repository.LastKnownLocationSource
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Priority-order coverage for [GetPreferredLibraryCampusUseCase]:
 * last-used library -> nearby device location within 5 km -> selected career programme -> null.
 */
class GetPreferredLibraryCampusUseCaseTest {

    private val libraryRepository: LibraryRepository = mockk()
    private val locationSource: LastKnownLocationSource = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private lateinit var useCase: GetPreferredLibraryCampusUseCase

    @Before
    fun setUp() {
        coEvery { libraryRepository.lastUsedLibraryId() } returns null
        every { locationSource.lastKnownLatLong() } returns null
        every { observeActiveAccount() } returns flowOf(null)
        useCase = GetPreferredLibraryCampusUseCase(libraryRepository, locationSource, observeActiveAccount)
    }

    @Test
    fun `last opened or booked library takes precedence over location and career`() = runTest {
        coEvery { libraryRepository.lastUsedLibraryId() } returns LibraryCatalog.MEDICINA_ID
        every { locationSource.lastKnownLatLong() } returns (LibraryCatalog.Centrale.latitude to LibraryCatalog.Centrale.longitude)
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("INFORMATICA"))

        assertThat(useCase()).isEqualTo(LibraryCampus.Monza)
    }

    @Test
    fun `unknown last-used library id falls through to device location`() = runTest {
        coEvery { libraryRepository.lastUsedLibraryId() } returns "unknown-site"
        every { locationSource.lastKnownLatLong() } returns (LibraryCatalog.Medicina.latitude to LibraryCatalog.Medicina.longitude)
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("INFORMATICA"))

        assertThat(useCase()).isEqualTo(LibraryCampus.Monza)
    }

    @Test
    fun `device location within 5 km takes precedence over career programme`() = runTest {
        every { locationSource.lastKnownLatLong() } returns (45.5200 to 9.2140)
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("MEDICINA E CHIRURGIA"))

        assertThat(useCase()).isEqualTo(LibraryCampus.Milano)
    }

    @Test
    fun `device location further than 5 km falls through to active career`() = runTest {
        every { locationSource.lastKnownLatLong() } returns (45.7000 to 9.6700)
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("FISIOTERAPIA (ABILITANTE)"))

        assertThat(useCase()).isEqualTo(LibraryCampus.Monza)
    }

    @Test
    fun `selected career description resolves Monza vs Milano and uses selectedCareerId`() = runTest {
        val account = accountWithCareers(
            careers = listOf(
                career(1L, "INFORMATICA"),
                career(2L, "BIOTECNOLOGIE MEDICHE"),
            ),
            selectedId = 2L,
        )
        every { observeActiveAccount() } returns flowOf(account)

        assertThat(useCase()).isEqualTo(LibraryCampus.Monza)
    }

    @Test
    fun `biotecnologie alone resolves to Milano`() = runTest {
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("BIOTECNOLOGIE"))

        assertThat(useCase()).isEqualTo(LibraryCampus.Milano)
    }

    @Test
    fun `blank career description or missing account returns null`() = runTest {
        every { observeActiveAccount() } returns flowOf(accountWithProgramme("   "))
        assertThat(useCase()).isNull()

        every { observeActiveAccount() } returns flowOf(null)
        assertThat(useCase()).isNull()
    }

    private fun accountWithProgramme(description: String): Account =
        accountWithCareers(listOf(career(1L, description)), selectedId = 1L)

    private fun career(id: Long, description: String) = Career(
        id = CareerId(id),
        enrollmentTraitId = 1L,
        programId = 2L,
        easyStaffProgramCode = "E01",
        academicYearEnrollmentId = 3L,
        studentNumber = "900001",
        description = description,
        academicYear = 2025,
        status = CareerStatus.ACTIVE,
    )

    private fun accountWithCareers(careers: List<Career>, selectedId: Long) = Account(
        id = AccountId("acc-1"),
        username = "student@campus.unimib.it",
        displayName = "Student",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 1L,
            fiscalCode = null,
            careers = careers,
            selectedCareerId = CareerId(selectedId),
        ),
        learning = LearningIdentity(
            lmsUserId = 1,
            lmsUsername = "student@campus.unimib.it",
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
