package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType

/** How one advertising slot of a beacon compares with what was set for it in Lab. */
enum class SlotState {
    /** Set and heard. */
    OK,

    /** Set, but not heard right now. */
    MISSING,

    /** Heard, but not set: the beacon sends something it should not. */
    UNEXPECTED,

    /** Heard, on a beacon with nothing set (not saved in the inventory). */
    HEARD,
}

object SlotPills {
    /** The slots to show for [tile], in a fixed order; slots neither set nor heard are left out. */
    fun of(tile: ResolvedBeaconTile): List<Pair<BeaconSlot, SlotState>> {
        val heard = if (tile.isOnline) {
            BeaconBattery.slotsFromFrames(tile.liveBeacon?.frameTypes ?: emptyList<BeaconFrameType>())
        } else {
            emptySet()
        }
        val set = tile.savedEntry?.let { BeaconBattery.slotsOf(it) }
            ?: return BeaconSlot.entries.filter { it in heard }.map { it to SlotState.HEARD }
        return BeaconSlot.entries.mapNotNull { slot ->
            when {
                slot in set && slot in heard -> slot to SlotState.OK
                slot in set -> slot to SlotState.MISSING
                slot in heard -> slot to SlotState.UNEXPECTED
                else -> null
            }
        }
    }
}
