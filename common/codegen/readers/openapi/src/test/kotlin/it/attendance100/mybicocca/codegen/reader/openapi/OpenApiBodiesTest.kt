package it.attendance100.mybicocca.codegen.reader.openapi

import io.swagger.v3.parser.OpenAPIV3Parser
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.SpecValidator
import it.attendance100.mybicocca.codegen.model.TypeRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Multipart forms with files, raw path parameters, and text and binary responses. */
class OpenApiBodiesTest {
    private val spec: ApiSpec = OpenApiModelBuilder(
        OpenAPIV3Parser().readContents(
            """
            |openapi: 3.0.3
            |info: { title: Demo, version: "1" }
            |x-mb-id: demo
            |x-mb-prefix: Demo
            |x-mb-client: { baseUrl: https://example.com }
            |paths:
            |  /files/{path}:
            |    get:
            |      tags: [Files]
            |      operationId: download
            |      parameters:
            |        - { name: path, in: path, required: true, x-mb-raw: true, schema: { type: string } }
            |      responses:
            |        "200": { description: OK, content: { application/octet-stream: { schema: { type: string, format: binary } } } }
            |  /upload:
            |    post:
            |      tags: [Files]
            |      operationId: upload
            |      x-mb-effect: irreversible
            |      requestBody:
            |        content:
            |          multipart/form-data:
            |            schema:
            |              type: object
            |              required: [file]
            |              properties:
            |                folder: { type: string }
            |                file: { type: string, format: binary }
            |      responses:
            |        "204": { description: OK }
            |  /playlist:
            |    get:
            |      tags: [Files]
            |      operationId: playlist
            |      responses:
            |        "200": { description: OK, content: { application/vnd.apple.mpegurl: { schema: { type: string } } } }
            |  /calendar:
            |    get:
            |      tags: [Files]
            |      operationId: calendar
            |      responses:
            |        "200": { description: OK, content: { text/calendar: { schema: { type: string, format: binary } } } }
            """.trimMargin()
        ).openAPI
    ).build()

    private val operations = spec.modules.single().operations.associateBy { it.name }

    @Test
    fun `reads a valid spec`() {
        assertTrue(SpecValidator.validate(spec).isEmpty(), SpecValidator.validate(spec).toString())
    }

    @Test
    fun `binary fields of a multipart form are files`() {
        val upload = operations.getValue("upload")
        assertTrue(upload.multipart)
        val fields = upload.parameters.associateBy { it.wireName }
        assertEquals(ParameterLocation.FORM, fields.getValue("file").location)
        assertEquals(TypeRef.Primitive(PrimitiveKind.FILE), fields.getValue("file").type)
        assertEquals(TypeRef.STRING, fields.getValue("folder").type)
    }

    @Test
    fun `raw path parameters keep their slashes`() {
        assertTrue(operations.getValue("download").pathParameters.single().raw)
    }

    @Test
    fun `string responses are text, and format binary is always bytes`() {
        assertEquals(ResponseBody.Text, operations.getValue("playlist").response)
        assertEquals(ResponseBody.Binary, operations.getValue("download").response)
        assertEquals(ResponseBody.Binary, operations.getValue("calendar").response)
    }
}
