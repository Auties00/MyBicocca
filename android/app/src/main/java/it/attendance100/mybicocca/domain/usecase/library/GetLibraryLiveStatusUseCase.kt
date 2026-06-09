package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryLiveStatusUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): LibraryLiveStatus =
        repository.getLiveStatus(libraryId)
}
