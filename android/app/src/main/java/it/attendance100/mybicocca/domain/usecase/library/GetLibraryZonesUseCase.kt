package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Loads a library's bookable seating zones when the user starts the booking wizard in the
 * Biblioteca flow.
 */
class GetLibraryZonesUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): List<LibraryZone> =
        repository.getZones(libraryId)
}
