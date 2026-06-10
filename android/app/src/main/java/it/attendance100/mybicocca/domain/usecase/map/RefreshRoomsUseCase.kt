package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

/**
 * Pulls a building's room list from EasyStaff into the Room store when its detail opens in the
 * map tab; the fetch is skipped while the cached copy is still fresh. Throws on failure.
 */
class RefreshRoomsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(buildingCode: BuildingCode) = repository.refreshRooms(buildingCode)
}
