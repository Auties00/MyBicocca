package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test

class SwiftTargetTest {
    @Test
    fun `generates the sample API`() {
        Golden.assertMatches("demo", SwiftTarget().generate(SampleSpecs.demo, TargetContext(JsonObject(emptyMap())) {}))
    }
}
