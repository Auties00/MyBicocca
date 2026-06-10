package it.attendance100.mybicocca.data.mapper.map

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.data.local.map.MapRoomEntity
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.GeoPoint
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.RoomCode
import org.junit.Test

/**
 * Covers the building/room entity -> domain (and room round-trip) mapping: value classes wrap
 * the raw strings, the geo point is assembled from the two columns, and an unrecognized stored
 * building category degrades to OTHER (distinct from the catalog mapper's TEACHING default).
 */
class MapEntityMapperTest {

    @Test
    fun `building entity maps to domain wrapping the code and point`() {
        val entity = MapBuildingEntity(
            code = "U06",
            name = "Edificio U6",
            latitude = 45.51,
            longitude = 9.21,
            category = "LIBRARY",
            address = "Via",
            city = "Milano",
        )
        val building = entity.toDomain()
        assertThat(building.code).isEqualTo(BuildingCode("U06"))
        assertThat(building.point).isEqualTo(GeoPoint(45.51, 9.21))
        assertThat(building.category).isEqualTo(BuildingCategory.LIBRARY)
        assertThat(building.address).isEqualTo("Via")
        assertThat(building.city).isEqualTo("Milano")
    }

    @Test
    fun `unrecognized stored building category degrades to OTHER`() {
        val entity = MapBuildingEntity(
            code = "U06",
            name = "Edificio U6",
            latitude = 0.0,
            longitude = 0.0,
            category = "teaching",
            address = null,
            city = null,
        )
        assertThat(entity.toDomain().category).isEqualTo(BuildingCategory.OTHER)
    }

    @Test
    fun `room round-trips through the entity preserving nullable fields`() {
        val room = MapRoom(
            code = RoomCode("U6-22"),
            buildingCode = BuildingCode("U06"),
            name = "U6-22 con Podio",
            capacity = null,
            floor = 0,
        )
        val entity: MapRoomEntity = room.toEntity()
        assertThat(entity.code).isEqualTo("U6-22")
        assertThat(entity.buildingCode).isEqualTo("U06")
        assertThat(entity.capacity).isNull()
        assertThat(entity.floor).isEqualTo(0)

        assertThat(entity.toDomain()).isEqualTo(room)
    }
}
