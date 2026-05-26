package it.attendance100.mybicocca.data.local.campus

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Reads the curated campus geometry bundled as an app asset. This is the source of truth for
// building coordinates — EasyStaff exposes no lat/lng, only codes and names.
@Singleton
class CampusGeometrySource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadBuildings(): List<CampusBuildingDto> = withContext(Dispatchers.IO) {
        context.assets.open(ASSET_PATH).use { stream ->
            json.decodeFromString<CampusGeometryDto>(stream.readBytes().decodeToString()).buildings
        }
    }

    private companion object {
        const val ASSET_PATH = "campus/buildings.json"
    }
}

@Serializable
data class CampusGeometryDto(
    val version: Int = 1,
    val buildings: List<CampusBuildingDto> = emptyList(),
)

@Serializable
data class CampusBuildingDto(
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val address: String? = null,
)
