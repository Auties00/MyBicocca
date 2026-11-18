package it.attendance100.mybicocca.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.observability.HttpMetrics
import io.ktor.client.plugins.UserAgent
import it.attendance100.mybicocca.data.elearning.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import it.attendance100.mybicocca.data.remote.elearning.exception.MoodleException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.FormElement
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.random.Random

/**
 * Logs into Moodle the way the official mobile app does: Moodle's mobile launch page hands off
 * to the UniMiB Shibboleth IdP, the password step is submitted, the SAML response is posted
 * back to Moodle, and Moodle finally redirects to `moodlemobile://token=…` carrying the web
 * service token.
 *
 * Each login runs on its own short-lived client so the cookie jar starts empty and never
 * carries one account's IdP session into another's login. The `MoodleSession` cookie left in
 * that jar is returned alongside the token, for the [ElearningApi] built afterwards.
 *
 * The final redirect is never followed: Ktor refuses to follow the `moodlemobile://` scheme,
 * which is what leaves its `Location` header readable.
 *
 * Once the browser session expires, [renewSession] mints a new one from the tokens alone, the
 * way the Moodle app opens web pages: no credentials and no IdP round-trip.
 */
@Singleton
class ElearningAuthenticator @Inject constructor() {

    suspend fun login(username: String, password: String): ElearningLogin {
        val cookies = AcceptAllCookiesStorage()
        return buildClient(cookies).use { client ->
            val token = runLogin(client, username, password)
            ElearningLogin(
                wsToken = token.wsToken,
                privateToken = token.privateToken,
                moodleSessionCookie = cookies.moodleSession(),
            )
        }
    }

    /**
     * Mints a new `MoodleSession` browser cookie through Moodle's mobile autologin: the web
     * service hands out a one-time key for the private token, and `autologin.php` trades the key
     * for a logged-in session.
     *
     * Moodle issues at most one key per user every six minutes, each valid for one minute and
     * only from the same IP address; any of these limits failing the renewal leaves the caller
     * to fall back to [login].
     *
     * @param wsToken The web service token.
     * @param privateToken The private token returned alongside it at login.
     * @param userId The Moodle user id the tokens belong to.
     * @return The new `MoodleSession` cookie value.
     * @throws ElearningLoginException If Moodle refuses the key or the autologin doesn't end in
     *   a logged-in page.
     */
    suspend fun renewSession(wsToken: String, privateToken: String, userId: Int): String {
        val cookies = AcceptAllCookiesStorage()
        return buildClient(cookies).use { client ->
            val autologin = MoodleApi(httpClientConfig = { install(UserAgent) { agent = MOODLE_MOBILE_USER_AGENT } }).use { moodle ->
                try {
                    moodle.mobile.getAutologinKey(privateToken, auth = MoodleTokenAuth(wsToken))
                } catch (e: MoodleException) {
                    throw ElearningLoginException("Cannot renew session: ${e.errorCode ?: e.errorMessage}")
                }
            }
            if (autologin.key.isEmpty() || autologin.autologinUrl.isEmpty()) {
                throw ElearningLoginException("Cannot renew session: no autologin key")
            }

            val landing = client.get(URLBuilder(autologin.autologinUrl).apply {
                parameters.append("userid", userId.toString())
                parameters.append("key", autologin.key)
                parameters.append("urltogo", AUTOLOGIN_LANDING_URL)
            }.buildString())
            // A refused key ends on the login page instead, still with a (guest) MoodleSession.
            if (landing.request.url.encodedPath != Url(AUTOLOGIN_LANDING_URL).encodedPath) {
                throw ElearningLoginException("Cannot renew session: autologin ended at ${landing.request.url.encodedPath}")
            }
            cookies.moodleSession()
                ?: throw ElearningLoginException("Cannot renew session: Moodle set no session cookie")
        }
    }

    private suspend fun AcceptAllCookiesStorage.moodleSession(): String? =
        get(Url(MOODLE_URL))
            .firstOrNull { it.name == ElearningApi.MOODLE_SESSION_COOKIE_NAME }
            ?.value

