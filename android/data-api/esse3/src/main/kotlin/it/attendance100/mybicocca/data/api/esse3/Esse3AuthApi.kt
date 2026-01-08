package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import it.attendance100.mybicocca.data.api.esse3.Esse3AbstractApi.Companion.BASE_URL

/**
 * Initiates the OpenID Connect authentication flow.
 *
 * This endpoint redirects the user to the University of Milano-Bicocca's
 * identity provider (IdP) for authentication. The IdP will present the
 * university's login page where users can enter their credentials.
 *
 * @return A string that represents the URL of the auth provider
 */
const val ESSE3_LOGIN_URL = "$BASE_URL/auth/studente/HomePageStudente.do"

/**
 * API for authentication operations.
 */
class Esse3AuthApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    /**
     * Logs out and invalidates the session.
     */
    suspend fun logout() {
        executeGet("/Logout.do")
    }
}
