package it.attendance100.mybicocca.data.local.map

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// The full campus building catalog, bundled as an app asset. This is the source of truth for
// building coordinates: EasyStaff exposes no usable lat/lng, so the set is hand-curated.
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

@Serializable
data class BuildingCatalogFile(
    val version: Int = 1,
    val buildings: List<BuildingCatalogDto> = emptyList(),
)

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
