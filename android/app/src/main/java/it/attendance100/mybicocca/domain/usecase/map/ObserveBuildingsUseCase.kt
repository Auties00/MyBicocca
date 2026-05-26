package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBuildingsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    operator fun invoke(): Flow<Loadable<List<MapBuilding>>> = repository.observeBuildings()
}
