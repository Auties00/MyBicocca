package it.attendance100.mybicocca.ui.component.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.ui.theme.PrimaryColor
import it.attendance100.mybicocca.util.U1
import it.attendance100.mybicocca.util.U1U4
import it.attendance100.mybicocca.util.U2
import it.attendance100.mybicocca.util.U2U3
import it.attendance100.mybicocca.util.U3
import it.attendance100.mybicocca.util.U4
import it.attendance100.mybicocca.util.U5
import it.attendance100.mybicocca.util.U5Tall
import it.attendance100.mybicocca.util.U6
import it.attendance100.mybicocca.util.U7
import it.attendance100.mybicocca.util.U9
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
import org.maplibre.compose.layers.FillExtrusionLayer
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
import org.maplibre.spatialk.geojson.Polygon
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

    // Editable building shapes
    val editableBuildings = remember {
        listOf(
            editableBuilding("U1", U1.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U2", U2.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U3", U3.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U4", U4.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U5", U5.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U6", U6.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U7", U7.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U9", U9.map { Pair(it.latitude, it.longitude) }),
        )
    }

    val highBuildings = remember {
        listOf(
            editableBuilding("U1 - U4 Bridge", U1U4.map { Pair(it.latitude, it.longitude) }),
            editableBuilding("U2 - U3 Bridge", U2U3.map { Pair(it.latitude, it.longitude) }),
        )
    }

    val tallBuildings = remember {
        listOf(
            editableBuilding("U5 Chimney", U5Tall.map { Pair(it.latitude, it.longitude) }),
        )
    }

    // Convert editable buildings to GeoJSON features (recomputed on every state change)
    val buildingShapeFeatures = editableBuildings.map { building ->
        val positions =
            building.points.map { (lat, lng) -> Position(longitude = lng, latitude = lat) }
        val closed: List<Position> = positions + listOf(positions.first()) // close the ring
        Feature(
            geometry = Polygon(listOf(closed)),
            properties = JsonObject(emptyMap()),
        )
    }
    val highBuildingShapeFeatures = highBuildings.map { building ->
        val positions =
            building.points.map { (lat, lng) -> Position(longitude = lng, latitude = lat) }
        val closed: List<Position> = positions + listOf(positions.first()) // close the ring

        Feature(
            geometry = Polygon(listOf(closed)),
            properties = JsonObject(emptyMap()),
        )
    }
    val tallBuildingShapeFeatures = tallBuildings.map { building ->
        val positions =
            building.points.map { (lat, lng) -> Position(longitude = lng, latitude = lat) }
        val closed: List<Position> = positions + listOf(positions.first()) // close the ring

        Feature(
            geometry = Polygon(listOf(closed)),
            properties = JsonObject(emptyMap()),
        )
    }


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

    Box(modifier = modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                if (isSystemInDarkTheme()) "https://tiles.openfreemap.org/styles/dark"
                else "https://tiles.openfreemap.org/styles/liberty"
            ),
            cameraState = cameraState,
            onMapClick = { _, _ -> ClickResult.Pass },
            options = MapOptions(ornamentOptions = OrnamentOptions.AllDisabled),
        ) {
            // Building shape polygons
            val shapesSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(FeatureCollection(buildingShapeFeatures))
            )
            FillExtrusionLayer(
                id = "building-shapes-fill",
                source = shapesSource,
                color = const(PrimaryColor),
                height = const(40f),
                base = const(0f),
                opacity = const(1f),
                verticalGradient = const(true),
            )
            // Building high shape polygons
            val highShapesSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(FeatureCollection(highBuildingShapeFeatures))
            )
            FillExtrusionLayer(
                id = "building-high-shapes-fill",
                source = highShapesSource,
                color = const(PrimaryColor),
                height = const(40f),
                base = const(20f),
                opacity = const(1f),
                verticalGradient = const(true),
            )
            // Building tall shape polygons
            val tallShapesSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(FeatureCollection(tallBuildingShapeFeatures))
            )
            FillExtrusionLayer(
                id = "building-tall-shapes-fill",
                source = tallShapesSource,
                color = const(PrimaryColor),
                height = const(60f),
                base = const(0f),
                opacity = const(1f),
                verticalGradient = const(true),
            )
//            SymbolLayer(
//                id = "building-shapes-label",
//                source = shapesSource,
//                textField = format(span(const("{label}"))),
//                textSize = const(0.9f.em),
//                textColor = const(textColor),
//                textAllowOverlap = const(true),
//            )

            // Building point markers
            if (featureCollection.isNotEmpty()) {
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

        // Coordinate editor overlay
        CoordinateEditor(
            buildings = editableBuildings + highBuildings + tallBuildings,
            modifier = Modifier.fillMaxSize(),
        )
    }
}