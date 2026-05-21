package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTranscriptStatsUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    operator fun invoke(careerId: CareerId): Flow<Loadable<TranscriptStats>> =
        repository.observeStats(careerId)
}
