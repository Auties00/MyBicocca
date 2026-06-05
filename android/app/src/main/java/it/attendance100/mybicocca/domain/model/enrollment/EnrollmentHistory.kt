package it.attendance100.mybicocca.domain.model.enrollment

// The full enrollment timeline of a career plus the derived renewal state. Years are
// sorted most-recent-first by the repository.
data class EnrollmentHistory(
    val years: List<AnnualEnrollment>,
    val renewal: RenewalState,
) {
    val latest: AnnualEnrollment?
        get() = years.firstOrNull()
}