    private suspend fun runLogin(client: HttpClient, username: String, password: String): MobileToken {
        val launchResponse = client.get(launchUrl(client))
        return when (launchResponse.status) {
            // Moodle still has a session for this client and issues the token straight away.
            HttpStatusCode.Found -> {
                val location = launchResponse.headers[HttpHeaders.Location]
                    ?: throw ElearningLoginException("Expected redirect (status code: ${launchResponse.status.value})")
                parseToken(location)
            }

            HttpStatusCode.OK -> {
                val ssoUrl = launchResponse.toHtml().selectFirst("a[title=UniMiB]")
                    ?.attr("href")
                    ?: throw ElearningLoginException("Cannot log in: missing login button")
                val ssoResponse = client.get(ssoUrl)
                if (ssoResponse.status != HttpStatusCode.OK) {
                    throw ElearningLoginException("Cannot log in: saml endpoint returned an invalid status code (status code: ${ssoResponse.status.value})")
                }

                val storageResponse = client.post(IDP_STEP_1_URL) {
                    setBody(FormDataContent(Parameters.build {
                        append("shib_idp_ls_exception.shib_idp_session_ss", "")
                        append("shib_idp_ls_success.shib_idp_session_ss", "true")
                        append("shib_idp_ls_value.shib_idp_session_ss", "")
                        append("shib_idp_ls_exception.shib_idp_persistent_ss", "")
                        append("shib_idp_ls_success.shib_idp_persistent_ss", "true")
                        append("shib_idp_ls_value.shib_idp_persistent_ss", "")
                        append("shib_idp_ls_supported", "true")
                        append("_eventId_proceed", "")
                    }))
                }
                if (storageResponse.status != HttpStatusCode.Found) {
                    throw ElearningLoginException("Cannot log in: invalid status code at step one (status code: ${storageResponse.status})")
                }

                val methodPageResponse = client.get(IDP_STEP_2_URL)
                if (methodPageResponse.status != HttpStatusCode.OK) {
                    throw ElearningLoginException("Cannot login: invalid status code at step two (status code: ${methodPageResponse.status})")
                }

                // Picks the password login over SPID.
                val methodResponse = client.post(IDP_STEP_2_URL) {
                    setBody(FormDataContent(Parameters.build {
                        append("_eventId_routing", "")
                        append("selected_flow", "")
                        append("auth_ctx", "authn/Password")
                        append("spid_idp", "")
                    }))
                }
                if (methodResponse.status != HttpStatusCode.Found) {
                    throw ElearningLoginException("Cannot log in: invalid status code at step three (status code: ${methodResponse.status})")
                }

                val credentialsResponse = client.post(IDP_STEP_3_URL) {
                    setBody(FormDataContent(Parameters.build {
                        append("j_username", username)
                        append("j_password", password)
                        append("_eventId_proceed", "")
                    }))
                }
                // On success the IdP answers with the auto-submitting SAML response form; on bad
                // credentials it re-renders the login page, which has no such form.
                val samlForm = credentialsResponse.toHtml().selectFirst("form") as? FormElement
                    ?: throw ElearningLoginException("Cannot log in: missing form at step three", badCredentials = true)
                val samlFormUrl = if (samlForm.hasAttr("action")) samlForm.absUrl("action") else samlForm.baseUri()
                val samlResponse = client.post(samlFormUrl) {
                    setBody(FormDataContent(Parameters.build {
                        samlForm.formData().forEach { append(it.key(), it.value()) }
                    }))
                }
                val redirectUrl = samlResponse.headers[HttpHeaders.Location]
                    ?: throw ElearningLoginException("Cannot log in: invalid status code at step four (status code: ${samlResponse.status})")
                val redirectResponse = client.get(redirectUrl)
                val tokenUrl = redirectResponse.headers[HttpHeaders.Location]
                    ?: throw ElearningLoginException("Cannot log in: invalid status code at step five (status code: ${redirectResponse.status})")
                parseToken(tokenUrl)
            }

            else -> throw ElearningLoginException("Cannot log in: saml endpoint returned an invalid status code (status code: ${launchResponse.status.value})")
        }
    }

