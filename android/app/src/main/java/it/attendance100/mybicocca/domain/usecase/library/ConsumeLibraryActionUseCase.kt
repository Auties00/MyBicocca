package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import javax.inject.Inject

/** Clears the pending library action once the Biblioteca flow has taken it over. */
class ConsumeLibraryActionUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke() = repository.consumeLibraryAction()
}
