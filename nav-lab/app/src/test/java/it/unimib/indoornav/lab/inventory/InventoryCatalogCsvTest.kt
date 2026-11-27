package it.unimib.indoornav.lab.inventory

import it.unimib.indoornav.lab.format.BeaconFrameType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InventoryCatalogCsvTest {
    private val header = "id,canonical_key,mac_address,linked_keys,label,alphanumeric_code,building,floor," +
        "position_description,seen_frame_types,last_rssi,last_median_rssi,last_tx_power_adv,last_batt_mv," +
        "last_temp_c,last_seen_ms,created_at_ms,updated_at_ms,x,y,adv_interval_ms,active_slots," +
        "battery_reset_at_ms,photo_file"

    @Test
    fun readsEveryExportedField() {
        val text = header + "\n" +
            "u1,ed:ns:a07e,CE:0F:43:3E:C4:C1,ed:ns:a07e;ib:x:2:1,B4,A07E,U14,Floor 1," +
            "\"far-side wall, by the door\",ed_uid;ed_tlm,-70,-72,-20,3000,21.5,100,50,90,22.1,7.82,1000,UID,40,p.jpg\n"
        val e = InventoryCatalogCsv.parse(text).single()
        assertEquals("u1", e.id)
        assertEquals("ed:ns:a07e", e.canonicalKey)
        assertEquals("CE:0F:43:3E:C4:C1", e.macAddress)
        assertEquals(listOf("ed:ns:a07e", "ib:x:2:1"), e.linkedKeys)
        assertEquals("far-side wall, by the door", e.positionDescription)
        assertEquals(setOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM), e.seenFrameTypes)
        assertEquals(22.1, e.x!!, 1e-9)
        assertEquals(1000, e.advIntervalMs)
        assertEquals(setOf(BeaconSlot.UID), e.activeSlots)
        assertEquals(40L, e.batteryResetAtMs)
        assertEquals("p.jpg", e.photoFile)
    }

    @Test
    fun blanksBecomeNulls() {
        val e = InventoryCatalogCsv.parse("$header\nu2,ed:ns:a08d,,,H2,A08D,Home,,,,,,,,,0,0,0,,,1000,,,\n").single()
        assertNull(e.x)
        assertNull(e.photoFile)
        assertNull(e.batteryResetAtMs)
        assertTrue(e.activeSlots.isEmpty())
    }

    @Test
    fun tellsCatalogFromLayout() {
        assertTrue(InventoryCatalogCsv.isCatalog(header))
        assertFalse(InventoryCatalogCsv.isCatalog("layout,label,beacon_id,alt_ids,floor,x,y,z"))
    }
}
