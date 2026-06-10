package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import kotlinx.coroutines.flow.Flow

/**
 * The student's libretto (transcript): rows, career-level stats and grade aggregates.
 *
 * The summary data (rows, stats, rollup) is Room-backed: the observe methods stream from
 * the local cache as single source of truth and [refresh] reconciles it with Esse3,
 * throwing on failure so the ViewModel can translate to a sync status. Per-activity
 * detail is the exception — it is NOT cached and is fetched live each time a course is
 * opened.
 */
interface TranscriptRepository {

    fun observeRows(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>>

    fun observeStats(careerId: CareerId): Flow<Loadable<TranscriptStats>>

    fun observeGradeRollup(careerId: CareerId): Flow<Loadable<GradeRollup>>

    /**
     * Reconciles the Room cache with Esse3. Without force the refresh is skipped while
     * the cache is within its staleness TTL; throws on failure.
     */
    suspend fun refresh(careerId: CareerId, force: Boolean = false)

    /**
     * Per-activity detail (attempt history + propedeuticità). Not cached — fetched live
     * when a course is opened. Throws when the primary fetch (prove) fails; the
     * prerequisite check is best-effort and resolves to Unknown on failure.
     * `alreadyPassed` skips the prerequisite call entirely, since Esse3 responds 422 on
     * passed activities.
     */
    suspend fun getCourseDetail(
        careerId: CareerId,
        activityChoiceId: Long,
        alreadyPassed: Boolean,
    ): CourseDetail

    /**
     * Propedeuticità check for a single not-yet-passed activity. Returns Unknown on any
     * failure (including the 422 returned for passed activities), so callers never
     * surface a misleading warning. Cheap enough to fan out over pending rows.
     */
    suspend fun getPrerequisiteStatus(careerId: CareerId, activityChoiceId: Long): PrerequisiteStatus

    /**
     * Normalized names of the activities already passed, used by calendar/elearning to
     * de-dup courses the student has completed.
     */
    suspend fun getPassedCourseNames(careerId: CareerId): Set<String>
}
