package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory

// Annual enrollment history is intentionally NOT cached locally — it changes at most once
// per academic year and is cheap to fetch, so it mirrors TaxRepository's no-cache pattern:
// every call hits Esse3 and THROWS on failure; the ViewModel translates to SyncStatus.
interface EnrollmentRepository {

    suspend fun getHistory(careerId: CareerId): EnrollmentHistory

    // The Esse3 web URL of the official annual re-enrollment flow ("iscrizione anni
    // successivi"), opened in the browser when no student REST submission path exists.
    fun renewalWebUrl(careerId: CareerId): String
}
