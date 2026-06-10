package it.attendance100.mybicocca.domain.model.tax

/**
 * ISEE declaration parameters for one enrollment year, derived from the Esse3
 * enrollments-for-taxes (parametri-iscrizioni-per-tasse) endpoint, which yields one entry per
 * enrollment year. Shown in the ISEE section of the registry "Tasse" sub-screen.
 *
 * @property academicYearEnrollmentId Esse3 id of the yearly enrollment the parameters refer to.
 * @property courseDescription Course of study the enrollment belongs to.
 * @property isee Declared ISEE value in euro; null when no declaration is on file for that year.
 * @property iseeThreshold ISEE threshold applied to compute the contribution band.
 * @property exemptionDescription Description of the exemption or contribution band applied.
 */
data class IseeDeclaration(
    val academicYearEnrollmentId: Long?,
    val courseDescription: String?,
    val isee: Double?,
    val iseeThreshold: Double?,
    val exemptionDescription: String?,
)
