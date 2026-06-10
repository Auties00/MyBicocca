package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Starts the e-mail link login of the Biblioteca flow: asks Affluences to send a validation
 * link to the address and returns the request id the login page polls for completion.
 */
class RequestLibraryEmailValidationUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(email: String): String = repository.requestEmailValidation(email)
}
