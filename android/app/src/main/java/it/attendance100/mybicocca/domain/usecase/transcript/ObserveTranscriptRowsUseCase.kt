package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the career's libretto rows from the Room cache (single source of truth) for
 * the profile screen's exam list; pairing with the refresh use case keeps the cache in
 * sync with Esse3.
 */
class ObserveTranscriptRowsUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    operator fun invoke(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>> =
        repository.observeRows(careerId)
}