    /**
     * Reads Moodle's mobile launch URL from the public site config (`tool_mobile_get_public_config`
     * through the no-login AJAX endpoint) and points it at the official mobile app service.
     */
    private suspend fun launchUrl(client: HttpClient): String {
        val response = client.post(AJAX_URL) {
            setBody(TextContent(PUBLIC_CONFIG_REQUEST, ContentType.Application.Json))
        }
        val result = (Json.parseToJsonElement(response.bodyAsText()) as? JsonArray)
            ?.firstOrNull() as? JsonObject
            ?: throw ElearningLoginException("Empty response from server")

        val error = result["error"]
        if (error is JsonPrimitive && error.contentOrNull != "false") {
            throw ElearningLoginException("Cannot get auth url: ${error.contentOrNull ?: "unknown error"}")
        }

        val data = result["data"] as? JsonObject
            ?: throw ElearningLoginException("Invalid response: 'data' field missing")
        val launchUrl = data["launchurl"]?.jsonPrimitive?.contentOrNull
            ?: throw ElearningLoginException("No auth url found")
        return URLBuilder(launchUrl).apply {
            parameters.append("service", "moodle_mobile_app")
            parameters.append("passport", (Random.nextDouble() * 1000).toString())
        }.buildString()
    }

    /**
     * Extracts the tokens from `moodlemobile://token=BASE64`, whose payload decodes to
     * `SIGNATURE:::TOKEN[:::PRIVATE_TOKEN]`. Moodle only includes the private token right after
     * a fresh login, not when it reuses an existing browser session.
     */
    private fun parseToken(url: String): MobileToken {
        val parts = Base64.decode(url.substringAfter("token=")).decodeToString().split(":::")
        val wsToken = parts.getOrNull(1)
            ?: throw ElearningLoginException("Cannot log in: malformed mobile launch payload")
        return MobileToken(wsToken, parts.getOrNull(2)?.takeIf { it.isNotBlank() })
    }

    private data class MobileToken(val wsToken: String, val privateToken: String?)

    private suspend fun HttpResponse.toHtml(): Document =
        Jsoup.parse(bodyAsChannel().toInputStream(), null, request.url.toString())

    private fun buildClient(cookies: AcceptAllCookiesStorage): HttpClient = HttpClient(OkHttp) {
        install(HttpCookies) {
            storage = cookies
        }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
        }
        install(HttpMetrics)
        followRedirects = true
    }

    private companion object {
        const val TIMEOUT_MS = 30_000L
        const val MOODLE_URL = "https://elearning.unimib.it/"
        const val AJAX_URL = "https://elearning.unimib.it/lib/ajax/service.php"
        const val AUTOLOGIN_LANDING_URL = "https://elearning.unimib.it/user/profile.php"

        /** Moodle only hands out autologin keys to clients that identify as its mobile app. */
        const val MOODLE_MOBILE_USER_AGENT = "MoodleMobile 4.5.0"
        const val PUBLIC_CONFIG_REQUEST = """[{"index":0,"methodname":"tool_mobile_get_public_config","args":{}}]"""
        const val IDP_STEP_1_URL = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s1"
        const val IDP_STEP_2_URL = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s2"
        const val IDP_STEP_3_URL = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s3"
    }
}

/**
 * The material a Moodle login yields.
 *
 * @property wsToken Web service token carried by every Moodle web service call.
 * @property privateToken Private token that lets [ElearningAuthenticator.renewSession] mint a
 *   new browser session; null if Moodle didn't issue one.
 * @property moodleSessionCookie The `MoodleSession` browser cookie, needed by the web-page
 *   endpoints such as Kaltura video resolution; null if Moodle didn't set one.
 */
data class ElearningLogin(
    val wsToken: String,
    val privateToken: String?,
    val moodleSessionCookie: String?,
)

/**
 * A Moodle login that didn't complete.
 *
 * @property badCredentials Whether the IdP rejected the username and password, as opposed to
 *   the flow breaking somewhere else.
 */
class ElearningLoginException(
    message: String,
    val badCredentials: Boolean = false,
) : Exception(message)
