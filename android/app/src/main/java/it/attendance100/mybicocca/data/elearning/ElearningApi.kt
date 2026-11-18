package it.attendance100.mybicocca.data.elearning

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.http.Cookie
import io.ktor.http.Url
import it.attendance100.mybicocca.data.remote.elearning.api.KalturaApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebCatalogApi

/**
 * The clients of the university's e-learning site for one account: Moodle's web service
 * functions, its pages and Kaltura's CDN, which serves the lecture recordings.
 *
 * Logging in is not part of it: the SSO login yields the web service token passed to each call
 * and the `MoodleSession` cookie passed here, which the pages needing the user's browser session
 * send. The cookie is fixed for the lifetime of the instance; an account switch or a renewed
 * session builds a new one.
 *
 * @param moodleSessionCookie The `MoodleSession` cookie ([MOODLE_SESSION_COOKIE_NAME]), or null
 *   when there is none; the pages needing it then report the session is missing.
 * @param language The Moodle langpack (`it`, `en`) web service calls format strings in; see
 *   [language].
 * @param httpClientConfig Additional configuration of every HTTP client.
 */
class ElearningApi(
    moodleSessionCookie: String? = null,
    language: String = DEFAULT_LANGUAGE,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {},
) : AutoCloseable {
    /** The web service functions, which need no cookies. */
    val moodle: MoodleApi = MoodleApi(httpClientConfig)

    /**
     * The langpack web service calls format strings in, which server-side filters (multilang)
     * also pick the language of. A new value affects every later call, so callers can follow
     * locale changes without rebuilding the clients.
     */
    var language: String
        get() = moodle.language
        set(value) {
            moodle.language = value
        }

    /** The pages sent with the user's browser session. */
    private val sessionPages = MoodleWebApi {
        httpClientConfig()
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
            if (moodleSessionCookie != null) {
                default {
                    val host = Url(BASE_URL).host
                    addCookie(BASE_URL, Cookie(name = MOODLE_SESSION_COOKIE_NAME, value = moodleSessionCookie, domain = host, path = "/", secure = true))
                }
            }
        }
    }

    /** The public pages, read without the session, as their anonymous view. */
    private val publicPages = MoodleWebApi(httpClientConfig)

    private val kalturaCdn = KalturaApi(httpClientConfig)

    /** The public course catalog and course pages. */
    val catalog: MoodleWebCatalogApi get() = publicPages.catalog

    /** Attendance (mod_attendance): reading activities and marking presences. */
    val attendance = ElearningAttendance(moodle, sessionPages.attendance)

    /** Playback of Kaltura video resources (mod/kalvidres). */
    val kaltura = ElearningKaltura(sessionPages.media, kalturaCdn.playback)

    /** Downloads of the files course contents link to, and uploads to draft areas. */
    val files = ElearningFiles(moodle.files)

    init {
        this.language = language
    }

    override fun close() {
        moodle.close()
        sessionPages.close()
        publicPages.close()
        kalturaCdn.close()
    }

    companion object {
        /** Name of Moodle's browser-session cookie, which the SSO login leaves in its cookie jar. */
        const val MOODLE_SESSION_COOKIE_NAME = "MoodleSession"

        /** The langpack until the caller sets another one. */
        const val DEFAULT_LANGUAGE = "en"

        private const val BASE_URL = "https://elearning.unimib.it/"
    }
}
