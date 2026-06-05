package it.attendance100.mybicocca.domain.model.internship

import it.attendance100.mybicocca.domain.model.career.CareerId

// Header-level view of a domanda di tirocinio, enough to render the list.
data class InternshipApplication(
    val id: InternshipApplicationId,
    val careerId: CareerId,
    val academicYear: Int?,
    val academicYearLabel: String?,
    val typeDescription: String?,
    val state: InternshipApplicationState,
    val stateDescription: String?,
    val companyName: String?,
    val opportunityTitle: String?,
    val conventionDescription: String?,
    val cfuRecognitionEnabled: Boolean,
)
