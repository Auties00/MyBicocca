package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipSearchFilters
import it.attendance100.mybicocca.data.dto.esse3.Esse3InternshipType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3InternshipApi].
 */
class Esse3InternshipApiTest : Esse3TestBase() {
    @Test
    fun `searchOpportunities returns results`() {
        runBlocking {
            val opportunities = api.internships.searchOpportunities()

            assertNotNull(opportunities, "Opportunities should not be null")

            println("Internship opportunities found: ${opportunities.size}")
            opportunities.take(5).forEach { opp ->
                println("  - ${opp.title}")
                println("    Company: ${opp.company.name}")
                println("    Type: ${opp.type}")
                opp.applicationEnd?.let { println("    Deadline: $it") }
                if (opp.isSaved) println("    [SAVED]")
            }

            if (opportunities.size > 5) {
                println("  ... and ${opportunities.size - 5} more opportunities")
            }
        }
    }

    @Test
    fun `searchOpportunities with filters returns filtered results`() {
        runBlocking {
            val filters = Esse3InternshipSearchFilters(
                type = Esse3InternshipType.CURRICULAR
            )
            val opportunities = api.internships.searchOpportunities(filters)

            assertNotNull(opportunities, "Filtered opportunities should not be null")

            println("Curricular internship opportunities: ${opportunities.size}")
        }
    }

    @Test
    fun `getSavedOpportunities returns saved list`() {
        runBlocking {
            val saved = api.internships.getSavedOpportunities()

            assertNotNull(saved, "Saved opportunities should not be null")

            println("Saved internship opportunities: ${saved.size}")
            saved.forEach { opp ->
                println("  - ${opp.title} at ${opp.company.name}")
            }
        }
    }

    @Test
    fun `getApplications returns application list`() {
        runBlocking {
            val applications = api.internships.getApplications()

            assertNotNull(applications, "Applications should not be null")

            println("Internship applications: ${applications.size}")
            applications.forEach { app ->
                println("  - ${app.opportunityTitle}")
                println("    Company: ${app.companyName}")
                println("    Status: ${app.status}")
                app.applicationDate?.let { println("    Applied: $it") }
            }
        }
    }

    @Test
    fun `getInternships returns active internships`() {
        runBlocking {
            val internships = api.internships.getInternships()

            assertNotNull(internships, "Internships should not be null")

            println("Active internships: ${internships.size}")
            internships.forEach { internship ->
                println("  - ${internship.title}")
                println("    Company: ${internship.company.name}")
                println("    Status: ${internship.status}")
                internship.startDate?.let { println("    Started: $it") }
                internship.endDate?.let { println("    Ends: $it") }
            }
        }
    }

    @Test
    fun `searchCompanies returns company list`() {
        runBlocking {
            val companies = api.internships.searchCompanies()

            assertNotNull(companies, "Companies should not be null")

            println("Companies found: ${companies.size}")
            companies.take(5).forEach { company ->
                println("  - ${company.name}")
                company.sector?.let { println("    Sector: $it") }
                if (company.hasConvention == true) println("    [HAS CONVENTION]")
            }

            if (companies.size > 5) {
                println("  ... and ${companies.size - 5} more companies")
            }
        }
    }

    @Test
    fun `getSavedSearches returns search list`() {
        runBlocking {
            val searches = api.internships.getSavedSearches()

            assertNotNull(searches, "Saved searches should not be null")

            println("Saved searches: ${searches.size}")
            searches.forEach { search ->
                println("  - ${search.description}")
            }
        }
    }

    @Test
    fun `save and unsave opportunity modifies state correctly`() {
        runBlocking {
            // Step 1: Search for opportunities to find one to test with
            val opportunities = api.internships.searchOpportunities()
            if (opportunities.isEmpty()) {
                println("No opportunities found, skipping save/unsave test")
                return@runBlocking
            }

            // Find an opportunity that is NOT currently saved
            val targetOpportunity = opportunities.find { !it.isSaved }
            if (targetOpportunity == null) {
                println("All opportunities are already saved, cannot perform clean save test")
                return@runBlocking
            }

            println("Testing save/unsave on opportunity: ${targetOpportunity.id} - ${targetOpportunity.title}")

            // Step 2: Save the opportunity
            println("Saving opportunity...")
            val saveResult = api.internships.saveOpportunity(targetOpportunity.id)
            assertTrue(saveResult, "Save opportunity should succeed")

            // Step 3: Verify it is in the saved list
            val savedOpportunities = api.internships.getSavedOpportunities()
            val isSaved = savedOpportunities.any { it.id == targetOpportunity.id }
            assertTrue(isSaved, "Opportunity should be present in saved list after saving")
            println("Opportunity successfully saved")

            // Step 4: Unsave the opportunity (Rollback)
            println("Unsaving opportunity...")
            val unsaveResult = api.internships.unsaveOpportunity(targetOpportunity.id)
            assertTrue(unsaveResult, "Unsave opportunity should succeed")

            // Step 5: Verify it is removed from the saved list
            val savedOpportunitiesAfterUnsave = api.internships.getSavedOpportunities()
            val isStillSaved = savedOpportunitiesAfterUnsave.any { it.id == targetOpportunity.id }
            assertFalse(isStillSaved, "Opportunity should NOT be present in saved list after unsaving")
            println("Opportunity successfully unsaved")
        }
    }

    @Test
    fun `getOpportunityDetail returns detail data`() {
        runBlocking {
            val opportunities = api.internships.searchOpportunities()
            if (opportunities.isNotEmpty()) {
                val firstOpp = opportunities.first()
                val detail = api.internships.getOpportunityDetail(firstOpp.id)

                assertNotNull(detail, "Opportunity detail should not be null")
                assertEquals(firstOpp.id, detail!!.id, "Opportunity ID should match")
                println("Opportunity detail retrieved: ${detail.title}")
                detail.description?.let { println("  Description length: ${it.length}") }
            }
        }
    }

    @Test
    fun `getCompanyDetail returns company data`() {
        runBlocking {
            val companies = api.internships.searchCompanies()
            if (companies.isNotEmpty()) {
                val firstCompany = companies.first()
                val detail = api.internships.getCompanyDetail(firstCompany.id)

                assertNotNull(detail, "Company detail should not be null")
                assertEquals(firstCompany.id, detail!!.id, "Company ID should match")
                println("Company detail retrieved: ${detail.name}")
                detail.sector?.let { println("  Sector: $it") }
            }
        }
    }

    @Test
    fun `getCompanyLogo returns bytes`() {
        runBlocking {
            val companies = api.internships.searchCompanies()
            if (companies.isNotEmpty()) {
                val firstCompany = companies.first()
                val logoBytes = api.internships.getCompanyLogo(firstCompany.id)

                if (logoBytes != null) {
                    assertTrue(logoBytes.isNotEmpty(), "Logo bytes should not be empty")
                    println("Company logo retrieved: ${logoBytes.size} bytes")
                } else {
                    println("No logo available for company: ${firstCompany.name}")
                }
            }
        }
    }
}
