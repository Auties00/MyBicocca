package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.enrollment.EnrollmentCacheDao
import it.attendance100.mybicocca.data.mapper.enrollment.toDomain
import it.attendance100.mybicocca.data.mapper.enrollment.toEntity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live Esse3 implementation of the enrollment contract: the timeline comes from the
 * student-permitted `/carriere/{stuId}/iscrizioni` list and the renewal state is derived
 * client-side, since no student REST submission endpoint exists. Each successful read is
 * written through to the offline enrollment mirror ([EnrollmentCacheDao]); the mirror is read
 * back only when a live call fails with an [IOException] (no network), with the renewal state
 * re-derived from the cached years. An offline read with no cached rows rethrows, surfacing the
 * error state rather than a misleading empty timeline.
 */
@Singleton
class EnrollmentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val enrollmentCacheDao: EnrollmentCacheDao,
) : EnrollmentRepository {

    /**
     * Fetches the annual enrollments with `stuId == CareerId.value` (mirroring the taxes
     * repository). The student-permitted endpoint returns all ~87 fields by default;
     * passing `fields=ALL` is a trap on Bicocca's Esse3 — it is a projection whitelist
     * where "ALL" matches nothing, yielding empty objects — so it is never set.
     * `order=-aaIscrId` gives most-recent-first.
     */
    override suspend fun getHistory(careerId: CareerId): EnrollmentHistory {
        requireCareer(careerId)
        return try {
            val raw = sessionManager.esse3().careers.getAnnualEnrollment(
                studentId = careerId.value,
                order = "-aaIscrId",
            )
            val years = withContext(Dispatchers.Default) {
                raw.mapNotNull { it.toDomain() }.sortedByDescending { it.academicYear }
            }
            enrollmentCacheDao.replaceYears(
                careerId.value,
                years.mapIndexed { index, year -> year.toEntity(careerId, index) },
            )
            EnrollmentHistory(years = years, renewal = deriveRenewalState(years))
        } catch (e: IOException) {
            val cached = enrollmentCacheDao.getYears(careerId.value).map { it.toDomain() }
            if (cached.isEmpty()) throw e
            EnrollmentHistory(years = cached, renewal = deriveRenewalState(cached))
        }
    }

    override fun renewalWebUrl(careerId: CareerId): String = RENEWAL_WEB_URL

    /**
     * Derives the renewal state from the timeline: a student awaiting the degree has
     * nothing to renew until graduation resolves; otherwise the state depends on whether
     * an active enrollment for the current academic year exists.
     */
    private fun deriveRenewalState(years: List<AnnualEnrollment>): RenewalState {
        val latest = years.firstOrNull() ?: return RenewalState.Renewable(currentAcademicYear())
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

    /**
     * The Italian academic year rolls over with the autumn term; enrollment for
     * a.y. N/N+1 opens in the summer/autumn of calendar year N, so from August onward
     * the "current" academic year to renew into is the calendar year itself.
     */
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

        /**
         * Official Esse3 student web flow for annual re-enrollment ("iscrizione anni
         * successivi"). Authenticated route (302 → SSO when not logged in). No student
         * REST submission endpoint exists, so this is the honest path to complete the
         * renewal.
         */
        const val RENEWAL_WEB_URL =
            "https://s3w.si.unimib.it/auth/studente/Iscrizioni/IscrizioniAnniSuccessivi.do"
    }
}
