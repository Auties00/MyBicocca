package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import javax.inject.Inject

/**
 * Hands a parsed Affluences reservation link to the Biblioteca flow. Invoked by the activity
 * when an incoming intent carries an affluences.com cancellation link.
 */
class SubmitLibraryActionUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(action: LibraryDeepLinkAction) = repository.submitLibraryAction(action)
}
