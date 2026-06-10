package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the e-mail address linked to the Affluences session, or `null` when not logged in;
 * switches the Biblioteca flow between its login prompt and the logged-in account state.
 */
class ObserveLibraryLinkedEmailUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<String?> = repository.observeLinkedEmail()
}
