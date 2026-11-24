package it.unimib.indoornav.lab.ui.map

import android.content.Context
import it.unimib.indoornav.lab.format.CsvCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

/** A taped cross on the floor, in the plan frame. [measured] is false until moved to its laser-measured spot. */
@Serializable
data class Checkpoint(
    val id: String,
    val building: String,
    val floor: String,
    val x: Double,
    val y: Double,
    val description: String = "",
    val temporary: Boolean = false,
    val measured: Boolean = false,
)

/**
 * The checkpoints of the test routes, kept in filesDir/checkpoints.json and mirrored to
 * `checkpoints.csv` (labtools' `id,floor,x,y,description,temporary`) in the survey folder.
 * Until moved on the map they sit at the planned spots of visit 2.
 */
class CheckpointStore(context: Context, private val csvDir: () -> File) {
    private val file = File(context.filesDir, "checkpoints.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _checkpoints = MutableStateFlow(load())
    val checkpoints: StateFlow<List<Checkpoint>> = _checkpoints.asStateFlow()

    private fun load(): List<Checkpoint> {
        val saved = runCatching { json.decodeFromString<List<Checkpoint>>(file.readText()) }.getOrNull().orEmpty()
        val savedIds = saved.map { it.id }.toSet()
        return saved + PLANNED_U14.filter { it.id !in savedIds }
    }

    fun move(id: String, x: Double, y: Double) {
        _checkpoints.value = _checkpoints.value.map { if (it.id == id) it.copy(x = x, y = y, measured = true) else it }
        persist()
    }

    private fun persist() {
        val list = _checkpoints.value
        runCatching {
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(json.encodeToString(list))
            if (!tmp.renameTo(file)) {
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
            val csv = buildString {
                appendLine("id,floor,x,y,description,temporary")
                list.forEach { cp ->
                    appendLine(
                        CsvCodec.formatRow(
                            cp.id,
                            cp.floor,
                            "%.3f".format(Locale.US, cp.x),
                            "%.3f".format(Locale.US, cp.y),
                            if (cp.measured) cp.description else "${cp.description} (planned, not measured)".trim(),
                            if (cp.temporary) 1 else 0,
                        ),
                    )
                }
            }
            File(csvDir().apply { mkdirs() }, "checkpoints.csv").writeText(csv)
        }
    }

    companion object {
        // Visit 2: every 5 m on the corridor centreline (y 7.0) from x 5 to 45, T1 for the U-turn after
        // CP3, the main staircase (ST, floors T/1/2) and the spot outside the lift next to it (LF).
        // CP0-CP8 and T1 were taped exactly at these spots on visit 2, so they count as measured.
        private val PLANNED_U14: List<Checkpoint> =
            (0..8).map { Checkpoint("CP$it", "U14", "1", 5.0 + 5.0 * it, 7.0, "corridor centreline", measured = true) } +
                Checkpoint("T1", "U14", "1", 23.0, 7.0, "U-turn point", temporary = true, measured = true) +
                listOf("T", "1", "2").mapIndexed { i, f -> Checkpoint("ST$i", "U14", f, 64.4, 11.0, "main staircase") } +
                listOf("T", "1", "2").mapIndexed { i, f -> Checkpoint("LF$i", "U14", f, 62.8, 8.3, "outside the lift") }
    }
}
