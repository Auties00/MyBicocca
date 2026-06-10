package it.attendance100.mybicocca.data.local.map

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the full campus building catalog bundled as the buildings.json app asset. The catalog is
 * the source of truth for building coordinates: EasyStaff exposes no usable latitude/longitude,
 * so the set is hand-curated.
 */
@Singleton
class BuildingCatalogSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(): List<BuildingCatalogDto> = withContext(Dispatchers.IO) {
        context.assets.open(ASSET_PATH).use { stream ->
            json.decodeFromString<BuildingCatalogFile>(stream.readBytes().decodeToString()).buildings
        }
    }

    private companion object {
        const val ASSET_PATH = "buildings.json"
    }
}

/**
 * Root of the bundled buildings.json document.
 *
 * @property version Catalog format version.
 * @property buildings The catalogued buildings.
 */
@Serializable
data class BuildingCatalogFile(
    val version: Int = 1,
    val buildings: List<BuildingCatalogDto> = emptyList(),
)

/**
 * One building of the bundled catalog.
 *
 * @property code Building code (e.g. "U01"), unique in the catalog and doubling as the EasyStaff
 * query key.
 * @property name Display name.
 * @property latitude Hand-curated pin latitude.
 * @property longitude Hand-curated pin longitude.
 * @property category Building-category name; unrecognized or absent values are treated as
 * teaching buildings.
 * @property address Street address; null when not catalogued.
 * @property city Municipality; null when not catalogued.
 */
@Serializable
data class BuildingCatalogDto(
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = "TEACHING",
    val address: String? = null,
    val city: String? = null,
)
