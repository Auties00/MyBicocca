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
 * An API described by several specs, such as a REST API and the web pages of the same site, lists
 * a reader for each under `readers` instead; their parts are merged into one API (see
 * [it.attendance100.mybicocca.codegen.model.ApiSpec.merge]).
 *
 * Output directories are relative to the file.
 */
@Serializable
data class CodegenConfig(
    val reader: ReaderConfig? = null,
    val readers: List<ReaderConfig> = emptyList(),
    val targets: Map<String, TargetConfig>,
) {
    init {
        require((reader == null) != readers.isEmpty()) { "Set either reader or readers in ${FILE_NAME}" }
    }

    /** The readers of the API's specs, in order. */
    val specReaders: List<ReaderConfig> get() = readers.ifEmpty { listOfNotNull(reader) }

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

/**
 * @property testOutputDir Where the target writes the API's live tests, relative to the file;
 *   none are written without it, or when the API has no `tests.yaml`.
 * @property resourceOutputDir Where the target writes the API's resources, relative to the file;
 *   the [outputDir] when not set.
 */
@Serializable
data class TargetConfig(
    val outputDir: String,
    val options: JsonObject = JsonObject(emptyMap()),
    val testOutputDir: String? = null,
    val resourceOutputDir: String? = null,
)
