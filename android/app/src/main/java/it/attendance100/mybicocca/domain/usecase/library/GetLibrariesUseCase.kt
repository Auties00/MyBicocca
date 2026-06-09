package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class GetLibrariesUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(): List<Library> = repository.getLibraries()
}
