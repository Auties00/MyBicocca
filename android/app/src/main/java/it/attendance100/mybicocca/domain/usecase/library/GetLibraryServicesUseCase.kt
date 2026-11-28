package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/** Fetches the facilities and services list for a library's detail page. */
class GetLibraryServicesUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): List<LibraryService> =
        repository.getLibraryServices(libraryId)
}
