package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the campus buildings from the Room store for the map tab's pins and building list. */
class ObserveBuildingsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    operator fun invoke(): Flow<Loadable<List<MapBuilding>>> = repository.observeBuildings()
}
