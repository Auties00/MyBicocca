package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import javax.inject.Inject

/**
 * Refreshes the Room transcript cache from Esse3, triggered on profile screen entry and
 * by pull-to-refresh. Without force the refresh is skipped while the cache is within
 * its staleness TTL. Throws on failure; the ViewModel translates that to a sync status.
 */
class RefreshTranscriptUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    suspend operator fun invoke(careerId: CareerId, force: Boolean = false) =
        repository.refresh(careerId, force)
}
