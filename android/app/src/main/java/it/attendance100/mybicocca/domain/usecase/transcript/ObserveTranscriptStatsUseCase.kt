package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the career-level libretto aggregates (credits, averages, exam counts) from
 * the Room cache for the profile screen's progress and averages cards.
 */
class ObserveTranscriptStatsUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    operator fun invoke(careerId: CareerId): Flow<Loadable<TranscriptStats>> =
        repository.observeStats(careerId)
}
