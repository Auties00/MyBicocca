package it.attendance100.mybicocca.codegen.reader.esse3

import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumValue
import it.attendance100.mybicocca.codegen.model.Field
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

class Esse3ReaderTest {
    private val spec: ApiSpec = Esse3Reader().read(
        ReaderContext(Path("src/test/resources/esse3"), JsonObject(emptyMap()), mayUpdateSpecFiles = false) {}
    )

    @Test
    fun `produces a valid spec with translated names`() {
        assertTrue(SpecValidator.validate(spec).isEmpty())
        val module = spec.modules.single()
        assertEquals("Catalog", module.name)
        assertEquals("/catalogo-service-v1", module.basePath)
        assertEquals(listOf("Esse3Entry"), module.records.map { it.name })
    }

    @Test
    fun `applies Esse3's decoding policy to fields`() {
        val fields = spec.modules.single().records.single().fields.associateBy { it.wireName }
        assertEquals(
            Field("name", "nome", TypeRef.STRING, nullable = true, default = DefaultValue.Null, description = "nome della voce"),
            fields.getValue("nome"),
            "a required field overridden to optional",
        )
        assertEquals(
            Field(
                "startDate", "dataIni", TypeRef.Primitive(PrimitiveKind.LOCAL_DATE), nullable = true, default = DefaultValue.Null,
                codec = Codec.Temporal("dd/MM/yyyy", blankAsNull = true),
            ),
            fields.getValue("dataIni"),
            "dates are always nullable",
        )
        assertEquals(TypeRef.Named("Esse3StatusCode"), fields.getValue("statoCod").type)
        assertEquals(
            Field("etichette", "etichette", TypeRef.ListOf(TypeRef.STRING), nullable = false, default = DefaultValue.EmptyList),
            fields.getValue("etichette"),
            "an untranslated name is kept",
        )
    }

    @Test
    fun `merges enums with the same values and names`() {
        val status = spec.enums.single { it.name == "Esse3StatusCode" }
        assertEquals(listOf(EnumValue("Active", "A"), EnumValue("Closed", "C")), status.values)
        assertTrue(status.open && status.acceptsWrappedValue)
        val query = spec.enums.single { it.name == "Esse3GetVociQ" }
        assertEquals(listOf(EnumValue("Tutte", "TUTTE", "tutte le voci")), query.values)
    }

    @Test
    fun `orders parameters as path, required query, optional query`() {
        val operation = spec.modules.single().operations.single()
        assertEquals("getEntries", operation.name)
        assertEquals(
            listOf("entryId" to ParameterLocation.PATH, "q" to ParameterLocation.QUERY, "limit" to ParameterLocation.QUERY, "status" to ParameterLocation.QUERY),
            operation.parameters.map { it.name to it.location },
        )
        assertEquals(TypeRef.Named("Esse3StatusCode"), operation.parameters.single { it.name == "status" }.type)
        assertEquals(mapOf("TUTTE" to "tutte le voci"), operation.parameters.single { it.name == "q" }.valueDescriptions)
        assertEquals(ResponseBody.Json(TypeRef.ListOf(TypeRef.Named("Esse3Entry"))), operation.response)
        assertEquals(setOf("STUDENT"), operation.roles)
    }
}
