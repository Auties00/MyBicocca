package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.OptionsJson
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains

class KotlinTargetTest {
    private val options = OptionsJson.parseToJsonElement(
        """
        {
          "packageName": "com.example.demo",
          "temporalSerializers": [
            { "kind": "LOCAL_DATE", "pattern": "dd/MM/yyyy", "blankAsNull": true, "nullable": true, "serializer": "DemoDateSerializer" },
            { "kind": "LOCAL_DATE_TIME", "pattern": "yyyy-MM-dd'T'HH:mm:ss", "blankAsNull": false, "nullable": true, "serializer": "com.example.runtime.IsoDateTimeSerializer" },
            { "kind": "LOCAL_TIME", "pattern": "HH:mm", "blankAsNull": false, "nullable": true, "serializer": "DemoTimeSerializer" }
          ]
        }
        """
    ).jsonObject

    @Test
    fun `generates the sample API`() {
        Golden.assertMatches("demo", KotlinTarget().generate(SampleSpecs.demo, TargetContext(options) {}))
    }

    @Test
    fun `fails when a date format has no configured serializer`() {
        val withoutSerializers = JsonObject(mapOf("packageName" to options.getValue("packageName")))
        val error = assertThrows<IllegalStateException> {
            KotlinTarget().generate(SampleSpecs.demo, TargetContext(withoutSerializers) {})
        }
        assertContains(error.message.orEmpty(), "temporalSerializers")
    }
}
