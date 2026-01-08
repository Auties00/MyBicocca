package it.attendance100.mybicocca.data.api.esse3

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Esse3InternshipApiTest : Esse3TestBase() {
    companion object {
        private const val MOCK_SEARCH_TEXT = "software"
        private const val MOCK_COMPANY_NAME = "Accenture"
    }

    @Test
    suspend fun searchOpportunities() {
        val opportunities = api.internships.searchOpportunities(MOCK_SEARCH_TEXT)
        assertNotNull(opportunities)
    }

    @Test
    suspend fun getOpportunityDetail() {
        val opportunities = api.internships.searchOpportunities(MOCK_SEARCH_TEXT)
        if (opportunities.isEmpty()) return

        val opportunity = opportunities.first()
        val detail = api.internships.getOpportunityDetail(opportunity)
        assertNotNull(detail.title)
        assertNotNull(detail.description)
        assertNotNull(detail.companyName)
    }

    @Test
    suspend fun getSavedOpportunities() {
        val savedOpportunities = api.internships.getSavedOpportunities()
        assertNotNull(savedOpportunities)
    }

    @Test
    suspend fun manageSavedOpportunity() {
        val opportunities = api.internships.searchOpportunities(MOCK_SEARCH_TEXT)
        if (opportunities.isEmpty()) return

        val unsavedOpportunity = opportunities.firstOrNull { !it.isSaved }
        if (unsavedOpportunity == null) return

        api.internships.saveOpportunity(unsavedOpportunity)

        val savedOpportunities = api.internships.getSavedOpportunities()
        val isSaved = savedOpportunities.any { it.id == unsavedOpportunity.id }
        assertTrue(isSaved)

        api.internships.unsaveOpportunity(unsavedOpportunity.id)

        val verifyOpportunities = api.internships.getSavedOpportunities()
        val isUnsaved = verifyOpportunities.none { it.id == unsavedOpportunity.id }
        assertTrue(isUnsaved)
    }

    @Test
    suspend fun getApplications() {
        val applications = api.internships.getApplications()
        assertNotNull(applications)
    }

    @Test
    suspend fun getInternships() {
        val internships = api.internships.getInternships()
        assertNotNull(internships)
    }

    @Test
    suspend fun searchCompanies() {
        val companies = api.internships.searchCompanies(MOCK_COMPANY_NAME)
        assertNotNull(companies)
    }

    @Test
    suspend fun getCompanyInformation() {
        val companies = api.internships.searchCompanies(MOCK_COMPANY_NAME)
        if (companies.isEmpty()) return

        val company = companies.first()
        val companyInfo = api.internships.getCompanyInformation(company)
        assertNotNull(companyInfo.name)
        assertNotNull(companyInfo.description)
    }

    @Test
    suspend fun getCompanyLogo() {
        val companies = api.internships.searchCompanies(MOCK_COMPANY_NAME)
        if (companies.isEmpty()) return

        val company = companies.first()
        val logoChannel = api.internships.getCompanyLogo(company)
        assertNotNull(logoChannel)
    }

    @Test
    suspend fun getSavedSearches() {
        val savedSearches = api.internships.getSavedSearches()
        assertNotNull(savedSearches)
    }
}
