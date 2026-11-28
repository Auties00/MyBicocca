package it.attendance100.mybicocca.data.remote.compass.signup

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Creates a RistoCloud / Fresh & Natural account through Compass's sign-up pages, so the app can
 * show its own form. There is no sign-up API: this drives the web form over HTTP (see
 * [CompassSignUpPages]) and throws [CompassSignUpUnavailable] whenever a page is not the one it
 * expects, so the caller can fall back to showing the web form itself.
 *
 * One client is one sign-up: it keeps its own cookie session, separate from the signed-in one, and
 * the captcha belongs to that session. The emailed code can be sent from any session ([activate]).
 */
class CompassSignUpClient(
    engine: HttpClientEngine = OkHttp.create(),
    private val baseUrl: String = "https://unibicocca.compasscloud.it",
) : AutoCloseable {

    private val client = HttpClient(engine) {
        install(HttpCookies) { storage = AcceptAllCookiesStorage() }
        install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        defaultRequest {
            header("User-Agent", USER_AGENT)
            header("Accept-Language", "it-IT,it;q=0.9")
        }
    }

    private var form: CompassSignUpForm? = null

    /**
     * Opens the sign-up: the app's session entry first (only such a session is served the form), then
     * the form page, read for its hidden fields.
     */
    suspend fun start(): CompassSignUpForm {
        client.get("$baseUrl/api/apps/v1/mylunch/auth/getToken").bodyAsText()
        val page = client.get("$baseUrl/area-riservata/register")
        if (!page.status.isSuccess() || "/area-riservata/regist" !in page.call.request.url.encodedPath) {
            throw CompassSignUpUnavailable("The sign-up form was not served (landed on ${page.call.request.url})")
        }
        return CompassSignUpPages.parseForm(page.bodyAsText()).also { form = it }
    }

    /** A new captcha image (PNG) for this session; each call replaces the previous one. */
    suspend fun captcha(): ByteArray {
        val response = client.get("$baseUrl/api/utilities/captcha?${System.currentTimeMillis()}")
        if (!response.status.isSuccess() || response.contentType()?.contentType != "image") {
            throw CompassSignUpUnavailable("The captcha is no longer an image (${response.status}, ${response.contentType()})")
        }
        return response.bodyAsBytes()
    }

    /** Whether Compass would take [email]; null when the page offers no check to ask. */
    suspend fun checkEmail(email: String): CompassEmailCheck? {
        val key = requireForm().emailCheckKey ?: return null
        val response = client.submitForm(
            url = "$baseUrl/area-riservata/check_email",
            formParameters = parameters {
                append("email", email)
                append("v", key)
            },
        ) { header("X-Requested-With", "XMLHttpRequest") }
        return CompassSignUpPages.parseEmailCheck(response.bodyAsText())
    }

    /** Sends the form. Refused input comes back with Compass's own reasons, ready to show. */
    suspend fun submit(input: CompassSignUpInput): CompassSignUpReply {
        val form = requireForm()
        val response = client.submitForm(
            url = form.action,
            formParameters = parameters {
                form.hiddenFields.forEach { (name, value) -> append(name, value) }
                append("nome", input.firstName)
                append("cognome", input.lastName)
                append("email", input.email)
                append("password", input.password)
                append("password_confirm", input.passwordConfirm)
                append("captcha", input.captcha)
                append("privacy", "1")
                append("registrati", "")
            },
        )
        return CompassSignUpPages.parseSubmitReply(response.readPage())
    }

    /** Sends the code emailed after [submit]. Works from a fresh session too. */
    suspend fun activate(email: String, code: String): CompassActivationReply {
        val response = client.submitForm(
            url = "$baseUrl/area_riservata/convalida_immediata",
            formParameters = parameters {
                append("username", email)
                append("codice_attivazione", code)
                append("invia", "")
            },
        )
        return CompassSignUpPages.parseActivationReply(response.readPage())
    }

    override fun close() {
        client.close()
    }

    private fun requireForm(): CompassSignUpForm =
        form ?: throw IllegalStateException("start() first: the form's fields come from its page")

    private suspend fun HttpResponse.readPage(): String {
        if (!status.isSuccess()) throw CompassSignUpUnavailable("Compass answered $status")
        return bodyAsText()
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/133.0 Mobile Safari/537.36"
    }
}

/** The form as served: where it posts, its hidden fields, and the key its email check sends. */
class CompassSignUpForm internal constructor(
    val action: String,
    val hiddenFields: Map<String, String>,
    val emailCheckKey: String?,
)

