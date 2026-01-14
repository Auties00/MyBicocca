package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.dto.esse3.*
import kotlin.time.Duration.Companion.days

/**
 * API for internship and stage operations.
 *
 * Provides access to:
 * - Search internship opportunities
 * - View opportunity details
 * - Manage applications
 * - View active internships
 * - Save/unsave opportunities
 * - Search companies
 */
class Esse3InternshipApi(
    client: HttpClient
) : Esse3AbstractApi(client) {
    companion object {
        private const val SEARCH_OPPORTUNITIES_ENTRYPOINT = "/auth/tirocini/TiroSearchOpportunita.do"
        private const val OPPORTUNITY_DETAIL_ENTRYPOINT = "/auth/studente/tirocini/DettaglioOpportunita.do"
        private const val SAVED_OPPORTUNITIES_ENTRYPOINT = "/auth/studente/tirocini/OpportunitaSalvate.do"
        private const val SAVE_OPPORTUNITY_ENTRYPOINT = "/auth/tirocini/SalvaOpportunitaFav.do"
        private const val UNSAVE_OPPORTUNITY_ENTRYPOINT = "/auth/tirocini/RimuoviOpportunitaFav.do"
        private const val APPLICATIONS_ENTRYPOINT = "/auth/studente/tirocini/RiepilogoCandidature.do"
        private const val INTERNSHIPS_ENTRYPOINT = "/auth/studente/tirocini/MieiStage.do"
        private const val SEARCH_COMPANIES_ENTRYPOINT = "/auth/studente/tirocini/ElencoAziende.do"
        private const val COMPANY_DETAIL_ENTRYPOINT = "/auth/studente/tirocini/VisualizzaPresentazioneAzienda.do"
        private const val COMPANY_LOGO_ENTRYPOINT = "/auth/tirocini/DownloadLogoAzienda.do"
        private const val SAVED_SEARCHES_ENTRYPOINT = "/auth/tirocini/TiroListaSavedSearch.do"
        private const val DELETE_SEARCH_ENTRYPOINT = "/auth/tirocini/TiroDeleteSearch.do"

        private val DATE_REGEX = "(\\d{2}/\\d{2}/\\d{4})".toRegex()
    }

    /**
     * Extracts a query parameter from a URL or href string.
     */
    private fun extractQueryParam(href: String, vararg paramNames: String): String? {
        val queryString = href.substringAfter("?", "")
        val params = parseQueryString(queryString)
        return paramNames.firstNotNullOfOrNull { params[it] }
    }

    /**
     * Searches for internship opportunities.
     *
     * @param searchText Search text
     * @param type Type of internship
     * @param sector Sector ID
     * @param campaignId Campaign ID
     * @param disciplineAreaId Discipline area ID
     * @return List of internship opportunities
     */
    suspend fun searchOpportunities(
        searchText: String,
        type: Esse3InternshipType? = null,
        sector: String? = null,
        campaignId: Long? = null,
        disciplineAreaId: Long? = null
    ): List<Esse3InternshipOpportunity> {
        val formFields = mutableMapOf(
            "sbmFiltra" to "Cerca",
            "form_id_searchForm" to "searchForm",
            "advanced_search" to "0",
            "search_testo" to searchText
        )
        type?.let {
            val code = when (it) {
                is Esse3InternshipType.Curricular -> "WTIR_C"
                is Esse3InternshipType.Extracurricular -> "WTIR_E"
                is Esse3InternshipType.Cfu60 -> "60CFU"
                is Esse3InternshipType.Tfa -> "TFA"
                is Esse3InternshipType.Other -> it.code
            }
            formFields["tipoOpportunita"] = code
        }
        sector?.let { formFields["settore"] = it }
        campaignId?.let { formFields["campagna_id"] = it.toString() }
        disciplineAreaId?.let { formFields["area_disc_id"] = it.toString() }

        val doc = executePost(SEARCH_OPPORTUNITIES_ENTRYPOINT, formFields)

        return doc.select("div.box-1.tiro_box_FloatingBox3Col").mapNotNull { element ->
            val paragraphs = element.select("p.box-1-p")

            if (paragraphs.size < 4) {
                throw IllegalStateException("Cannot search opportunities: expected four paragraphs")
            }

            val anchor = paragraphs[0].selectFirst("a")
                ?: throw IllegalStateException("Cannot search opportunities: missing anchor")
            val title = anchor.text().trim()

            val href = anchor.attr("href")
            val id = extractQueryParam(href, "cnvz_off_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot search opportunities: missing id")

            val companyName = paragraphs[1]
                .text()
                .substringAfter("presso:", "")
                .trim()
                .takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Cannot search opportunities: missing company name")

            val applicationEndDateText = paragraphs[2].text()
                .substringAfter("Iscrizioni aperte fino al:", "")
                .cleanText()
                .takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Cannot search opportunities: missing application end date")
            val applicationEndDate = parseDate(applicationEndDateText)
                ?: throw IllegalStateException("Cannot search opportunities: invalid application end date '$applicationEndDateText'")

            val type = paragraphs[3]
                .text()
                .substringAfter("Tipo opportunità:", "")
                .cleanText()
                .let { Esse3InternshipType.fromDescription(it) }

            Esse3InternshipOpportunity(
                id = id,
                title = title,
                companyName = companyName,
                type = type,
                applicationEndDate = applicationEndDate,
                isSaved = false // TODO: Maybe we can get this data?
            )
        }
    }

    /**
     * Gets detailed opportunity information.
     *
     * @param opportunity The opportunity
     * @return The opportunity details
     */
    suspend fun getOpportunityDetail(opportunity: Esse3InternshipOpportunity): Esse3InternshipOpportunityDetail {
        val doc = executeGet(
            OPPORTUNITY_DETAIL_ENTRYPOINT,
            mapOf("cnvz_off_id" to opportunity.id.toString(), "from_page" to "TIRO_SEARCH_OPP")
        )

        val title = doc.pageTitle()
            ?: throw IllegalStateException("Cannot get opportunity detail: missing title")

        val detailLists = doc.select("dl.record-riga")

        val mainDetails = mutableMapOf<String, String>()
        val requirementDetails = mutableMapOf<String, String>()
        val languageDetails = mutableMapOf<String, String>()

        for ((index, dl) in detailLists.withIndex()) {
            val target = when (index) {
                0 -> mainDetails
                1 -> requirementDetails
                else -> languageDetails
            }
            for (dt in dl.select("dt")) {
                val key = dt.text().cleanText().removeSuffix(":").lowercase()
                val value = dt.nextElementSibling()?.text()?.cleanText() ?: ""
                target[key] = value
            }
        }

        val companyName = mainDetails["azienda"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing company name")

        val companyDescription = mainDetails["descrizione azienda"]
            ?: mainDetails["descizione azienda"]

        val description = mainDetails["descrizione opportunità"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing description")

        val trainingObjectives = mainDetails["obiettivi formativi"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing training objectives")

        val location = mainDetails["sede svolgimento"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing location")

        val functionalArea = mainDetails["area funzionale"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing functional area")

        val benefits = mainDetails["facilitazioni previste"]

        val expectedStartDateText = mainDetails["data indicativa inizio"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing expected start date")
        val expectedStartDate = parseDate(expectedStartDateText)
            ?: throw IllegalStateException("Cannot get opportunity detail: invalid start date '${expectedStartDateText}'")

        val expectedDurationText = mainDetails["durata indicativa prevista"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing expected duration")
        val expectedDurationTextSpaceIndex = expectedDurationText.indexOf(' ').takeIf { it != -1 }
            ?: throw IllegalStateException("Cannot get opportunity detail: invalid expected duration '${expectedDurationText}'")
        val expectedDurationTextNumber = expectedDurationText.take(expectedDurationTextSpaceIndex).toLongOrNull()
            ?: throw IllegalStateException("Cannot get opportunity detail: invalid expected duration '${expectedDurationText}'")
        val expectedDuration = when (expectedDurationText[expectedDurationTextSpaceIndex + 1]) {
            'g' -> expectedDurationTextNumber.days              // giorno/giorni
            's' -> (expectedDurationTextNumber * 7).days        // settimana/settimane
            'm' -> (expectedDurationTextNumber * 30).days       // mese/mesi
            'a' -> (expectedDurationTextNumber * 365).days      // anno/anni
            else -> throw IllegalStateException("Cannot get opportunity detail: invalid expected duration '${expectedDurationText}'")
        }

        val reservedFor = requirementDetails["riservato a"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing reserved for")
        val careerTypesText = requirementDetails["tipo carriera"]
            ?: throw IllegalStateException("Cannot get opportunity detail: missing career types")
        val careerTypes = Esse3CareerType.parseList(careerTypesText)

        val languages = languageDetails.map { (lang, level) ->
            Esse3LanguageRequirement(language = lang, level = level)
        }

        val typeText = doc.selectFirst("h2")?.text()?.cleanText() ?: ""
        val type = Esse3InternshipType.fromDescription(typeText)

        val isSaved = doc.selectFirst("a[href*=Rimuovi]") != null

        return Esse3InternshipOpportunityDetail(
            id = opportunity.id,
            title = title,
            type = type,
            companyName = companyName,
            companyDescription = companyDescription,
            description = description,
            trainingObjectives = trainingObjectives,
            location = location,
            functionalArea = functionalArea,
            benefits = benefits,
            expectedStartDate = expectedStartDate,
            expectedDuration = expectedDuration,
            requirements = Esse3InternshipRequirements(
                reservedFor = reservedFor,
                careerTypes = careerTypes,
                languages = languages
            ),
            applicationEndDate = opportunity.applicationEndDate,
            isSaved = isSaved
        )
    }

    /**
     * Gets saved (favorite) opportunities.
     *
     * @return List of saved opportunities
     */
    suspend fun getSavedOpportunities(): List<Esse3InternshipOpportunity> {
        val doc = executeGet(SAVED_OPPORTUNITIES_ENTRYPOINT)

        val table = doc.selectFirst("#tableOpportunitaSalvate")
            ?: throw IllegalStateException("Cannot get applications: missing table")

        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }

        return table.select("tbody tr").map { row ->
            val cells = row.select("td")
            val rowMap = headers.zip(cells).toMap()

            val link = rowMap["titolo"]?.selectFirst("a")
                ?: throw IllegalStateException("Cannot get saved opportunities: missing opportunity link")
            val id = extractQueryParam(link.attr("href"), "cnvz_off_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot get saved opportunities: missing opportunity ID")

            val title = rowMap["titolo"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get saved opportunities: missing title")

            val companyName = rowMap["azienda"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get saved opportunities: missing company name")

            val typeText = rowMap["tipo"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get saved opportunities: missing type")
            val type = Esse3InternshipType.fromDescription(typeText)

            val applicationEndDateText = rowMap["chiusura iscrizioni"]?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get saved opportunities: missing end date")
            val applicationEndDate = parseDate(applicationEndDateText)
                ?: throw IllegalStateException("Cannot get saved opportunities: invalid date '${applicationEndDateText}'")

            Esse3InternshipOpportunity(
                id = id,
                title = title,
                companyName = companyName,
                type = type,
                applicationEndDate = applicationEndDate,
                isSaved = true
            )
        }
    }

    /**
     * Saves an opportunity to favorites.
     *
     * @param opportunity The opportunity
     */
    suspend fun saveOpportunity(opportunity: Esse3InternshipOpportunity) {
        if(opportunity.isSaved) {
            return
        }

        val savedOpportunities = getSavedOpportunities()
        if(savedOpportunities.firstOrNull { it.id == opportunity.id } == null) {
            val response = executeGetRaw(
                SAVE_OPPORTUNITY_ENTRYPOINT,
                mapOf("cnvz_off_id" to opportunity.id.toString())
            )
            if (response.status != HttpStatusCode.OK) {
                throw IllegalStateException("Cannot save opportunity: status code ${response.status.value}")
            }
        }

        opportunity.isSaved = true
    }

    /**
     * Removes an opportunity from favorites.
     *
     * @param opportunity The opportunity
     */
    suspend fun unsaveOpportunity(opportunity: Esse3InternshipOpportunity) {
        if(!opportunity.isSaved) {
            return
        }

        val savedOpportunities = getSavedOpportunities()
        if(savedOpportunities.firstOrNull { it.id == opportunity.id } != null) {
            val response = executeGetRaw(
                UNSAVE_OPPORTUNITY_ENTRYPOINT,
                mapOf(
                    "cnvz_off_id" to opportunity.id.toString(),
                    "GO_TO_LISTA_OPP_SAVED" to "1"
                )
            )
            if(response.status == HttpStatusCode.InternalServerError) {
                throw IllegalStateException("Cannot unsave opportunity: there are duplicate saved opportunities with id '${opportunity.id}' and they cannot be deleted")
            } else if (response.status != HttpStatusCode.OK) {
                throw IllegalStateException("Cannot unsave opportunity: status code ${response.status.value}")
            }
        }

        opportunity.isSaved = false
    }

    /**
     * Gets the student's internship applications.
     *
     * @return List of applications
     */
    suspend fun getApplications(): List<Esse3InternshipApplication> {
        val doc = executeGet(APPLICATIONS_ENTRYPOINT)

        val table = doc.selectFirst("table.table-1")
            ?: return emptyList()

        val headers = table.select("thead tr th").map {
            val node = it.firstChild() ?: it
            node.nodeValue().trim().lowercase()
        }

        return table.select("tbody tr").map { row ->
            val cells = row.select("td")
            if (cells.size < 3) throw IllegalStateException("Cannot get applications: insufficient columns")

            val rowMap = headers.zip(cells).toMap()

            val applicationId = row.select("a[href*=cand_id], a[href*=candidatura_id]")
                .firstNotNullOfOrNull { element ->
                    extractQueryParam(element.attr("href"), "cand_id", "candidatura_id")?.toLongOrNull()
                }

            val opportunityLink = row.selectFirst("a[href*=cnvz_off_id]")
                ?: throw IllegalStateException("Cannot get applications: missing opportunity link")
            val opportunityId = extractQueryParam(opportunityLink.attr("href"), "cnvz_off_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot get applications: missing opportunity ID")

            val opportunityTitle = rowMap.entries.firstOrNull()?.value?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get applications: missing opportunity title")

            val companyName = rowMap.entries.drop(1).firstOrNull()?.value?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get applications: missing company name")

            val statusText = rowMap.entries.drop(2).firstOrNull()?.value?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get applications: missing status")
            val status = Esse3ApplicationStatus.fromString(statusText)

            val applicationDate = rowMap.entries.drop(3).firstOrNull()?.value?.text()?.let { parseDateTime(it) }

            val notes = rowMap.entries.drop(4).firstOrNull()?.value?.text()?.cleanText()?.takeIf { it.isNotBlank() }

            Esse3InternshipApplication(
                id = applicationId,
                opportunityId = opportunityId,
                opportunityTitle = opportunityTitle,
                companyName = companyName,
                status = status,
                applicationDate = applicationDate,
                notes = notes
            )
        }
    }

    /**
     * Gets the student's active internships.
     *
     * @return List of internships
     */
    suspend fun getInternships(): List<Esse3Internship> {
        val doc = executeGet(INTERNSHIPS_ENTRYPOINT)

        return doc.select("div.record, div.box-1").map { card ->
            val text = card.text()

            val title = card.selectFirst("h3, h4, strong")?.text()?.cleanText()
                ?: throw IllegalStateException("Cannot get internships: missing title")

            val internshipLink = card.selectFirst("a[href*=tiro_id], a[href*=stage_id]")
                ?: throw IllegalStateException("Cannot get internships: missing internship link")
            val internshipId = extractQueryParam(internshipLink.attr("href"), "tiro_id", "stage_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot get internships: missing internship ID")

            val opportunityId = card.selectFirst("a[href*=cnvz_off_id]")?.attr("href")?.let {
                extractQueryParam(it, "cnvz_off_id")?.toLongOrNull()
            }

            val companyLink = card.selectFirst("a[href*=sog_id]")
                ?: throw IllegalStateException("Cannot get internships: missing company link")
            val companyId = extractQueryParam(companyLink.attr("href"), "sog_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot get internships: missing company ID")
            val companyName = companyLink.text().cleanText()

            val dates = DATE_REGEX.findAll(text).map { it.groupValues[1] }.toList()
            val startDate = dates.getOrNull(0)?.let { parseDate(it) }
            val endDate = dates.getOrNull(1)?.let { parseDate(it) }

            val status = Esse3InternshipStatus.fromString(text)

            Esse3Internship(
                id = internshipId,
                opportunityId = opportunityId,
                title = title,
                companyId = companyId,
                companyName = companyName,
                startDate = startDate,
                endDate = endDate,
                status = status
            )
        }
    }

    /**
     * Searches for companies.
     *
     * @param companyName Company name to search
     * @param sector Sector ID
     * @param onlyWithConvention Only return companies with convention
     * @return List of companies
     */
    suspend fun searchCompanies(
        companyName: String,
        sector: String? = null,
        onlyWithConvention: Boolean = true,
    ): List<Esse3Company> {
        val formFields = mutableMapOf(
            "form_id_tiro-Aziende-formSearch" to "tiro-Aziende-formSearch",
            "sbmFiltra" to "Cerca",
            "ragione_sociale" to companyName,
            "cds_flg" to if (onlyWithConvention) "1" else "0",
            "advanced_search" to "0"
        )
        sector?.let { formFields["settore"] = it }

        val doc = executePost(SEARCH_COMPANIES_ENTRYPOINT, formFields)

        val table = doc.selectFirst("table.table-1")
            ?: return emptyList()

        return table.select("tbody tr").map { row ->
            val cells = row.select("td")
            if (cells.isEmpty()) throw IllegalStateException("Cannot search companies: empty row")

            val link = cells.firstOrNull()?.selectFirst("a")
                ?: throw IllegalStateException("Cannot search companies: missing company link")
            val id = extractQueryParam(link.attr("href"), "sog_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot search companies: missing company ID")

            val name = link.text().cleanText()
            val sectorText = cells.getOrNull(1)?.text()?.cleanText() ?: ""
            val hasConvention = cells.any { it.text().contains("convenzione", ignoreCase = true) }

            Esse3Company(
                id = id,
                name = name,
                sector = sectorText,
                hasConvention = hasConvention
            )
        }
    }

    /**
     * Gets company details.
     *
     * @param company The company
     * @return The company details
     */
    suspend fun getCompanyInformation(company: Esse3Company): Esse3CompanyInformation {
        val doc = executeGet(
            COMPANY_DETAIL_ENTRYPOINT,
            mapOf("sog_id" to company.id.toString())
        )

        val name = doc.pageTitle()
            ?: throw IllegalStateException("Cannot get company detail: missing name")

        val description = doc.selectFirst("div.record p, div.description")?.text()?.cleanText()
            ?: ""

        val locationsTable = doc.selectFirst("#tiro-anteprimaazi-tableSedi")
        val locations = locationsTable?.select("tbody tr")?.map { row ->
            val cells = row.select("td")
            Esse3CompanyLocation(
                address = cells.getOrNull(0)?.text()?.cleanText() ?: "",
                type = cells.getOrNull(1)?.text()?.cleanText() ?: "",
                email = cells.getOrNull(2)?.text()?.cleanText()?.takeIf { it.isNotBlank() }
            )
        } ?: emptyList()

        val conventionsTable = doc.selectFirst("#tiro-anteprimaazi-tableconvenzioni")
        val conventions = conventionsTable?.select("tbody tr")?.map { row ->
            val cells = row.select("td")
            Esse3Convention(
                name = cells.getOrNull(0)?.text()?.cleanText() ?: "",
                startDate = cells.getOrNull(1)?.text()?.cleanText()?.let { parseDate(it) },
                endDate = cells.getOrNull(2)?.text()?.cleanText()?.let { parseDate(it) },
                durationYears = cells.getOrNull(3)?.text()?.cleanText()?.toIntOrNull(),
                autoRenewal = cells.getOrNull(4)?.text()?.contains("S", ignoreCase = true) == true
            )
        } ?: emptyList()

        val logoUrl = doc.selectFirst("img[src*=DownloadLogoAzienda]")?.attr("src")?.let { src ->
            extractQueryParam(src, "allegato_id")?.let { allegatoId ->
                "$BASE_URL$COMPANY_LOGO_ENTRYPOINT?allegato_id=$allegatoId"
            }
        }

        return Esse3CompanyInformation(
            id = company.id,
            name = name,
            description = description,
            logoUrl = logoUrl,
            locations = locations,
            conventions = conventions
        )
    }

    /**
     * Gets company logo.
     *
     * @param company The company
     * @return The logo bytes
     */
    suspend fun getCompanyLogo(company: Esse3Company): ByteReadChannel {
        val response = executeGetRaw(
            COMPANY_LOGO_ENTRYPOINT,
            mapOf("allegato_id" to company.id.toString())
        )
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Cannot get company logo: status code ${response.status.value}")
        }
        return response.bodyAsChannel()
    }

    /**
     * Gets saved searches.
     *
     * @return List of saved searches
     */
    suspend fun getSavedSearches(): List<Esse3SavedSearch> {
        val doc = executeGet(SAVED_SEARCHES_ENTRYPOINT)

        return doc.select("div.saved-search, li.search-item, tr:has(td)").map { item ->
            val link = item.selectFirst("a[href*=search_id]")
                ?: throw IllegalStateException("Cannot get saved searches: missing search link")
            val id = extractQueryParam(link.attr("href"), "search_id")?.toLongOrNull()
                ?: throw IllegalStateException("Cannot get saved searches: missing search ID")

            val description = link.text().cleanText().takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Cannot get saved searches: missing description")

            Esse3SavedSearch(
                id = id,
                description = description
            )
        }
    }

    /**
     * Deletes a saved search.
     *
     * @param search The saved search
     */
    suspend fun deleteSavedSearch(search: Esse3SavedSearch) {
        val response = executeGetRaw(
            DELETE_SEARCH_ENTRYPOINT,
            mapOf("search_id" to search.id.toString())
        )
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Cannot delete saved search: status code ${response.status.value}")
        }
    }
}
