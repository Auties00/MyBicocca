package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Cancels a booking from the reservation token carried by its e-mailed Affluences cancellation
 * link, after the user confirms the deep-linked action in the Biblioteca flow; the cached
 * reservation list re-syncs afterwards.
 */
class CancelLibraryBookingByTokenUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(token: String) = repository.cancelByToken(token)
}
