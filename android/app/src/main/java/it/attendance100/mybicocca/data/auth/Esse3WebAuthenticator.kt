package it.attendance100.mybicocca.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.Url
import it.attendance100.mybicocca.data.observability.HttpMetrics
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logs into Esse3's web portal (`s3w.si.unimib.it`) through the university's Shibboleth IdP, for
 * the pages its REST API doesn't cover. The REST API takes HTTP Basic credentials instead; the
 * portal only takes the service provider's session cookies (`JSESSIONID` and `_shibsession_*`),
 * which this SAML browser flow mints:
 *
 * 1. GET the student home page; the service provider redirects to the IdP's local-storage
 *    probe (`execution=e1s1`).
 * 2. POST the probe fields, which advances to `e1s2`.
 * 3. Route to the password flow (`auth_ctx=authn/Password`), which advances to `e1s3`.
 * 4. POST the credentials; the IdP answers with the auto-submitting SAMLResponse form.
 * 5. POST that form to the service provider, which sets its session cookies.
 *
 * The IdP steps are those of the Moodle login ([ElearningAuthenticator]); only the service
 * provider differs. Each login runs on its own short-lived client, so the cookie jar starts
 * empty and never carries one account's IdP session into another's.
 */
@Singleton
class Esse3WebAuthenticator @Inject constructor() {

    /**
     * @return The service provider's session cookies, for [Esse3WebApiFactory.create].
     * @throws Esse3WebLoginException On bad credentials or a broken login chain.
     */
    suspend fun login(username: String, password: String): List<Cookie> {
        val cookies = AcceptAllCookiesStorage()
        return buildClient(cookies).use { client ->
            runLogin(client, username, password)
            // Host-only cookies come back without a domain; the session client needs one to send them.
            val host = Url(PORTAL_URL).host
            cookies.get(Url(PORTAL_URL)).map { if (it.domain.isNullOrBlank()) it.copy(domain = host) else it }
        }
    }

    private suspend fun runLogin(client: HttpClient, username: String, password: String) {
        val entry = client.get(LOGIN_URL)
        if (entry.status != HttpStatusCode.OK) {
            throw Esse3WebLoginException("Cannot log in: the portal answered ${entry.status.value}")
        }

        client.submitForm(IDP_STEP_1_URL, Parameters.build {
            append("shib_idp_ls_exception.shib_idp_session_ss", "")
            append("shib_idp_ls_success.shib_idp_session_ss", "true")
            append("shib_idp_ls_value.shib_idp_session_ss", "")
            append("shib_idp_ls_exception.shib_idp_persistent_ss", "")
            append("shib_idp_ls_success.shib_idp_persistent_ss", "true")
            append("shib_idp_ls_value.shib_idp_persistent_ss", "")
            append("shib_idp_ls_supported", "true")
            append("_eventId_proceed", "")
        })

        client.get(IDP_STEP_2_URL)
        client.submitForm(IDP_STEP_2_URL, Parameters.build {
            append("_eventId_routing", "")
            append("selected_flow", "")
            append("auth_ctx", "authn/Password")
            append("spid_idp", "")
        })

        val credentials = client.submitForm(IDP_STEP_3_URL, Parameters.build {
            append("j_username", username)
            append("j_password", password)
            append("_eventId_proceed", "")
        })
        val samlPage = Jsoup.parse(credentials.bodyAsText(), credentials.request.url.toString())
        val samlForm = samlPage.selectFirst("form") as? FormElement
        if (samlForm?.selectFirst("input[name=SAMLResponse]") == null) {
            throw Esse3WebLoginException("Cannot log in: the IdP returned no SAML assertion", badCredentials = true)
        }
        val acsUrl = if (samlForm.hasAttr("action")) samlForm.absUrl("action") else samlForm.baseUri()

        val landing = client.submitForm(acsUrl, Parameters.build {
            samlForm.formData().forEach { append(it.key(), it.value()) }
        })
        if (landing.request.url.host == IDP_HOST) {
            throw Esse3WebLoginException("Cannot log in: still on the IdP after the SAML round-trip")
        }
    }

    private suspend fun HttpClient.submitForm(url: String, parameters: Parameters): HttpResponse =
        post(url) { setBody(FormDataContent(parameters)) }

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
        const val PORTAL_URL = "https://s3w.si.unimib.it/"
        const val LOGIN_URL = "https://s3w.si.unimib.it/auth/studente/HomePageStudente.do"
        const val IDP_HOST = "idp-idm.unimib.it"
        const val IDP_URL = "https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO"
        const val IDP_STEP_1_URL = "$IDP_URL?execution=e1s1"
        const val IDP_STEP_2_URL = "$IDP_URL?execution=e1s2"
        const val IDP_STEP_3_URL = "$IDP_URL?execution=e1s3"
    }
}

/**
 * The Esse3 web portal login failed.
 *
 * @property badCredentials Whether the IdP refused the credentials, rather than the chain breaking.
 */
class Esse3WebLoginException(
    message: String,
    val badCredentials: Boolean = false,
) : Exception(message)
