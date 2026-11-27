package it.unimib.indoornav.lab.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionEditorTest {
    private val manifest = SessionManifest(
        app_version = "0.1.0",
        phone = PhoneInfo(label = "A1", model = "SM-S928B", os = "17", manufacturer = "samsung"),
        capabilities = CapabilitiesInfo(barometer = true, gyroscope = true, wifi_rtt = true, arcore = true),
        setup = SetupManifestInfo(
            operator = "LL",
            marker = "",
            building = "U14",
            floor = 1,
            test_id = "E2-d=3-mode=stand-beacon=A088",
            carry_mode = "stand",
            recording_mode = "free",
            template_fields = mapOf("d" to "3", "mode" to "stand", "beacon" to "A088"),
            notes = "3m",
        ),
        clock = ClockInfo(wall_ms = 1795220000000L, elapsed_ns = 1L),
        stopped_cleanly = true,
    )

    @Test
    fun `editing a field rebuilds the test ID, carry mode and folder name`() {
        val edits = SessionEditor.editsOf(manifest).copy(
            templateFields = mapOf("d" to "4 ", "mode" to "pocket", "beacon" to "A088"),
            notes = " 4m ",
            marker = "AF",
        )
        val edited = SessionEditor.apply(manifest, ExperimentTemplate.E2, edits)

        assertEquals("E2-d=4-mode=pocket-beacon=A088", edited.setup.test_id)
        assertEquals("pocket", edited.setup.carry_mode)
        assertEquals("4m", edited.setup.notes)
        assertEquals("AF", edited.setup.marker)
        assertEquals("4", edited.setup.template_fields["d"])
        assertEquals(manifest.clock, edited.clock)
        assertEquals(
            SessionNaming.buildFolderName(1795220000000L, "A1", "E2-d=4-mode=pocket-beacon=A088"),
            SessionEditor.folderNameFor(edited),
        )
    }

    @Test
    fun `a blank phone label keeps the recorded one`() {
        val edits = SessionEditor.editsOf(manifest).copy(phoneLabel = "  ")
        assertEquals("A1", SessionEditor.apply(manifest, ExperimentTemplate.E2, edits).phone.label)
    }

    @Test
    fun `unchanged edits give back the same manifest`() {
        assertEquals(manifest, SessionEditor.apply(manifest, ExperimentTemplate.E2, SessionEditor.editsOf(manifest)))
    }
}
