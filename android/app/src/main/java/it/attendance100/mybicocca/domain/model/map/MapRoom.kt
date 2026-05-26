package it.attendance100.mybicocca.domain.model.map

data class MapRoom(
    val code: RoomCode,
    val buildingCode: BuildingCode,
    val name: String,
    val capacity: Int?,
)
