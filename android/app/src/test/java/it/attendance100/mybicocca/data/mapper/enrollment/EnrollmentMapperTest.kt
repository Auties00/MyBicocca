package it.attendance100.mybicocca.data.mapper.enrollment

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import org.junit.Test
import java.time.LocalDate

/**
 * Covers the Esse3 `IscrizioneAnnuale` -> domain mapping: the year-less guard, the id fallback
 * chain, the flag-gated part-time and suspension sub-objects, the "N"/blank exemption and
 * zero-disability suppression, the degree-class code/description preferences, and the
 * `valoreMin` -> minimum-credits coercion.
 */
class EnrollmentMapperTest {

    private fun enrollment(
        academicYearEnrollmentId: Long? = 2024L,
        enrollmentId: Long? = 500L,
        matId: Long? = 700L,
        courseYear: Int? = 2,
        fcYears: Int? = 0,
        enrollmentTypeCode: String? = "IC",
        enrollmentStatusCode: String? = "A",
        conditionFlag: Int? = 0,
        searchFlag: Int? = 0,
        ptFlag: Int? = 0,
        ptCredits: Int? = null,
        ptExtraCredits: Int? = null,
        ptBlockedFlag: Int? = null,
        suspensionFlag: Int? = 0,
        suspensionReasonCode: String? = null,
        degreeAwardFlag: Int? = 0,
        exemptionTypeCode: String? = null,
        exemptionTypeDescription: String? = null,
        disabilityPercentage: Float? = null,
        enrollmentClassCode: String? = null,
        classMurstCode: String? = null,
        classMurstDescription: String? = null,
        classUniversityDescription: String? = null,
        minimumValue: String? = null,
        enrollmentDate: String? = null,
    ) = Esse3AnnualEnrollment(
        academicYearEnrollmentId = academicYearEnrollmentId,
        enrollmentId = enrollmentId,
        matId = matId,
        courseYear = courseYear,
        fcYears = fcYears,
        enrollmentTypeCode = enrollmentTypeCode,
        enrollmentStatusCode = enrollmentStatusCode,
        conditionFlag = conditionFlag,
        searchFlag = searchFlag,
        ptFlag = ptFlag,
        ptCredits = ptCredits,
        ptExtraCredits = ptExtraCredits,
        ptBlockedFlag = ptBlockedFlag,
        suspensionFlag = suspensionFlag,
        suspensionReasonCode = suspensionReasonCode,
        degreeAwardFlag = degreeAwardFlag,
        exemptionTypeCode = exemptionTypeCode,
        exemptionTypeDescription = exemptionTypeDescription,
        disabilityPercentage = disabilityPercentage,
        enrollmentClassCode = enrollmentClassCode,
        classMurstCode = classMurstCode,
        classMurstDescription = classMurstDescription,
        classUniversityDescription = classUniversityDescription,
        minimumValue = minimumValue,
        enrollmentDate = enrollmentDate,
    )

    @Test
    fun `returns null without an academic year`() {
        assertThat(enrollment(academicYearEnrollmentId = null).toDomain()).isNull()
    }

    @Test
    fun `academic year and course year carried through`() {
        val domain = enrollment(academicYearEnrollmentId = 2024L, courseYear = 3).toDomain()!!
        assertThat(domain.academicYear).isEqualTo(2024)
        assertThat(domain.courseYear).isEqualTo(3)
    }

    @Test
    fun `course year and off-course years default to zero`() {
        val domain = enrollment(courseYear = null, fcYears = null).toDomain()!!
        assertThat(domain.courseYear).isEqualTo(0)
        assertThat(domain.outOfCourseYears).isEqualTo(0)
    }

    @Test
    fun `id prefers enrollment id`() {
        val domain = enrollment(enrollmentId = 11L, matId = 22L, academicYearEnrollmentId = 2024L)
            .toDomain()!!
        assertThat(domain.id.value).isEqualTo(11L)
    }

    @Test
    fun `id falls back to matId then academic year`() {
        assertThat(
            enrollment(enrollmentId = null, matId = 22L, academicYearEnrollmentId = 2024L)
                .toDomain()!!.id.value,
        ).isEqualTo(22L)
        assertThat(
            enrollment(enrollmentId = null, matId = null, academicYearEnrollmentId = 2024L)
                .toDomain()!!.id.value,
        ).isEqualTo(2024L)
    }

    @Test
    fun `enrollment type and status decoded from codes`() {
        val domain = enrollment(enrollmentTypeCode = "FC", enrollmentStatusCode = "S").toDomain()!!
        assertThat(domain.type).isEqualTo(EnrollmentType.OutOfCourse)
        assertThat(domain.status).isEqualTo(EnrollmentStatus.Suspended)
    }

