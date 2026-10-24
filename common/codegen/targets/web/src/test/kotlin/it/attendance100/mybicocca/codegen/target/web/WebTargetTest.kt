package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.SHARED_FILES_OWNER
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.generatedMarker
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import it.attendance100.mybicocca.codegen.testing.SampleTests
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebTargetTest {
    private val context = TargetContext(JsonObject(emptyMap())) {}

    @Test
    fun `generates the sample API`() {
        Golden.assertMatches("demo", WebTarget().generate(SampleSpecs.demo, context))
    }

    @Test
    fun `indexes every folder of the APIs' modules but the root`() {
        val paths = setOf("elearning/api/MoodleApi.ts", "elearning/api/KalturaApi.ts", "elearning/models/MoodleCourseTypes.ts", "esse3/api/Esse3Api.ts")
        val indexes = WebTarget().indexFiles(paths, context).associate { it.path to it.content }

        assertEquals(setOf("elearning/index.ts", "elearning/api/index.ts", "elearning/models/index.ts", "esse3/index.ts", "esse3/api/index.ts"), indexes.keys)
        val header = "// ${generatedMarker(SHARED_FILES_OWNER)}. Do not edit.\n\n"
        assertEquals(header + "export * from \"./api/index.js\";\nexport * from \"./models/index.js\";\n", indexes["elearning/index.ts"])
        assertEquals(header + "export * from \"./KalturaApi.js\";\nexport * from \"./MoodleApi.js\";\n", indexes["elearning/api/index.ts"])
    }

    @Test
    fun `generates the live tests of the sample API`() {
        Golden.assertMatches("demo-tests", WebTarget().generateTests(SampleTests.plan, context))
    }

    @Test
    fun `imports the SDK from the live tests through the output directories`() {
        val dirs = TargetContext(JsonObject(emptyMap()), Path("web/data-api/src"), Path("web/data-api/test")) {}
        val runtime = WebTarget().sharedTestFiles(dirs).single { it.path == "runtime/live.ts" }
        assertTrue("from \"../../src/runtime/index.js\"" in runtime.content)
    }

    @Test
    fun `imports files relative to each other`() {
        assertEquals("./B.js", TsFile.relativeModule("a/api/A.ts", "a/api/B.ts"))
        assertEquals("../models/B.js", TsFile.relativeModule("a/api/A.ts", "a/models/B.ts"))
        assertEquals("../../runtime/index.js", TsFile.relativeModule("a/api/A.ts", "runtime/index.ts"))
    }

    @Test
    fun `writes the runtime as its shared files`() {
        val runtime = WebTarget().sharedFiles(context)
        assertTrue(runtime.any { it.path == "runtime/index.ts" })
        assertTrue(runtime.all { it.content.startsWith("// ${generatedMarker(SHARED_FILES_OWNER)}.") })
    }
}
