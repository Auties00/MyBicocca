package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path
import java.util.ServiceLoader

/**
 * Turns an API's own spec format into an [ApiSpec].
 *
 * Implementations are discovered with [ServiceLoader]: a reader module lists its class in
 * `META-INF/services/it.attendance100.mybicocca.codegen.core.SpecReader`.
 */
interface SpecReader {
    /** Identifier referenced by the `reader.type` key of an API's `codegen.json`. */
    val id: String

    fun read(context: ReaderContext): ApiSpec
}

/**
 * @property specDir Directory holding the API's spec files and `codegen.json`.
 * @property options The reader's `options` object from `codegen.json`; see [decodeOptions].
 * @property mayUpdateSpecFiles Whether the reader may rewrite maintained spec files, such as a
 *   glossary it adds newly seen names to. False when only checking that output is current.
 */
class ReaderContext(
    val specDir: Path,
    val options: JsonObject,
    val mayUpdateSpecFiles: Boolean,
    val log: (String) -> Unit,
)

/**
 * Generates an SDK for one platform from an [ApiSpec].
 *
 * Implementations are discovered with [ServiceLoader]: a target module lists its class in
 * `META-INF/services/it.attendance100.mybicocca.codegen.core.CodeTarget`. Adding a platform
 * therefore means adding a module, without changing readers, the model or other targets.
 */
interface CodeTarget {
    /** Identifier used as a key of the `targets` object in an API's `codegen.json`. */
    val id: String

    /**
     * Returns the files of the SDK, with paths relative to the target's output directory.
     * Each file must contain the [generatedMarker] of the API in its first lines, so that files
     * a later run no longer produces can be recognised and removed.
     */
    fun generate(spec: ApiSpec, context: TargetContext): List<GeneratedFile>

    /**
     * Returns the files every API this target generates into the same output directory builds
     * on, such as a runtime that isn't published as a library of its own. They are generated once
     * per output directory, however many APIs share it, and must contain the [generatedMarker] of
     * [SHARED_FILES_OWNER], so that no API's run removes them.
     *
     * @param context The options of one of the APIs generated into the directory; every API
     *   sharing it must lead to the same files.
     */
    fun sharedFiles(context: TargetContext): List<GeneratedFile> = emptyList()

    /**
     * Returns files derived from the files the APIs generated into an output directory, such as
     * the index modules re-exporting each folder. They are written and owned like the
     * [sharedFiles], once every API of the run is generated, and also cover the files of APIs
     * the run skipped, which are already on disk.
     *
     * @param paths The APIs' generated files in the directory once the run is applied, relative to
     *   it; shared files are left out.
     */
    fun indexFiles(paths: Set<String>, context: TargetContext): List<GeneratedFile> = emptyList()

    /**
     * Returns the live tests of an API, from the [TestPlan] worked out from its `tests.yaml`, with
     * paths relative to the target's test output directory. Like [generate]'s, each file must
     * contain the [generatedMarker] of the API.
     */
    fun generateTests(plan: TestPlan, context: TargetContext): List<GeneratedFile> = emptyList()

    /**
     * Returns the files the live tests of every API in a test output directory build on, such as
     * the runtime that runs the cases: like [sharedFiles], for the tests.
     */
    fun sharedTestFiles(context: TargetContext): List<GeneratedFile> = emptyList()
}

/**
 * Shared files copied from the resource directory [directory] of [anchor]'s module, such as a
 * hand-written runtime: each of [names] lands under [outputPath], after [transform] and a line
 * comment carrying the [generatedMarker] of [SHARED_FILES_OWNER].
 */
fun resourceFiles(
    anchor: Class<*>,
    directory: String,
    names: List<String>,
    outputPath: String,
    transform: (String) -> String = { it },
): List<GeneratedFile> = names.map { name ->
    val source = anchor.getResource("/$directory/$name")?.readText()?.replace("\r\n", "\n")
        ?: error("Missing resource $directory/$name")
    GeneratedFile("$outputPath/$name", "// ${generatedMarker(SHARED_FILES_OWNER)}. Do not edit.\n" + transform(source))
}

/** The owner named by the [generatedMarker] of [CodeTarget.sharedFiles]; no API has this id. */
const val SHARED_FILES_OWNER = "shared files"

/**
 * @property options The target's `options` object from `codegen.json`; see [decodeOptions].
 * @property outputDir Where the target writes the SDK, when known.
 * @property testOutputDir Where the target writes the live tests, when the API has some.
 */
class TargetContext(
    val options: JsonObject,
    val outputDir: Path? = null,
    val testOutputDir: Path? = null,
    val log: (String) -> Unit,
)

/**
 * @property path Path relative to the target's output directory, with `/` separators.
 */
data class GeneratedFile(val path: String, val content: String) {
    init {
        require(!path.startsWith("/") && ".." !in path.split('/')) { "Generated path must stay inside the output directory: $path" }
    }
}

/** Text every generated file carries in its header comment. */
const val GENERATED_MARKER = "Generated by MyBicocca codegen"

/**
 * The text marking a file generated for the API [apiId], followed in the file by a period: it
 * names the API so that APIs sharing an output directory only ever delete their own files.
 */
fun generatedMarker(apiId: String): String = "$GENERATED_MARKER for $apiId"

/** The readers and targets a [Generator] runs, by id. */
interface Plugins {
    fun reader(id: String): SpecReader

    fun target(id: String): CodeTarget
}

/** Finds the available readers and targets. */
object PluginRegistry : Plugins {
    val readers: Map<String, SpecReader> by lazy { load<SpecReader>().associateByUniqueId { it.id } }
    val targets: Map<String, CodeTarget> by lazy { load<CodeTarget>().associateByUniqueId { it.id } }

    override fun reader(id: String): SpecReader =
        readers[id] ?: error("Unknown reader '$id'. Available: ${readers.keys.sorted()}")

    override fun target(id: String): CodeTarget =
        targets[id] ?: error("Unknown target '$id'. Available: ${targets.keys.sorted()}")

    private inline fun <reified T> load(): List<T> =
        ServiceLoader.load(T::class.java, T::class.java.classLoader).toList()

    private fun <T> List<T>.associateByUniqueId(id: (T) -> String): Map<String, T> {
        val duplicates = groupBy(id).filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "Plugin ids registered more than once: $duplicates" }
        return associateBy(id)
    }
}
