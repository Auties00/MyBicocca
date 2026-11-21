package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvWriterTest {
    @Test
    fun `stream CSV headers match specification exactly`() {
        assertEquals("t_ns,rx_ns,x,y,z", StreamSpec.ACC.headerLine)
        assertEquals("t_ns,rx_ns,x,y,z,bx,by,bz", StreamSpec.ACC_RAW.headerLine)
        assertEquals("t_ns,rx_ns,x,y,z", StreamSpec.GYRO.headerLine)
        assertEquals("t_ns,rx_ns,x,y,z,bx,by,bz", StreamSpec.GYRO_RAW.headerLine)
        assertEquals("t_ns,rx_ns,x,y,z", StreamSpec.MAG.headerLine)
        assertEquals("t_ns,rx_ns,x,y,z,bx,by,bz", StreamSpec.MAG_RAW.headerLine)
        assertEquals("t_ns,rx_ns,hpa", StreamSpec.BARO.headerLine)
        assertEquals("t_ns,rx_ns,qx,qy,qz,qw", StreamSpec.GAME_ROT.headerLine)
        assertEquals("t_ns,rx_ns,qx,qy,qz,qw,heading_acc_rad", StreamSpec.ROT.headerLine)
        assertEquals("t_ns,rx_ns,kind,value", StreamSpec.STEPS.headerLine)
        assertEquals("rx_ns,sensor,accuracy", StreamSpec.SENSOR_ACCURACY.headerLine)
        assertEquals(
            "t_ns,rx_ns,addr,rssi,frame,beacon_id,tx_power_adv,tlm_batt_mv,tlm_temp_c,adv_hex",
            StreamSpec.BLE.headerLine,
        )
        assertEquals(
            "t_ns,rx_ns,scan_id,bssid,ssid,rssi,freq_mhz,channel_width,la_bit,rtt_responder,az_responder",
            StreamSpec.WIFI.headerLine,
        )
        assertEquals(
            "t_ns,rx_ns,bssid,status,dist_mm,std_mm,rssi,attempted,successful",
            StreamSpec.RTT.headerLine,
        )
        assertEquals("rx_ns,kind,detail", StreamSpec.EVENTS.headerLine)
        assertEquals("t_ns,rx_ns,kind,label,source", StreamSpec.MARKS.headerLine)
        assertEquals("t_ns,rx_ns,lat,lon,acc_m,alt,provider", StreamSpec.GNSS.headerLine)
    }

    @Test
    fun `formats and parses RFC 4180 quoted CSV fields`() {
        val row = CsvCodec.formatRow(123456L, "note", "door \"main\", floor 1")
        assertEquals("123456,note,\"door \"\"main\"\", floor 1\"", row)
        assertEquals(listOf("123456", "note", "door \"main\", floor 1"), CsvCodec.parseLine(row))
    }
}
