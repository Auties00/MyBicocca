package it.attendance100.mybicocca.codegen.core

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

/**
 * The changes that bring an output directory in line with a target's generated files.
 *
 * @property writes Files that are missing or whose content differs. A file is never written over
 *   a hand-written one: planning fails instead.
 * @property deletions Files previously generated for the same API (recognised by its
 *   [generatedMarker]) that the target no longer produces. Hand-written files, and files
 *   generated for other APIs sharing the directory, are never touched.
 */
data class SyncPlan(
    val outputDir: Path,
    val writes: List<GeneratedFile>,
    val deletions: List<Path>,
) {
    val isUpToDate: Boolean get() = writes.isEmpty() && deletions.isEmpty()
}

/** Compares and applies generated files against an output directory. */
object OutputSync {
    /** How far into a file the generated marker is looked for. */
    private const val MARKER_SEARCH_CHARS = 512

    /** Plans the changes for the files generated for the API [apiId]. */
    fun plan(outputDir: Path, files: List<GeneratedFile>, apiId: String): SyncPlan {
        val duplicates = files.groupBy { it.path }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "Generated more than one file at: $duplicates" }
        val owner = "${generatedMarker(apiId)}."
        for (file in files) {
            require(owner in file.content.take(MARKER_SEARCH_CHARS)) { "Generated file lacks the generated marker of '$apiId': ${file.path}" }
        }

        val writes = files.filter { file ->
            val target = outputDir.resolve(file.path)
            !target.exists() || target.readText().normalizeNewlines() != file.content
        }
        val handWritten = writes.map { outputDir.resolve(it.path) }.filter { it.exists() && !it.head().contains(GENERATED_MARKER) }
        require(handWritten.isEmpty()) {
            "Generated files would replace hand-written ones; move or delete them first:\n" + handWritten.joinToString("\n") { "  - $it" }
        }

        val produced = files.map { it.path }.toSet()
        val deletions = if (outputDir.exists()) {
            Files.walk(outputDir).use { paths ->
                paths.filter { it.isRegularFile() }
                    .filter { it.relativeTo(outputDir).invariantSeparatorsPathString !in produced }
                    .filter { owner in it.head() }
                    .sorted()
                    .toList()
            }
        } else {
            emptyList()
        }
        return SyncPlan(outputDir, writes, deletions)
    }

    /** The generated files in [outputDir], relative to it, except those of [exceptOwner]. */
    fun generatedPaths(outputDir: Path, exceptOwner: String): Set<String> {
        if (!outputDir.exists()) return emptySet()
        val excluded = "${generatedMarker(exceptOwner)}."
        return Files.walk(outputDir).use { paths ->
            paths.filter { it.isRegularFile() }
                .filter { file -> file.head().let { GENERATED_MARKER in it && excluded !in it } }
                .map { it.relativeTo(outputDir).invariantSeparatorsPathString }
                .toList()
                .toSet()
        }
    }

    fun apply(plan: SyncPlan) {
        for (file in plan.writes) {
            val target = plan.outputDir.resolve(file.path)
            target.parent.createDirectories()
            target.writeText(file.content)
        }
        plan.deletions.forEach { it.deleteExisting() }
    }

    /** The start of the file, where the generated marker is looked for. */
    private fun Path.head(): String =
        Files.newBufferedReader(this).use { reader ->
            val head = CharArray(MARKER_SEARCH_CHARS)
            val read = reader.read(head)
            if (read > 0) String(head, 0, read) else ""
        }

    private fun String.normalizeNewlines(): String = replace("\r\n", "\n")
}
