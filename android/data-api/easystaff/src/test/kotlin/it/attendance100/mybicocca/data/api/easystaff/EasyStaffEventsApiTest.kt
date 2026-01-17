package it.attendance100.mybicocca.data.api.easystaff

import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffEvent
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffLanguage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*

class EasyStaffEventsApiTest : EasyStaffTestBase() {
    companion object {
        private val MOCK_START_DATE = LocalDate.now()
        private val MOCK_END_DATE = LocalDate.now().plusMonths(1)
        private const val MOCK_KEYWORD = "informatica"
    }

    @Test
    suspend fun getTodayEvents() {
        val events = api.events.getTodayEvents()
        assertNotNull(events)
        // Today's events might be empty on weekends/holidays

        // Verify events if any exist
        val today = LocalDate.now()
        events.forEach { event ->
            validateEvent(event)
            assertTrue(
                event.date == today,
                "Event date ${event.date} should be today ($today)"
            )
        }
    }

    @Test
    suspend fun getTodayEventsWithBuilding() {
        val buildings = api.buildings.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty(), "Buildings should not be empty")
        val building = buildings.first()

        val events = api.events.getTodayEvents(buildings = listOf(building))
        assertNotNull(events)
        // Today's events might be empty on weekends/holidays

        // Verify events have valid properties
        events.forEach { event ->
            validateEvent(event)
        }
    }

    @Test
    suspend fun getWeekEvents() {
        val events = api.events.getWeekEvents()
        assertNotNull(events)
        // Weeks's events might be empty

        // Verify events are within the week
        val today = LocalDate.now()
        events.forEach { event ->
            validateEvent(event)
            assertTrue(
                event.date.isSameWeek(today),
                "Event date ${event.date} should be within the week"
            )
        }
    }

    private fun LocalDate.isSameWeek(other: LocalDate, locale: Locale = Locale.getDefault()): Boolean {
        val weekFields = WeekFields.of(locale)

        val thisWeek = this.get(weekFields.weekOfWeekBasedYear())
        val otherWeek = other.get(weekFields.weekOfWeekBasedYear())

        val thisYear = this.get(weekFields.weekBasedYear())
        val otherYear = other.get(weekFields.weekBasedYear())

        return thisWeek == otherWeek && thisYear == otherYear
    }

    @Test
    suspend fun getMonthEvents() {
        val events = api.events.getMonthEvents()
        assertNotNull(events)
        assertTrue(events.isNotEmpty(), "Month events should not be empty")

        // Verify events are within the month
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
        events.forEach { event ->
            validateEvent(event)
            assertTrue(
                !event.date.isBefore(monthStart) && !event.date.isAfter(monthEnd),
                "Event date ${event.date} should be within the month"
            )
        }
    }

    @Test
    suspend fun getEvents() {
        val events = api.events.getEvents(
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE
        )
        assertNotNull(events)
        assertTrue(events.isNotEmpty(), "Event should not be empty")

        // Verify events are within the date range
        events.forEach { event ->
            validateEvent(event)
            assertTrue(
                !event.date.isBefore(MOCK_START_DATE) && !event.date.isAfter(MOCK_END_DATE),
                "Event date ${event.date} should be within range $MOCK_START_DATE - $MOCK_END_DATE"
            )
        }
    }

    @Test
    suspend fun getEventsWithKeyword() {
        val events = api.events.getEvents(
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE,
            keyword = MOCK_KEYWORD
        )
        assertNotNull(events)
        assertTrue(events.isNotEmpty(), "Event should not be empty")

        // Verify events have valid properties
        events.forEach { event ->
            validateEvent(event)
        }
    }

    @Test
    suspend fun getEventsWithBuildingFilter() {
        val buildings = api.buildings.getBuildings()
        assertNotNull(buildings)
        assertTrue(buildings.isNotEmpty(), "Buildings should not be empty")
        val building = buildings.first()

        val events = api.events.getEvents(
            startDate = MOCK_START_DATE,
            endDate = MOCK_END_DATE,
            buildings = listOf(building)
        )
        assertNotNull(events)
        assertTrue(events.isNotEmpty(), "Events should not be empty")

        // Verify events have valid properties
        events.forEach { event ->
            validateEvent(event)
        }
    }

    @Test
    suspend fun getEventTypes() {
        val eventTypes = api.events.getEventTypes(EasyStaffLanguage.ITALIAN)
        assertNotNull(eventTypes)
        assertTrue(eventTypes.isNotEmpty(), "Event types should not be empty")

        // Verify each event type has valid properties
        eventTypes.forEach { eventType ->
            assertTrue(eventType.code > 0, "Event type ID should be positive")
            assertTrue(eventType.name.isNotBlank(), "Event type label should not be blank")
        }

        // Verify event type IDs are unique
        val eventTypeIds = eventTypes.map { it.code }
        assertEquals(
            eventTypeIds.size,
            eventTypeIds.toSet().size,
            "Event type IDs should be unique"
        )
    }

    private fun validateEvent(event: EasyStaffEvent) {
        assertTrue(event.id.isNotBlank(), "Event ID is missing")
        assertTrue(event.title.isNotBlank(), "Event title is blank for ID: ${event.id}")
        assertTrue(event.roomName.isNotBlank(), "Room name is missing for: ${event.title}")

        assertNotNull(event.status, "Status parsing failed for: ${event.title}")

        assertNotNull(event.date, "Date object is null")
        assertTrue(
            event.startDateTime.toLocalDate() == event.date,
            "Start time date (${event.startDateTime.toLocalDate()}) does not match event date (${event.date})"
        )
        assertTrue(
            event.startDateTime.isBefore(event.endDateTime),
            "Event starts after it ends: ${event.startDateTime} -> ${event.endDateTime}"
        )

        // Verify event type has valid properties
        assertNotNull(event.eventType, "Event type should not be null")
        assertTrue(event.eventType.isNotBlank(), "Event type code should not be blank")

        // Verify teachers list is not null (may be empty)
        assertNotNull(event.teachersList, "Teachers list should not be null")
        event.teachersList.forEach { teacher ->
            assertTrue(teacher.code != null || teacher.email != null, "Teacher code and email cannot be null at the same time")
            assertTrue(teacher.code == null || teacher.code.isNotBlank(), "Teacher code should not be blank if present")
            assertTrue(teacher.email == null || teacher.email.isNotBlank(), "Teacher email should not be blank if present")
        }

        // Verify activities list is not null (may be empty)
        assertNotNull(event.activities, "Activities list should not be null")
    }
}
