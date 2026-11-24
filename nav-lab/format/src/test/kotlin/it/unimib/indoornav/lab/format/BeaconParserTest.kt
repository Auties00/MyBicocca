package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BeaconParserTest {
    @Test
    fun `parses real Eddystone-UID raw advertisement bytes and matches project label`() {
        // Flags (02 01 06) + 16-bit Service UUID 0xFEAA (03 03 AA FE) +
        // Service Data 0xFEAA: len=0x17 (23), type=0x16, uuid=AA FE, frame=0x00, tx=-21 (0xEB),
        // namespace=515b86185cea0ec01b22, instance=0000000e0007 (U14 = 0x000e, B07 = 0x0007)
        val raw = hexBytes(
            "020106" +
                "0303AAFE" +
                "1716AAFE00EB" +
                "515B86185CEA0EC01B22" +
                "0000000E0007" +
                "0000" +
                "00000000"
        )

        val parsed = BeaconFrameParser.parseRawAdvertisement(raw)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.ED_UID, parsed.frameType)
        assertEquals("ed:515b86185cea0ec01b22:0000000e0007", parsed.beaconId)
        assertEquals(-21, parsed.txPowerAdv)
        assertNull(parsed.tlmBattMv)
        assertNull(parsed.tlmTempC)
        assertEquals("B07", ProjectConstants.matchProjectLabel(parsed.beaconId!!, 14))
    }

    @Test
    fun `parses real Eddystone-TLM raw advertisement bytes`() {
        // Service Data 0xFEAA: len=0x11 (17), type=0x16, uuid=AA FE, frame=0x20, ver=0x00,
        // vbatt=3000mV (0x0BB8), temp=23.5C (23*256 + 128 = 0x1780), advCnt=0x00010000, secCnt=0x00000200
        val raw = hexBytes(
            "020106" +
                "0303AAFE" +
                "1116AAFE20000BB817800001000000000200"
        )

        val parsed = BeaconFrameParser.parseRawAdvertisement(raw)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.ED_TLM, parsed.frameType)
        assertNull(parsed.beaconId)
        assertEquals(3000, parsed.tlmBattMv)
        assertEquals(23.5, parsed.tlmTempC!!, 0.001)
    }

    @Test
    fun `parses real iBeacon raw advertisement bytes and formats canonical ID without leading zeros`() {
        // Flags (02 01 06) + Manufacturer Data: len=0x1A (26), type=0xFF, company=4C 00,
        // iBeacon prefix=02 15, UUID=515b8618-bead-46de-ba72-5cea0ec01b22,
        // major=14 (0x000E), minor=7 (0x0007), txPower=-59 (0xC5)
        val raw = hexBytes(
            "020106" +
                "1AFF4C000215" +
                "515B8618BEAD46DEBA725CEA0EC01B22" +
                "000E0007C5"
        )

        val parsed = BeaconFrameParser.parseRawAdvertisement(raw)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.IBEACON, parsed.frameType)
        assertEquals("ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:7", parsed.beaconId)
        assertEquals(-59, parsed.txPowerAdv)
        assertEquals("B07", ProjectConstants.matchProjectLabel(parsed.beaconId!!, 14))
    }

    @Test
    fun `parses AltBeacon raw advertisement bytes`() {
        val raw = hexBytes(
            "020106" +
                "1BFF1801BEAC" +
                "515B8618BEAD46DEBA725CEA0EC01B22000E0007" +
                "C600"
        )
        val parsed = BeaconFrameParser.parseRawAdvertisement(raw)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.ALTBEACON, parsed.frameType)
        assertEquals("ab:515b8618bead46deba725cea0ec01b22000e0007", parsed.beaconId)
        assertEquals(-58, parsed.txPowerAdv)
    }

    @Test
    fun `ignores non-beacon Apple Continuity advertisement`() {
        // Apple company ID 0x004C with Continuity type 0x10 (Nearby Info) instead of 0x02 0x15
        val raw = hexBytes("02011A0AFF4C001005011C33A291")
        assertNull(BeaconFrameParser.parseRawAdvertisement(raw))
    }

    @Test
    fun `parses and validates canonical IDs`() {
        val ib = CanonicalBeaconId.parse("IB:515B8618-BEAD-46DE-BA72-5CEA0EC01B22:14:7")
        assertEquals("ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:7", ib?.canonical)

        val ed = CanonicalBeaconId.parse("ed:515B86185CEA0EC01B22:0000000E0007")
        assertEquals("ed:515b86185cea0ec01b22:0000000e0007", ed?.canonical)

        assertNull(CanonicalBeaconId.parse("ed:short:1234"))
        assertNull(CanonicalBeaconId.parse("ib:not-a-uuid:1:2"))
    }

    @Test
    fun `parses Eddystone-UID from scan response in 62-byte ScanRecord buffer`() {
        val primary = hexBytes("02010604094C4142") // Flags + "LAB" (8 bytes)
        val scanResp = hexBytes(
            "0303AAFE" +
                "1716AAFE00EB" +
                "515B86185CEA0EC01B22" +
                "0000000E0007" +
                "0000"
        ) // 28 bytes
        val buffer = ByteArray(62)
        primary.copyInto(buffer, destinationOffset = 0)
        scanResp.copyInto(buffer, destinationOffset = 31)

        assertEquals(31 + scanResp.size, BeaconFrameParser.activeAdvertisementLength(buffer))

        val parsed = BeaconFrameParser.parseRawAdvertisement(buffer)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.ED_UID, parsed.frameType)
        assertEquals("ed:515b86185cea0ec01b22:0000000e0007", parsed.beaconId)
        assertEquals(-21, parsed.txPowerAdv)
        assertEquals(buffer.copyOfRange(0, 31 + scanResp.size).toHexLower(), parsed.advHex)
    }

    @Test
    fun `parses iBeacon from scan response and trims trailing zero padding in 62-byte buffer`() {
        val primary = hexBytes("02010604094C4142")
        val iBeacon = hexBytes(
            "1AFF4C000215" +
                "515B8618BEAD46DEBA725CEA0EC01B22" +
                "000E0007C5"
        ) // 27 bytes
        val buffer = ByteArray(62)
        primary.copyInto(buffer, destinationOffset = 0)
        iBeacon.copyInto(buffer, destinationOffset = 31)

        assertEquals(31 + iBeacon.size, BeaconFrameParser.activeAdvertisementLength(buffer))

        val parsed = BeaconFrameParser.parseRawAdvertisement(buffer)
        assertNotNull(parsed)
        parsed!!
        assertEquals(BeaconFrameType.IBEACON, parsed.frameType)
        assertEquals("ib:515b8618-bead-46de-ba72-5cea0ec01b22:14:7", parsed.beaconId)
        assertEquals(-59, parsed.txPowerAdv)
    }

    @Test
    fun `activeAdvertisementLength excludes empty zero-padded scan response`() {
        val primary = hexBytes(
            "020106" +
                "0303AAFE" +
                "1116AAFE20000BB817800001000000000200"
        ) // 25 bytes
        val buffer = ByteArray(62)
        primary.copyInto(buffer, destinationOffset = 0)

        assertEquals(primary.size, BeaconFrameParser.activeAdvertisementLength(buffer))
        val parsed = BeaconFrameParser.parseRawAdvertisement(buffer)
        assertNotNull(parsed)
        assertEquals(BeaconFrameType.ED_TLM, parsed!!.frameType)
        assertEquals(primary.toHexLower(), parsed.advHex)
    }

    private fun hexBytes(hex: String): ByteArray = ProjectConstants.hexToBytes(hex)
}
