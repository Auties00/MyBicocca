package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

class RefreshBuildingsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke() = repository.refreshBuildings()
}
