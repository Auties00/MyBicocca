package it.unimib.indoornav.lab.ui.map

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
