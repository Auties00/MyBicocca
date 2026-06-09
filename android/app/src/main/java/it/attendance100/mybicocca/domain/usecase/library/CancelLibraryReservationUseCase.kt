package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class CancelLibraryReservationUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(reservation: LibraryReservation) =
        repository.cancelReservation(reservation)
}
