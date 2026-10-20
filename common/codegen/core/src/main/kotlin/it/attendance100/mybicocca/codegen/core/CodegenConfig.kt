package it.attendance100.mybicocca.codegen.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * Contents of an API's `codegen.json`, which sits next to its spec files:
 *
 * ```json
 * {
 *   "reader": { "type": "esse3", "options": { } },
 *   "targets": {
 *     "kotlin": { "outputDir": "../../../android/data-api/esse3/src/main/kotlin", "options": { } }
 *   }
 * }
 * ```
 *
 * Output directories are relative to the file.
 */
@Serializable
data class CodegenConfig(
    val reader: ReaderConfig,
    val targets: Map<String, TargetConfig>,
) {
    companion object {
        const val FILE_NAME = "codegen.json"

        fun load(file: Path): CodegenConfig = OptionsJson.decodeFromString(serializer(), file.readText())
    }
}

@Serializable
data class ReaderConfig(
    val type: String,
    val options: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class TargetConfig(
    val outputDir: String,
    val options: JsonObject = JsonObject(emptyMap()),
)
