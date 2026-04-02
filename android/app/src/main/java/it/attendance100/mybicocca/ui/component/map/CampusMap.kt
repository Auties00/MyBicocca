package it.attendance100.mybicocca.ui.component.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.util.OutlinedText
import it.attendance100.mybicocca.util.campusBuildings
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
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
import org.maplibre.compose.sources.GeoJsonData.Features
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
private const val LABEL_MIN_ZOOM = 15

private data class ExtrusionStyle(val height: Float, val base: Float, val color: Color)

@OptIn(ExperimentalComposeUiApi::class)
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

    // Group campus buildings by their extrusion style to minimize layers
    val buildingsByStyle = remember {
        campusBuildings.groupBy { ExtrusionStyle(it.height, it.base, it.color) }
    }

    // Editable buildings for the coordinate editor
    val editableBuildings = remember {
        campusBuildings.map { b ->
            editableBuilding(
                b.label.ifEmpty { b.id },
                b.points.map { Pair(it.latitude, it.longitude) })
        }
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

    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary

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
            // Render one FillExtrusionLayer per unique (height, base, color) group
            buildingsByStyle.entries.forEachIndexed { index, (style, group) ->
                val features = group.map { building ->
                    val closed = building.points + listOf(building.points.first())
                    Feature(
                        geometry = Polygon(listOf(closed)),
                        properties = JsonObject(emptyMap()),
                    )
                }
                val source = rememberGeoJsonSource(
                    data = Features(FeatureCollection(features))
                )
                FillExtrusionLayer(
                    id = "building-shapes-$index",
                    source = source,
                    color = const(style.color),
                    height = const(style.height),
                    base = const(style.base),
                    opacity = const(1f),
                    verticalGradient = const(true),
                )
            }

            // Building point markers
            if (featureCollection.isNotEmpty()) {
                val source = rememberGeoJsonSource(data = Features(featureCollection))
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

        // Campus Building Labels
        val showLabels = cameraState.position.zoom >= LABEL_MIN_ZOOM
        campusBuildings.filter { it.label.isNotEmpty() }.forEach { building ->
            CampusBuildingLabel(
                cameraState = cameraState,
                targetPosition = building.labelPosition,
            ) {
                AnimatedVisibility(
                    visible = showLabels,
//                    enter = fadeIn(),
//                    exit = fadeOut(),
                ) {
                    OutlinedText(
                        building.label,
                        Modifier.padding(4.dp),
                        fillColor = Color.White,
                        outlineColor = Color(0xFF1a0e10),
                        outlineDrawStyle = Stroke(width = 9f)
                    )
                }
            }
        }

        // Coordinate editor overlay
        CoordinateEditor(
            buildings = editableBuildings,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// Helper functions to convert between Pixels and DP
@Composable
@ReadOnlyComposable
internal fun Offset.toDpOffset(): DpOffset =
    with(LocalDensity.current) { DpOffset(x.toDp(), y.toDp()) }

@Composable
@ReadOnlyComposable
internal fun DpOffset.toOffset(): Offset = with(LocalDensity.current) { Offset(x.toPx(), y.toPx()) }

@Composable
fun CampusBuildingLabel(
    cameraState: CameraState,
    targetPosition: Position,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit),
) {
    // Project the geographic position to screen pixels
    val dpTarget = remember(targetPosition, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(targetPosition)
    }

    val target = dpTarget?.toOffset() ?: return // don't render anything if not ready

    // Position the content centered on the target point
    Box(modifier = modifier.fillMaxSize()) {
        val dpOffset = target.toDpOffset()
        Box(
            modifier = Modifier
                .absoluteOffset(dpOffset.x, dpOffset.y)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(-placeable.width / 2, -placeable.height / 2)
                    }
                }
        ) {
            content()
        }
    }
}
