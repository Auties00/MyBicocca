package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

/**
 * Seeds the bundled campus building catalog into the Room store when the map tab starts, so
 * catalog updates shipped with the app take effect.
 */
class RefreshBuildingsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke() = repository.refreshBuildings()
}
