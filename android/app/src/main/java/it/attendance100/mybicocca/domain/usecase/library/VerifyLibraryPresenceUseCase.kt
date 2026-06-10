package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Validates on-site presence for a booked seat with the code the user scanned or typed in the
 * Biblioteca flow. Returns `false` for an invalid code; a successful validation re-syncs the
 * reservation list.
 */
class VerifyLibraryPresenceUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(code: String): Boolean = repository.verifyPresence(code)
}
