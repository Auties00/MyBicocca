package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLibraryReservationsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<List<LibraryReservation>> = repository.observeReservations()
}
