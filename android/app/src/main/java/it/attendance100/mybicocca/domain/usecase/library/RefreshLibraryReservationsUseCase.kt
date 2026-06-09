package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class RefreshLibraryReservationsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke() = repository.refreshReservations()
}
