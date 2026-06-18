package it.attendance100.mybicocca.domain.usecase.update

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import javax.inject.Inject

/**
 * Loads the published releases (newest first) shown on the What's New page of the About modal.
 * Hits GitHub directly; throws on failure.
 */
class GetReleasesUseCase @Inject constructor(
    private val repository: UpdateRepository,
) {
    suspend operator fun invoke(): List<AppRelease> = repository.releases()
}
