package it.attendance100.mybicocca.codegen.reader.moodle

import it.attendance100.mybicocca.codegen.core.EffectsOverlay
import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.SpecValidator
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoodleReaderTest {
    private val spec: ApiSpec = MoodleReader().read(
        ReaderContext(Path("src/test/resources/demo"), JsonObject(emptyMap()), mayUpdateSpecFiles = false) {}
    )

    private val records get() = spec.modules.flatMap { it.records }.associateBy { it.name }

    @Test
    fun `reads a valid spec with a module per component`() {
        // Write functions are classified by the effects overlay.
        val classified = EffectsOverlay.apply(spec, Path("src/test/resources/demo"))
        assertTrue(SpecValidator.validate(classified).isEmpty(), SpecValidator.validate(classified).toString())
        assertEquals(listOf("Course", "Forum"), spec.modules.map { it.name })
        assertEquals(mapOf("wstoken" to "{token}"), spec.authSchemes.single().form)
    }

    @Test
    fun `calls each function through the REST server`() {
        val operation = spec.modules.first().operations.single()
        assertEquals("getContents", operation.name)
        assertEquals("/webservice/rest/server.php", operation.path)
        assertEquals(mapOf("wsfunction" to "core_course_get_contents", "moodlewsrestformat" to "json"), operation.query)
        assertEquals(AuthRequirement(listOf("Token"), required = true), operation.auth)
        assertEquals(Effect.READ, operation.effect)
        assertEquals(null, spec.modules.last().operations.single().effect)
        assertEquals(ResponseBody.Json(TypeRef.ListOf(TypeRef.Named("MoodleCourseSection"))), operation.response)
    }

    @Test
    fun `bracket-encodes lists and booleans`() {
        val parameters = spec.modules.first().operations.single().parameters.associateBy { it.wireName }
        assertTrue(parameters.values.all { it.location == ParameterLocation.FORM })
        assertEquals(ParameterEncoding.PLAIN to true, parameters.getValue("courseid").let { it.encoding to it.required })
        assertEquals(ParameterEncoding.BRACKETS to false, parameters.getValue("options").let { it.encoding to it.required })
        assertEquals(ParameterEncoding.BRACKETS, parameters.getValue("includestealth").encoding)
        assertEquals(TypeRef.ListOf(TypeRef.Named("MoodleOption")), parameters.getValue("options").type)
    }

    @Test
    fun `names records from types json or the key holding them, sharing identical ones`() {
        assertEquals(setOf("MoodleCourseSection", "MoodleOption", "MoodleBadge", "MoodleWarning", "MoodleForumViewForumResponse"), records.keys)
        val warnings = TypeRef.ListOf(TypeRef.Named("MoodleWarning"))
        assertEquals(warnings, records.getValue("MoodleForumViewForumResponse").fields.single { it.wireName == "warnings" }.type)
    }

    @Test
    fun `derives fields with names, defaults and codecs`() {
        val fields = records.getValue("MoodleCourseSection").fields.associateBy { it.wireName }
        assertEquals(DefaultValue.Zero to false, fields.getValue("id").let { it.default to it.nullable })
        assertEquals(TypeRef.Primitive(PrimitiveKind.INT64), fields.getValue("timemodified").type)
        assertEquals("timeModified" to DefaultValue.Null, fields.getValue("timemodified").let { it.name to it.default })
        assertEquals(DefaultValue.Literal("true"), fields.getValue("visible").default)
        assertEquals(Codec.FoldedObject to true, fields.getValue("badge").let { it.codec to it.nullable })
        assertEquals(DefaultValue.EmptyList, fields.getValue("warnings").default)
    }
}