    @Test
    fun `unknown codes fold to Unknown`() {
        val domain = enrollment(enrollmentTypeCode = "ZZ", enrollmentStatusCode = "ZZ").toDomain()!!
        assertThat(domain.type).isEqualTo(EnrollmentType.Unknown)
        assertThat(domain.status).isEqualTo(EnrollmentStatus.Unknown)
    }

    @Test
    fun `conditional reconstructed and awaiting flags map from one`() {
        val domain = enrollment(conditionFlag = 1, searchFlag = 1, degreeAwardFlag = 1).toDomain()!!
        assertThat(domain.conditional).isTrue()
        assertThat(domain.reconstructed).isTrue()
        assertThat(domain.awaitingDegree).isTrue()
    }

    @Test
    fun `part time present only when its flag is set`() {
        assertThat(enrollment(ptFlag = 0).toDomain()!!.partTime).isNull()

        val pt = enrollment(
            ptFlag = 1,
            ptCredits = 40,
            ptExtraCredits = 6,
            ptBlockedFlag = 1,
        ).toDomain()!!.partTime!!
        assertThat(pt.credits).isEqualTo(40)
        assertThat(pt.extraCredits).isEqualTo(6)
        assertThat(pt.locked).isTrue()
    }

    @Test
    fun `part time extra credits dropped when not positive`() {
        val pt = enrollment(ptFlag = 1, ptExtraCredits = 0).toDomain()!!.partTime!!
        assertThat(pt.extraCredits).isNull()
    }

    @Test
    fun `suspension present only when its flag is set`() {
        assertThat(enrollment(suspensionFlag = 0).toDomain()!!.suspension).isNull()
        val suspension = enrollment(suspensionFlag = 1, suspensionReasonCode = "MAT")
            .toDomain()!!.suspension!!
        assertThat(suspension.reasonCode).isEqualTo("MAT")
    }

    @Test
    fun `exemption suppressed for N code and blank description`() {
        assertThat(
            enrollment(exemptionTypeCode = "n", exemptionTypeDescription = "Esonero totale")
                .toDomain()!!.exemptionDescription,
        ).isNull()
        assertThat(
            enrollment(exemptionTypeCode = "TOT", exemptionTypeDescription = "   ")
                .toDomain()!!.exemptionDescription,
        ).isNull()
        assertThat(
            enrollment(exemptionTypeCode = "TOT", exemptionTypeDescription = "Esonero totale")
                .toDomain()!!.exemptionDescription,
        ).isEqualTo("Esonero totale")
    }

    @Test
    fun `disability percentage dropped when zero`() {
        assertThat(enrollment(disabilityPercentage = 0f).toDomain()!!.disabilityPercentage).isNull()
        assertThat(enrollment(disabilityPercentage = 66f).toDomain()!!.disabilityPercentage)
            .isEqualTo(66f)
    }

    @Test
    fun `degree class code prefers enrollment-specific over murst`() {
        assertThat(
            enrollment(enrollmentClassCode = "LM-18", classMurstCode = "47/S")
                .toDomain()!!.degreeClassCode,
        ).isEqualTo("LM-18")
        assertThat(
            enrollment(enrollmentClassCode = "  ", classMurstCode = "47/S")
                .toDomain()!!.degreeClassCode,
        ).isEqualTo("47/S")
    }

    @Test
    fun `degree class description prefers murst over university`() {
        assertThat(
            enrollment(classMurstDescription = "Classe MURST", classUniversityDescription = "Locale")
                .toDomain()!!.degreeClassDescription,
        ).isEqualTo("Classe MURST")
        assertThat(
            enrollment(classMurstDescription = null, classUniversityDescription = "Locale")
                .toDomain()!!.degreeClassDescription,
        ).isEqualTo("Locale")
    }

    @Test
    fun `minimum credits coerced from valoreMin string`() {
        assertThat(enrollment(minimumValue = "180.0").toDomain()!!.minimumCredits).isEqualTo(180)
        assertThat(enrollment(minimumValue = "abc").toDomain()!!.minimumCredits).isNull()
        assertThat(enrollment(minimumValue = null).toDomain()!!.minimumCredits).isNull()
    }

    @Test
    fun `dates parsed accepting both esse3 and iso shapes`() {
        assertThat(enrollment(enrollmentDate = "01/10/2024").toDomain()!!.enrollmentDate)
            .isEqualTo(LocalDate.of(2024, 10, 1))
        assertThat(enrollment(enrollmentDate = "2024-10-01").toDomain()!!.enrollmentDate)
            .isEqualTo(LocalDate.of(2024, 10, 1))
        assertThat(enrollment(enrollmentDate = null).toDomain()!!.enrollmentDate).isNull()
    }
}
