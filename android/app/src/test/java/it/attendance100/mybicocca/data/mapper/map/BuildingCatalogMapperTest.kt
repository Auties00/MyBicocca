package it.attendance100.mybicocca.data.mapper.map

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.map.BuildingCatalogDto
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import org.junit.Test

/**
 * Covers the bundled-catalog -> Room mapping: the category string is normalized (trimmed,
 * upper-cased) to a known BuildingCategory name, with unrecognized values treated as teaching
 * buildings.
 */
class BuildingCatalogMapperTest {

    private fun dto(category: String) = BuildingCatalogDto(
        code = "U01",
        name = "Edificio U1",
        latitude = 45.5,
        longitude = 9.2,
        category = category,
        address = "Piazza dell'Ateneo Nuovo 1",
        city = "Milano",
    )

    @Test
    fun `normalizes a known category respecting case and whitespace`() {
        assertThat(dto("  library  ").toEntity().category).isEqualTo(BuildingCategory.LIBRARY.name)
    }

    @Test
    fun `keeps an already-canonical category`() {
        assertThat(dto("CANTEEN").toEntity().category).isEqualTo(BuildingCategory.CANTEEN.name)
    }

    @Test
    fun `unrecognized category degrades to TEACHING`() {
        assertThat(dto("warehouse").toEntity().category).isEqualTo(BuildingCategory.TEACHING.name)
    }

    @Test
    fun `copies the scalar fields through unchanged`() {
        val entity = dto("SPORT").toEntity()
        assertThat(entity.code).isEqualTo("U01")
        assertThat(entity.name).isEqualTo("Edificio U1")
        assertThat(entity.latitude).isEqualTo(45.5)
        assertThat(entity.longitude).isEqualTo(9.2)
        assertThat(entity.address).isEqualTo("Piazza dell'Ateneo Nuovo 1")
        assertThat(entity.city).isEqualTo("Milano")
    }
}
