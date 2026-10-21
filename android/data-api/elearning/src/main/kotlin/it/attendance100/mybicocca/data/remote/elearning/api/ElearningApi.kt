package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningAbstractApi.Companion.BASE_URL
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleRuntime.language
import kotlinx.serialization.json.Json

/**
 * Main entry point for the Elearning (Moodle) API.
 *
 * This class serves as a facade that provides access to all specialized API classes
 * for interacting with a Moodle learning management system.
 *
 * Logging in is not part of this API: the caller runs the SSO login, which yields the web
 * service token passed to each call and the `MoodleSession` cookie passed here.
 *
 * @param moodleSessionCookie Optional `MoodleSession` browser cookie ([MOODLE_SESSION_COOKIE_NAME]).
 *   When non-`null`, the cookie is installed into this instance's cookie jar before the
 *   first request, so web-scope endpoints (e.g. `mod/kalvidres/view.php`) work. Pass
 *   `null` (or omit) when the caller has no session cookie; those endpoints then report
 *   that the session is missing. The cookie is fixed for the lifetime of this instance;
 *   account switches should construct a new [ElearningApi].
 * @param language Moodle langpack code (e.g. `"it"`, `"en"`) sent as
 *   `moodlewssettinglang` with every web service request. It selects the language of
 *   Moodle UI strings in responses and the language picked by server-side filters
 *   (e.g. multilang) where the site applies them. Can be changed later via [language].
 *   Defaults to [ElearningAbstractApi.DEFAULT_LANGUAGE].
 * @param httpClientConfig Optional configuration block for the underlying HTTP client.
 */
class ElearningApi(
    moodleSessionCookie: String? = null,
    language: String = ElearningAbstractApi.DEFAULT_LANGUAGE,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {},
) : AutoCloseable {
    /**
     * JSON serializer configured for Moodle API responses.
     */
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Plugin to optionally skip cookies.
     */
    private val skipCookiesPlugin = createClientPlugin("SkipCookies") {
        on(SendingRequest) { request, _ ->
            if (request.attributes.contains(ElearningAttributes.SkipCookies)) {
                request.headers.remove(HttpHeaders.Cookie)
            }
        }
    }

    /**
     * Shared HTTP client for all API requests.
     */
    private val client = HttpClient {
        httpClientConfig()

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
            if (moodleSessionCookie != null) {
                default {
                    addCookie(
                        BASE_URL,
                        Cookie(
                            name = MOODLE_SESSION_COOKIE_NAME,
                            value = moodleSessionCookie,
                            domain = Url(BASE_URL).host,
                            path = "/",
                            secure = true,
                        )
                    )
                }
            }
        }
        install(skipCookiesPlugin)

        followRedirects = true

        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * The Moodle langpack code sent as `moodlewssettinglang` with every web service
     * request. Stored as a client attribute ([ElearningAttributes.Language]) shared by
     * all API classes; assigning a new value affects every subsequent request, so
     * callers can follow runtime locale changes without rebuilding the client.
     */
    var language: String
        get() = client.attributes.getOrNull(ElearningAttributes.Language)
            ?: ElearningAbstractApi.DEFAULT_LANGUAGE
        set(value) = client.attributes.put(ElearningAttributes.Language, value)

    init {
        this.language = language
    }

    /**
     * The Moodle web service functions, generated from Moodle's own descriptions; calls are
     * made in [language]. It has its own HTTP client, since web service calls need no cookies.
     */
    val moodle: MoodleApi = MoodleApi(httpClientConfig = {
        httpClientConfig()
        language { this@ElearningApi.language }
    })

    /**
     * The public course pages: the course catalog and each course's info page.
     */
    /** The public pages, read on a client of their own, without the session, as their anonymous view. */
    private val publicPages = MoodleWebApi(httpClientConfig)

    /** The public course catalog and course pages. */
    val catalog: MoodleWebCatalogApi get() = publicPages.catalog

    /**
     * Attendance (mod_attendance), whose views are reached through the mobile app bridge.
     */
    val attendance: ElearningAttendanceApi = ElearningAttendanceApi(client, json, moodle)

    /**
     * Kaltura video resource playback (mod/kalvidres).
     */
    val kaltura: ElearningKalturaApi = ElearningKalturaApi(client, json, MoodleWebMediaApi(client, json))

    /**
     * Downloads of files served by the web service file endpoint, and uploads to draft areas.
     */
    val files: ElearningFileApi = ElearningFileApi(client, json)

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
        moodle.close()
        publicPages.close()
    }

    companion object {
        /**
         * Name of Moodle's browser-session cookie, which the SSO login leaves in its cookie jar.
         */
        const val MOODLE_SESSION_COOKIE_NAME = "MoodleSession"
    }
}
