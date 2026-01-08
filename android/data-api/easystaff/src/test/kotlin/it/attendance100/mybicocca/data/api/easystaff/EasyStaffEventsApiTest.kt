package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffEventSearchQuery
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffEventType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasyStaffEventsApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_START_DATE = LocalDate.now()
        private val MOCK_END_DATE = LocalDate.now().plusMonths(1)
        private const val MOCK_KEYWORD = "informatica"
    }

    @Test
    suspend fun getSearchOptions() {
        val options = api.events.getSearchOptions()
        assertNotNull(options.buildings)
        assertNotNull(options.rooms)
        assertNotNull(options.eventTypes)
    }

    @Test
    suspend fun getTodayEvents() {
        val results = api.events.getTodayEvents()
        assertNotNull(results.events)
    }

    @Test
    suspend fun getTodayEventsWithBuilding() {
        val options = api.events.getSearchOptions()
        if (options.buildings.isEmpty()) return

        val building = options.buildings.first()
        val results = api.events.getTodayEvents(buildings = listOf(building.code))
        assertNotNull(results.events)
    }

    @Test
    suspend fun getWeekEvents() {
        val results = api.events.getWeekEvents()
        assertNotNull(results.events)
    }

    @Test
    suspend fun getWeekEventsWithEventType() {
        val results = api.events.getWeekEvents(eventTypes = listOf(EasyStaffEventType.LESSON))
        assertNotNull(results.events)
    }

    @Test
    suspend fun searchByKeyword() {
        val results = api.events.searchByKeyword(
            keyword = MOCK_KEYWORD,
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )
        assertNotNull(results.events)
    }

    @Test
    suspend fun getEventsByType() {
        val results = api.events.getEventsByType(
            eventType = EasyStaffEventType.LESSON,
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )
        assertNotNull(results.events)
    }

    @Test
    suspend fun searchEvents() {
        val query = EasyStaffEventSearchQuery(
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )

        val results = api.events.searchEvents(query)
        assertNotNull(results.events)
        assertNotNull(results.searchSummary)
    }

    @Test
    suspend fun searchEventsWithFilters() {
        val options = api.events.getSearchOptions()
        if (options.buildings.isEmpty()) return

        val building = options.buildings.first()
        val query = EasyStaffEventSearchQuery(
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE,
            buildings = listOf(building.code),
            eventTypes = listOf(EasyStaffEventType.LESSON)
        )

        val results = api.events.searchEvents(query)
        assertNotNull(results.events)
        assertNotNull(results.searchSummary)
    }
}
