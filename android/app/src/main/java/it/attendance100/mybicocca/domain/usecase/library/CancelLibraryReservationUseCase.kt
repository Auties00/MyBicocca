package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Cancels a reservation the user picked from the Biblioteca list, using the reservation's own
 * cancellation token, then re-syncs the cached list.
 */
class CancelLibraryReservationUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(reservation: LibraryReservation) =
        repository.cancelReservation(reservation)
}
