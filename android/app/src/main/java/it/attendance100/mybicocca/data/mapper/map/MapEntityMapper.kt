package it.attendance100.mybicocca.data.mapper.map

import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.data.local.map.MapRoomEntity
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.GeoPoint
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.RoomCode

internal fun MapBuildingEntity.toDomain(): MapBuilding = MapBuilding(
    code = BuildingCode(code),
    name = name,
    point = GeoPoint(latitude, longitude),
    category = runCatching { BuildingCategory.valueOf(category) }.getOrDefault(BuildingCategory.OTHER),
    address = address,
    city = city,
)

internal fun MapRoomEntity.toDomain(): MapRoom = MapRoom(
    code = RoomCode(code),
    buildingCode = BuildingCode(buildingCode),
    name = name,
    capacity = capacity,
    floor = floor,
)

internal fun MapRoom.toEntity(): MapRoomEntity = MapRoomEntity(
    code = code.value,
    buildingCode = buildingCode.value,
    name = name,
    capacity = capacity,
    floor = floor,
)
