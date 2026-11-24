package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.survey.BeaconInventoryItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BeaconInventoryLogicTest {

    @Test
    fun `natural order comparator sorts numbers numerically inside labels`() {
        val input = listOf("B10", "B2", "B1", "b3", "A12", "A2")
        val sorted = input.sortedWith(NaturalOrderComparator)
        assertEquals(listOf("A2", "A12", "B1", "B2", "b3", "B10"), sorted)
    }

    @Test
    fun `grouping merges frames sharing MAC and picks Eddystone UID identity first`() {
        val mac = "D9:84:CE:34:5C:5C"
        val uidId = "ed:515b86185cea0ec01b22:0000000e0007"
        val ibId = "ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:7"

        val frames = listOf(
            BeaconInventoryItem(
                key = "ibeacon:$ibId:$mac",
                frameType = BeaconFrameType.IBEACON,
                canonicalId = ibId,
                projectLabel = "B07",
                addr = mac,
                currentRssi = -58,
                medianRssi = -59,
                packetsPerSec = 2.0,
                txPowerAdv = -59,
                tlmBattMv = null,
                tlmTempC = null,
                advHex = "ibhex",
                lastSeenMs = 10_000L,
            ),
            BeaconInventoryItem(
                key = "ed_tlm:$mac",
                frameType = BeaconFrameType.ED_TLM,
                canonicalId = uidId,
                projectLabel = "B07",
                addr = mac,
                currentRssi = -55,
                medianRssi = -56,
                packetsPerSec = 0.5,
                txPowerAdv = null,
                tlmBattMv = 3080,
                tlmTempC = 23.5,
                advHex = "tlmhex",
                lastSeenMs = 10_500L,
                tlmAdvCount = 42_000L,
                tlmUptimeSec = 1234.5,
            ),
            BeaconInventoryItem(
                key = "ed_uid:$uidId:$mac",
                frameType = BeaconFrameType.ED_UID,
                canonicalId = uidId,
                projectLabel = "B07",
                addr = mac,
                currentRssi = -56,
                medianRssi = -57,
                packetsPerSec = 3.0,
                txPowerAdv = -40,
                tlmBattMv = 3080,
                tlmTempC = 23.5,
                advHex = "uidhex",
                lastSeenMs = 10_200L,
                interArrivalSamplesMs = listOf(266L, 267L, 268L),
            ),
        )

        val grouped = BeaconGrouping.groupByPhysicalBeacon(frames)
        assertEquals(1, grouped.size)

        val tile = grouped.first()
        assertEquals(uidId, tile.identityKey)
        assertEquals(mac, tile.macAddress)
        assertEquals(setOf(uidId, ibId), tile.canonicalKeys)
        assertEquals(listOf(BeaconFrameType.ED_UID, BeaconFrameType.IBEACON, BeaconFrameType.ED_TLM), tile.frameTypes)
        assertEquals(5.5, tile.packetsPerSec, 0.001)
        assertEquals(-55, tile.currentRssi)
        assertEquals(-40, tile.txPowerAdv)
        assertEquals(3080, tile.tlmBattMv)
        assertEquals(23.5, tile.tlmTempC)
        assertEquals(42_000L, tile.tlmAdvCount)
        assertEquals(1234.5, tile.tlmUptimeSec)
        assertEquals(200, tile.estimatedIntervalByFrame[BeaconFrameType.ED_UID])
    }

    @Test
    fun `matches saved entry by canonicalKey, linkedKeys, or MAC address`() {
        val uidId = "ed:515b86185cea0ec01b22:0000000e0007"
        val ibId = "ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:7"
        val mac = "D9:84:CE:34:5C:5C"

        val saved = SavedBeaconEntry(
            id = "entry-1",
            canonicalKey = uidId,
            macAddress = mac,
            linkedKeys = listOf(uidId, ibId),
            label = "B07",
            alphanumericCode = "K4M2",
            building = "U14",
            floor = "Floor 1",
        )

        val onlyIBeaconDifferentMac = BeaconGrouping.groupByPhysicalBeacon(
            listOf(
                BeaconInventoryItem(
                    key = "ib",
                    frameType = BeaconFrameType.IBEACON,
                    canonicalId = ibId,
                    projectLabel = "B07",
                    addr = "11:22:33:44:55:66",
                    currentRssi = -60,
                    medianRssi = -60,
                    packetsPerSec = 1.0,
                    txPowerAdv = -59,
                    tlmBattMv = null,
                    tlmTempC = null,
                    advHex = "",
                    lastSeenMs = 1_000L,
                ),
            ),
        ).first()
        assertTrue(BeaconGrouping.matchesSavedEntry(saved, onlyIBeaconDifferentMac))

        val onlyTlmSameMac = BeaconGrouping.groupByPhysicalBeacon(
            listOf(
                BeaconInventoryItem(
                    key = "tlm",
                    frameType = BeaconFrameType.ED_TLM,
                    canonicalId = "",
                    projectLabel = null,
                    addr = mac.lowercase(),
                    currentRssi = -62,
                    medianRssi = -62,
                    packetsPerSec = 0.5,
                    txPowerAdv = null,
                    tlmBattMv = 3000,
                    tlmTempC = 20.0,
                    advHex = "",
                    lastSeenMs = 1_000L,
                ),
            ),
        ).first()
        assertTrue(BeaconGrouping.matchesSavedEntry(saved, onlyTlmSameMac))
    }

    @Test
    fun `resolveInventoryTiles marks beacons offline after 20 seconds and sorts online first then natural label`() {
        val nowMs = 100_000L
        val savedB10 = SavedBeaconEntry(
            id = "1",
            canonicalKey = "ed:ns:10",
            macAddress = "00:00:00:00:00:10",
            label = "B10",
            alphanumericCode = "Z1",
            building = "U14",
            floor = "Floor 1",
            lastSeenMs = nowMs - 5_000L,
        )
        val savedB2 = SavedBeaconEntry(
            id = "2",
            canonicalKey = "ed:ns:02",
            macAddress = "00:00:00:00:00:02",
            label = "B2",
            alphanumericCode = "A1",
            building = "U14",
            floor = "Floor 1",
            lastSeenMs = nowMs - 25_000L,
        )
        val savedB3 = SavedBeaconEntry(
            id = "3",
            canonicalKey = "ed:ns:03",
            macAddress = "00:00:00:00:00:03",
            label = "B3",
            alphanumericCode = "B1",
            building = "U14",
            floor = "Floor 1",
            lastSeenMs = nowMs - 2_000L,
        )

        val live = BeaconGrouping.groupByPhysicalBeacon(
            listOf(
                BeaconInventoryItem(
                    key = "k10",
                    frameType = BeaconFrameType.ED_UID,
                    canonicalId = "ed:ns:10",
                    projectLabel = null,
                    addr = "00:00:00:00:00:10",
                    currentRssi = -65,
                    medianRssi = -65,
                    packetsPerSec = 1.0,
                    txPowerAdv = -40,
                    tlmBattMv = null,
                    tlmTempC = null,
                    advHex = "",
                    lastSeenMs = nowMs - 5_000L,
                ),
                BeaconInventoryItem(
                    key = "k2",
                    frameType = BeaconFrameType.ED_UID,
                    canonicalId = "ed:ns:02",
                    projectLabel = null,
                    addr = "00:00:00:00:00:02",
                    currentRssi = -50,
                    medianRssi = -50,
                    packetsPerSec = 1.0,
                    txPowerAdv = -40,
                    tlmBattMv = null,
                    tlmTempC = null,
                    advHex = "",
                    lastSeenMs = nowMs - 20_001L,
                ),
                BeaconInventoryItem(
                    key = "k3",
                    frameType = BeaconFrameType.ED_UID,
                    canonicalId = "ed:ns:03",
                    projectLabel = null,
                    addr = "00:00:00:00:00:03",
                    currentRssi = -70,
                    medianRssi = -70,
                    packetsPerSec = 1.0,
                    txPowerAdv = -40,
                    tlmBattMv = null,
                    tlmTempC = null,
                    advHex = "",
                    lastSeenMs = nowMs - 2_000L,
                ),
            ),
        )

        val resolved = BeaconGrouping.resolveInventoryTiles(listOf(savedB10, savedB2, savedB3), live, nowMs)
        val byId = resolved.associateBy { it.tileKey }
        assertTrue(byId.getValue("1").isOnline)
        assertFalse(byId.getValue("2").isOnline)
        assertTrue(byId.getValue("3").isOnline)

        val sorted = BeaconGrouping.sortInventoryTiles(resolved, InventorySortOption.AVAILABILITY_THEN_LABEL)
        assertEquals(listOf("B3", "B10", "B2"), sorted.map { it.displayLabel })
    }

    @Test
    fun `advertising interval estimator applies 0_75 scaling and rounds to 50 ms`() {
        assertNull(AdvertisingIntervalEstimator.estimateIntervalMs(emptyList()))
        assertNull(AdvertisingIntervalEstimator.estimateIntervalMs(listOf(0L, -5L)))
        assertEquals(200, AdvertisingIntervalEstimator.estimateIntervalMs(listOf(266L, 267L, 268L, 900L)))
        assertEquals(1000, AdvertisingIntervalEstimator.estimateIntervalMs(listOf(1333L, 1334L)))
        assertEquals(50, AdvertisingIntervalEstimator.estimateIntervalMs(listOf(10L, 12L)))
    }

    @Test
    fun `eddystone tlm extras parser reads advCount and uptimeSec`() {
        // 02 01 06 | 03 03 AA FE | 11 16 AA FE 20 00 0C 30 16 66 00 07 5E 4E 00 0E 5E 46
        val raw = byteArrayOf(
            0x02, 0x01, 0x06,
            0x03, 0x03, 0xAA.toByte(), 0xFE.toByte(),
            0x11, 0x16, 0xAA.toByte(), 0xFE.toByte(),
            0x20, 0x00,
            0x0C, 0x30,
            0x16, 0x66,
            0x00, 0x07, 0x5E, 0x4E, // advCount = 482894
            0x00, 0x0E, 0x5E, 0x46, // secCount = 941638 -> 94163.8 s
        )
        val parsed = EddystoneTlmExtrasParser.parse(raw)
        assertNotNull(parsed)
        requireNotNull(parsed)
        assertEquals(482_894L, parsed.advCount)
        assertEquals(94_163.8, parsed.uptimeSec, 0.001)

        assertNull(EddystoneTlmExtrasParser.parse(byteArrayOf(0x02, 0x01, 0x06)))
    }
}
