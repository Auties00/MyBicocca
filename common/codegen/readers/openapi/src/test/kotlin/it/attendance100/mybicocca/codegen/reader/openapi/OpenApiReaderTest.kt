package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.SpecValidator
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenApiReaderTest {
    private val spec: ApiSpec = OpenApiReader().read(
        ReaderContext(Path("src/test/resources/demo"), JsonObject(mapOf("spec" to JsonPrimitive("demo.yaml"))), mayUpdateSpecFiles = false) {}
    )

    @Test
    fun `reads a valid spec with servers, auth schemes and one module per tag`() {
        assertTrue(SpecValidator.validate(spec).isEmpty(), SpecValidator.validate(spec).toString())
        assertEquals("demo", spec.id)
        assertEquals("Demo", spec.prefix)
        assertEquals(listOf("Main", "Files"), spec.servers.map { it.name })
        assertEquals(mapOf("Authorization" to "Bearer {token}"), spec.authSchemes.single().headers)
        assertEquals(listOf("Items", "Files"), spec.modules.map { it.name })
    }

    @Test
    fun `reads effects, which default to read for GETs`() {
        val effects = spec.modules.flatMap { it.operations }.associate { it.name to it.effect }
        assertEquals(Effect.READ, effects["getItem"])
        assertEquals(Effect.BENIGN, effects["touchFile"])
    }

    @Test
    fun `assigns records to the first module that uses them`() {
        assertEquals(listOf("DemoItem", "DemoOwner"), spec.modules.first().records.map { it.name })
        assertEquals(listOf("DemoSlot", "DemoSettings", "DemoCalendar"), spec.modules.last().records.map { it.name })
    }

    @Test
    fun `reads API parameters, cookies and JSON-encoded parameters`() {
        val tenant = spec.parameters.single()
        assertEquals(Triple("tenant", "X-TENANT", ParameterLocation.HEADER), Triple(tenant.name, tenant.wireName, tenant.location))
        val filters = spec.modules.last().operations.last().parameters.single()
        assertEquals(ParameterLocation.COOKIE, filters.location)
        assertEquals(ParameterEncoding.JSON, filters.encoding)
        assertEquals(DefaultValue.Literal("{}"), filters.default)
    }

    @Test
    fun `reads maps, delimited records, integer enums and codecs`() {
        val records = spec.modules.last().records.associateBy { it.name }
        assertEquals(RecordEncoding.Delimited("-"), records.getValue("DemoSlot").encoding)
        val fields = records.getValue("DemoCalendar").fields.associateBy { it.wireName }
        val date = TypeRef.Primitive(PrimitiveKind.LOCAL_DATE)
        assertEquals(TypeRef.MapOf(date, TypeRef.ListOf(TypeRef.Named("DemoSlot"))), fields.getValue("days").type)
        assertEquals(DefaultValue.EmptyMap, fields.getValue("days").default)
        assertEquals(TypeRef.MapOf(TypeRef.STRING, TypeRef.STRING), fields.getValue("labels").type)
        assertEquals(TypeRef.MapOf(TypeRef.STRING, TypeRef.Primitive(PrimitiveKind.JSON_VALUE)), fields.getValue("extra").type)
        assertEquals(Codec.BlankAsNull(listOf("0")), fields.getValue("note").codec)
        assertEquals(Codec.FoldedObject to TypeRef.Named("DemoSettings"), fields.getValue("settings").let { it.codec to it.type })
        assertEquals(Codec.EmbeddedJson, fields.getValue("policy").codec)
        val level = spec.enums.single { it.name == "DemoLevel" }
        assertTrue(level.integer)
        assertEquals(listOf("1", "2"), level.values.map { it.wireValue })
    }

    @Test
    fun `derives fields with names, defaults and codecs`() {
        val fields = spec.modules.first().records.first().fields.associateBy { it.wireName }
        assertEquals(Field("itemId", "item_id", TypeRef.Primitive(PrimitiveKind.INT64), nullable = false, default = null), fields["item_id"])
        assertEquals("name", fields.getValue("nome").name)
        assertEquals(Field("stato", "stato", TypeRef.Named("DemoStatus"), nullable = true, default = DefaultValue.Null), fields["stato"])
        assertEquals(DefaultValue.EmptyList, fields.getValue("tags").default)
        assertEquals(DefaultValue.Literal("2.5"), fields.getValue("rating").default)
        assertEquals(Codec.Temporal("HH:mm", blankAsNull = false), fields.getValue("opens_at").codec)
        assertEquals(TypeRef.Primitive(PrimitiveKind.JSON_VALUE), fields.getValue("payload").type)
    }

    @Test
    fun `reads enum names and the wrapped flag`() {
        val status = spec.enums.single { it.name == "DemoStatus" }
        assertEquals(listOf(EnumValue("InUse", "IN_USE"), EnumValue("Retired", "retired")), status.values)
        assertTrue(status.open && status.acceptsWrappedValue)
    }

    @Test
    fun `reads operations with their server, envelope, headers and auth`() {
        val get = spec.modules.first().operations.single()
        assertEquals("Main", get.server)
        assertEquals(ResponseBody.Json(TypeRef.Named("DemoItem"), envelope = listOf("data")), get.response)
        assertEquals(mapOf("x-client" to "sdk"), get.headers)
        assertEquals(AuthRequirement(listOf("Token"), required = false), get.auth)
        val zone = get.parameters.single { it.location == ParameterLocation.HEADER }
        assertEquals("zone" to DefaultValue.Literal("Europe/Rome"), zone.name to zone.default)

        val touch = spec.modules.last().operations.first()
        assertEquals("Files", touch.server)
        assertEquals(RequestBody.EmptyObject, touch.body)
        assertEquals(ResponseBody.None, touch.response)
    }
}
