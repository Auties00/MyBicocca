package it.unimib.indoornav.lab.format

data class TemplateFieldDef(
    val key: String,
    val label: String,
    val defaultValue: String = "",
    val suggestions: List<String> = emptyList(),
    val afterWalk: Boolean = false,
)

private val WALK_RECORDING_MODES = listOf("guided", "follower", "marker_only")
private val STAND_RECORDING_MODES = listOf("free")

enum class ExperimentTemplate(
    val code: String,
    val title: String,
    val defaultCarryMode: String,
    val defaultRecordingMode: String,
    val allowedRecordingModes: List<String>,
    val fields: List<TemplateFieldDef>,
) {
    E2(
        code = "E2",
        title = "E2 - Beacon RSSI vs Distance",
        defaultCarryMode = "stand",
        defaultRecordingMode = "free",
        allowedRecordingModes = STAND_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef(
                "d",
                "Distance (m)",
                "8",
                listOf("0.5", "1", "2", "3", "5", "8", "12", "16", "20", "30"),
            ),
            TemplateFieldDef(
                "mode",
                "Mode",
                "stand",
                listOf("stand", "hand-up", "pocket", "body", "crowd"),
            ),
            TemplateFieldDef("beacon", "Beacon", "B01", listOf("B01", "B02", "B03", "B04")),
        ),
    ),
    E3(
        code = "E3",
        title = "E3 - Phone-to-Phone Differences",
        defaultCarryMode = "stand",
        defaultRecordingMode = "free",
        allowedRecordingModes = STAND_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("orient", "Orientation (deg)", "0", listOf("0", "90")),
        ),
    ),
    E4(
        code = "E4",
        title = "E4 - Beacon Interval & Power",
        defaultCarryMode = "stand",
        defaultRecordingMode = "free",
        allowedRecordingModes = STAND_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("d", "Distance (m)", "5", listOf("5", "12")),
            TemplateFieldDef(
                "setting",
                "Setting",
                "int100-pwr0-both",
                listOf("int100-pwr0-both", "int300-pwr0-both", "int1000-pwr0-both", "int300-pwr0-ibeacon"),
            ),
            // Several beacons are usually in range; labtools analyses the one named here.
            TemplateFieldDef("beacon", "Beacon", "B01", listOf("B01", "B02", "B03", "B04")),
        ),
    ),
    E4B(
        code = "E4b",
        title = "E4b - Screen Off & Background",
        defaultCarryMode = "stand",
        defaultRecordingMode = "free",
        allowedRecordingModes = STAND_RECORDING_MODES,
        fields = emptyList(),
    ),
    E5(
        code = "E5",
        title = "E5 - Corridor Walk",
        defaultCarryMode = "hand-low",
        defaultRecordingMode = "guided",
        allowedRecordingModes = WALK_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("route", "Route", "R1", listOf("R1", "R1r", "R1u")),
            TemplateFieldDef("dir", "Direction", "out", listOf("out", "back")),
            TemplateFieldDef("pace", "Pace", "normal", listOf("slow", "normal", "fast")),
            TemplateFieldDef(
                "mode",
                "Mode",
                "hand-low",
                listOf("pocket", "hand-low", "hand-up", "swing", "bag"),
            ),
            TemplateFieldDef("layout", "Layout", "1", listOf("1", "2", "3")),
        ),
    ),
    E6(
        code = "E6",
        title = "E6 - Steps & Heading",
        defaultCarryMode = "hand-low",
        defaultRecordingMode = "guided",
        allowedRecordingModes = WALK_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("route", "Route", "R1", listOf("R1", "R1r", "R1u")),
            TemplateFieldDef("dir", "Direction", "out", listOf("out", "back")),
            TemplateFieldDef("pace", "Pace", "normal", listOf("slow", "normal", "fast")),
            TemplateFieldDef(
                "mode",
                "Mode",
                "hand-low",
                listOf("hand-low", "pocket", "hand-up", "swing", "bag"),
            ),
            TemplateFieldDef("walker", "Walker", ""),
            TemplateFieldDef("steps", "Steps counted", "", afterWalk = true),
        ),
    ),
    E8(
        code = "E8",
        title = "E8 - Barometer (Still, Stairs & Lift)",
        defaultCarryMode = "stairs",
        defaultRecordingMode = "guided",
        allowedRecordingModes = WALK_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("route", "Route", "S1", listOf("still", "S1", "S1r", "L1", "L1r")),
            TemplateFieldDef("mode", "Mode", "stairs", listOf("stairs", "lift", "still")),
        ),
    ),
    E9(
        code = "E9",
        title = "E9 - Magnetic Repeat Walks",
        defaultCarryMode = "hand-low",
        defaultRecordingMode = "guided",
        allowedRecordingModes = WALK_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("route", "Route", "R1", listOf("R1", "R1r", "R1L", "R1R")),
            TemplateFieldDef("dir", "Direction", "out", listOf("out", "back")),
            TemplateFieldDef("offset", "Offset (m)", "0", listOf("-1", "0", "1")),
        ),
    ),
    E10(
        code = "E10",
        title = "E10 - Wi-Fi Fingerprints & RTT",
        defaultCarryMode = "stand",
        defaultRecordingMode = "free",
        allowedRecordingModes = STAND_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef(
                "cp",
                "Checkpoint",
                "CP0",
                listOf("CP0", "CP1", "CP2", "CP3", "CP4", "CP5", "CP6", "CP7", "CP8", "rtt"),
            ),
            TemplateFieldDef("face", "Facing", "pos", listOf("pos", "neg")),
            TemplateFieldDef("d", "Distance (m)", "", listOf("2", "5", "10", "15")),
        ),
    ),
    E11(
        code = "E11",
        title = "E11 - Busy Hour Corridor Walk",
        defaultCarryMode = "hand-low",
        defaultRecordingMode = "guided",
        allowedRecordingModes = WALK_RECORDING_MODES,
        fields = listOf(
            TemplateFieldDef("route", "Route", "R1", listOf("R1", "R1r", "R1u")),
            TemplateFieldDef("dir", "Direction", "out", listOf("out", "back")),
            TemplateFieldDef("pace", "Pace", "normal", listOf("slow", "normal", "fast")),
            TemplateFieldDef(
                "mode",
                "Mode",
                "hand-low",
                listOf("pocket", "hand-low", "hand-up", "swing", "bag"),
            ),
            TemplateFieldDef("layout", "Layout", "1", listOf("1", "2", "3")),
        ),
    );

    fun hasModeField(): Boolean = fields.any { it.key == "mode" }

    companion object {
        val CARRY_MODES = listOf("stand", "hand-up", "hand-low", "swing", "pocket", "bag")
        val RECORDING_MODES = listOf("guided", "follower", "marker_only", "free")
        val STAND_TEMPLATE_CODES = setOf("E2", "E3", "E4", "E4b", "E10")

        fun fromCode(code: String): ExperimentTemplate? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

object RouteCheckpoints {
    private val KNOWN_ROUTES: Map<String, List<String>> = mapOf(
        "R1" to (0..8).map { "CP$it" },
        "R1r" to (8 downTo 0).map { "CP$it" },
        "R1u" to listOf("CP0", "CP1", "CP2", "CP3", "T1", "CP3", "CP2", "CP1", "CP0"),
        "R1L" to listOf("CP0L", "CP4L", "CP8L"),
        "R1Lr" to listOf("CP8L", "CP4L", "CP0L"),
        "R1R" to listOf("CP0R", "CP4R", "CP8R"),
        "R1Rr" to listOf("CP8R", "CP4R", "CP0R"),
        "S1" to listOf("ST0", "ST1", "ST2"),
        "S1r" to listOf("ST2", "ST1", "ST0"),
        "L1" to listOf("LF0", "LF1", "LF2"),
        "L1r" to listOf("LF2", "LF1", "LF0"),
    )

    fun sequenceFor(route: String?): List<String> {
        val trimmed = route?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.equals("still", ignoreCase = true)) {
            return emptyList()
        }
        KNOWN_ROUTES[trimmed]?.let { return it }
        KNOWN_ROUTES.entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.let { return it.value }
        return emptyList()
    }

    /** True once every checkpoint of a known route has been marked; unknown routes never complete. */
    fun isComplete(route: String?, index: Int): Boolean {
        val seq = sequenceFor(route)
        return seq.isNotEmpty() && index >= seq.size
    }

    fun labelAt(route: String?, index: Int): String {
        val safeIndex = index.coerceAtLeast(0)
        val seq = sequenceFor(route)
        return if (safeIndex < seq.size) seq[safeIndex] else "CP$safeIndex"
    }
}

