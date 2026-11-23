package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BeaconBatteryAndImportTest {
    private val year = 365.25

    @Test
    fun `battery model matches the published figures it was fitted to`() {
        val oneSlot = BeaconBattery.lifeDays(1000, setOf(BeaconSlot.UID)) / year
        val idAndIBeacon = BeaconBattery.lifeDays(1000, setOf(BeaconSlot.UID, BeaconSlot.IBEACON)) / year
        val fast = BeaconBattery.lifeDays(200, setOf(BeaconSlot.UID, BeaconSlot.IBEACON)) / year * 12
        assertTrue(oneSlot in 3.5..4.5, "one slot at 1 s: $oneSlot years")
        assertTrue(idAndIBeacon in 2.0..2.9, "ID + iBeacon at 1 s: $idAndIBeacon years")
        assertTrue(fast in 5.0..7.0, "ID + iBeacon at 200 ms: $fast months")
    }

    @Test
    fun `battery levels follow the agreed thresholds`() {
        assertEquals(BatteryLevel.NEW, BatteryLevel.of(0.80))
        assertEquals(BatteryLevel.GREAT, BatteryLevel.of(0.79))
        assertEquals(BatteryLevel.GOOD, BatteryLevel.of(0.35))
        assertEquals(BatteryLevel.LOW, BatteryLevel.of(0.34))
        assertEquals(BatteryLevel.VERY_LOW, BatteryLevel.of(0.05))
        assertEquals(BatteryLevel.DYING, BatteryLevel.of(0.04))
    }

    @Test
    fun `estimate needs a reset and counts down from it`() {
        val entry = SavedBeaconEntry(
            id = "1", canonicalKey = "ed:x", macAddress = "", label = "B1", building = "U14", floor = "Floor 1",
            activeSlots = setOf(BeaconSlot.UID),
        )
        assertNull(BeaconBattery.estimate(entry, nowMs = 0L))
        val life = BeaconBattery.lifeDays(1000, setOf(BeaconSlot.UID))
        val halfway = (life / 2 * 24 * 60 * 60 * 1000).toLong()
        val estimate = BeaconBattery.estimate(entry.copy(batteryResetAtMs = 0L), nowMs = halfway)!!
        assertEquals(0.5, estimate.remainingFraction, 0.001)
        assertEquals(BatteryLevel.GREAT, estimate.level)
    }

    @Test
    fun `layout import keeps physical beacons and numbers them`() {
        val csv = """
            layout,label,beacon_id,alt_ids,floor,x,y,z,wall,tx_power,interval_ms,frames,status
            ,B01,ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:1,ed:515b86185cea0ec01b22:0000000e0001,,,,,,,,ibeacon+ed_uid,spare
            L0,A07D,ed:8b9cc73c3ae747ef65bc:00000001a07d,,1,19.31,6.16,,lab-side,,,ed_uid+ed_tlm,mounted
            L0,A084,ed:8b9cc73c3ae747ef65bc:00000001a084,,1,,,,,,,ed_uid+ed_tlm,found
            L0,X01,ib:50765cb7-d9ea-4e21-99a4-fa879613a492:4825:63247,,,,,,,,,ibeacon,excluded
            L0,A08C,ed:8b9cc73c3ae747ef65bc:00000001a08c,ib:8b9cc73c-3ae7-47ef-65bc-000000000000:2:6636,,,,,,,,ed_uid+ed_tlm+ibeacon+ed_url,spare
        """.trimIndent()
        val entries = LayoutCsvImporter.parse(csv, building = "U14", nowMs = 1L)

        assertEquals(listOf("A07D", "A084", "A08C"), entries.map { it.alphanumericCode })
        assertEquals(listOf("B5", "B12", "H1"), entries.map { it.label })
        val mounted = entries[0]
        assertEquals("Floor 1", mounted.floor)
        assertEquals("x 19.31 m, y 6.16 m, lab-side wall", mounted.positionDescription)
        assertEquals(setOf(BeaconSlot.UID, BeaconSlot.TLM), mounted.activeSlots)
        assertEquals(setOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM), mounted.seenFrameTypes)
        assertEquals("Mounted, position not known yet", entries[1].positionDescription)
        assertTrue("ib:8b9cc73c-3ae7-47ef-65bc-000000000000:2:6636" in entries[2].linkedKeys)
        assertEquals(4, entries[2].activeSlots.size)
    }
}
