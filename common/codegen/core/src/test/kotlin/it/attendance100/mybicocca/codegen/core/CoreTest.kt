package it.attendance100.mybicocca.codegen.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodeWriterTest {
    @Test
    fun `indents blocks and nested items, leaving blank lines unindented`() {
        val code = CodeWriter("  ").apply {
            block("a {") {
                line("b")
                line()
                commaList("c(", listOf("d", nested { block("e {") { line("f") } }), ")", trailingComma = true)
            }
        }.toString()
        assertEquals("a {\n  b\n\n  c(\n    d,\n    e {\n      f\n    },\n  )\n}\n", code)
    }
}

class TextTest {
    @Test
    fun `converts identifiers to lowerCamel`() {
        assertEquals("inCorso", Text.lowerCamel("InCorso"))
        assertEquals("authenticatedUser", Text.lowerCamel("AUTHENTICATED_USER"))
        assertEquals("urlValue", Text.lowerCamel("URLValue"))
        assertEquals("a", Text.lowerCamel("A"))
    }
}

class OutputSyncTest {
    private fun generated(path: String, body: String) = GeneratedFile(path, "// $GENERATED_MARKER\n$body\n")

    @Test
    fun `writes changed files and deletes only stale generated ones`(@TempDir dir: Path) {
        dir.resolve("pkg").createDirectories()
        dir.resolve("pkg/Same.kt").writeText(generated("pkg/Same.kt", "same").content.replace("\n", "\r\n"))
        dir.resolve("pkg/Stale.kt").writeText(generated("pkg/Stale.kt", "old").content)
        dir.resolve("pkg/HandWritten.kt").writeText("class HandWritten\n")
        val files = listOf(generated("pkg/Same.kt", "same"), generated("pkg/New.kt", "new"))

        val plan = OutputSync.plan(dir, files)
        assertEquals(listOf("pkg/New.kt"), plan.writes.map { it.path })
        assertEquals(listOf(dir.resolve("pkg/Stale.kt")), plan.deletions)

        OutputSync.apply(plan)
        assertTrue(dir.resolve("pkg/New.kt").readText().contains("new"))
        assertFalse(dir.resolve("pkg/Stale.kt").exists())
        assertTrue(dir.resolve("pkg/HandWritten.kt").exists())
        assertTrue(OutputSync.plan(dir, files).isUpToDate)
    }

    @Test
    fun `refuses to replace a hand-written file`(@TempDir dir: Path) {
        dir.resolve("Api.kt").writeText("class Api\n")
        val error = assertThrows<IllegalArgumentException> { OutputSync.plan(dir, listOf(generated("Api.kt", "class Api"))) }
        assertContains(error.message.orEmpty(), "hand-written")
    }
}
