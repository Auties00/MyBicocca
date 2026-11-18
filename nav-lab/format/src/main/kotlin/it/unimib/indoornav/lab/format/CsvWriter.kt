package it.unimib.indoornav.lab.format

enum class StreamSpec(
    val streamKey: String,
    val fileName: String,
    val columns: List<String>,
    val timeBaseExempt: Boolean = false,
) {
    ACC(
        streamKey = "acc",
        fileName = "acc.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z"),
    ),
    ACC_RAW(
        streamKey = "acc_raw",
        fileName = "acc_raw.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z", "bx", "by", "bz"),
    ),
    GYRO(
        streamKey = "gyro",
        fileName = "gyro.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z"),
    ),
    GYRO_RAW(
        streamKey = "gyro_raw",
        fileName = "gyro_raw.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z", "bx", "by", "bz"),
    ),
    MAG(
        streamKey = "mag",
        fileName = "mag.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z"),
    ),
    MAG_RAW(
        streamKey = "mag_raw",
        fileName = "mag_raw.csv",
        columns = listOf("t_ns", "rx_ns", "x", "y", "z", "bx", "by", "bz"),
    ),
    BARO(
        streamKey = "baro",
        fileName = "baro.csv",
        columns = listOf("t_ns", "rx_ns", "hpa"),
    ),
    GAME_ROT(
        streamKey = "game_rot",
        fileName = "game_rot.csv",
        columns = listOf("t_ns", "rx_ns", "qx", "qy", "qz", "qw"),
    ),
    ROT(
        streamKey = "rot",
        fileName = "rot.csv",
        columns = listOf("t_ns", "rx_ns", "qx", "qy", "qz", "qw", "heading_acc_rad"),
    ),
    STEPS(
        streamKey = "steps",
        fileName = "steps.csv",
        columns = listOf("t_ns", "rx_ns", "kind", "value"),
        timeBaseExempt = true,
    ),
    SENSOR_ACCURACY(
        streamKey = "sensor_accuracy",
        fileName = "sensor_accuracy.csv",
        columns = listOf("rx_ns", "sensor", "accuracy"),
        timeBaseExempt = true,
    ),
    BLE(
        streamKey = "ble",
        fileName = "ble.csv",
        columns = listOf(
            "t_ns",
            "rx_ns",
            "addr",
            "rssi",
            "frame",
            "beacon_id",
            "tx_power_adv",
            "tlm_batt_mv",
            "tlm_temp_c",
            "adv_hex",
        ),
    ),
    WIFI(
        streamKey = "wifi",
        fileName = "wifi.csv",
        columns = listOf(
            "t_ns",
            "rx_ns",
            "scan_id",
            "bssid",
            "ssid",
            "rssi",
            "freq_mhz",
            "channel_width",
            "la_bit",
            "rtt_responder",
        ),
        timeBaseExempt = true,
    ),
    RTT(
        streamKey = "rtt",
        fileName = "rtt.csv",
        columns = listOf(
            "t_ns",
            "rx_ns",
            "bssid",
            "status",
            "dist_mm",
            "std_mm",
            "rssi",
            "attempted",
            "successful",
        ),
        timeBaseExempt = true,
    ),
    EVENTS(
        streamKey = "events",
        fileName = "events.csv",
        columns = listOf("rx_ns", "kind", "detail"),
        timeBaseExempt = true,
    ),
    MARKS(
        streamKey = "marks",
        fileName = "marks.csv",
        columns = listOf("t_ns", "rx_ns", "kind", "label", "source"),
        timeBaseExempt = true,
    ),
    GNSS(
        streamKey = "gnss",
        fileName = "gnss.csv",
        columns = listOf("t_ns", "rx_ns", "lat", "lon", "acc_m", "alt", "provider"),
        timeBaseExempt = true,
    );

    val headerLine: String
        get() = columns.joinToString(",")

    companion object {
        fun byFileName(fileName: String): StreamSpec? = entries.firstOrNull { it.fileName == fileName }
        fun byKey(key: String): StreamSpec? = entries.firstOrNull { it.streamKey == key }
    }
}

object CsvCodec {
    fun formatRow(fields: List<Any?>): String {
        val sb = StringBuilder()
        fields.forEachIndexed { index, value ->
            if (index > 0) sb.append(',')
            val text = value?.toString() ?: ""
            sb.append(escapeField(text))
        }
        return sb.toString()
    }

    fun formatRow(vararg fields: Any?): String = formatRow(fields.asList())

    fun escapeField(text: String): String {
        val needsQuotes = text.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuotes) return text
        val sb = StringBuilder(text.length + 4)
        sb.append('"')
        for (ch in text) {
            if (ch == '"') sb.append("\"\"") else sb.append(ch)
        }
        sb.append('"')
        return sb.toString()
    }

    fun parseLine(line: String): List<String>? {
        val result = ArrayList<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    current.append(ch)
                }
            } else {
                when (ch) {
                    ',' -> {
                        result.add(current.toString())
                        current.setLength(0)
                    }
                    '"' -> {
                        if (current.isNotEmpty()) return null
                        inQuotes = true
                    }
                    else -> current.append(ch)
                }
            }
            i++
        }
        if (inQuotes) return null
        result.add(current.toString())
        return result
    }
}

object SurveyCsvHeaders {
    val BEACON_INVENTORY = listOf(
        "timestamp_ms",
        "location_note",
        "frame",
        "beacon_id",
        "project_label",
        "addr",
        "rssi",
        "median_rssi",
        "pps",
        "tx_power_adv",
        "tlm_batt_mv",
        "tlm_temp_c",
    )

    val WIFI_SURVEY = listOf(
        "timestamp_ms",
        "checkpoint",
        "bssid",
        "ssid",
        "rssi",
        "freq_mhz",
        "channel_width",
        "la_bit",
        "rtt_responder",
        "connected",
    )

    val CONNECTED_AP = listOf(
        "timestamp_ms",
        "elapsed_ns",
        "bssid",
        "ssid",
        "rssi",
        "freq_mhz",
    )
}
