package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Re-syncs the Room reservation cache from the Affluences "my reservations" list. Runs when the
 * Biblioteca flow opens with a logged-in session and after booking changes; throws a
 * not-logged-in exception when there is no valid session.
 */
class RefreshLibraryReservationsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke() = repository.refreshReservations()
}
