package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.remote.common.exception.AuthenticationException
import it.attendance100.mybicocca.data.remote.common.util.toHtml
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3AbstractApi.Companion.BASE_URL
import org.jsoup.nodes.FormElement

/**
 * Initiates the Shibboleth SAML authentication flow.
 *
 * GETting this URL while unauthenticated kicks off the redirect chain to the
 * University of Milano-Bicocca identity provider (`idp-idm.unimib.it`), which
 * renders the credentials page. After a successful SAML assertion the SP
 * (`s3w.si.unimib.it`) issues the session cookie (`JSESSIONID`).
 */
const val ESSE3_LOGIN_URL = "$BASE_URL/auth/studente/HomePageStudente.do"

/**
 * API for authentication operations.
 *
 * ## Web SSO (legacy scrape) session
 *
 * Unlike the REST `esse3` module — which authenticates `/e3rest` with an OIDC
 * Bearer JWT — the legacy Struts (`.do`) pages can only be reached with a real
 * SP **web session cookie** (`JSESSIONID`). That cookie is minted by the
 * Shibboleth SAML2 browser flow, which is implemented here in [login].
 *
 * The flow (verified live against the test account on 2026-06-05) is:
 *
 * 1. GET [ESSE3_LOGIN_URL]; the SP 302-redirects (Ktor follows) to the IdP
 *    `shib_idp_ls` local-storage probe page (`execution=e1s1`).
 * 2. POST the `shib_idp_ls_*` probe fields to `e1s1` → IdP advances to `e1s2`.
 * 3. GET then POST `e1s2` with `auth_ctx=authn/Password` to route to the
 *    username/password flow → IdP advances to `e1s3`.
 * 4. POST `j_username`/`j_password` to `e1s3`. The IdP answers with the
 *    SAMLResponse auto-submit form (action `…/Shibboleth.sso/SAML2/POST`).
 * 5. POST that form back to the SP ACS; the SP sets `JSESSIONID` on
 *    `s3w.si.unimib.it`. We read the cookie jar and return the SP cookies.
 *
 * These IdP steps are the exact ones the Elearning Moodle SAML login uses (same
 * IdP, same `execution=e1sN` cadence); only the SP entry point and the ACS POST
 * target differ. Steps 1–7 (incl. a follow-up GET of `ListaCertificati.do`
 * returning real HTML) were confirmed live; this Kotlin mirrors that capture.
 */
class Esse3AuthApi(
    client: HttpClient
) : Esse3AbstractApi(client) {

    companion object {
        private const val IDP_BASE = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO"
        private const val AUTH_STEP_1_ENDPOINT = "$IDP_BASE?execution=e1s1"
        private const val AUTH_STEP_2_ENDPOINT = "$IDP_BASE?execution=e1s2"
        private const val AUTH_STEP_3_ENDPOINT = "$IDP_BASE?execution=e1s3"

        /** Host of the identity provider — landing here after login means it failed. */
        private const val IDP_HOST = "idp-idm.unimib.it"

        /**
         * Performs the full Shibboleth SAML web-login on a throwaway cookie-jar
         * [HttpClient] and returns the resulting `s3w.si.unimib.it` session cookies
         * (chiefly `JSESSIONID` and the `_shibsession_*` cookie). Seed a fresh
         * [Esse3Api] with these to get an authenticated legacy session.
         *
         * The client used here is created and closed internally; only the extracted
         * cookies survive. This keeps login independent from the long-lived
         * authenticated [Esse3Api] client, mirroring how the app's Elearning login
         * runs on a dedicated client and hands back the `MoodleSession` cookie.
         *
         * @throws AuthenticationException on any unexpected hop (bad credentials,
         *   missing SAML form, or a final landing still on the IdP).
         */
        suspend fun login(
            username: String,
            password: String,
            httpClientConfig: HttpClientConfig<*>.() -> Unit = {},
        ): List<Cookie> {
            val client = HttpClient {
                httpClientConfig()
                install(HttpCookies)
                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000L
                    connectTimeoutMillis = 30_000L
                    socketTimeoutMillis = 30_000L
                }
                followRedirects = true
            }
            return try {
                performLogin(client, username, password)
                val host = Url(BASE_URL).host
                // Ktor stores host-only cookies (no explicit Domain attribute) with a null
                // domain; Esse3Api.addCookie skips cookies whose domain is null, so backfill
                // the SP host here to guarantee the session cookies are actually re-applied.
                client.plugin(HttpCookies).get(Url(BASE_URL)).map { cookie ->
                    if (cookie.domain.isNullOrBlank()) cookie.copy(domain = host) else cookie
                }
            } finally {
                client.close()
            }
        }

        private suspend fun performLogin(client: HttpClient, username: String, password: String) {
            // 1. Enter via the SP; Ktor follows the 302 chain to the IdP e1s1 probe page.
            val entry = client.get(ESSE3_LOGIN_URL)
            if (entry.status != HttpStatusCode.OK) {
                throw AuthenticationException("Cannot log in: SP entry returned ${entry.status.value}")
            }

            // 2. Submit the local-storage probe to advance e1s1 -> e1s2.
            client.submitForm(AUTH_STEP_1_ENDPOINT, parameters {
                append("shib_idp_ls_exception.shib_idp_session_ss", "")
                append("shib_idp_ls_success.shib_idp_session_ss", "true")
                append("shib_idp_ls_value.shib_idp_session_ss", "")
                append("shib_idp_ls_exception.shib_idp_persistent_ss", "")
                append("shib_idp_ls_success.shib_idp_persistent_ss", "true")
                append("shib_idp_ls_value.shib_idp_persistent_ss", "")
                append("shib_idp_ls_supported", "true")
                append("_eventId_proceed", "")
            })

            // 3. Route to the password flow (e1s2 -> e1s3).
            client.get(AUTH_STEP_2_ENDPOINT)
            client.submitForm(AUTH_STEP_2_ENDPOINT, parameters {
                append("_eventId_routing", "")
                append("selected_flow", "")
                append("auth_ctx", "authn/Password")
                append("spid_idp", "")
            })

            // 4. Submit credentials; the IdP answers with the SAMLResponse auto-submit form.
            val credentialResponse = client.submitForm(AUTH_STEP_3_ENDPOINT, parameters {
                append("j_username", username)
                append("j_password", password)
                append("_eventId_proceed", "")
            })
            val samlDocument = credentialResponse.toHtml()
            val samlForm = samlDocument.selectFirst("form") as? FormElement
                ?: throw AuthenticationException("Cannot log in: missing SAMLResponse form (bad credentials?)")
            if (samlForm.selectFirst("input[name=SAMLResponse]") == null) {
                throw AuthenticationException("Cannot log in: IdP did not return a SAML assertion (bad credentials?)")
            }
            val acsUrl = if (samlForm.hasAttr("action")) samlForm.absUrl("action") else samlForm.baseUri()

            // 5. POST the assertion back to the SP ACS; this sets the JSESSIONID.
            val acsResponse = client.submitForm(acsUrl, parameters {
                samlForm.formData().forEach { append(it.key(), it.value()) }
            })
            val landing = acsResponse.request.url.toString()
            if (landing.contains(IDP_HOST, ignoreCase = true)) {
                throw AuthenticationException("Cannot log in: still on the identity provider after the SAML round-trip")
            }
        }

        private suspend fun HttpClient.submitForm(url: String, params: Parameters): HttpResponse =
            post(url) { setBody(FormDataContent(params)) }
    }

    /**
     * Logs out and invalidates the session.
     */
    suspend fun logout() {
        executeGet("/Logout.do")
    }
}
