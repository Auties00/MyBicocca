package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory

/**
 * Annual enrollment history and renewal state for a career.
 *
 * Enrollment history is live-first — it changes at most once per academic year and is
 * cheap to fetch, mirroring the taxes feature: every call hits Esse3 while connectivity
 * exists and throws on failure (the ViewModel translates that to a sync status), and the
 * last successful read is kept as an offline snapshot purely for display when the device
 * has no network.
 */
interface EnrollmentRepository {

    suspend fun getHistory(careerId: CareerId): EnrollmentHistory

    /**
     * The Esse3 web URL of the official annual re-enrollment flow ("iscrizione anni
     * successivi"), opened in the browser because no student REST submission path
     * exists.
     */
    fun renewalWebUrl(careerId: CareerId): String
}
