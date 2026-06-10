package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Logs out of the Affluences account from the Biblioteca account section, clearing the stored
 * session and the cached reservation list.
 */
class LogoutLibraryUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke() = repository.logout()
}
