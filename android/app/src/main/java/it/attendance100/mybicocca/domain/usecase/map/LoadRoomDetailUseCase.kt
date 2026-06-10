package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.repository.MapRepository
import javax.inject.Inject

/**
 * Fetches rich detail (floor, capacity, accessibility, equipment) for a room the user opened in
 * the map tab. Live EasyStaff data, never cached; returns `null` when the room has no showcase
 * card and throws on failure.
 */
class LoadRoomDetailUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(room: MapRoom): MapRoomDetail? = repository.loadRoomDetail(room)
}
