package it.unimib.indoornav.lab.ui.map

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.unimib.indoornav.lab.inventory.BeaconInventoryRepository

/**
 * Beacon moves made in the map's edit mode, held until the user saves or discards them. It lives in
 * the app, not the screen, so leaving the map can ask what to do with them instead of losing them.
 */
class MapEditSession {
    var editing by mutableStateOf(false)
        private set

    /** New positions by saved-beacon id, in plan metres. */
    val moves = mutableStateMapOf<String, Pair<Double, Double>>()

    val hasChanges get() = moves.isNotEmpty()

    fun start() {
        editing = true
    }

    fun move(entryId: String, x: Double, y: Double) {
        moves[entryId] = x to y
    }

    fun save(repository: BeaconInventoryRepository) {
        moves.forEach { (id, xy) -> repository.updatePosition(id, xy.first, xy.second) }
        moves.clear()
        editing = false
    }

    fun discard() {
        moves.clear()
        editing = false
    }
}

/** A beacon another screen asked the map to show: the map switches floor, centres and zooms on it. */
class MapFocusRequest {
    var beaconId by mutableStateOf<String?>(null)

    /** A beacon to place: the map opens its floor in edit mode with the beacon at the centre of the view. */
    var placeBeaconId by mutableStateOf<String?>(null)
}

/** The map's last floor and view, kept across tab switches and app restarts. */
class MapCameraStore(context: Context) {
    data class Camera(val rotated: Boolean, val pxPerM: Float, val originX: Float, val originY: Float)

    private val prefs = context.getSharedPreferences("map_camera", Context.MODE_PRIVATE)

    var floor: String? = prefs.getString("floor", null)
        private set
    var camera: Camera? = if (prefs.contains("px_per_m")) {
        Camera(
            rotated = prefs.getBoolean("rotated", true),
            pxPerM = prefs.getFloat("px_per_m", 0f),
            originX = prefs.getFloat("origin_x", 0f),
            originY = prefs.getFloat("origin_y", 0f),
        )
    } else {
        null
    }
        private set

    fun save(floor: String, camera: Camera) {
        this.floor = floor
        this.camera = camera
        prefs.edit()
            .putString("floor", floor)
            .putBoolean("rotated", camera.rotated)
            .putFloat("px_per_m", camera.pxPerM)
            .putFloat("origin_x", camera.originX)
            .putFloat("origin_y", camera.originY)
            .apply()
    }
}
