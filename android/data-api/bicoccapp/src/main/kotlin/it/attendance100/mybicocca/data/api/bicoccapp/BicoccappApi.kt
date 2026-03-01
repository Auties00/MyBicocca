package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpResponseRedirectEvent
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.authority
import io.ktor.http.isSecure
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import it.attendance100.mybicocca.data.common.util.isRedirect
import kotlinx.serialization.json.Json

/**
 * Main entry point for the BicoccApp API.
 *
 * This class serves as a facade that provides access to all specialized API classes
 * for interacting with the University of Milano-Bicocca's mobile app backend.
 *
 * @param httpClientConfig Optional configuration block for the underlying HTTP client.
 */
class BicoccappApi(httpClientConfig: HttpClientConfig<*>.() -> Unit = {}) : AutoCloseable {
    companion object {
        private const val REDIRECT_URL = "https://backoffice-app.unimib.it/inappbrowser"

        private val ALLOWED_FOR_REDIRECT: Set<HttpMethod> = setOf(HttpMethod.Get, HttpMethod.Head)
    }

    /**
     * JSON serializer configured for BicoccApp API responses.
     */
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Redirect plugin copied from the Ktor internals
     * and modified to not redirect if the URL starts with REDIRECT_URL
     */
    @OptIn(InternalAPI::class)
    private val redirectPlugin = createClientPlugin(
        "HttpRedirect"
    ) {
        suspend fun Send.Sender.handleCall(
            context: HttpRequestBuilder,
            origin: HttpClientCall,
            client: HttpClient
        ): HttpClientCall {
            var call = origin
            var requestBuilder = context

            while (true) {
                // Protocol/authority of the current hop
                val previousProtocol = call.request.url.protocol
                val previousAuthority = call.request.url.authority
                if (!call.response.status.isRedirect()) return call

                val location = call.response.headers[HttpHeaders.Location] ?: return call

                client.monitor.raise(HttpResponseRedirectEvent, call.response)

                requestBuilder = HttpRequestBuilder().apply {
                    takeFromWithExecutionContext(requestBuilder)
                    url.parameters.clear()
                    url.takeFrom(location)

                    if (previousProtocol.isSecure() && !url.protocol.isSecure()) {
                        return call
                    }

                    if(location.startsWith(REDIRECT_URL)) {
                        return call
                    }

                    if (previousAuthority != url.authority) {
                        headers.remove(HttpHeaders.Authorization)
                    }
                }

                call = proceed(requestBuilder)
            }
        }

        on(Send) { request ->
            val origin = proceed(request)
            if (origin.request.method !in ALLOWED_FOR_REDIRECT) {
                origin
            } else {
                handleCall(request, origin, client)
            }
        }
    }

    /**
     * Shared HTTP client for all API requests.
     */
    private val client = HttpClient {
        httpClientConfig()

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        followRedirects = false
        install(redirectPlugin)
    }

    /**
     * API for authentication and OAuth2/OpenID Connect flows.
     */
    val auth: BicoccappAuthApi = BicoccappAuthApi(client, json)

    /**
     * API for student profile.
     */
    val profile: BicoccappProfileApi = BicoccappProfileApi(client, json)

    /**
     * API for student career and registrations.
     */
    val career: BicoccappCareerApi = BicoccappCareerApi(client, json)

    /**
     * API for student exams and exam sessions.
     */
    val exams: BicoccappExamsApi = BicoccappExamsApi(client, json)

    /**
     * API for student tuition fees.
     */
    val taxes: BicoccappTaxesApi = BicoccappTaxesApi(client, json)

    /**
     * API for academic calendar and course scheduling.
     */
    val calendar: BicoccappCalendarApi = BicoccappCalendarApi(client, json)

    /**
     * API for course selection wizard for calendar setup.
     */
    val wizard: BicoccappWizardApi = BicoccappWizardApi(client, json)

    /**
     * API for points of interest and faculty directory.
     */
    val campus: BicoccappCampusApi = BicoccappCampusApi(client, json)

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the API instance should not be used.
     * All API calls will fail after closing.
     *
     * This method is idempotent - calling it multiple times has no effect.
     */
    override fun close() {
        client.close()
    }
}