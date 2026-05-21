package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.plugin
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.remote.common.util.toHtml
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetPublicConfigResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningLoginResponse
import kotlinx.serialization.json.*
import org.jsoup.nodes.FormElement
import it.attendance100.mybicocca.data.remote.common.exception.AuthenticationException
import kotlin.io.encoding.Base64
import kotlin.text.substringAfter

/**
 * API for auth-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningAuthApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    companion object {
        internal const val MOODLE_SESSION_COOKIE_NAME = "MoodleSession"
        private const val AUTH_STEP_1_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s1"
        private const val AUTH_STEP_2_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s2"
        private const val AUTH_STEP_3_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s3"
    }

    /**
     * Authenticates a user using the OpenID Connect flow.
     *
     * @param username The user's email
     * @param password The user's password
     * @return [ElearningLoginResponse] containing the ws token
     */
    suspend fun login(username: String, password: String): ElearningLoginResponse {
        val loginUrl = getAuthUrl()
        val loginResponse = executeGet(loginUrl)
        when(loginResponse.status) {
            HttpStatusCode.OK -> {
                val loginPage = loginResponse.toHtml()
                val oidcUrl = loginPage.selectFirst("a[title=UniMiB]")
                    ?.attr("href")
                    ?: throw AuthenticationException("Cannot log in: missing login button")
                val samlResponse = executeGet(oidcUrl)
                if(samlResponse.status != HttpStatusCode.OK) {
                    throw AuthenticationException("Cannot log in: saml endpoint returned an invalid status code (status code: ${samlResponse.status.value})")
                }

                val shibResponse = executePost(AUTH_STEP_1_ENDPOINT) {
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
                if (shibResponse.status != HttpStatusCode.Found) {
                    throw AuthenticationException("Cannot log in: invalid status code at step one (status code: ${shibResponse.status})")
                }

                val loginPageResponse = executeGet(AUTH_STEP_2_ENDPOINT)
                if (loginPageResponse.status != HttpStatusCode.OK) {
                    throw AuthenticationException("Cannot login: invalid status code at step two (status code: ${loginPageResponse.status})")
                }

                val loginMethodResponse = executePost(AUTH_STEP_2_ENDPOINT) {
                    setBody(FormDataContent(Parameters.build {
                        append("_eventId_routing", "")
                        append("selected_flow", "")
                        append("auth_ctx", "authn/Password")
                        append("spid_idp", "")
                    }))
                }
                if (loginMethodResponse.status != HttpStatusCode.Found) {
                    throw AuthenticationException("Cannot log in: invalid status code at step three (status code: ${loginMethodResponse.status})")
                }

                val loginResponse = executePost(AUTH_STEP_3_ENDPOINT) {
                    setBody(FormDataContent(Parameters.build {
                        append("j_username", username)
                        append("j_password", password)
                        append("_eventId_proceed", "")
                    }))
                }
                val loginDocument = loginResponse.toHtml()
                val loginForm = loginDocument.selectFirst("form") as? FormElement
                    ?: throw AuthenticationException("Cannot log in: missing form at step three")
                val loginFormUrl = if(loginForm.hasAttr("action")) {
                    loginForm.absUrl("action")
                } else {
                    loginForm.baseUri()
                }
                val moduleResponse = executePost(loginFormUrl) {
                    setBody(FormDataContent(Parameters.build {
                        loginForm.formData().forEach {
                            append(it.key(), it.value())
                        }
                    }))
                }
                val redirectUrl = moduleResponse.headers[HttpHeaders.Location]
                    ?: throw AuthenticationException("Cannot log in: invalid status code at step four (status code: ${moduleResponse.status})")
                val redirectResponse = executeGet(redirectUrl)
                val finalUrl = redirectResponse.headers[HttpHeaders.Location]
                    ?: throw AuthenticationException("Cannot log in: invalid status code at step five (status code: ${redirectResponse.status})")
                return parseLoginResponse(finalUrl)
            }

            HttpStatusCode.Found -> {
                val resultUrl = loginResponse.headers[HttpHeaders.Location]
                    ?: throw AuthenticationException("Expected redirect (status code: ${loginResponse.status.value})")
                return parseLoginResponse(resultUrl)
            }

            else -> throw AuthenticationException("Cannot log in: saml endpoint returned an invalid status code (status code: ${loginResponse.status.value})")
        }
    }

    private suspend fun parseLoginResponse(finalUrl: String): ElearningLoginResponse.Success {
        val base64Tokens = finalUrl.substringAfter("token=")
        val decoded = Base64.decode(base64Tokens).decodeToString()
        val wsToken = decoded.split(":::").getOrNull(1)
            ?: throw AuthenticationException("Cannot log in: malformed mobile launch payload")
        return ElearningLoginResponse.Success(
            wsToken = wsToken,
            moodleSessionCookie = readMoodleSessionCookie(),
        )
    }

    private suspend fun readMoodleSessionCookie(): String? =
        client.plugin(HttpCookies).get(Url(BASE_URL))
            .firstOrNull { it.name == MOODLE_SESSION_COOKIE_NAME }
            ?.value

    /**
     * Gets the SSO authentication URL for user login.
     *
     * This URL should be opened in a browser. After successful authentication,
     * the browser will be redirected to a `moodlemobile://` URL containing
     * the web service token.
     *
     * ## Token Extraction
     *
     * The redirect URL format is:
     * `moodlemobile://token=BASE64_ENCODED_DATA`
     *
     * The BASE64 decoded data contains the token in the format:
     * `SITE_URL:::TOKEN:::OTHER_DATA`
     *
     * @return The authentication URL to open in a browser
     * @throws AuthenticationException If the public config cannot be retrieved
     */
    private suspend fun getAuthUrl(): String {
        val moodleRequest = buildJsonArray {
            add(buildJsonObject {
                put("index", 0)
                put("methodname", "tool_mobile_get_public_config")
                put("args", buildJsonObject {
                    // Empty args
                })
            })
        }

        val httpResponse = client.post("$BASE_URL/lib/ajax/service.php") {
            contentType(ContentType.Application.Json)
            setBody(moodleRequest)
        }

        val responseArray = httpResponse.body<JsonArray>()
        if (responseArray.isEmpty()) throw AuthenticationException("Empty response from server")
        val responseJson = responseArray[0].jsonObject

        if (responseJson.containsKey("error")) {
            val errorElement = responseJson["error"]
            if (errorElement is JsonPrimitive && errorElement.contentOrNull != "false") {
                throw AuthenticationException("Cannot get auth url: ${errorElement.contentOrNull ?: "unknown error"}")
            }
        }

        val data = responseJson["data"]
            ?: throw AuthenticationException("Invalid response: 'data' field missing")
        val config = json.decodeFromJsonElement<ElearningGetPublicConfigResponse>(data)

        val baseUrl = config.launchUrl ?: throw AuthenticationException("No auth url found")
        return URLBuilder(baseUrl).apply {
            parameters.append("service", "moodle_mobile_app")
            parameters.append("passport", (Math.random() * 1000).toString())
        }.buildString()
    }

    /**
     * Terminates the user session and revokes all associated tokens.
     * Client should clear local tokens regardless of response status.
     */
    fun logout(): Boolean {
        // TODO: Logout
        return true
    }
}
