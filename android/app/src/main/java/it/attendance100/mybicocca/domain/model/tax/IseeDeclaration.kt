package it.attendance100.mybicocca.domain.model.tax

// Derived from Esse3 parametri-iscrizioni-per-tasse (one per enrollment year).
data class IseeDeclaration(
    val academicYearEnrollmentId: Long?,
    val courseDescription: String?,
    val isee: Double?,
    val iseeThreshold: Double?,
    val exemptionDescription: String?,
)
