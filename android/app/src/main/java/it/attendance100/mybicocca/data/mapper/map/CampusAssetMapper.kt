package it.attendance100.mybicocca.data.mapper.map

import it.attendance100.mybicocca.data.local.campus.CampusBuildingDto
import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.domain.model.map.BuildingCategory

internal fun CampusBuildingDto.toEntity(): MapBuildingEntity = MapBuildingEntity(
    code = code,
    name = name,
    latitude = latitude,
    longitude = longitude,
    category = category.toBuildingCategory().name,
    address = address,
)

private fun String.toBuildingCategory(): BuildingCategory =
    runCatching { BuildingCategory.valueOf(trim().uppercase()) }.getOrDefault(BuildingCategory.OTHER)
