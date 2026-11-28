package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/** Persists the Affluences site id of the library the student opened or booked. */
class RecordLastUsedLibraryUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String) = repository.recordLastUsedLibrary(libraryId)
}
