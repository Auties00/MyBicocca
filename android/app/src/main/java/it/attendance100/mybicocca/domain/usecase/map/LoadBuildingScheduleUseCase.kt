package it.attendance100.mybicocca.domain.usecase.map

import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.domain.repository.MapRepository
import java.time.LocalDate
import javax.inject.Inject

class LoadBuildingScheduleUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(buildingCode: BuildingCode, date: LocalDate): List<RoomScheduleEntry> =
        repository.loadDaySchedule(buildingCode, date)
}
