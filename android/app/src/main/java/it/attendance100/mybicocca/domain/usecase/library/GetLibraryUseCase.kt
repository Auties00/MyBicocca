package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/** Fetches full site metadata (address, phone, e-mail, website, coordinates) for one library. */
class GetLibraryUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): Library = repository.getLibrary(libraryId)
}
