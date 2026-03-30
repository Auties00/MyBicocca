package it.attendance100.mybicocca.ui.component.map

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import it.attendance100.mybicocca.data.model.campus.Building
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.layers.Anchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

private const val CAMPUS_CENTER_LAT = 45.5170
private const val CAMPUS_CENTER_LNG = 9.2115

@Composable
fun CampusMap(
    buildings: List<Building>,
    selectedBuilding: Building?,
    onBuildingClick: (Building) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(
                longitude = CAMPUS_CENTER_LNG,
                latitude = CAMPUS_CENTER_LAT,
            ),
            zoom = 14.5,
        ),
    )

    val featureCollection = remember(buildings) {
        FeatureCollection(
            buildings.filter { it.latitude != null && it.longitude != null }
                .map { building ->
                    Feature(
                        geometry = Point(
                            Position(
                                longitude = building.longitude!!,
                                latitude = building.latitude!!,
                            )
                        ),
                        properties = JsonObject(
                            mapOf(
                                "code" to JsonPrimitive(building.code),
                                "name" to JsonPrimitive(building.name),
                            )
                        ),
                    )
                }
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(
            if (isSystemInDarkTheme()) "https://tiles.openfreemap.org/styles/dark"
            else "https://tiles.openfreemap.org/styles/liberty"
        ),
        cameraState = cameraState,
        onMapClick = { _, _ -> ClickResult.Pass },
        options = MapOptions(ornamentOptions = OrnamentOptions.AllDisabled),
    ) {
        if(featureCollection.isNotEmpty()) {
            val source = rememberGeoJsonSource(data = GeoJsonData.Features(featureCollection))
            Anchor.Top {
                CircleLayer(
                    id = "campus-buildings-circle",
                    source = source,
                    color = const(primaryColor),
                    radius = const(6.dp),
                    strokeColor = const(textColor),
                    strokeWidth = const(2.dp),
                    onClick = { features ->
                        val code = features.firstOrNull()?.getStringProperty("code")
                        val building = buildings.firstOrNull { it.code == code }
                        if (building != null) {
                            onBuildingClick(building)
                        }
                        ClickResult.Consume
                    },
                )

                SymbolLayer(
                    id = "campus-buildings-label",
                    source = source,
                    textField = format(span(const("{name}"))),
                    textSize = const(0.8f.em),
                    textColor = const(textColor),
                    textOffset = offset(0f.em, 1.2f.em),
                    textAllowOverlap = const(true),
                )
            }
        }
    }
}
