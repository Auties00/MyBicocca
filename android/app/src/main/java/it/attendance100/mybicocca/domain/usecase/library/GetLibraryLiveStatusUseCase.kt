package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Fetches a library's full live status — open state, occupancy, and hourly forecast — when its
 * detail page opens in the Biblioteca flow.
 */
class GetLibraryLiveStatusUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): LibraryLiveStatus =
        repository.getLiveStatus(libraryId)
}
