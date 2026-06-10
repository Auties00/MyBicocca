package it.attendance100.mybicocca.domain.model.map

/**
 * A room of a campus building, listed in the map tab's building detail. The room list comes from
 * EasyStaff, with the floor enriched from EasyStaff's room showcase (vetrina_aule census); rooms
 * are cached in Room per building, so the campus-wide set grows as buildings are visited.
 *
 * @property code Room code, unique within the building.
 * @property buildingCode Code of the owning building.
 * @property name Display name, typically the code plus a qualifier.
 * @property capacity Seating capacity; null when unknown.
 * @property floor Floor level, 0 being the ground floor; null when the showcase has no card for
 * the room.
 */
data class MapRoom(
    val code: RoomCode,
    val buildingCode: BuildingCode,
    val name: String,
    val capacity: Int?,
    val floor: Int?,
)
