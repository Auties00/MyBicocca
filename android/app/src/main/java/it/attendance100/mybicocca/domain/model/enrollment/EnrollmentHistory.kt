package it.attendance100.mybicocca.domain.model.enrollment

/**
 * The full enrollment timeline of a career plus the derived renewal state, as shown by
 * the enrollments sub-screen in the registry tab.
 *
 * @property years All annual enrollments, sorted most-recent-first by the repository.
 * @property renewal Whether the student can, must not, or already did renew for the
 *   current academic year.
 */
data class EnrollmentHistory(
    val years: List<AnnualEnrollment>,
    val renewal: RenewalState,
) {
    /** The most recent annual enrollment, or null for an empty history. */
    val latest: AnnualEnrollment?
        get() = years.firstOrNull()
}
