package it.attendance100.mybicocca.codegen.testing

import it.attendance100.mybicocca.codegen.core.GeneratedFile
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

/**
 * Compares generated files with the golden copies in a module's `src/test/golden/<name>`.
 *
 * After an intended change to a target's output, regenerate the golden copies with
 * `gradlew test -PupdateGolden=true` and review the diff.
 */
object Golden {
    private val update = System.getProperty("codegen.updateGolden") == "true"

    fun assertMatches(name: String, files: List<GeneratedFile>) {
        val directory = Path("src/test/golden").resolve(name)
        if (update) {
            write(directory, files)
            return
        }
        val expected = read(directory)
        assertEquals(expected.keys, files.map { it.path }.toSet(), "Generated file set differs from $directory")
        for (file in files) {
            assertEquals(expected.getValue(file.path), file.content, "${file.path} differs from its golden copy in $directory")
        }
    }

    private fun write(directory: Path, files: List<GeneratedFile>) {
        directory.toFile().deleteRecursively()
        for (file in files) {
            val target = directory.resolve(file.path)
            target.parent.createDirectories()
            target.writeText(file.content)
        }
    }

    private fun read(directory: Path): Map<String, String> {
        require(directory.exists()) { "No golden files in $directory; run the tests with -PupdateGolden=true" }
        val files = Files.walk(directory).use { paths -> paths.filter { it.isRegularFile() }.toList() }
        return files.associate { it.relativeTo(directory).invariantSeparatorsPathString to it.readText().replace("\r\n", "\n") }
    }
}
