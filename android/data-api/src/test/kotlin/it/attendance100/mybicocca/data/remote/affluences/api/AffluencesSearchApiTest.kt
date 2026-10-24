package it.attendance100.mybicocca.data.remote.affluences.api

import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteSearchRequest
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull

class AffluencesSearchApiTest : AffluencesTestBase() {
    companion object {
        /**
         * Identifier of the "Biblioteca universitaria" category, stable on the production
         * backend.
         */
        private const val UNIVERSITY_LIBRARY_CATEGORY_ID = 20
    }

    @Test
    suspend fun searchSites() {
        val results = api.search.searchSites("bicocca")
        assertTrue(results.sites.isNotEmpty(), "The query should match at least one site")
        assertTrue(
            results.sites.any { it.slug == ATENEO_LIBRARY_SLUG },
            "The query should match the Bicocca library system"
        )
        results.sites.forEach { site ->
            assertTrue(site.id.isNotBlank(), "Site id should not be blank")
            assertTrue(site.slug.isNotBlank(), "Site slug should not be blank")
            assertFalse(site.primaryName.isNullOrBlank(), "Site primary name should not be blank")
        }
    }

    @Test
    suspend fun searchSitesFiltered() {
        val page = api.search.searchSitesFiltered(
            AffluencesSiteSearchRequest(
                searchQuery = "bicocca",
                page = 0,
                selectedCategories = listOf(UNIVERSITY_LIBRARY_CATEGORY_ID)
            )
        )
        assertEquals(1, page.nextPage, "The page following the requested one should be returned")
        assertTrue(page.maxSize > 0, "The page size should be positive")
        assertTrue(page.results.isNotEmpty(), "The query should match at least one site")
        assertTrue(
            page.results.any { it.slug == ATENEO_LIBRARY_SLUG },
            "The query should match the Bicocca library system"
        )
        page.results.forEach { site ->
            assertTrue(site.id.isNotBlank(), "Site id should not be blank")
            assertTrue(site.primaryName.isNotBlank(), "Site primary name should not be blank")
            assertTrue(
                site.categories.any { it.id == UNIVERSITY_LIBRARY_CATEGORY_ID },
                "Results should belong to the requested category"
            )
        }
    }

    /** The searched coordinates are those of the Bicocca campus. */
    @Test
    suspend fun searchSitesOnMap() {
        val results = api.search.searchSitesOnMap(
            AffluencesSiteSearchRequest(
                latitude = 45.518,
                longitude = 9.21375
            )
        )
        assertTrue(results.results.isNotEmpty(), "The map search should match at least one site")
        assertNotNull(results.center, "The map center should be present")
        assertTrue(results.center.latitude in -90.0..90.0, "Center latitude should be valid")
        assertTrue(results.center.longitude in -180.0..180.0, "Center longitude should be valid")
    }

    @Test
    suspend fun getSiteFilters() {
        val filters = api.search.getSiteFilters()
        assertTrue(filters.categories.isNotEmpty(), "Categories should not be empty")
        assertTrue(
            filters.categories.any { it.id == UNIVERSITY_LIBRARY_CATEGORY_ID },
            "Categories should include university libraries"
        )
        filters.categories.forEach { category ->
            assertTrue(category.name.isNotBlank(), "Category name should not be blank")
        }
        assertTrue(filters.services.isNotEmpty(), "Services should not be empty")
        filters.services.forEach { service ->
            assertTrue(service.id > 0, "Service id should be positive")
            assertTrue(service.name.isNotBlank(), "Service name should not be blank")
        }
    }

    @Test
    suspend fun getPlaylistWithUnknownIdentifierThrows() {
        val error = runCatching { api.search.getPlaylist(Int.MAX_VALUE) }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Unknown playlists should be reported as AffluencesException")
        assertEquals("error_not_found", affluencesError.errorCode, "Error code should identify the missing playlist")
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    @Test
    suspend fun getSuggestedCategories() {
        val categories = api.search.getSuggestedCategories()
        assertTrue(categories.isNotEmpty(), "Suggested categories should not be empty")
        categories.forEach { category ->
            assertTrue(category.name.isNotBlank(), "Suggested category name should not be blank")
            assertTrue(category.categoryIds.isNotEmpty(), "Suggested category should group at least one category")
        }
    }

    @Test
    suspend fun getSuggestedPlaylists() {
        val highlighted = api.search.getSuggestedPlaylists(highlighted = true)
        highlighted.forEach { playlist ->
            assertTrue(playlist.id > 0, "Playlist id should be positive")
        }

        val regular = api.search.getSuggestedPlaylists(highlighted = false)
        regular.forEach { playlist ->
            assertTrue(playlist.id > 0, "Playlist id should be positive")
        }
    }

    @Test
    suspend fun getSuggestedSites() {
        val sites = api.search.getSuggestedSites()
        sites.forEach { site ->
            assertFalse(site.id.isNullOrBlank(), "Suggested site id should not be blank")
        }
    }
}
