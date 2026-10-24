package it.attendance100.mybicocca.data.remote.runtime

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.seconds

/** The request policy every generated module sends its requests with. */
class RestApiTest {
    private class DemoException(val failure: Failure) : Exception(failure.errorMessage)

    private enum class Role { STUDENT }

    /** A module of a generated API, reduced to the runtime calls its operations make. */
    private class DemoApi(engine: MockEngine, policy: RestPolicy, settings: ClientSettings = ClientSettings(emptyList())) : RestApi(
        HttpClient(engine) { configure(Json, RestClientOptions(headers = mapOf("Accept-Language" to "it")), settings) },
        Json,
        "/v1",
        policy,
        mapOf("portal" to "7"),
    ) {
        suspend fun items(): List<String> =
            jsonList(HttpMethod.Get, "/items", deserializer = ListSerializer(String.serializer()), roles = setOf(Role.STUDENT))

        suspend fun name(): String = json(HttpMethod.Get, "/name", deserializer = String.serializer(), envelope = listOf("data"))

        suspend fun touch() = send(HttpMethod.Post, "/touch")
    }

    private val requests = mutableListOf<HttpRequestData>()

    private fun engine(vararg responses: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): MockEngine {
        var next = 0
        return MockEngine { request ->
            requests += request
            responses[minOf(next++, responses.lastIndex)](request)
        }
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))

    private fun policy(
        emptyStatuses: Set<Int> = emptySet(),
        errors: ErrorFormat = ErrorFormat(code = listOf("/error/code", "/error"), message = listOf("/error/message", "/message")),
        retries: List<RetryRule> = emptyList(),
    ) = RestPolicy("https://example.com", emptyStatuses, errors, retries) { DemoException(it) }

    @Test
    fun `reads the value inside the envelope`() = runTest {
        val api = DemoApi(engine({ json("""{"data": "Ada"}""") }), policy())
        assertEquals("Ada", api.name())
        assertEquals("https://example.com/v1/name", requests.single().url.toString())
        assertEquals("it", requests.single().headers["Accept-Language"])
    }

    @Test
    fun `an empty status is an empty list`() = runTest {
        val api = DemoApi(engine({ json("""{"error": "not_found"}""", HttpStatusCode.NotFound) }), policy(emptyStatuses = setOf(404)))
        assertEquals(emptyList<String>(), api.items())
    }

    @Test
    fun `reads the code and message of an error, with the operation's roles`() = runTest {
        val api = DemoApi(engine({ json("""{"error": {"code": "denied", "message": "No access"}}""", HttpStatusCode.Forbidden) }), policy())
        val failure = assertThrows<DemoException> { api.items() }.failure
        assertEquals(403, failure.statusCode)
        assertEquals("denied", failure.errorCode)
        assertEquals("No access", failure.errorMessage)
        assertEquals(setOf(Role.STUDENT), failure.roles)
    }

    @Test
    fun `the code stands in for a missing message, and the status for both`() = runTest {
        val coded = DemoApi(engine({ json("""{"error": "gone"}""", HttpStatusCode.Gone) }), policy())
        assertEquals("gone", assertThrows<DemoException> { coded.touch() }.failure.errorMessage)
        val bare = DemoApi(engine({ respond("<html></html>", HttpStatusCode.InternalServerError) }), policy())
        val failure = assertThrows<DemoException> { bare.touch() }.failure
        assertNull(failure.errorCode)
        assertEquals("Request failed with status 500 Internal Server Error", failure.errorMessage)
    }

    @Test
    fun `detects errors reported with a successful status`() = runTest {
        val errors = ErrorFormat(detect = listOf("/exception", "/error"), code = listOf("/errorcode"), message = listOf("/message"))
        val failing = DemoApi(engine({ json("""{"exception": "x", "errorcode": "invalidtoken", "message": "Invalid token"}""") }), policy(errors = errors))
        assertEquals("invalidtoken", assertThrows<DemoException> { failing.touch() }.failure.errorCode)
        val succeeding = DemoApi(engine({ json("""{"data": "Ada", "error": false}""") }), policy(errors = errors))
        assertEquals("Ada", succeeding.name())
    }

    @Test
    fun `sends the recovery request, waits and tries again`() = runTest {
        val rules = listOf(
            RetryRule(status = 401, code = "expired", before = RetryRequest(HttpMethod.Get, "/token/{portal}", mapOf("X-Portal" to "{portal}"))),
            RetryRule(status = 401, code = "too_fast", delay = 12.seconds),
        )
        val api = DemoApi(
            engine(
                { json("""{"error": "expired"}""", HttpStatusCode.Unauthorized) },
                { json("{}") },
                { json("""{"error": "too_fast"}""", HttpStatusCode.Unauthorized) },
                { json("""{"data": "Ada"}""") },
            ),
            policy(retries = rules),
        )
        assertEquals("Ada", api.name())
        assertEquals(listOf("/v1/name", "/token/7", "/v1/name", "/v1/name"), requests.map { it.url.encodedPath })
        assertEquals("7", requests[1].headers["X-Portal"])
    }

    @Test
    fun `retries only the failures a rule matches`() = runTest {
        val rules = listOf(RetryRule(status = 401, code = "expired", delay = 1.seconds))
        val api = DemoApi(engine({ json("""{"error": "denied"}""", HttpStatusCode.Unauthorized) }), policy(retries = rules))
        assertEquals("denied", assertThrows<DemoException> { api.touch() }.failure.errorCode)
        assertEquals(1, requests.size)
    }

    @Test
    fun `sends the current value of every setting`() = runTest {
        val settings = ClientSettings(listOf(ClientSetting("language", ClientSetting.Location.QUERY, "lang", "en")))
        val api = DemoApi(engine({ json("{}") }), policy(), settings)
        api.touch()
        settings["language"] = "it"
        api.touch()
        assertEquals(listOf("en", "it"), requests.map { it.url.parameters["lang"] })
    }
}
