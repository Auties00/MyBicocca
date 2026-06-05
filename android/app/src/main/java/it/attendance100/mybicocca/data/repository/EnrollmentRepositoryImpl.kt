package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.enrollment.toDomain
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollmentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : EnrollmentRepository {

    // stuId == CareerId.value (mirrors TaxRepositoryImpl). The STUDENT-permitted endpoint
    // returns all ~87 fields by default; passing fields=ALL is a TRAP on Bicocca's Esse3
    // (it's a projection whitelist where "ALL" matches nothing → empty objects), so we
    // never set it. order=-aaIscrId gives most-recent-first.
    override suspend fun getHistory(careerId: CareerId): EnrollmentHistory {
        requireCareer(careerId)
        val raw = sessionManager.esse3().careers.getAnnualEnrollment(
            studentId = careerId.value,
            order = "-aaIscrId",
        )
        return withContext(Dispatchers.Default) {
            val years = raw.mapNotNull { it.toDomain() }
                .sortedByDescending { it.academicYear }
            EnrollmentHistory(years = years, renewal = deriveRenewalState(years))
        }
    }

    override fun renewalWebUrl(careerId: CareerId): String = RENEWAL_WEB_URL

    private fun deriveRenewalState(years: List<AnnualEnrollment>): RenewalState {
        val latest = years.firstOrNull() ?: return RenewalState.Renewable(currentAcademicYear())
        // A student awaiting the degree (or whose last year is already enrolled and is the
        // legal final year) has nothing to renew until graduation resolves.
        if (latest.awaitingDegree) return RenewalState.NotApplicable

        val currentYear = currentAcademicYear()
        val enrolledThisYear = years.any {
            it.academicYear == currentYear && it.status == EnrollmentStatus.Active
        }
        return if (enrolledThisYear) {
            RenewalState.Enrolled(currentYear)
        } else {
            RenewalState.Renewable(currentYear)
        }
    }

    // Italian academic year rolls over with the autumn term; enrollment for a.y. N/N+1
    // opens in the summer/autumn of calendar year N, so from August onward the "current"
    // a.y. to renew into is the calendar year itself.
    private fun currentAcademicYear(): Int {
        val today = LocalDate.now()
        return if (today.monthValue >= ACADEMIC_YEAR_ROLLOVER_MONTH) today.year else today.year - 1
    }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for enrollments.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    private companion object {
        const val ACADEMIC_YEAR_ROLLOVER_MONTH = 8

        // Official Esse3 student web flow for annual re-enrollment ("iscrizione anni
        // successivi"). Authenticated route (302 → SSO when not logged in). No student REST
        // submission endpoint exists, so this is the honest path to complete the renewal.
        const val RENEWAL_WEB_URL =
            "https://s3w.si.unimib.it/auth/studente/Iscrizioni/IscrizioniAnniSuccessivi.do"
    }
}
