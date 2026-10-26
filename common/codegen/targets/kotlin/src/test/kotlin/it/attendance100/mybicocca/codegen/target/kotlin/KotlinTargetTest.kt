package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.OptionsJson
import it.attendance100.mybicocca.codegen.core.TargetContext
import it.attendance100.mybicocca.codegen.testing.Golden
import it.attendance100.mybicocca.codegen.testing.SampleSpecs
import it.attendance100.mybicocca.codegen.testing.SampleTests
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinTargetTest {
    private val options = OptionsJson.parseToJsonElement(
        """
        {
          "packageName": "com.example.demo",
          "temporalSerializers": [
            { "kind": "LOCAL_DATE", "pattern": "dd/MM/yyyy", "blankAsNull": true, "nullable": true, "serializer": "DemoDateSerializer" },
            { "kind": "LOCAL_DATE", "pattern": "yyyy-MM-dd", "blankAsNull": false, "nullable": false, "serializer": "DemoIsoDateSerializer" },
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
    fun `writes the sample API's resources`() {
        Golden.assertMatches("demo-resources", KotlinTarget().generateResources(SampleSpecs.demo, SampleSpecs.demoResources, TargetContext(options) {}))
    }

    @Test
    fun `generates the live tests of the sample API`() {
        Golden.assertMatches("demo-tests", KotlinTarget().generateTests(SampleTests.plan, TargetContext(options) {}))
    }

    @Test
    fun `writes the live test runtime as its shared test files`() {
        val runtime = KotlinTarget().sharedTestFiles(TargetContext(options) {})
        assertEquals(setOf("com/example/runtime/live/Live.kt", "com/example/runtime/live/LiveSupport.kt"), runtime.map { it.path }.toSet())
        assertTrue(runtime.all { "package com.example.runtime.live" in it.content && "__" !in it.content })
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
