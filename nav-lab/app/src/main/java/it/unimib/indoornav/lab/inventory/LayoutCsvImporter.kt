package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import java.util.Locale
import java.util.UUID

/**
 * Reads a labtools venue layout (`layouts.csv`: layout, label, beacon_id, alt_ids, floor, x, y, z, wall,
 * tx_power, interval_ms, frames, status) into inventory entries. Rows without a layout are planned IDs,
 * not physical beacons, and excluded rows are other people's devices; both are skipped. Spares are
 * the home test set and are labelled H1, H2, ... in file order instead of by instance.
 */
object LayoutCsvImporter {
    // Our numbering: the Confidex instance A079 is B1, A07A is B2, up to A091 = B25.
    private const val FIRST_INSTANCE = 0xA079

    fun parse(text: String, building: String, nowMs: Long): List<SavedBeaconEntry> {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.isEmpty()) return emptyList()
        val header = lines.first().split(',').map { it.trim().lowercase(Locale.ROOT) }
        fun col(row: List<String>, name: String) = header.indexOf(name).let { if (it in row.indices) row[it].trim() else "" }

        var spareCount = 0
        return lines.drop(1).mapNotNull { line ->
            val row = line.split(',')
            val id = col(row, "beacon_id")
            val status = col(row, "status").lowercase(Locale.ROOT)
            if (id.isEmpty() || col(row, "layout").isEmpty() || status == "excluded") return@mapNotNull null

            val code = col(row, "label").ifEmpty { id.takeLast(4) }.uppercase(Locale.ROOT)
            val frames = col(row, "frames").split('+').map { it.trim().lowercase(Locale.ROOT) }.toSet()
            val floor = col(row, "floor")
            SavedBeaconEntry(
                id = UUID.randomUUID().toString(),
                canonicalKey = id,
                macAddress = "",
                linkedKeys = (listOf(id) + col(row, "alt_ids").split(';')).map { it.trim() }.filter { it.isNotEmpty() },
                label = if (status == "spare") "H${++spareCount}" else labelFor(code) ?: code,
                alphanumericCode = code,
                building = if (status == "spare") "Home" else building,
                floor = if (floor.isNotEmpty()) "Floor $floor" else "",
                positionDescription = positionOf(row = row, col = ::col, status = status),
                x = col(row, "x").toDoubleOrNull(),
                y = col(row, "y").toDoubleOrNull(),
                seenFrameTypes = frames.mapNotNull { frameOf(it) }.toSet(),
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
                advIntervalMs = col(row, "interval_ms").toIntOrNull() ?: BeaconBattery.DEFAULT_INTERVAL_MS,
                activeSlots = frames.mapNotNull { slotOf(it) }.toSet(),
            )
        }
    }

    private fun labelFor(code: String): String? {
        val n = code.toIntOrNull(16)?.minus(FIRST_INSTANCE - 1) ?: return null
        return if (n in 1..25) "B$n" else null
    }

    private fun positionOf(
        row: List<String>,
        col: (List<String>, String) -> String,
        status: String,
    ): String {
        val x = col(row, "x")
        val y = col(row, "y")
        val wall = col(row, "wall")
        return when {
            x.isNotEmpty() && y.isNotEmpty() -> wall.takeIf { it.isNotEmpty() }?.let { "$it wall" } ?: ""
            status == "found" -> "Mounted, position not known yet"
            status == "spare" -> "Spare, not mounted"
            else -> ""
        }
    }

    private fun frameOf(name: String): BeaconFrameType? = when (name) {
        "ed_uid" -> BeaconFrameType.ED_UID
        "ed_tlm" -> BeaconFrameType.ED_TLM
        "ibeacon" -> BeaconFrameType.IBEACON
        else -> null
    }

    private fun slotOf(name: String): BeaconSlot? = when (name) {
        "ed_uid" -> BeaconSlot.UID
        "ed_tlm" -> BeaconSlot.TLM
        "ibeacon" -> BeaconSlot.IBEACON
        "ed_url" -> BeaconSlot.URL
        else -> null
    }
}
