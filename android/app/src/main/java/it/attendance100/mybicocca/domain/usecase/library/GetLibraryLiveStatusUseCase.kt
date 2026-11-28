package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Fetches a library's full live status — open state, occupancy, and forecast ranges — and
 * best-effort upserts today's crowding history entry.
 */
class GetLibraryLiveStatusUseCase @Inject constructor(
    private val repository: LibraryRepository,
    private val recordCrowding: RecordLibraryCrowdingUseCase,
) {
    suspend operator fun invoke(libraryId: String): LibraryLiveStatus {
        val status = repository.getLiveStatus(libraryId)
        try {
            recordCrowding(libraryId, status)
        } catch (cause: CancellationException) {
            throw cause
        } catch (_: Throwable) {
            // Crowding cache failures must not hide live status from the UI.
        }
        return status
    }
}
