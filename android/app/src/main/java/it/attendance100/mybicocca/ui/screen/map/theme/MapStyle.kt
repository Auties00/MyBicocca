package it.attendance100.mybicocca.ui.screen.map.theme

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import java.io.File

private const val PMTILES_ASSET_PATH = "map/basemap.pmtiles"
private const val STYLE_ASSET_PATH = "map/bicocca_style.json"
private const val PMTILES_SOURCE_PLACEHOLDER = "pmtiles://asset://map/basemap.pmtiles"

// MapLibre's Android asset:// file source ignores the byte-range that PMTiles' random access relies
// on — it hands back the whole archive for every request — so reading the basemap straight from
// assets aborts the native renderer with a zlib "incorrect header check". The local file:// source
// does honor ranges, so copy the bundled archive into filesDir once and rewrite the style's source
// url to point at the copy. Glyphs and the sprite are whole-file reads, so they stay on asset://.
suspend fun resolveBicoccaStyleJson(context: Context): String = withContext(Dispatchers.IO) {
    val assets = context.assets
    val dest = File(context.filesDir, PMTILES_ASSET_PATH)
    // openFd works only because the archive is stored uncompressed (androidResources.noCompress);
    // it gives the asset's true length cheaply so we can skip the copy when it is already current.
    val assetLength = assets.openFd(PMTILES_ASSET_PATH).use { it.length }
    if (!dest.exists() || dest.length() != assetLength) {
        dest.parentFile?.mkdirs()
        assets.open(PMTILES_ASSET_PATH).use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }
    assets.open(STYLE_ASSET_PATH).bufferedReader().use { it.readText() }
        .replace(PMTILES_SOURCE_PLACEHOLDER, "pmtiles://file://${dest.absolutePath}")
}

// Protomaps basemaps (v5 flavor) layer ids, grouped by the MapPalette role they take. Recoloring
// these in place — rather than reloading the style — is what makes the map theme-reactive and,
// unlike Google's cloud styling, free of the zoom-17 "Surface" flip (Protomaps has no such layer).
private val PARK_FILLS = listOf("landcover", "landuse_park", "landuse_urban_green", "landuse_zoo", "landuse_beach")
private val URBAN_FILLS = listOf(
    "landuse_hospital", "landuse_industrial", "landuse_school", "landuse_aerodrome",
    "landuse_runway", "landuse_pedestrian", "landuse_pier",
)
private val ROAD_CASINGS = listOf(
    "roads_tunnels_other_casing", "roads_tunnels_minor_casing", "roads_tunnels_link_casing", "roads_tunnels_major_casing",
    "roads_minor_service_casing", "roads_minor_casing", "roads_link_casing", "roads_major_casing_late", "roads_major_casing_early",
    "roads_bridges_other_casing", "roads_bridges_link_casing", "roads_bridges_minor_casing", "roads_bridges_major_casing",
)
private val ROAD_FILLS = listOf(
    "roads_pier", "roads_other", "roads_link", "roads_minor_service", "roads_minor", "roads_major",
    "roads_tunnels_other", "roads_tunnels_minor", "roads_tunnels_link", "roads_tunnels_major",
    "roads_bridges_other", "roads_bridges_minor", "roads_bridges_link", "roads_bridges_major",
)
private val HIGHWAY_FILLS = listOf("roads_highway", "roads_tunnels_highway", "roads_bridges_highway")
private val HIGHWAY_CASINGS = listOf(
    "roads_highway_casing_late", "roads_highway_casing_early", "roads_tunnels_highway_casing", "roads_bridges_highway_casing",
)
private val ROAD_LABELS = listOf("roads_labels_minor", "roads_labels_major")
private val PLACE_LABELS = listOf("places_subplace", "places_region", "places_locality", "places_country")
private val WATER_LABELS = listOf("water_waterway_label", "water_label_ocean", "water_label_lakes", "earth_label_islands")

// Recolor the live GL style to [palette]. Safe to call on every theme change — it mutates layer
// paint in place (no setStyle), so there is no flash. The `pois` layer is deliberately left alone
// so Protomaps' default category icons keep their own colors.
fun Style.applyBicoccaPalette(palette: MapPalette) {
    val ground = palette.ground.toArgb()
    val urban = palette.urban.toArgb()
    val park = palette.park.toArgb()
    val water = palette.water.toArgb()
    val building = palette.building.toArgb()
    val buildingOutline = palette.buildingOutline.toArgb()
    val road = palette.road.toArgb()
    val roadCasing = palette.roadCasing.toArgb()
    val highway = palette.highway.toArgb()
    val highwayCasing = palette.highwayCasing.toArgb()
    val label = palette.label.toArgb()
    val roadLabel = palette.roadLabel.toArgb()
    val halo = palette.labelHalo.toArgb()

    fun fill(id: String, color: Int, outline: Int? = null) {
        val layer = getLayer(id) as? FillLayer ?: return
        layer.setProperties(PropertyFactory.fillColor(color))
        if (outline != null) layer.setProperties(PropertyFactory.fillOutlineColor(outline))
    }
    fun lines(ids: List<String>, color: Int) = ids.forEach { id ->
        (getLayer(id) as? LineLayer)?.setProperties(PropertyFactory.lineColor(color))
    }
    fun labels(ids: List<String>, text: Int) = ids.forEach { id ->
        (getLayer(id) as? SymbolLayer)?.setProperties(
            PropertyFactory.textColor(text),
            PropertyFactory.textHaloColor(halo),
        )
    }

    (getLayer("background") as? BackgroundLayer)?.setProperties(PropertyFactory.backgroundColor(ground))
    fill("earth", ground)
    PARK_FILLS.forEach { fill(it, park) }
    URBAN_FILLS.forEach { fill(it, urban) }
    fill("water", water)
    lines(listOf("water_stream", "water_river"), water)
    fill("buildings", building, buildingOutline)
    lines(ROAD_CASINGS, roadCasing)
    lines(ROAD_FILLS, road)
    lines(HIGHWAY_FILLS, highway)
    lines(HIGHWAY_CASINGS, highwayCasing)
    // Transit off, like the original Bicocca style.
    (getLayer("roads_rail") as? LineLayer)?.setProperties(PropertyFactory.visibility(Property.NONE))
    labels(ROAD_LABELS, roadLabel)
    labels(PLACE_LABELS, label)
    labels(WATER_LABELS, roadLabel)
}
