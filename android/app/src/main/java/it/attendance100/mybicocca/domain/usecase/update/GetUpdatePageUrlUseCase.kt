package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Resolves the store-aware destination for a release: its GitHub release page on sideloaded
 * builds, the Play listing on Play builds (future). The caller opens the returned URL in a
 * Custom Tab.
 */
class GetUpdatePageUrlUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    operator fun invoke(release: AppRelease): String = repository.updatePageUrl(release)
}
