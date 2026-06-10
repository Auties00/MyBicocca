package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Loads the bookable Bicocca libraries (Affluences sites) for the landing page of the Biblioteca
 * flow in the registry screen's services tab. Network-only — the catalog is never cached.
 */
class GetLibrariesUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(): List<Library> = repository.getLibraries()
}
