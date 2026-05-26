package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

class RefreshRoomsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(buildingCode: BuildingCode) = repository.refreshRooms(buildingCode)
}
