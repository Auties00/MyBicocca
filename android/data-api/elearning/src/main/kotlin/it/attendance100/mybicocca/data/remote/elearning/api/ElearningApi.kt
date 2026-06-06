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
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningAuthApi.Companion.MOODLE_SESSION_COOKIE_NAME
import kotlinx.serialization.json.Json

/**
 * Main entry point for the Elearning (Moodle) API.
 *
 * This class serves as a facade that provides access to all specialized API classes
 * for interacting with a Moodle learning management system.
 *
 * @param moodleSessionCookie Optional pre-seeded `MoodleSession` browser cookie. When
 *   non-`null`, the cookie is installed into this instance's cookie jar before the
 *   first request, so web-scope endpoints (e.g. `mod/kalvidres/view.php`) can be hit
 *   on a cold process start without re-running the SAML login flow. The matching
 *   value comes from [it.attendance100.mybicocca.data.remote.elearning.dto.ElearningLoginResponse.Success.moodleSessionCookie]
 *   on a previous successful [ElearningAuthApi.login] and should be persisted by the
 *   caller. Pass `null` (or omit) when no persisted cookie is available — a fresh
 *   [ElearningAuthApi.login] will install one via Moodle's `Set-Cookie` response
 *   naturally. The cookie is fixed for the lifetime of this instance; account switches
 *   should construct a new [ElearningApi].
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
     * API for auth-related operations.
     */
    val auth: ElearningAuthApi = ElearningAuthApi(client, json)

    /**
     * API for site-level operations.
     */
    val site: ElearningSiteApi = ElearningSiteApi(client, json)

    /**
     * API for user-related operations.
     */
    val users: ElearningUserApi = ElearningUserApi(client, json)

    /**
     * API for course-related operations.
     */
    val courses: ElearningCourseApi = ElearningCourseApi(client, json)

    /**
     * API for quiz-related operations.
     */
    val quizzes: ElearningQuizApi = ElearningQuizApi(client, json)

    /**
     * API for assignment-related operations.
     */
    val assignments: ElearningAssignApi = ElearningAssignApi(client, json)

    /**
     * API for forum-related operations.
     */
    val forums: ElearningForumApi = ElearningForumApi(client, json)

    /**
     * API for calendar-related operations.
     */
    val calendar: ElearningCalendarApi = ElearningCalendarApi(client, json)

    /**
     * API for badge-related operations.
     */
    val badges: ElearningBadgeApi = ElearningBadgeApi(client, json)

    /**
     * API for completion-related operations.
     */
    val completion: ElearningCompletionApi = ElearningCompletionApi(client, json)

    /**
     * API for attendance-related operations (mod_attendance).
     */
    val attendance: ElearningAttendanceApi = ElearningAttendanceApi(client, json)

    /**
     * API for grade-related operations.
     */
    val grades: ElearningGradeApi = ElearningGradeApi(client, json)

    /**
     * API for messaging operations.
     */
    val messages: ElearningMessageApi = ElearningMessageApi(client, json)

    /**
     * API for Kaltura video resource playback (mod/kalvidres).
     */
    val kaltura: ElearningKalturaApi = ElearningKalturaApi(client, json)

    /**
     * API for downloading files served by the web service file endpoint.
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
    }
}
