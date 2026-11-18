package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.remote.common.exception.AuthenticationException
import it.attendance100.mybicocca.data.remote.common.util.cleanText
import it.attendance100.mybicocca.data.remote.common.util.extractQueryParam
import it.attendance100.mybicocca.data.remote.common.util.extractQueryParamAsInt
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Scraper API for the Esse3 student areas that have **no REST endpoint** on the
 * Bicocca instance (`s3w.si.unimib.it`).
 *
 * ## Why this exists
 *
 * The bulk of Segreterie is served by the 30 `e3rest` JSON services (wrapped by
 * the sibling `esse3` module). A handful of student flows were never ported to
 * REST and remain pure server-rendered Struts (`.do`) pages. The only one fully
 * modelled here is:
 *
 * - **Autocertificazioni / certificati** — self-declaration PDFs (the "Stampe"
 *   Jasper system has no REST surface).
 *
 * This class reaches it the only way left: it rides the authenticated Esse3
 * session cookie (`JSESSIONID`), GETs the `.do` page and parses the HTML with
 * JSoup, exposing DTOs shaped like the REST ones so the repository layer cannot
 * tell the difference.
 *
 * ## Session lifecycle (mirrors [ElearningKalturaApi] philosophy)
 *
 * This API is **stateless and owns no credentials**. The session cookie is
 * established once by the SSO login (see `Esse3GlobalApiData`/the app's auth
 * flow) and injected into the shared [HttpClient] by [Esse3Api]. When the cookie
 * has expired, Esse3 answers a `.do` request with a 302 to the identity provider
 * (`idp-idm.unimib.it`); Ktor follows it and we end up parsing the IdP login
 * page. [requireSession] detects this and raises [AuthenticationException] so the
 * caller can re-login and retry — this API never re-authenticates on its own.
 *
 * @param client Shared Ktor client with `HttpCookies` carrying the Esse3 session.
 */
class Esse3LegacyApi(
    client: HttpClient
) : Esse3AbstractApi(client) {

    companion object {
        private const val SELF_CERTIFICATIONS_ENTRYPOINT =
            "/auth/studente/Certificati/ListaCertificati.do?menu_opened_cod=menu_link-navbox_studenti_Segreteria"

        /** Host of the identity provider — a redirect here means the session died. */
        private const val IDP_HOST = "idp-idm.unimib.it"
    }

    /**
     * Lists the self-declaration templates the student can generate.
     *
     * @return The available self-certifications. Empty when none are configured.
     * @throws AuthenticationException If the session has expired.
     */
    suspend fun getSelfCertifications(): List<Esse3SelfCertification> {
        val doc = executeGet(SELF_CERTIFICATIONS_ENTRYPOINT)
        requireSession(doc)

        val content = mainContent(doc) ?: return emptyList()
        return content.select("a[href*=MessaggiCertificato.do], a[href*=cert_conf_id]")
            .mapNotNull { link ->
                val href = link.attr("href")
                val configurationId = extractQueryParamAsInt(href, "cert_conf_id") ?: return@mapNotNull null
                val documentId = extractQueryParamAsInt(href, "doc_id") ?: return@mapNotNull null
                val typeCode = extractQueryParam(href, "tipo_cert_cod").orEmpty()
                val solarYear = extractQueryParamAsInt(href, "annosolare")
                val signed = extractQueryParam(href, "firma_dig_flg")?.isNotBlank() == true

                Esse3SelfCertification(
                    configurationId = configurationId,
                    documentId = documentId,
                    description = link.cleanText(),
                    type = Esse3SelfCertificationType.fromCode(typeCode),
                    solarYear = solarYear,
                    digitallySigned = signed,
                    requestPath = href
                )
            }
            // The same template can appear twice (IT + EN); keep distinct configs.
            .distinctBy { it.configurationId to it.solarYear }
            .toList()
    }

    /**
     * Requests and downloads a self-declaration PDF.
     *
     * Esse3 routes the request link through an intermediate `MessaggiCertificato.do`
     * page that immediately streams the PDF; [executeGetRaw] follows the chain.
     *
     * @param certification A template from [getSelfCertifications].
     * @return The PDF as a byte channel.
     * @throws AuthenticationException If the session has expired (the body will be
     *   the IdP login HTML rather than a PDF; the caller should re-login).
     */
    suspend fun downloadSelfCertification(certification: Esse3SelfCertification): ByteReadChannel {
        return executeGetRaw(certification.requestPath).bodyAsChannel()
    }

    /**
     * Raises [AuthenticationException] when [doc] is the IdP login page, which is
     * what Esse3 serves (after a 302) once the session cookie has expired.
     */
    private fun requireSession(doc: Document) {
        val location = doc.location().orEmpty()
        val onIdp = location.contains(IDP_HOST, ignoreCase = true)
        val hasLoginForm = doc.selectFirst("input[name=j_username], form[action*=SAML2]") != null
        if (onIdp || hasLoginForm) {
            throw AuthenticationException(
                "Esse3 session expired: request redirected to the identity provider. Re-login and retry."
            )
        }
    }

    /**
     * The main content container, isolating the page body from the masthead menu
     * and footer.
     */
    private fun mainContent(doc: Document): Element? =
        doc.selectFirst("#gu-contentSx")
            ?: doc.selectFirst("#contentArea")
            ?: doc.selectFirst("[role=main]")
            ?: doc.body()
}
