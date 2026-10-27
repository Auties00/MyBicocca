package it.attendance100.mybicocca.data.observability

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pins which responses [describeBlockedResponse] reports and what it leaves out of the line. */
class HttpDiagnosticsPluginTest {

    private val url = Url("https://s3w.si.unimib.it/e3rest/api/login?wstoken=secret")

    @Test
    fun `a challenge page is described with its mitigation header and ray id`() {
        val headers = headersOf(
            "cf-mitigated" to listOf("challenge"),
            "Server" to listOf("cloudflare"),
            "cf-ray" to listOf("9abc-MXP"),
            "Content-Type" to listOf("text/html"),
        )

        val line = describeBlockedResponse(HttpMethod.Get, url, HttpStatusCode.Forbidden, headers)

        assertEquals(
            "GET https://s3w.si.unimib.it/e3rest/api/login -> 403 " +
                "cf-mitigated=challenge server=cloudflare cf-ray=9abc-MXP content-type=text/html",
            line,
        )
    }

    @Test
    fun `a plain cloudflare block without the mitigation header is still reported`() {
        val headers = headersOf("Server" to listOf("cloudflare"))

        val line = describeBlockedResponse(HttpMethod.Post, url, HttpStatusCode.Forbidden, headers)

        assertEquals(
            "POST https://s3w.si.unimib.it/e3rest/api/login -> 403 server=cloudflare",
            line,
        )
    }

    @Test
    fun `the query string never reaches the line`() {
        val headers = headersOf("cf-mitigated" to listOf("challenge"))

        val line = describeBlockedResponse(HttpMethod.Get, url, HttpStatusCode.Forbidden, headers)!!

        assertEquals(false, "wstoken" in line || "secret" in line)
    }

    @Test
    fun `an origin 401 behind cloudflare stays quiet`() {
        val headers = headersOf("Server" to listOf("cloudflare"), "cf-ray" to listOf("1-MXP"))

        assertNull(describeBlockedResponse(HttpMethod.Get, url, HttpStatusCode.Unauthorized, headers))
    }

    @Test
    fun `a response that did not pass through cloudflare stays quiet`() {
        assertNull(describeBlockedResponse(HttpMethod.Get, url, HttpStatusCode.Forbidden, headersOf()))
    }
}
