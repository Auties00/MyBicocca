package it.attendance100.mybicocca.data.mapper.map

import it.attendance100.mybicocca.data.local.map.BuildingCatalogDto
import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.domain.model.map.BuildingCategory

internal fun BuildingCatalogDto.toEntity(): MapBuildingEntity = MapBuildingEntity(
    code = code,
    name = name,
    latitude = latitude,
    longitude = longitude,
    category = runCatching { BuildingCategory.valueOf(category.trim().uppercase()) }
        .getOrDefault(BuildingCategory.TEACHING)
        .name,
    address = address,
    city = city,
)
