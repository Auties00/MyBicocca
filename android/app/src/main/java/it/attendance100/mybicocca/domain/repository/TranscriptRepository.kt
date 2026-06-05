package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import kotlinx.coroutines.flow.Flow

interface TranscriptRepository {

    fun observeRows(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>>

    fun observeStats(careerId: CareerId): Flow<Loadable<TranscriptStats>>

    fun observeGradeRollup(careerId: CareerId): Flow<Loadable<GradeRollup>>

    suspend fun refresh(careerId: CareerId, force: Boolean = false)

    // Per-activity detail (attempt history + propedeuticità). Not cached — fetched live
    // when a course is opened. Throws on the primary fetch (prove); the prereq check is
    // best-effort and resolves to Unknown on failure. `alreadyPassed` skips the /prop call
    // entirely, since Esse3 422s on passed activities.
    suspend fun getCourseDetail(
        careerId: CareerId,
        activityChoiceId: Long,
        alreadyPassed: Boolean,
    ): CourseDetail

    // Propedeuticità check for a single not-yet-passed activity (the /prop endpoint).
    // Returns Unknown on any failure (incl. the 422 returned for passed activities), so
    // callers never surface a misleading warning. Cheap enough to fan out over pending rows.
    suspend fun getPrerequisiteStatus(careerId: CareerId, activityChoiceId: Long): PrerequisiteStatus

    // Used by calendar/elearning to de-dup courses already passed.
    suspend fun getPassedCourseNames(careerId: CareerId): Set<String>
}
