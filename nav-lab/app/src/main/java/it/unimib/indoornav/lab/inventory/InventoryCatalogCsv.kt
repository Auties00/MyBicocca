package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.format.CsvCodec

/** Reads back the inventory export ([BeaconInventoryRepository.exportCatalogCsv]) with every field it wrote. */
object InventoryCatalogCsv {
    fun isCatalog(text: String): Boolean =
        text.lineSequence().firstOrNull()?.split(',')?.map { it.trim().lowercase() }?.contains("canonical_key") == true

    fun parse(text: String): List<SavedBeaconEntry> {
        val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()
        val header = CsvCodec.parseLine(lines.first()).orEmpty().map { it.trim().lowercase() }
        return lines.drop(1).mapNotNull { line ->
            val row = CsvCodec.parseLine(line) ?: return@mapNotNull null
            fun col(name: String) = header.indexOf(name).let { if (it in row.indices) row[it].trim() else "" }
            fun list(name: String) = col(name).split(';').map { it.trim() }.filter { it.isNotEmpty() }
            val id = col("id")
            if (id.isEmpty() || col("canonical_key").isEmpty()) return@mapNotNull null
            SavedBeaconEntry(
                id = id,
                canonicalKey = col("canonical_key"),
                macAddress = col("mac_address"),
                linkedKeys = list("linked_keys"),
                label = col("label"),
                alphanumericCode = col("alphanumeric_code"),
                building = col("building"),
                floor = col("floor"),
                positionDescription = col("position_description"),
                seenFrameTypes = list("seen_frame_types")
                    .mapNotNull { w -> BeaconFrameType.entries.firstOrNull { it.wireName == w } }
                    .toSet(),
                lastRssi = col("last_rssi").toIntOrNull(),
                lastMedianRssi = col("last_median_rssi").toIntOrNull(),
                lastTxPowerAdv = col("last_tx_power_adv").toIntOrNull(),
                lastBattMv = col("last_batt_mv").toIntOrNull(),
                lastTempC = col("last_temp_c").toDoubleOrNull(),
                lastSeenMs = col("last_seen_ms").toLongOrNull() ?: 0L,
                createdAtMs = col("created_at_ms").toLongOrNull() ?: 0L,
                updatedAtMs = col("updated_at_ms").toLongOrNull() ?: 0L,
                x = col("x").toDoubleOrNull(),
                y = col("y").toDoubleOrNull(),
                advIntervalMs = col("adv_interval_ms").toIntOrNull() ?: BeaconBattery.DEFAULT_INTERVAL_MS,
                activeSlots = list("active_slots").mapNotNull { n -> BeaconSlot.entries.firstOrNull { it.name == n } }.toSet(),
                batteryResetAtMs = col("battery_reset_at_ms").toLongOrNull(),
                photoFile = col("photo_file").ifEmpty { null },
            )
        }
    }
}
