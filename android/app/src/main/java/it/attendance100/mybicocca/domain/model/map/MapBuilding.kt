package it.attendance100.mybicocca.domain.model.map

data class MapBuilding(
    val code: BuildingCode,
    val name: String,
    val point: GeoPoint,
    val category: BuildingCategory,
    val address: String?,
    val city: String?,
)
