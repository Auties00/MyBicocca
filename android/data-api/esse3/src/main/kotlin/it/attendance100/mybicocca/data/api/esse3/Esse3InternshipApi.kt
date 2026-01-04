package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.*
import it.attendance100.mybicocca.data.dto.esse3.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * API for internship and stage operations.
 *
 * Provides access to:
 * - Search internship opportunities
 * - View company information
 * - Manage applications
 * - View active internships
 * - Save/unsave opportunities
 */
class Esse3InternshipApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    /**
     * Searches for internship opportunities.
     *
     * @param filters Search filters
     * @return List of internship opportunities
     */
    suspend fun searchOpportunities(
        filters: Esse3InternshipSearchFilters = Esse3InternshipSearchFilters()
    ): List<Esse3InternshipOpportunity> {
        val formFields = filters.toFormFields().toMutableMap()
        formFields["sbmFiltra"] = "Cerca"
        formFields["form_id_tiro-formSearchOpportunita"] = "tiro-formSearchOpportunita"

        val doc = executePost(
            "/auth/tirocini/TiroSearchOpportunita.do",
            formFields
        )
        return parseOpportunities(doc)
    }

    /**
     * Gets opportunity details.
     *
     * @param opportunityId The opportunity ID
     * @return The opportunity details or null if not found
     */
    suspend fun getOpportunityDetail(
        opportunityId: Long
    ): Esse3InternshipOpportunity? {
        val doc = executeGet(
            "/auth/studente/tirocini/DettaglioOpportunita.do",
            mapOf("cnvz_off_id" to opportunityId.toString(), "from_page" to "TIRO_SEARCH_OPP")
        )
        return parseOpportunityDetail(doc, opportunityId)
    }

    /**
     * Gets saved (favorite) opportunities.
     *
     * @return List of saved opportunities
     */
    suspend fun getSavedOpportunities(): List<Esse3InternshipOpportunity> {
        val doc = executeGet("/auth/studente/tirocini/OpportunitaSalvate.do")
        return parseOpportunities(doc)
    }

    /**
     * Saves an opportunity to favorites.
     *
     * @param opportunityId The opportunity ID
     * @return True if successful
     */
    suspend fun saveOpportunity(opportunityId: Long): Boolean {
        val response = executeGetRaw(
            "/auth/tirocini/SalvaOpportunitaFav.do",
            mapOf("cnvz_off_id" to opportunityId.toString())
        )
        return response.status.value in 200..399
    }

    /**
     * Removes an opportunity from favorites.
     *
     * @param opportunityId The opportunity ID
     * @return True if successful
     */
    suspend fun unsaveOpportunity(opportunityId: Long): Boolean {
        val response = executeGetRaw(
            "/auth/tirocini/RimuoviOpportunitaFav.do",
            mapOf("cnvz_off_id" to opportunityId.toString())
        )
        return response.status.value in 200..399
    }

    /**
     * Gets the student's internship applications.
     *
     * @return List of applications
     */
    suspend fun getApplications(): List<Esse3InternshipApplication> {
        val doc = executeGet("/auth/studente/tirocini/RiepilogoCandidature.do")
        return parseApplications(doc)
    }

    /**
     * Gets the student's active internships.
     *
     * @return List of internships
     */
    suspend fun getInternships(): List<Esse3Internship> {
        val doc = executeGet("/auth/studente/tirocini/MieiStage.do")
        return parseInternships(doc)
    }

    /**
     * Searches for companies.
     *
     * @param companyName Company name to search
     * @param sector Sector ID
     * @return List of companies
     */
    suspend fun searchCompanies(
        companyName: String? = null,
        sector: String? = null
    ): List<Esse3Company> {
        val formFields = mutableMapOf(
            "form_id_tiro-Aziende-formSearch" to "tiro-Aziende-formSearch",
            "sbmFiltra" to "Cerca",
            "cds_flg" to "1",
            "advanced_search" to "0"
        )
        companyName?.let { formFields["ragione_sociale"] = it }
        sector?.let { formFields["settore"] = it }

        val doc = executePost("/auth/studente/tirocini/ElencoAziende.do", formFields)
        return parseCompanies(doc)
    }

    /**
     * Gets company details.
     *
     * @param companyId The company ID
     * @return The company details or null if not found
     */
    suspend fun getCompanyDetail(companyId: Long): Esse3Company? {
        val doc = executeGet(
            "/auth/studente/tirocini/VisualizzaPresentazioneAzienda.do",
            mapOf("sog_id" to companyId.toString())
        )
        return parseCompanyDetail(doc, companyId)
    }

    /**
     * Gets company logo.
     *
     * @param companyId The company ID
     * @return The logo bytes or null
     */
    suspend fun getCompanyLogo(companyId: Long): ByteArray? = runCatching {
        val response = executeGetRaw(
            "/auth/tirocini/DownloadLogoAzienda.do",
            mapOf("sog_id" to companyId.toString())
        )
        response.body<ByteArray>()
    }.getOrNull()

    /**
     * Gets saved searches.
     *
     * @return List of saved searches
     */
    suspend fun getSavedSearches(): List<Esse3SavedSearch> {
        val doc = executeGet("/auth/tirocini/TiroListaSavedSearch.do")
        return parseSavedSearches(doc)
    }

    /**
     * Deletes a saved search.
     *
     * @param searchId The search ID
     * @return True if successful
     */
    suspend fun deleteSavedSearch(searchId: Long): Boolean {
        val response = executeGetRaw(
            "/auth/tirocini/TiroDeleteSearch.do",
            mapOf("search_id" to searchId.toString())
        )
        return response.status.value in 200..399
    }

    private fun parseOpportunities(doc: Document): List<Esse3InternshipOpportunity> {
        val opportunities = mutableListOf<Esse3InternshipOpportunity>()

        // Look for opportunity cards
        val cards = doc.select("div.box-1, div.tiro_box_FloatingBox3Col")
        for (card in cards) {
            val opportunity = parseOpportunityCard(card)
            if (opportunity != null) {
                opportunities.add(opportunity)
            }
        }

        return opportunities
    }

    private fun parseOpportunityCard(card: Element): Esse3InternshipOpportunity? {
        val text = card.text()

        // Extract title
        val title = card.selectFirst("h3, h4, .title, strong")?.text()?.cleanText()
            ?: text.substringBefore("presso:").trim().takeIf { it.isNotBlank() }
            ?: return null

        // Extract company name
        val companyName = "presso:\\s*(.+?)(?:Iscrizioni|Tipo|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()
            ?: card.selectFirst("a[href*=Azienda]")?.text()?.trim()
            ?: ""

        // Extract dates
        val startMatch = "dal:\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex().find(text)
        val applicationStart = startMatch?.groupValues?.get(1)?.let { parseDate(it)?.atStartOfDay() }

        val deadlineMatch = "al:\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex().find(text)
        val deadline = deadlineMatch?.groupValues?.get(1)?.let { parseDate(it)?.atStartOfDay() }

        // Extract type
        val typeMatch = "Tipo\\s*:?\\s*(curriculare|extracurriculare)".toRegex(RegexOption.IGNORE_CASE)
            .find(text)
        val type = typeMatch?.groupValues?.get(1)?.let { Esse3InternshipType.fromString(it) }
            ?: Esse3InternshipType.CURRICULAR

        // Extract ID from link
        val link = card.selectFirst("a[href*=cnvz_off_id]")?.attr("href")
        val idMatch = "cnvz_off_id=(\\d+)".toRegex().find(link ?: "")
        val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: return null

        // Extract company ID
        val companyLink = card.selectFirst("a[href*=sog_id]")?.attr("href")
        val companyIdMatch = "sog_id=(\\d+)".toRegex().find(companyLink ?: "")
        val companyId = companyIdMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

        // Extract sector from card
        val sector = "Settore\\s*:?\\s*(.+?)(?:\\s{2,}|Tipo|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract location
        val location = "(?:Sede|Luogo|Location)\\s*:?\\s*(.+?)(?:\\s{2,}|Tipo|Durata|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract duration
        val duration = "Durata\\s*:?\\s*(.+?)(?:\\s{2,}|Tipo|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract description snippet if present
        val description = card.selectFirst("p.description, div.description, .abstract")?.text()?.cleanText()

        return Esse3InternshipOpportunity(
            id = id,
            title = title,
            company = Esse3Company(
                id = companyId,
                name = companyName,
                description = null,
                sector = sector,
                logoUrl = if (companyId > 0) "$BASE_URL/auth/tirocini/DownloadLogoAzienda.do?sog_id=$companyId" else null,
                website = null
            ),
            type = type,
            applicationStart = applicationStart,
            applicationEnd = deadline,
            description = description,
            requirements = null,
            location = location,
            duration = duration,
            isSaved = card.selectFirst("a[href*=Rimuovi]") != null
        )
    }

    private fun parseOpportunityDetail(doc: Document, opportunityId: Long): Esse3InternshipOpportunity? {
        val title = doc.pageTitle() ?: "Opportunity $opportunityId"
        val text = doc.text()

        // Extract dates
        val startMatch = "Candidature dal\\s*(\\d{2}/\\d{2}/\\d{4}\\s*\\d{2}:\\d{2})".toRegex().find(text)
        val applicationStart = startMatch?.groupValues?.get(1)?.let { parseDateTime(it) }

        val endMatch = "al\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex().find(text)
        val applicationEnd = endMatch?.groupValues?.get(1)?.let { parseDate(it)?.atStartOfDay() }

        // Extract type
        val typeMatch = "(Tirocinio curriculare|Tirocinio extracurriculare)".toRegex()
            .find(text)
        val type = typeMatch?.groupValues?.get(1)?.let { Esse3InternshipType.fromString(it) }
            ?: Esse3InternshipType.CURRICULAR

        // Extract company info
        val record = doc.selectFirst("div.record")
        val recordText = record?.text() ?: ""
        val companyName = "Azienda\\s*(.+?)(?:\\s{2,}|Descizione|Descrizione|$)".toRegex()
            .find(recordText)?.groupValues?.get(1)?.trim() ?: ""

        // Extract company ID from link
        val companyLink = doc.selectFirst("a[href*=sog_id]")?.attr("href")
        val companyId = "sog_id=(\\d+)".toRegex().find(companyLink ?: "")?.groupValues?.get(1)?.toLongOrNull() ?: 0L

        // Extract description
        val description = doc.selectFirst("div.description, div:contains(Descrizione) + div, p.description")?.text()?.cleanText()
            ?: record?.selectFirst("p")?.text()?.cleanText()

        // Extract requirements
        val requirements = "(?:Requisiti|Requirements)\\s*:?\\s*(.+?)(?:\\s{2,}|Competenze|Sede|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract location
        val location = "(?:Sede|Luogo|Location)\\s*:?\\s*(.+?)(?:\\s{2,}|Durata|Requisiti|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract duration
        val duration = "Durata\\s*:?\\s*(.+?)(?:\\s{2,}|Sede|Requisiti|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        // Extract sector
        val sector = "Settore\\s*:?\\s*(.+?)(?:\\s{2,}|Tipo|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        return Esse3InternshipOpportunity(
            id = opportunityId,
            title = title,
            company = Esse3Company(
                id = companyId,
                name = companyName,
                description = null,
                sector = sector,
                logoUrl = if (companyId > 0) "$BASE_URL/auth/tirocini/DownloadLogoAzienda.do?sog_id=$companyId" else null,
                website = null
            ),
            type = type,
            applicationStart = applicationStart,
            applicationEnd = applicationEnd,
            description = description,
            requirements = requirements,
            location = location,
            duration = duration,
            isSaved = doc.selectFirst("a[href*=Rimuovi]") != null
        )
    }

    private fun parseApplications(doc: Document): List<Esse3InternshipApplication> {
        val applications = mutableListOf<Esse3InternshipApplication>()

        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 3) {
                    // Extract application ID from action links or hidden fields
                    val applicationId = row.select("a[href*=cand_id], a[href*=candidatura_id], input[name*=cand_id]")
                        .firstNotNullOfOrNull { element ->
                            val href = element.attr("href").ifBlank { element.attr("value") }
                            "(?:cand_id|candidatura_id)=(\\d+)".toRegex().find(href)?.groupValues?.get(1)?.toLongOrNull()
                        }
                        ?: row.selectFirst("input[type=radio], input[type=checkbox]")?.attr("value")?.toLongOrNull()

                    // Extract opportunity ID from link
                    val opportunityId = row.selectFirst("a[href*=cnvz_off_id]")?.attr("href")?.let {
                        "cnvz_off_id=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toLongOrNull()
                    } ?: 0L

                    // Extract notes column if present
                    val notes = cells.getOrNull(4)?.text()?.cleanText()?.takeIf { it.isNotBlank() }

                    applications.add(
                        Esse3InternshipApplication(
                            id = applicationId,
                            opportunityId = opportunityId,
                            opportunityTitle = cells[0].text().cleanText(),
                            companyName = cells[1].text().cleanText(),
                            status = Esse3ApplicationStatus.fromString(cells[2].text().cleanText()),
                            applicationDate = cells.getOrNull(3)?.text()?.let { parseDateTime(it) },
                            notes = notes
                        )
                    )
                }
            }
        }

        return applications
    }

    private fun parseInternships(doc: Document): List<Esse3Internship> {
        val internships = mutableListOf<Esse3Internship>()

        val cards = doc.select("div.record, div.box-1, tr:has(td)")
        for (card in cards) {
            val text = card.text()

            val title = card.selectFirst("h3, h4, strong")?.text()?.cleanText()
                ?: text.substringBefore("\n").trim().takeIf { it.isNotBlank() }
                ?: continue

            // Extract internship ID from links
            val internshipId = card.select("a[href*=tiro_id], a[href*=stage_id]")
                .firstNotNullOfOrNull { element ->
                    val href = element.attr("href")
                    "(?:tiro_id|stage_id)=(\\d+)".toRegex().find(href)?.groupValues?.get(1)?.toLongOrNull()
                } ?: 0L

            // Extract opportunity ID
            val opportunityId = card.selectFirst("a[href*=cnvz_off_id]")?.attr("href")?.let {
                "cnvz_off_id=(\\d+)".toRegex().find(it)?.groupValues?.get(1)?.toLongOrNull()
            }

            // Extract company info
            val companyName = "(?:Azienda|Ente)\\s*:?\\s*(.+?)(?:\\s{2,}|Tutor|Data|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim() ?: ""
            val companyLink = card.selectFirst("a[href*=sog_id]")?.attr("href")
            val companyId = "sog_id=(\\d+)".toRegex().find(companyLink ?: "")?.groupValues?.get(1)?.toLongOrNull() ?: 0L

            // Extract dates
            val startDate = "(?:Data inizio|Inizio)\\s*:?\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex()
                .find(text)?.groupValues?.get(1)?.let { parseDate(it) }
            val endDate = "(?:Data fine|Fine)\\s*:?\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex()
                .find(text)?.groupValues?.get(1)?.let { parseDate(it) }

            // Extract tutor
            val tutor = "Tutor\\s*:?\\s*(.+?)(?:\\s{2,}|Data|Crediti|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()

            // Extract credits
            val credits = "(?:Crediti|CFU)\\s*:?\\s*(\\d+)".toRegex()
                .find(text)?.groupValues?.get(1)?.toIntOrNull()

            // Determine status
            val status = when {
                text.contains("completato", ignoreCase = true) -> Esse3InternshipStatus.COMPLETED
                text.contains("sospeso", ignoreCase = true) -> Esse3InternshipStatus.SUSPENDED
                text.contains("annullato", ignoreCase = true) -> Esse3InternshipStatus.CANCELLED
                else -> Esse3InternshipStatus.ACTIVE
            }

            internships.add(
                Esse3Internship(
                    id = internshipId,
                    opportunityId = opportunityId,
                    title = title,
                    company = Esse3Company(
                        id = companyId,
                        name = companyName,
                        description = null,
                        sector = null,
                        logoUrl = if (companyId > 0) "$BASE_URL/auth/tirocini/DownloadLogoAzienda.do?sog_id=$companyId" else null,
                        website = null
                    ),
                    startDate = startDate,
                    endDate = endDate,
                    status = status,
                    tutor = tutor,
                    credits = credits
                )
            )
        }

        return internships
    }

    private fun parseCompanies(doc: Document): List<Esse3Company> {
        val companies = mutableListOf<Esse3Company>()

        val table = doc.selectFirst("table.table-1")
        if (table != null) {
            val rows = table.select("tbody tr, tr:has(td)")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.isNotEmpty()) {
                    val link = cells[0].selectFirst("a")
                    val idMatch = "sog_id=(\\d+)".toRegex().find(link?.attr("href") ?: "")
                    val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: continue

                    companies.add(
                        Esse3Company(
                            id = id,
                            name = link?.text()?.cleanText() ?: "",
                            description = null,
                            sector = cells.getOrNull(1)?.text()?.cleanText(),
                            logoUrl = "$BASE_URL/auth/tirocini/DownloadLogoAzienda.do?sog_id=$id",
                            website = null,
                            hasConvention = cells.any { it.text().contains("convenzione", ignoreCase = true) }
                        )
                    )
                }
            }
        }

        return companies
    }

    private fun parseCompanyDetail(doc: Document, companyId: Long): Esse3Company? {
        val title = doc.pageTitle() ?: return null
        val text = doc.text()

        val description = doc.selectFirst("div.record p, div.description")?.text()?.cleanText()
        val sector = "Settore\\s*:?\\s*(.+?)(?:\\s{2,}|$)".toRegex()
            .find(text)?.groupValues?.get(1)?.trim()

        return Esse3Company(
            id = companyId,
            name = title,
            description = description,
            sector = sector,
            logoUrl = "$BASE_URL/auth/tirocini/DownloadLogoAzienda.do?sog_id=$companyId",
            website = null
        )
    }

    private fun parseSavedSearches(doc: Document): List<Esse3SavedSearch> {
        val searches = mutableListOf<Esse3SavedSearch>()

        val items = doc.select("div.saved-search, li.search-item, tr:has(td)")
        for (item in items) {
            val link = item.selectFirst("a[href*=search_id]")
            val idMatch = "search_id=(\\d+)".toRegex().find(link?.attr("href") ?: "")
            val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: continue

            val text = item.text().cleanText()

            // Extract description (usually the first part)
            val description = link?.text()?.cleanText()
                ?: text.substringBefore(":").trim().takeIf { it.isNotBlank() }
                ?: text

            // Extract search text from criteria displayed
            val searchText = "(?:Testo|Keyword|Ricerca)\\s*:?\\s*(.+?)(?:\\s{2,}|Settore|Tipo|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()

            // Extract filters from the display text
            val filters = mutableMapOf<String, String>()
            "Settore\\s*:?\\s*(.+?)(?:\\s{2,}|Tipo|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()?.let { filters["settore"] = it }
            "Tipo\\s*:?\\s*(.+?)(?:\\s{2,}|Sede|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()?.let { filters["tipo"] = it }
            "(?:Sede|Luogo)\\s*:?\\s*(.+?)(?:\\s{2,}|$)".toRegex()
                .find(text)?.groupValues?.get(1)?.trim()?.let { filters["sede"] = it }

            searches.add(
                Esse3SavedSearch(
                    id = id,
                    description = description,
                    searchText = searchText,
                    filters = filters.toMap()
                )
            )
        }

        return searches
    }
}
