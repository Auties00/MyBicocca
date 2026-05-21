package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import kotlinx.coroutines.flow.Flow

interface TranscriptRepository {

    fun observeRows(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>>

    fun observeStats(careerId: CareerId): Flow<Loadable<TranscriptStats>>

    fun observeGradeRollup(careerId: CareerId): Flow<Loadable<GradeRollup>>

    suspend fun refresh(careerId: CareerId, force: Boolean = false)

    // Used by calendar/elearning to de-dup courses already passed.
    suspend fun getPassedCourseNames(careerId: CareerId): Set<String>
}
