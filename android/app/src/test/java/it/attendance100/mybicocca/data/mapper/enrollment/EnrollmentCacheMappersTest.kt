package it.attendance100.mybicocca.data.mapper.enrollment

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.enrollment.AnnualEnrollmentEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.PartTimeInfo
import it.attendance100.mybicocca.domain.model.enrollment.SuspensionInfo
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the offline enrollment mirror entity<->domain round-trip: the flag-gated part-time and
 * suspension flattening, the enum name round-trip with an Unknown fallback, and the ISO date
 * round-trip with unparseable values dropped to null.
 */
class EnrollmentCacheMappersTest {

    private val career = CareerId(99L)

    private fun domain(
        id: Long = 500L,
        type: EnrollmentType = EnrollmentType.InProgress,
        status: EnrollmentStatus = EnrollmentStatus.Active,
        partTime: PartTimeInfo? = null,
        suspension: SuspensionInfo? = null,
        degreeAwardDate: LocalDate? = null,
        enrollmentDate: LocalDate? = null,
        insertionDate: LocalDate? = null,
        modificationDate: LocalDate? = null,
    ) = AnnualEnrollment(
        id = EnrollmentId(id),
        academicYear = 2024,
        courseYear = 2,
        outOfCourseYears = 0,
        type = type,
        typeDescription = "In corso",
        status = status,
        statusReasonCode = "RC",
        conditional = false,
        reconstructed = false,
        partTime = partTime,
        suspension = suspension,
        awaitingDegree = false,
        degreeAwardDate = degreeAwardDate,
        studentTypeDescription = "Studente",
        exemptionDescription = "Esonero",
        incomeBandId = 1L,
        canteenBandId = 2L,
        meritBandId = 3L,
        meritNote = "merito",
        enrollmentNote = "nota",
        disabilityPercentage = 50f,
        disabilityTypeDescription = "tipo",
        courseDescription = "Informatica",
        courseTypeDescription = "Laurea",
        degreeClassCode = "L-31",
        degreeClassDescription = "Scienze e Tecnologie Informatiche",
        orientationDescription = "orient",
        addressDescription = "indirizzo",
        studyOrderDescription = "ordinamento",
        minimumCredits = 180,
        courseDuration = 3,
        teachingLanguage = "ITA",
        regulationCode = "NORM",
        universityDescription = "Milano-Bicocca",
        siteDescription = "Milano",
        enrollmentDate = enrollmentDate,
        insertionDate = insertionDate,
        modificationDate = modificationDate,
    )

    @Test
    fun `careerId and order recorded on the entity`() {
        val entity = domain().toEntity(career, order = 4)
        assertThat(entity.careerId).isEqualTo(career.value)
        assertThat(entity.enrollmentId).isEqualTo(500L)
        assertThat(entity.cacheOrder).isEqualTo(4)
    }

    @Test
    fun `scalar fields round-trip through the cache`() {
        val original = domain()
        val restored = original.toEntity(career, order = 0).toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `part time absence flattens then reconstructs as null`() {
        val entity = domain(partTime = null).toEntity(career, order = 0)
        assertThat(entity.hasPartTime).isFalse()
        assertThat(entity.partTimeCredits).isNull()
        assertThat(entity.partTimeLocked).isNull()
        assertThat(entity.toDomain().partTime).isNull()
    }

    @Test
    fun `part time present round-trips and locked maps via flag`() {
        val original = domain(partTime = PartTimeInfo(credits = 40, extraCredits = 6, locked = true))
        val entity = original.toEntity(career, order = 0)
        assertThat(entity.hasPartTime).isTrue()
        assertThat(entity.partTimeCredits).isEqualTo(40)
        assertThat(entity.partTimeExtraCredits).isEqualTo(6)
        assertThat(entity.partTimeLocked).isTrue()
        assertThat(entity.toDomain().partTime).isEqualTo(original.partTime)
    }

    @Test
    fun `suspension presence round-trips behind its flag`() {
        assertThat(domain(suspension = null).toEntity(career, 0).hasSuspension).isFalse()
        val original = domain(suspension = SuspensionInfo(reasonCode = "MAL"))
        val entity = original.toEntity(career, 0)
        assertThat(entity.hasSuspension).isTrue()
        assertThat(entity.suspensionReasonCode).isEqualTo("MAL")
        assertThat(entity.toDomain().suspension).isEqualTo(original.suspension)
    }

    @Test
    fun `enums round-trip by name`() {
        val original = domain(type = EnrollmentType.Repeating, status = EnrollmentStatus.Canceled)
        val entity = original.toEntity(career, 0)
        assertThat(entity.type).isEqualTo(EnrollmentType.Repeating.name)
        assertThat(entity.status).isEqualTo(EnrollmentStatus.Canceled.name)
        val restored = entity.toDomain()
        assertThat(restored.type).isEqualTo(EnrollmentType.Repeating)
        assertThat(restored.status).isEqualTo(EnrollmentStatus.Canceled)
    }

    @Test
    fun `unknown stored enum names fall back to Unknown`() {
        val entity = baseEntity().copy(type = "WAT", status = "HUH")
        val restored = entity.toDomain()
        assertThat(restored.type).isEqualTo(EnrollmentType.Unknown)
        assertThat(restored.status).isEqualTo(EnrollmentStatus.Unknown)
    }

    @Test
    fun `dates round-trip as iso and unparseable values drop to null`() {
        val original = domain(
            degreeAwardDate = LocalDate.of(2025, 7, 1),
            enrollmentDate = LocalDate.of(2024, 10, 1),
            insertionDate = LocalDate.of(2024, 9, 1),
            modificationDate = LocalDate.of(2024, 11, 1),
        )
        val entity = original.toEntity(career, 0)
        assertThat(entity.enrollmentDate).isEqualTo("2024-10-01")
        val restored = entity.toDomain()
        assertThat(restored.degreeAwardDate).isEqualTo(LocalDate.of(2025, 7, 1))
        assertThat(restored.enrollmentDate).isEqualTo(LocalDate.of(2024, 10, 1))

        val broken = baseEntity().copy(enrollmentDate = "10/10/2024", insertionDate = "")
        assertThat(broken.toDomain().enrollmentDate).isNull()
        assertThat(broken.toDomain().insertionDate).isNull()
    }

    private fun baseEntity(): AnnualEnrollmentEntity =
        domain().toEntity(career, order = 0)
}
