package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

class LoadRoomDetailUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(room: MapRoom): MapRoomDetail? = repository.loadRoomDetail(room)
}
