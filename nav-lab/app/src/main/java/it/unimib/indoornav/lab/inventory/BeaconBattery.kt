package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import kotlinx.serialization.Serializable

/** The advertising slots of a Confidex Viking Slim; each one transmits on its own timer. */
@Serializable
enum class BeaconSlot(val label: String) {
    UID("UID"),
    TLM("TLM"),
    IBEACON("iBeacon"),
    URL("URL"),
}

enum class BatteryLevel(val label: String, val minFraction: Double) {
    NEW("New", 0.80),
    GREAT("Great", 0.50),
    GOOD("Good", 0.35),
    LOW("Low", 0.15),
    VERY_LOW("Very low", 0.05),
    DYING("Dying", 0.0),
    ;

    companion object {
        fun of(remainingFraction: Double): BatteryLevel = entries.first { remainingFraction >= it.minFraction }
    }
}

data class BatteryEstimate(
    val lifeDays: Double,
    val remainingFraction: Double,
    val replaceByMs: Long,
) {
    val level: BatteryLevel get() = BatteryLevel.of(remainingFraction)
}

/**
 * Battery life of a Viking Slim on a CR2477 (3 V, about 1000 mAh), from the slots it transmits and its
 * advertising interval. Average current is sleep current plus one advertising event's charge per slot
 * per interval. The charge per event is fitted to three references: Confidex's 3-4 years for one slot
 * at 1 s, and the published 2.2-2.9 years at 1 s and 5-7 months at 200 ms for ID plus iBeacon.
 */
object BeaconBattery {
    const val DEFAULT_INTERVAL_MS = 1000

    private const val USABLE_CAPACITY_MAH = 850.0
    private const val SLEEP_CURRENT_UA = 2.0
    private const val CHARGE_PER_ADVERT_UC = 21.0

    // Telemetry goes out once every this many advertising intervals.
    private const val TLM_INTERVAL_FACTOR = 10

    private const val DAY_MS = 24L * 60 * 60 * 1000

    fun averageCurrentUa(intervalMs: Int, slots: Set<BeaconSlot>): Double {
        val intervalS = intervalMs.coerceAtLeast(20) / 1000.0
        val advertsPerSecond = slots.sumOf { slot ->
            if (slot == BeaconSlot.TLM) 1.0 / (intervalS * TLM_INTERVAL_FACTOR) else 1.0 / intervalS
        }
        return SLEEP_CURRENT_UA + CHARGE_PER_ADVERT_UC * advertsPerSecond
    }

    fun lifeDays(intervalMs: Int, slots: Set<BeaconSlot>): Double =
        USABLE_CAPACITY_MAH * 1000.0 / averageCurrentUa(intervalMs, slots) / 24.0

    /** Null until the battery has been reset at least once, since we do not know when it went in. */
    fun estimate(entry: SavedBeaconEntry, nowMs: Long): BatteryEstimate? {
        val since = entry.batteryResetAtMs ?: return null
        val life = lifeDays(entry.advIntervalMs, slotsOf(entry))
        val elapsedDays = (nowMs - since).coerceAtLeast(0L).toDouble() / DAY_MS
        return BatteryEstimate(
            lifeDays = life,
            remainingFraction = (1.0 - elapsedDays / life).coerceIn(0.0, 1.0),
            replaceByMs = since + (life * DAY_MS).toLong(),
        )
    }

    /** The slots saved with the beacon, or the ones its frames show if none were chosen yet. */
    fun slotsOf(entry: SavedBeaconEntry): Set<BeaconSlot> =
        entry.activeSlots.ifEmpty { slotsFromFrames(entry.seenFrameTypes) }

    fun slotsFromFrames(frames: Collection<BeaconFrameType>): Set<BeaconSlot> = frames.mapNotNull {
        when (it) {
            BeaconFrameType.ED_UID -> BeaconSlot.UID
            BeaconFrameType.ED_TLM -> BeaconSlot.TLM
            BeaconFrameType.IBEACON -> BeaconSlot.IBEACON
            else -> null
        }
    }.toSet()
}
