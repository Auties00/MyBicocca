package it.attendance100.mybicocca.data.remote.esse3.scraper.api

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
        for(opportunity in opportunities) {
            val detail = api.internships.getOpportunityDetail(opportunity)
            assertNotNull(detail.title)
            assertNotNull(detail.description)
            assertNotNull(detail.companyName)
        }
    }

    @Test
    suspend fun getSavedOpportunities() {
        val savedOpportunities = api.internships.getSavedOpportunities()
        assertNotNull(savedOpportunities)
    }

    @Test
    suspend fun manageSavedOpportunity() {
        val opportunities = api.internships.searchOpportunities(MOCK_SEARCH_TEXT)
        for(opportunity in opportunities) {
            if(opportunity.isSaved) {
                api.internships.saveOpportunity(opportunity)

                val savedOpportunities = api.internships.getSavedOpportunities()
                val isSaved = savedOpportunities.any { it.id == opportunity.id }
                assertTrue(isSaved)

                api.internships.unsaveOpportunity(opportunity)

                val verifyOpportunities = api.internships.getSavedOpportunities()
                val isUnsaved = verifyOpportunities.none { it.id == opportunity.id }
                assertTrue(isUnsaved)
            }
        }
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
        for(company in companies) {
            val companyInfo = api.internships.getCompanyInformation(company)
            assertNotNull(companyInfo.name)
            assertNotNull(companyInfo.description)
        }
    }

    @Test
    suspend fun getCompanyLogo() {
        val companies = api.internships.searchCompanies(MOCK_COMPANY_NAME)
        for(company in companies) {
            val logoChannel = api.internships.getCompanyLogo(company)
            assertNotNull(logoChannel)
            assertTrue(logoChannel.isPdf())
        }
    }

    @Test
    suspend fun getSavedSearches() {
        val savedSearches = api.internships.getSavedSearches()
        assertNotNull(savedSearches)
    }
}
