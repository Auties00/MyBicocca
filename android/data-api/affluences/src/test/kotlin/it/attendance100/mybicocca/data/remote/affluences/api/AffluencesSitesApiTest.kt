package it.attendance100.mybicocca.data.remote.affluences.api

import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import java.time.LocalDate

class AffluencesSitesApiTest : AffluencesTestBase() {
    companion object {
        private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
        private val DATE_TIME_REGEX = Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""")
    }

    @Test
    suspend fun getSite() {
        val site = api.sites.getSite(ATENEO_LIBRARY_SLUG)
        assertEquals(ATENEO_LIBRARY_SLUG, site.slug, "Slug should match the requested site")
        assertTrue(site.id.isNotBlank(), "Site id should not be blank")
        assertTrue(site.primaryName.isNotBlank(), "Primary name should not be blank")
        assertNotNull(site.booking, "Booking info should be present")
        assertTrue(site.categories.isNotEmpty(), "Categories should not be empty")
        site.categories.forEach { category ->
            assertTrue(category.id > 0, "Category id should be positive")
            assertTrue(category.name.isNotBlank(), "Category name should not be blank")
        }
        assertTrue(site.pictures.isNotEmpty(), "Pictures should not be empty")
        assertNotNull(site.latitude, "Latitude should be present")
        assertNotNull(site.longitude, "Longitude should be present")
        assertTrue(site.latitude in -90.0..90.0, "Latitude should be valid")
        assertTrue(site.longitude in -180.0..180.0, "Longitude should be valid")
        assertEquals("Europe/Rome", site.timeZone, "Time zone should be Europe/Rome")
    }

    @Test
    suspend fun getSiteWithUnknownIdentifierThrows() {
        val error = runCatching { api.sites.getSite("definitely-not-a-real-site-xyz") }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Unknown sites should be reported as AffluencesException")
        assertNotNull(affluencesError.errorCode, "Error code should be present")
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    @Test
    suspend fun getLiveData() {
        val liveData = api.sites.getLiveData(ATENEO_LIBRARY_SLUG)
        assertNotNull(liveData.status, "Status should be present")

        val allSlots = liveData.todayForecasts + liveData.forecasts.flatMap { it.forecasts }
        allSlots.forEach { slot ->
            assertEquals(2, slot.hourRange.size, "Hour range should have a start and an end")
            slot.hourRange.forEach { boundary ->
                assertTrue(DATE_TIME_REGEX.matches(boundary), "Hour range boundary should be a date-time: $boundary")
            }
            assertTrue(slot.hourRange[0] < slot.hourRange[1], "Hour range should be ordered")
            slot.occupancyPercentage?.let { occupancy ->
                assertTrue(occupancy in 0..100, "Occupancy should be a percentage: $occupancy")
            }
        }

        liveData.forecasts.forEachIndexed { index, day ->
            assertTrue(DATE_REGEX.matches(day.day), "Forecast day should be a date: ${day.day}")
            assertTrue(day.todayOffset >= 0, "Today offset should not be negative")
            if (index > 0) {
                assertTrue(day.day > liveData.forecasts[index - 1].day, "Forecast days should be ordered")
            }
        }
    }

    @Test
    suspend fun getWeekTimetable() {
        val currentWeek = api.sites.getWeekTimetable(ATENEO_LIBRARY_SLUG)
        assertEquals(7, currentWeek.entries.size, "A week should have 7 entries")
        assertTrue(currentWeek.startDate <= currentWeek.endDate, "Week range should be ordered")
        val today = LocalDate.now().toString()
        currentWeek.entries.forEach { entry ->
            assertTrue(DATE_REGEX.matches(entry.day), "Entry day should be a date: ${entry.day}")
            assertTrue(entry.day >= currentWeek.startDate, "Entry should not precede the week start")
            assertTrue(entry.day <= currentWeek.endDate, "Entry should not follow the week end")
            assertEquals(entry.day == today, entry.isToday, "isToday should match the current date")
            entry.openingHours.forEach { range ->
                assertTrue(DATE_TIME_REGEX.matches(range.openingHour), "Opening hour should be a date-time")
                assertTrue(DATE_TIME_REGEX.matches(range.closingHour), "Closing hour should be a date-time")
                assertTrue(range.openingHour < range.closingHour, "Opening range should be ordered")
            }
        }

        val nextWeek = api.sites.getWeekTimetable(ATENEO_LIBRARY_SLUG, weekOffset = 1)
        assertEquals(7, nextWeek.entries.size, "Next week should have 7 entries")
        assertTrue(nextWeek.startDate > currentWeek.endDate, "Next week should follow the current week")
        assertTrue(nextWeek.entries.none { it.isToday }, "Next week should not contain today")
    }

    @Test
    suspend fun getDetails() {
        val details = api.sites.getDetails(ATENEO_LIBRARY_SLUG)
        assertTrue(details.services.isNotEmpty(), "Services should not be empty for the reference site")
        details.services.forEach { service ->
            assertTrue(service.type.isNotBlank(), "Service type should not be blank")
            assertTrue(service.name.isNotBlank(), "Service name should not be blank")
        }
        assertTrue(details.infos.isNotEmpty(), "Info sections should not be empty for the reference site")
        details.infos.forEach { info ->
            assertFalse(info.title.isNullOrBlank(), "Info title should not be blank")
            assertFalse(info.contentHtml.isNullOrBlank(), "Info content should not be blank")
        }
    }

    @Test
    suspend fun getChildren() {
        val children = api.sites.getChildren(ATENEO_LIBRARY_SLUG)
        assertTrue(children.size >= 2, "The Bicocca library system should have at least 2 child libraries")
        children.forEach { child ->
            assertTrue(child.id.isNotBlank(), "Child id should not be blank")
            assertTrue(child.slug.isNotBlank(), "Child slug should not be blank")
            assertTrue(child.primaryName.isNotBlank(), "Child primary name should not be blank")
            assertTrue(child.pictures.isNotEmpty(), "Child pictures should not be empty")
            assertNotNull(child.status, "Child status should be present")
        }
        assertTrue(children.any { it.id == CENTRAL_LIBRARY_ID }, "Children should include the central library")
    }

    @Test
    suspend fun getSimilarSites() {
        val similar = api.sites.getSimilarSites(ATENEO_LIBRARY_SLUG)
        similar.forEach { site ->
            assertTrue(site.id.isNotBlank(), "Similar site id should not be blank")
            assertTrue(site.slug.isNotBlank(), "Similar site slug should not be blank")
            assertTrue(site.primaryName.isNotBlank(), "Similar site primary name should not be blank")
        }
    }

    @Test
    suspend fun getEvents() {
        val events = api.sites.getEvents(ATENEO_LIBRARY_SLUG)
        events.forEach { event ->
            assertTrue(event.id > 0, "Event id should be positive")
        }
    }

    /**
     * The `lastCheckin` checkpoint only filters out older messages, so the call must succeed
     * even when it filters everything.
     */
    @Test
    suspend fun getMessages() {
        val messages = api.sites.getMessages(ATENEO_LIBRARY_SLUG)
        messages.forEach { message ->
            assertFalse(message.contentHtml.isNullOrBlank(), "Message content should not be blank")
        }

        val filteredMessages = api.sites.getMessages(ATENEO_LIBRARY_SLUG, lastCheckin = "2026-01-01T00:00:00Z")
        assertTrue(filteredMessages.size <= messages.size, "The checkpoint should not add messages")
    }

    @Test
    suspend fun getSiteOverview() {
        val overview = api.sites.getSiteOverview(ATENEO_LIBRARY_SLUG)
        assertEquals(ATENEO_LIBRARY_SLUG, overview.slug, "Slug should match the requested site")
        assertTrue(overview.id.isNotBlank(), "Site id should not be blank")
        assertTrue(overview.primaryName.isNotBlank(), "Primary name should not be blank")
        assertTrue(overview.categories.isNotEmpty(), "Categories should not be empty")
        assertNotNull(overview.location, "Location should be present")
        val coordinates = overview.location.coordinates
        assertNotNull(coordinates, "Coordinates should be present")
        assertTrue(coordinates.latitude in -90.0..90.0, "Latitude should be valid")
        assertTrue(coordinates.longitude in -180.0..180.0, "Longitude should be valid")
        val address = overview.location.address
        assertNotNull(address, "Address should be present")
        assertEquals("IT", address.countryCode, "Country should be Italy")
        assertNotNull(overview.currentForecast, "Current forecast should be present")
        overview.currentForecast.occupancyPercentage?.let { occupancy ->
            assertTrue(occupancy in 0..100, "Occupancy should be a percentage: $occupancy")
        }
        overview.todayForecasts.forEach { hourly ->
            assertTrue(Regex("""\d{2}:\d{2}""").matches(hourly.hour), "Forecast hour should be a time: ${hourly.hour}")
        }
    }
}
