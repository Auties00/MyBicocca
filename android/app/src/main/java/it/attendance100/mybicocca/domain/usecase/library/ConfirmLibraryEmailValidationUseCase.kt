package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Polls a pending e-mail validation from the Biblioteca login page. Returns `true` once the
 * user has opened the e-mailed link — storing the session and syncing reservations — and
 * `false` while still pending.
 */
class ConfirmLibraryEmailValidationUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(email: String, requestUuid: String): Boolean =
        repository.confirmEmailValidation(email, requestUuid)
}
