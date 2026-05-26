package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRoomsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    operator fun invoke(buildingCode: BuildingCode): Flow<Loadable<List<MapRoom>>> =
        repository.observeRooms(buildingCode)
}