object TestIdCodec {
    private val KEY_REGEX = Regex("^[a-z_]+$")
    private val SPLIT_REGEX = Regex("-(?=[a-z0-9_]+=)")
    private val PREFIX_REGEX = Regex("^E\\d+[a-z]?$")

    fun sanitizeValue(raw: String): String {
        return raw.trim()
            .replace("=", "_")
            .replace(":", "_")
            .replace("/", "_")
            .replace("\\", "_")
            .replace(" ", "_")
    }

    fun build(templateCode: String, orderedFields: List<Pair<String, String>>): String {
        val sb = StringBuilder(templateCode.trim())
        for ((rawKey, rawVal) in orderedFields) {
            val key = rawKey.trim().lowercase()
            val value = sanitizeValue(rawVal)
            if (value.isEmpty() || !KEY_REGEX.matches(key)) continue
            sb.append('-').append(key).append('=').append(value)
        }
        return sb.toString()
    }

    fun build(template: ExperimentTemplate, fieldValues: Map<String, String>): String {
        val ordered = template.fields.map { it.key to (fieldValues[it.key] ?: "") }
        return build(template.code, ordered)
    }

    data class ParsedTestId(
        val prefix: String,
        val fields: Map<String, String>,
    )

    fun parse(testId: String): ParsedTestId? {
        val trimmed = testId.trim()
        if (trimmed.isEmpty()) return null
        val parts = trimmed.split(SPLIT_REGEX)
        val prefix = parts.firstOrNull() ?: return null
        if (!PREFIX_REGEX.matches(prefix)) return null
        val map = LinkedHashMap<String, String>()
        for (i in 1 until parts.size) {
            val eqIdx = parts[i].indexOf('=')
            if (eqIdx <= 0) continue
            val k = parts[i].substring(0, eqIdx)
            val v = parts[i].substring(eqIdx + 1)
            map[k] = v
        }
        return ParsedTestId(prefix, map)
    }
}
