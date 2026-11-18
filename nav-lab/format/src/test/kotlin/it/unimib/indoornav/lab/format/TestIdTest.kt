package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TestIdTest {
    @Test
    fun `builds E2 stand and carry variant test IDs`() {
        val stand = TestIdCodec.build(
            ExperimentTemplate.E2,
            mapOf("d" to "0.5", "mode" to "stand", "beacon" to "B03"),
        )
        assertEquals("E2-d=0.5-mode=stand-beacon=B03", stand)

        val crowd = TestIdCodec.build(
            ExperimentTemplate.E2,
            mapOf("d" to "30", "mode" to "crowd", "beacon" to "B03"),
        )
        assertEquals("E2-d=30-mode=crowd-beacon=B03", crowd)
    }

    @Test
    fun `builds E3 test ID with orient only`() {
        assertEquals("E3-orient=0", TestIdCodec.build(ExperimentTemplate.E3, mapOf("orient" to "0")))
        assertEquals("E3-orient=90", TestIdCodec.build(ExperimentTemplate.E3, mapOf("orient" to "90")))
    }

    @Test
    fun `builds E4 test ID with d and free-form setting`() {
        val id = TestIdCodec.build(
            ExperimentTemplate.E4,
            mapOf("d" to "5", "setting" to "int100-pwr0-both"),
        )
        assertEquals("E4-d=5-setting=int100-pwr0-both", id)
    }

    @Test
    fun `builds E4b test ID`() {
        assertEquals("E4b", TestIdCodec.build(ExperimentTemplate.E4B, emptyMap()))
    }

    @Test
    fun `builds E5 test ID matching guide key order including mode`() {
        val id = TestIdCodec.build(
            ExperimentTemplate.E5,
            mapOf(
                "route" to "R1",
                "dir" to "out",
                "pace" to "normal",
                "mode" to "hand-low",
                "layout" to "3",
            ),
        )
        assertEquals("E5-route=R1-dir=out-pace=normal-mode=hand-low-layout=3", id)
    }

    @Test
    fun `builds E6 test ID with walker and post-walk steps`() {
        val beforeWalk = TestIdCodec.build(
            ExperimentTemplate.E6,
            mapOf(
                "route" to "R1r",
                "dir" to "back",
                "pace" to "fast",
                "mode" to "pocket",
                "walker" to "Marco",
                "steps" to "",
            ),
        )
        assertEquals("E6-route=R1r-dir=back-pace=fast-mode=pocket-walker=Marco", beforeWalk)

        val afterWalk = TestIdCodec.build(
            ExperimentTemplate.E6,
            mapOf(
                "route" to "R1r",
                "dir" to "back",
                "pace" to "fast",
                "mode" to "pocket",
                "walker" to "Marco",
                "steps" to "64",
            ),
        )
        assertEquals("E6-route=R1r-dir=back-pace=fast-mode=pocket-walker=Marco-steps=64", afterWalk)
    }

    @Test
    fun `builds E8 test IDs for still stairs and lift`() {
        assertEquals(
            "E8-route=still-mode=still",
            TestIdCodec.build(ExperimentTemplate.E8, mapOf("route" to "still", "mode" to "still")),
        )
        assertEquals(
            "E8-route=S1-mode=stairs",
            TestIdCodec.build(ExperimentTemplate.E8, mapOf("route" to "S1", "mode" to "stairs")),
        )
        assertEquals(
            "E8-route=L1-mode=lift",
            TestIdCodec.build(ExperimentTemplate.E8, mapOf("route" to "L1", "mode" to "lift")),
        )
    }

    @Test
    fun `builds E9 test ID with route dir and offset`() {
        val id = TestIdCodec.build(
            ExperimentTemplate.E9,
            mapOf("route" to "R1L", "dir" to "out", "offset" to "-1"),
        )
        assertEquals("E9-route=R1L-dir=out-offset=-1", id)
    }

    @Test
    fun `builds E10 test IDs for checkpoint face and rtt ranging variants`() {
        val cp = TestIdCodec.build(
            ExperimentTemplate.E10,
            mapOf("cp" to "CP0", "face" to "pos"),
        )
        assertEquals("E10-cp=CP0-face=pos", cp)

        val rtt = TestIdCodec.build(
            ExperimentTemplate.E10,
            mapOf("cp" to "rtt", "d" to "5"),
        )
        assertEquals("E10-cp=rtt-d=5", rtt)
    }

    @Test
    fun `builds E11 test ID matching E5 fields`() {
        val id = TestIdCodec.build(
            ExperimentTemplate.E11,
            mapOf(
                "route" to "R1",
                "dir" to "out",
                "pace" to "normal",
                "mode" to "pocket",
                "layout" to "3",
            ),
        )
        assertEquals("E11-route=R1-dir=out-pace=normal-mode=pocket-layout=3", id)
    }

    @Test
    fun `derives checkpoint sequences starting at CP0 for known routes`() {
        assertEquals(
            listOf("CP0", "CP1", "CP2", "CP3", "CP4", "CP5", "CP6", "CP7", "CP8"),
            RouteCheckpoints.sequenceFor("R1"),
        )
        assertEquals("CP0", RouteCheckpoints.labelAt("R1", 0))
        assertEquals("CP1", RouteCheckpoints.labelAt("R1", 1))
        assertEquals("CP8", RouteCheckpoints.labelAt("R1r", 0))
        assertEquals("CP0", RouteCheckpoints.labelAt("R1r", 8))
        assertEquals("T1", RouteCheckpoints.labelAt("R1u", 4))
        assertEquals("CP3", RouteCheckpoints.labelAt("R1u", 5))
        assertEquals("ST0", RouteCheckpoints.labelAt("S1", 0))
        assertEquals("LF0", RouteCheckpoints.labelAt("L1", 0))
        assertEquals("CP0", RouteCheckpoints.labelAt("R2", 0))
        assertEquals("CP9", RouteCheckpoints.labelAt("R1", 9))
    }

    @Test
    fun `splits hyphenated values only on -key= boundaries`() {
        val id = "E4-d=5-setting=int100-pwr0-both"
        val parsed = TestIdCodec.parse(id)
        assertNotNull(parsed)
        parsed!!
        assertEquals("E4", parsed.prefix)
        assertEquals("5", parsed.fields["d"])
        assertEquals("int100-pwr0-both", parsed.fields["setting"])
    }

    @Test
    fun `splits negative offsets and hand-up carry mode`() {
        val id = "E9-route=R1-dir=out-offset=-1"
        val parsed = TestIdCodec.parse(id)
        assertNotNull(parsed)
        parsed!!
        assertEquals("-1", parsed.fields["offset"])

        val handUp = TestIdCodec.parse("E2-d=8-mode=hand-up-beacon=B03")
        assertNotNull(handUp)
        assertEquals("hand-up", handUp!!.fields["mode"])
    }

    @Test
    fun `sanitizes illegal folder characters in values`() {
        val sanitized = TestIdCodec.sanitizeValue("a:b/c\\d=e")
        assertEquals("a_b_c_d_e", sanitized)
    }
}
