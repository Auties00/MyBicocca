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
 * @property writes Files that are missing or whose content differs.
 * @property deletions Previously generated files (recognised by [GENERATED_MARKER]) that the
 *   target no longer produces. Hand-written files in the same directory are never touched.
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

    fun plan(outputDir: Path, files: List<GeneratedFile>): SyncPlan {
        val duplicates = files.groupBy { it.path }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "Generated more than one file at: $duplicates" }
        for (file in files) {
            require(GENERATED_MARKER in file.content.take(MARKER_SEARCH_CHARS)) { "Generated file lacks the generated marker: ${file.path}" }
        }

        val writes = files.filter { file ->
            val target = outputDir.resolve(file.path)
            !target.exists() || target.readText().normalizeNewlines() != file.content
        }

        val produced = files.map { it.path }.toSet()
        val deletions = if (outputDir.exists()) {
            Files.walk(outputDir).use { paths ->
                paths.filter { it.isRegularFile() }
                    .filter { it.relativeTo(outputDir).invariantSeparatorsPathString !in produced }
                    .filter { it.isGenerated() }
                    .sorted()
                    .toList()
            }
        } else {
            emptyList()
        }
        return SyncPlan(outputDir, writes, deletions)
    }

    fun apply(plan: SyncPlan) {
        for (file in plan.writes) {
            val target = plan.outputDir.resolve(file.path)
            target.parent.createDirectories()
            target.writeText(file.content)
        }
        plan.deletions.forEach { it.deleteExisting() }
    }

    private fun Path.isGenerated(): Boolean =
        Files.newBufferedReader(this).use { reader ->
            val head = CharArray(MARKER_SEARCH_CHARS)
            val read = reader.read(head)
            read > 0 && GENERATED_MARKER in String(head, 0, read)
        }

    private fun String.normalizeNewlines(): String = replace("\r\n", "\n")
}
