package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.enrollment.AnnualEnrollmentEntity
import it.attendance100.mybicocca.data.local.enrollment.EnrollmentCacheDao
import it.attendance100.mybicocca.data.mapper.enrollment.toEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/**
 * Live-first triad plus renewal-state derivation for the Esse3 enrollment repository.
 * `getHistory` maps and caches on success, falls back to the offline mirror on
 * [IOException], and rethrows when the mirror is empty; the renewal state is re-derived
 * from whichever year list it ends up with.
 */
class EnrollmentRepositoryImplTest {

    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val enrollmentCacheDao: EnrollmentCacheDao = mockk(relaxed = true)
    private val esse3Api: Esse3Api = mockk(relaxed = true)

    private val careerId = RepositoryTestFixtures.careerId

    private lateinit var repository: EnrollmentRepositoryImpl

    /** Mirrors `EnrollmentRepositoryImpl.currentAcademicYear` (rollover in August). */
    private val currentAcademicYear: Int =
        LocalDate.now().let { if (it.monthValue >= 8) it.year else it.year - 1 }

    @Before
    fun setUp() {
        every { sessionManager.activeAccount } returns MutableStateFlow(RepositoryTestFixtures.account())
        coEvery { sessionManager.esse3() } returns esse3Api
        repository = EnrollmentRepositoryImpl(sessionManager, enrollmentCacheDao)
    }

    @Test
    fun `getHistory success maps domain sorts most-recent-first and writes through`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } returns
            listOf(enrollmentDto(year = 2022L), enrollmentDto(year = 2024L))

        val history = repository.getHistory(careerId)

        assertThat(history.years.map { it.academicYear }).containsExactly(2024, 2022).inOrder()
        coVerify(exactly = 1) { enrollmentCacheDao.replaceYears(careerId.value, any()) }
    }

    @Test
    fun `getHistory offline with cache returns the mirrored timeline`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } throws
            IOException("offline")
        coEvery { enrollmentCacheDao.getYears(careerId.value) } returns
            listOf(cachedEntity(year = currentAcademicYear, status = EnrollmentStatus.Active))

        val history = repository.getHistory(careerId)

        assertThat(history.years).hasSize(1)
        assertThat(history.years.single().academicYear).isEqualTo(currentAcademicYear)
        coVerify(exactly = 0) { enrollmentCacheDao.replaceYears(any(), any()) }
    }

    @Test
    fun `getHistory offline with empty cache rethrows`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } throws
            IOException("offline")
        coEvery { enrollmentCacheDao.getYears(careerId.value) } returns emptyList()

        val thrown = runCatching { repository.getHistory(careerId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `renewal is Renewable when no years exist`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } returns
            emptyList()

        val history = repository.getHistory(careerId)

        assertThat(history.renewal).isEqualTo(RenewalState.Renewable(currentAcademicYear))
    }

    @Test
    fun `renewal is NotApplicable when the latest year awaits the degree`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } returns
            listOf(enrollmentDto(year = currentAcademicYear.toLong(), degreeAwardFlag = 1))

        val history = repository.getHistory(careerId)

        assertThat(history.renewal).isEqualTo(RenewalState.NotApplicable)
    }

    @Test
    fun `renewal is Enrolled when an active enrollment exists for the current year`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } returns
            listOf(enrollmentDto(year = currentAcademicYear.toLong(), statusCode = "A"))

        val history = repository.getHistory(careerId)

        assertThat(history.renewal).isEqualTo(RenewalState.Enrolled(currentAcademicYear))
    }

    @Test
    fun `renewal is Renewable when the current year is not yet enrolled`() = runTest {
        coEvery { esse3Api.careers.getAnnualEnrollment(any(), any(), any(), any(), any(), any(), any()) } returns
            listOf(enrollmentDto(year = (currentAcademicYear - 1).toLong(), statusCode = "A"))

        val history = repository.getHistory(careerId)

        assertThat(history.renewal).isEqualTo(RenewalState.Renewable(currentAcademicYear))
    }

    @Test
    fun `renewalWebUrl points at the official Esse3 web flow`() {
        assertThat(repository.renewalWebUrl(careerId))
            .isEqualTo("https://s3w.si.unimib.it/auth/studente/Iscrizioni/IscrizioniAnniSuccessivi.do")
    }

    private fun enrollmentDto(
        year: Long,
        statusCode: String? = "A",
        degreeAwardFlag: Int? = null,
    ) = Esse3AnnualEnrollment(
        academicYearEnrollmentId = year,
        enrollmentStatusCode = statusCode,
        degreeAwardFlag = degreeAwardFlag,
    )

    private fun cachedEntity(year: Int, status: EnrollmentStatus): AnnualEnrollmentEntity =
        AnnualEnrollment(
            id = EnrollmentId(year.toLong()),
            academicYear = year,
            courseYear = 1,
            outOfCourseYears = 0,
            type = EnrollmentType.Unknown,
            typeDescription = null,
            status = status,
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
            courseDescription = null,
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
        ).toEntity(careerId, 0)
}