/** What the user typed. */
data class CompassSignUpInput(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val captcha: String,
)

sealed interface CompassEmailCheck {
    data object Usable : CompassEmailCheck

    /** Already used, or refused for another reason; [message] is Compass's own, as plain text. */
    data class Refused(val message: String) : CompassEmailCheck
}

sealed interface CompassSignUpReply {
    /** Accepted: Compass emailed a code to [email] and waits for it. */
    data class NeedsActivation(val email: String) : CompassSignUpReply

    /** Refused, with Compass's reasons ("Captcha non valido.", the password rules…); may be empty. */
    data class Refused(val reasons: List<String>) : CompassSignUpReply
}

sealed interface CompassActivationReply {
    data object Activated : CompassActivationReply

    /** Wrong or expired code; [message] is Compass's explanation. */
    data class Failed(val message: String) : CompassActivationReply
}

/** A page did not look like the sign-up page it should be: show the web form instead. */
class CompassSignUpUnavailable(message: String, cause: Throwable? = null) : Exception(message, cause)

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** The page readers, apart from the HTTP calls so they can be tested on saved pages. */
internal object CompassSignUpPages {

    private val RequiredFields = listOf("nome", "cognome", "email", "password", "password_confirm", "captcha")
    private val EmailCheckKey = Regex("""check_email'\s*,\s*\{\s*email\s*:\s*email\s*,\s*v\s*:\s*'([^']+)'""")

    fun parseForm(html: String): CompassSignUpForm {
        val doc = org.jsoup.Jsoup.parse(html)
        val form = doc.selectFirst("form.registration_form")
            ?: throw CompassSignUpUnavailable("No registration form on the page")
        val action = form.attr("action").takeIf { "/area-riservata/registration" in it }
            ?: throw CompassSignUpUnavailable("The form posts somewhere else: ${form.attr("action")}")
        val names = form.select("input[name]").map { it.attr("name") }.toSet()
        val missing = RequiredFields.filterNot { it in names }
        if (missing.isNotEmpty()) throw CompassSignUpUnavailable("The form lost its fields: $missing")
        val hidden = form.select("input[type=hidden][name]").associate { it.attr("name") to it.attr("value") }
        return CompassSignUpForm(action, hidden, EmailCheckKey.find(html)?.groupValues?.get(1))
    }

    fun parseEmailCheck(body: String): CompassEmailCheck {
        val reply = runCatching { lenientJson.parseToJsonElement(body).jsonObject }.getOrElse {
            throw CompassSignUpUnavailable("The email check is no longer JSON", it)
        }
        val status = reply["status"]?.jsonPrimitive?.content
        val message = reply["messaggio"]?.jsonPrimitive?.content.orEmpty()
        return if (status == "success") {
            CompassEmailCheck.Usable
        } else {
            CompassEmailCheck.Refused(org.jsoup.Jsoup.parse(message).text().trim())
        }
    }

    fun parseSubmitReply(html: String): CompassSignUpReply {
        val doc = org.jsoup.Jsoup.parse(html)
        doc.selectFirst("form[action*=convalida_immediata]")?.let { activation ->
            val email = activation.selectFirst("input[name=username]")?.attr("value").orEmpty()
            return CompassSignUpReply.NeedsActivation(email)
        }
        if (doc.selectFirst("form.registration_form") == null) {
            throw CompassSignUpUnavailable("The reply is neither the form nor the activation page")
        }
        // "ATTENZIONE: il modulo contiene degli errori…:<br> - Captcha non valido.<br> - …"
        val notice = doc.selectFirst(".avvisi")?.html().orEmpty()
        val reasons = notice.split(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE))
            .map { org.jsoup.Jsoup.parse(it).text().trim() }
            .filter { it.startsWith("-") }
            .map { it.removePrefix("-").trim() }
            .filter { it.isNotEmpty() }
        return CompassSignUpReply.Refused(reasons)
    }

    fun parseActivationReply(html: String): CompassActivationReply {
        val doc = org.jsoup.Jsoup.parse(html)
        val headline = doc.selectFirst("p.enfasi")?.text().orEmpty()
        return when {
            "ATTIVATO" in headline.uppercase() -> CompassActivationReply.Activated
            "ERRORE" in headline.uppercase() -> CompassActivationReply.Failed(
                doc.selectFirst("p.enfasi")?.nextElementSibling()?.text()?.trim().orEmpty().ifEmpty { headline },
            )
            else -> throw CompassSignUpUnavailable("The activation reply is not one this client knows")
        }
    }
}
