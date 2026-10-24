package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
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
    private fun generated(path: String, body: String, apiId: String = "demo") = GeneratedFile(path, "// ${generatedMarker(apiId)}. Do not edit.\n$body\n")

    @Test
    fun `writes changed files and deletes only stale generated ones`(@TempDir dir: Path) {
        dir.resolve("pkg").createDirectories()
        dir.resolve("pkg/Same.kt").writeText(generated("pkg/Same.kt", "same").content.replace("\n", "\r\n"))
        dir.resolve("pkg/Stale.kt").writeText(generated("pkg/Stale.kt", "old").content)
        dir.resolve("pkg/HandWritten.kt").writeText("class HandWritten\n")
        val files = listOf(generated("pkg/Same.kt", "same"), generated("pkg/New.kt", "new"))

        val plan = OutputSync.plan(dir, files, "demo")
        assertEquals(listOf("pkg/New.kt"), plan.writes.map { it.path })
        assertEquals(listOf(dir.resolve("pkg/Stale.kt")), plan.deletions)

        OutputSync.apply(plan)
        assertTrue(dir.resolve("pkg/New.kt").readText().contains("new"))
        assertFalse(dir.resolve("pkg/Stale.kt").exists())
        assertTrue(dir.resolve("pkg/HandWritten.kt").exists())
        assertTrue(OutputSync.plan(dir, files, "demo").isUpToDate)
    }

    @Test
    fun `refuses to replace a hand-written file`(@TempDir dir: Path) {
        dir.resolve("Api.kt").writeText("class Api\n")
        val error = assertThrows<IllegalArgumentException> { OutputSync.plan(dir, listOf(generated("Api.kt", "class Api")), "demo") }
        assertContains(error.message.orEmpty(), "hand-written")
    }

    @Test
    fun `leaves the files of other APIs sharing the directory`(@TempDir dir: Path) {
        dir.resolve("DemoPlanningApi.kt").writeText(generated("DemoPlanningApi.kt", "class DemoPlanningApi", apiId = "demo-planning").content)
        val plan = OutputSync.plan(dir, listOf(generated("DemoApi.kt", "class DemoApi")), "demo")
        assertTrue(plan.deletions.isEmpty())
    }
}

class GeneratorTest {
    private fun file(path: String, owner: String, body: String = "") = GeneratedFile(path, "// ${generatedMarker(owner)}. Do not edit.\n$body\n")

    /** Reads an API named after its directory, and generates one file plus a shared one into `out`. */
    private fun registry(sharedBody: (apiId: String) -> String = { "" }) = object : Plugins {
        override fun reader(id: String) = object : SpecReader {
            override val id = "fake"
            override fun read(context: ReaderContext) = ApiSpec(id = context.specDir.fileName.toString(), prefix = "Fake", modules = emptyList(), enums = emptyList())
        }

        override fun target(id: String) = object : CodeTarget {
            override val id = "fake"
            override fun generate(spec: ApiSpec, context: TargetContext) = listOf(file("${spec.id}.txt", spec.id))
            override fun sharedFiles(context: TargetContext) =
                listOf(file("Runtime.txt", SHARED_FILES_OWNER, sharedBody(context.options.getValue("api").toString())))
        }
    }

    private fun api(root: Path, id: String): Path = root.resolve(id).apply {
        createDirectories()
        resolve(CodegenConfig.FILE_NAME).writeText(
            """{"reader": {"type": "fake"}, "targets": {"fake": {"outputDir": "../out", "options": {"api": "$id"}}}}"""
        )
    }

    @Test
    fun `writes the shared files once per directory, and no API's run deletes them`(@TempDir root: Path) {
        val apis = listOf(api(root, "a"), api(root, "b"))
        Generator(registry(), log = {}).run(apis)
        val out = root.resolve("out")
        assertTrue(out.resolve("Runtime.txt").exists())

        // Regenerating one API keeps both the other API's files and the shared ones.
        val plans = Generator(registry(), log = {}).run(listOf(apis[0]))
        assertTrue(plans.all(SyncPlan::isUpToDate))
        assertTrue(out.resolve("b.txt").exists())
    }

    @Test
    fun `refuses APIs that disagree on a shared file`(@TempDir root: Path) {
        val error = assertThrows<IllegalArgumentException> {
            Generator(registry(sharedBody = { it }), log = {}).run(listOf(api(root, "a"), api(root, "b")))
        }
        assertContains(error.message.orEmpty(), "Runtime.txt")
    }
}

class CodegenConfigTest {
    @Test
    fun `reads one reader or several`(@TempDir dir: Path) {
        val single = dir.resolve("single.json").apply { writeText("""{"reader": {"type": "openapi"}, "targets": {}}""") }
        assertEquals(listOf("openapi"), CodegenConfig.load(single).specReaders.map { it.type })
        val several = dir.resolve("several.json").apply {
            writeText("""{"readers": [{"type": "esse3"}, {"type": "openapi"}], "targets": {}}""")
        }
        assertEquals(listOf("esse3", "openapi"), CodegenConfig.load(several).specReaders.map { it.type })
    }

    @Test
    fun `needs exactly one of reader and readers`(@TempDir dir: Path) {
        val neither = dir.resolve("neither.json").apply { writeText("""{"targets": {}}""") }
        assertThrows<IllegalArgumentException> { CodegenConfig.load(neither) }
    }
}
