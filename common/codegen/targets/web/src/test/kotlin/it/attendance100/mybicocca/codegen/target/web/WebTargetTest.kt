package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test

class WebTargetTest {
    @Test
    fun `generates the sample API`() {
        Golden.assertMatches("demo", WebTarget().generate(SampleSpecs.demo, TargetContext(JsonObject(emptyMap())) {}))
    }
}
