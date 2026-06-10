package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import it.attendance100.mybicocca.domain.repository.DeepLinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the action parsed from an externally-opened Affluences reservation link, or `null`
 * when there is none. The Biblioteca flow collects it, consumes it, and runs it server-side.
 */
class ObservePendingLibraryActionUseCase @Inject constructor(
    private val repository: DeepLinkRepository,
) {
    operator fun invoke(): Flow<LibraryDeepLinkAction?> = repository.observePendingLibraryAction()
}
