package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.SHARED_FILES_OWNER
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.core.generatedMarker
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import it.attendance100.mybicocca.codegen.testing.SampleTests
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwiftTargetTest {
    private val context = TargetContext(JsonObject(emptyMap())) {}

    @Test
    fun `generates the sample API`() {
        Golden.assertMatches("demo", SwiftTarget().generate(SampleSpecs.demo, context))
    }

    @Test
    fun `generates the live tests of the sample API`() {
        Golden.assertMatches("demo-tests", SwiftTarget().generateTests(SampleTests.plan, context))
    }

    @Test
    fun `writes the live test runtime as its shared test files`() {
        assertEquals(setOf("Runtime/Live.swift", "Runtime/LiveSupport.swift"), SwiftTarget().sharedTestFiles(context).map { it.path }.toSet())
    }

    @Test
    fun `writes the runtime module as its shared files`() {
        val runtime = SwiftTarget().sharedFiles(context)
        assertTrue(runtime.any { it.path == "DataApiRuntime/RestClient.swift" })
        assertTrue(runtime.all { it.content.startsWith("// ${generatedMarker(SHARED_FILES_OWNER)}.") })
    }
}
