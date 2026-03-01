package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.common.exception.AuthenticationException
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLoginResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappLogoutResponse
import kotlinx.serialization.json.Json


/**
 * API client for auth-related operations.
 */
class BicoccappAuthApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {
    companion object {
        private const val AUTH_OPEN_ID_ENDPOINT = "/auth/openid_connect"

        private const val AUTH_STEP_1_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/oidc/authorize?execution=e1s1"
        private const val AUTH_STEP_2_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/oidc/authorize?execution=e1s2"
        private const val AUTH_STEP_3_ENDPOINT = "https://idp-idm.unimib.it/idp/profile/oidc/authorize?execution=e1s3"
    }

    /**
     * Authenticates a user using the OpenID Connect flow.
     *
     * @param username The user's email
     * @param password The user's password
     * @return [BicoccappLoginResponse] containing the access token and user information
     */
    suspend fun login(username: String, password: String): BicoccappLoginResponse {
        val oidcResponse = executeGet(AUTH_OPEN_ID_ENDPOINT)
        when(oidcResponse.status) {
            HttpStatusCode.OK -> {
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

                val redirectUrl = loginResponse.headers[HttpHeaders.Location]
                    ?: throw AuthenticationException("Expected redirect (status code: ${loginResponse.status.value})")
                val finalResponse = executeGet(redirectUrl)
                val resultUrl = finalResponse.headers[HttpHeaders.Location]
                    ?.let { URLBuilder(it).build() }
                    ?: throw AuthenticationException("Expected redirect (status code: ${finalResponse.status.value})")
                return parseLoginResponse(resultUrl)
            }

            HttpStatusCode.Found -> {
                val resultUrl = oidcResponse.headers[HttpHeaders.Location]
                    ?.let { URLBuilder(it).build() }
                    ?: throw AuthenticationException("Expected redirect (status code: ${oidcResponse.status.value})")
                return parseLoginResponse(resultUrl)
            }

            else -> throw AuthenticationException("Cannot log in: openid endpoint returned an invalid status code (status code: ${oidcResponse.status.value})")
        }
    }

    private fun parseLoginResponse(finalUrl: Url): BicoccappLoginResponse {
        val params = parseQueryString(finalUrl.encodedQuery)
        val authenticated = params["user_authenticated"]?.toBoolean() ?: false
        if (!authenticated) {
            return BicoccappLoginResponse.Error("Authentication failed: invalid credentials")
        }

        val accessToken = params["access_token"]
            ?: return BicoccappLoginResponse.Error("Access token not found")
        val clientId = params["client"]
            ?: return BicoccappLoginResponse.Error("Client not found")
        val favouriteCareer = params["favourite_career"]?.toLongOrNull()
            ?: return BicoccappLoginResponse.Error("Favourite career not found")
        val fiscalCode = params["fiscal_code"]
            ?: return BicoccappLoginResponse.Error("Fiscal code not found")
        val id = params["id"]?.toLongOrNull()
            ?: return BicoccappLoginResponse.Error("ID not found")
        val uid = params["uid"]
            ?: return BicoccappLoginResponse.Error("UID not found")
        val userType = params["user_type"]

        return BicoccappLoginResponse.Success(
            accessToken = accessToken,
            client = clientId,
            favouriteCareer = favouriteCareer,
            fiscalCode = fiscalCode,
            id = id,
            uid = uid,
            userType = userType
        )
    }

    /**
     * Terminates the user session and revokes all associated tokens.
     * Client should clear local tokens regardless of response status.
     */
    suspend fun logout(): Boolean {
        val response: BicoccappLogoutResponse = executeJsonDelete("auth/sign_out")
        return response.success
    }
}
