package it.attendance100.mybicocca.codegen.reader.openapi

import io.swagger.v3.parser.OpenAPIV3Parser
import it.attendance100.mybicocca.codegen.model.ClientSetting
import it.attendance100.mybicocca.codegen.model.ClientSpec
import it.attendance100.mybicocca.codegen.model.ErrorCode
import it.attendance100.mybicocca.codegen.model.ErrorFormat
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.RetryRequest
import it.attendance100.mybicocca.codegen.model.RetryRule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ClientSyntaxTest {
    private fun client(yaml: String): ClientSpec {
        val document = """
            |openapi: 3.0.3
            |info: { title: Demo, version: "1" }
            |x-mb-id: demo
            |x-mb-prefix: Demo
            |$yaml
            |paths: {}
        """.trimMargin()
        val api = OpenAPIV3Parser().readContents(document).openAPI
        return OpenApiModelBuilder(api).build().client
    }

    @Test
    fun `a document without x-mb-client describes the default client`() {
        assertEquals(ClientSpec.DEFAULT, client(""))
    }

    @Test
    fun `reads every key of x-mb-client`() {
        val client = client(
            """
            |x-mb-client:
            |  baseUrl: https://example.com/api
            |  timeoutSeconds: 30
            |  keepsCookies: true
            |  caches: true
            |  sendsDefaults: true
            |  sendsNulls: false
            |  headers: { Accept-Language: it }
            |  query: { format: json }
            |  settings:
            |    - { name: language, in: query, wireName: lang, default: en, description: The language. }
            |  emptyStatuses: [404]
            |  error:
            |    detect: [/exception]
            |    code: [/error/code, /error]
            |    message: [/error/message]
            |    codes:
            |      NotFound: { value: not_found, description: Nothing has that id. }
            |      Expired: expired
            |  retries:
            |    - when: { status: 401, code: expired }
            |      before: { path: "/token", headers: { X-Portal: "{portal}" } }
            |    - when: { status: 429 }
            |      delaySeconds: 5
            |      description: Too many requests.
            """.trimMargin()
        )
        assertEquals(
            ClientSpec(
                baseUrl = "https://example.com/api",
                timeoutSeconds = 30,
                keepsCookies = true,
                caches = true,
                sendsDefaults = true,
                sendsNulls = false,
                headers = mapOf("Accept-Language" to "it"),
                query = mapOf("format" to "json"),
                settings = listOf(ClientSetting("language", ParameterLocation.QUERY, "lang", "en", "The language.")),
                emptyStatuses = setOf(404),
                error = ErrorFormat(
                    detect = listOf("/exception"),
                    code = listOf("/error/code", "/error"),
                    message = listOf("/error/message"),
                    codes = listOf(ErrorCode("NotFound", "not_found", "Nothing has that id."), ErrorCode("Expired", "expired")),
                ),
                retries = listOf(
                    RetryRule(status = 401, code = "expired", before = RetryRequest(HttpMethod.GET, "/token", mapOf("X-Portal" to "{portal}"))),
                    RetryRule(status = 429, delaySeconds = 5, description = "Too many requests."),
                ),
            ),
            client,
        )
    }

    @Test
    fun `refuses unknown keys`() {
        val error = assertThrows<IllegalArgumentException> { client("x-mb-client: { baseURL: https://example.com }") }
        assertContains(error.message.orEmpty(), "baseURL")
    }
}
