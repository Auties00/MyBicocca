package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningEntryFilters
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningIdReference
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningLoginState
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningMonthSchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningNamedGroup
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationDeletion
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationUpdate
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.data.remote.easystaff.dto.entryId
import it.attendance100.mybicocca.data.remote.easystaff.dto.options
import it.attendance100.mybicocca.data.remote.easystaff.dto.slots
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class EasyStaffPlanningApiTest : EasyStaffTestBase() {
    companion object {
        /** Caps the services scanned for availability to keep the suite fast. */
        private const val MAX_AVAILABILITY_COMBOS = 10

        private val CAMPUS_TIME_ZONE: ZoneId = ZoneId.of("Europe/Rome")

        /**
         * Clearly-marked synthetic identity for the write test, which creates a provisional
         * hold that is force-deleted before ever being confirmed, mirroring the web front-end
         * rollback.
         */
        private const val TEST_EMAIL = "mybicocca.apitest@example.com"
        private const val TEST_NAME = "MYBICOCCA TEST AUTOMATICO"
        private const val TEST_NOTE = "Prenotazione di test automatica, annullata immediatamente. Si prega di ignorare."
        private const val TEST_PHONE = "+39 333 0000000"
        private const val UPDATED_TEST_NOTE = "Prenotazione di test automatica (aggiornata), annullata immediatamente. Si prega di ignorare."
    }

    /** The information desks portal. */
    private val desks get() = planning

    /** A service/area pair with at least one bookable day, discovered once per class. */
    private data class AvailabilityCombo(
        val service: EasyStaffPlanningService,
        val area: EasyStaffPlanningArea,
        val schedule: EasyStaffPlanningMonthSchedule
    )

    private var cachedCombo: AvailabilityCombo? = null
    private var comboSearched = false

    private fun serviceFilter(serviceId: Int) = EasyStaffPlanningEntryFilters(service = EasyStaffPlanningIdReference(serviceId))

    private suspend fun findAvailabilityCombo(): AvailabilityCombo? {
        if (comboSearched) {
            return cachedCombo
        }
        comboSearched = true
        val services = desks.catalog.getServices()
        for (service in services.take(MAX_AVAILABILITY_COMBOS)) {
            val area = desks.catalog.getAreas(serviceFilter(service.id)).firstOrNull() ?: continue
            var month = YearMonth.now()
            repeat(2) {
                val schedule = desks.availability.getMonthSchedule(
                    serviceId = service.id,
                    month = month.toString(),
                    areaId = area.id,
                    durationSeconds = service.durationSeconds
                )
                if (schedule.days.isNotEmpty()) {
                    cachedCombo = AvailabilityCombo(service, area, schedule)
                    return cachedCombo
                }
                month = schedule.firstAvailable?.let { YearMonth.from(it) } ?: month.plusMonths(1)
            }
        }
        return null
    }

    @Test
    suspend fun getPortals() {
        val portals = desks.portals.getPortals()
        assertTrue(portals.isNotEmpty(), "The portal index should not be empty")
        portals.forEach { portal ->
            assertTrue(portal.id > 0, "Portal id should be positive")
            assertTrue(portal.name.isNotBlank(), "Portal name should not be blank")
            assertTrue(portal.code.isNotBlank(), "Portal code should not be blank")
        }
    }

    @Test
    suspend fun getInformationDesksPortal() {
        val portal = desks.portals.getPortal()
        assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.id, portal.id, "Portal id should match the enum")
        assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.code, portal.code, "Portal code should match the enum")
        assertTrue(portal.name.isNotBlank(), "Portal name should not be blank")
        assertNotNull(portal.settings, "The portal should have settings")
        val settings = portal.settings!!
        assertEquals(false, settings.general?.captchaRequired, "The information desks portal should not require a captcha")
        assertEquals(EasyStaffPlanningLoginState.Optional, settings.publicLogin?.state, "Public login should be optional")
        assertEquals(false, settings.publicLogin?.authBlocked, "Authentication should not be blocked")
        assertNotNull(portal.colors?.textColor, "The portal should have an accent color")
    }

    @Test
    suspend fun getStudyRoomsPortalRequiresLogin() {
        EasyStaffPlanningApi(EasyStaffPlanningPortal.STUDY_ROOMS).use { studyRooms ->
            val portal = studyRooms.portals.getPortal()
            assertEquals(EasyStaffPlanningPortal.STUDY_ROOMS.id, portal.id, "Portal id should match the enum")
            assertEquals(EasyStaffPlanningPortal.STUDY_ROOMS.code, portal.code, "Portal code should match the enum")
            assertEquals(EasyStaffPlanningLoginState.Mandatory, portal.settings?.publicLogin?.state, "Public login should be mandatory")
            assertEquals(true, portal.settings?.publicLogin?.authBlocked, "Authentication should be blocked on the legacy portal")
        }
    }

    @Test
    suspend fun getStudyRoomsListingsAreEmptyForAnonymousUsers() {
        EasyStaffPlanningApi(EasyStaffPlanningPortal.STUDY_ROOMS).use { studyRooms ->
            assertTrue(studyRooms.catalog.getServices().isEmpty(), "The study rooms portal should hide services from anonymous users")
            assertTrue(studyRooms.catalog.getAreas().isEmpty(), "The study rooms portal should hide areas from anonymous users")
        }
    }

    @Test
    suspend fun getServiceGroups() {
        val groups = desks.catalog.getServiceGroups()
        assertTrue(groups.isNotEmpty(), "The information desks portal should have service groups")
        groups.forEach { group ->
            assertTrue(group.name.isNotBlank(), "Service group name should not be blank")
        }
    }

    @Test
    suspend fun getServices() {
        val services = desks.catalog.getServices()
        assertTrue(services.isNotEmpty(), "The information desks portal should have services")
        services.forEach { service ->
            assertTrue(service.id > 0, "Service id should be positive")
            assertTrue(service.name.isNotBlank(), "Service name should not be blank")
            assertTrue(service.durationSeconds > 0, "Service duration should be positive")
            service.durationOptionsSeconds.forEach { duration ->
                assertTrue(duration > 0, "Service duration option should be positive: $duration")
            }
            assertTrue(service.resourceCount > 0, "Service resource count should be positive")
        }
    }

    @Test
    suspend fun getServicesFilteredByGroup() {
        val groups = desks.catalog.getServiceGroups()
        assumeTrue(groups.isNotEmpty(), "No service group available, cannot test the filtered service listing")

        val group = groups.first().name
        val services = desks.catalog.getServices(EasyStaffPlanningEntryFilters(serviceGroup = EasyStaffPlanningNamedGroup(group)))
        assertTrue(services.isNotEmpty(), "The group should have at least one service")
        services.forEach { service ->
            assertEquals(group, service.group, "Filtered services should belong to the requested group")
        }
    }

    @Test
    suspend fun getAreaGroups() {
        val groups = desks.catalog.getAreaGroups()
        assertTrue(groups.isNotEmpty(), "The information desks portal should have area groups")
        groups.forEach { group ->
            assertTrue(group.id > 0, "Area group id should be positive")
            assertTrue(group.name.isNotBlank(), "Area group name should not be blank")
        }
    }

    @Test
    suspend fun getAreas() {
        val areas = desks.catalog.getAreas()
        assertTrue(areas.isNotEmpty(), "The information desks portal should have areas")
        assertTrue(areas.any { it.code == "U17" }, "The U17 building should be among the areas")
        areas.forEach { area ->
            assertTrue(area.id > 0, "Area id should be positive")
            assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.id, area.portalId, "Area should belong to the queried portal")
            assertTrue(area.name.isNotBlank(), "Area name should not be blank")
            assertTrue(area.services.isEmpty(), "Unfiltered areas should not embed services")
        }
    }

    /** The filter cookie is stored by the client, so an unfiltered listing must still clear it. */
    @Test
    suspend fun getAreasAfterFilteredListingIsUnfiltered() {
        val services = desks.catalog.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the filter reset")

        val all = desks.catalog.getAreas()
        desks.catalog.getAreas(serviceFilter(services.first().id))
        assertEquals(all.map { it.id }, desks.catalog.getAreas().map { it.id }, "An unfiltered listing should list every area again")
    }

    @Test
    suspend fun getAreasFilteredByServiceEmbedsServices() {
        val services = desks.catalog.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the filtered area listing")

        val service = services.first()
        val areas = desks.catalog.getAreas(serviceFilter(service.id))
        assertTrue(areas.isNotEmpty(), "At least one area should offer the service")
        areas.forEach { area ->
            assertTrue(area.services.isNotEmpty(), "Filtered areas should embed their services")
            assertTrue(
                area.services.any { it.id == service.id },
                "The embedded services should include the filtering service"
            )
            area.services.forEach { embedded ->
                assertTrue(embedded.id > 0, "Embedded service id should be positive")
                assertTrue(embedded.name.isNotBlank(), "Embedded service name should not be blank")
                assertTrue(embedded.durationSeconds > 0, "Embedded service duration should be positive")
                embedded.suspensionPolicy?.let { policy ->
                    assertTrue(policy.suspensionDays >= 0, "Suspension days should not be negative")
                }
            }
        }
    }

    @Test
    suspend fun getServiceFormHasSinglePrimaryEmailField() {
        val services = desks.catalog.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the booking form")

        val form = desks.portals.getServiceForm(services.first().id)
        assertTrue(form.isNotEmpty(), "The booking form should not be empty")
        form.forEach { field ->
            assertTrue(field.code.isNotBlank(), "Field code should not be blank")
            assertTrue(field.label.isNotBlank(), "Field label should not be blank")
        }
        val primaryFields = form.filter { it.primary }
        assertEquals(1, primaryFields.size, "The booking form should have exactly one primary field")
        assertEquals(EasyStaffPlanningFieldType.Email, primaryFields.single().type, "The primary field should be an email input")
        assertTrue(
            form.any { it.position == EasyStaffPlanningFieldPosition.User },
            "The booking form should have at least one user field"
        )
    }

    @Test
    suspend fun getServiceFormWithoutGuestTokenRenewsIt() {
        val services = desks.catalog.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the guest token renewal")

        // A fresh client has no `jwt_public` cookie yet, so the first form request is rejected.
        EasyStaffPlanningApi(EasyStaffPlanningPortal.INFORMATION_DESKS).use { freshApi ->
            val form = freshApi.portals.getServiceForm(services.first().id)
            assertTrue(form.isNotEmpty(), "The booking form should load once the guest token is renewed")
        }
    }

    @Test
    suspend fun getPrimaryField() {
        val field = desks.portals.getPrimaryField()
        assertTrue(field.primary, "The primary field should be flagged as primary")
        assertEquals(EasyStaffPlanningFieldType.Email, field.type, "The primary field should be an email input")
        assertEquals("email", field.code, "The primary field should be the email field")
    }

    @Test
    suspend fun getMonthSchedule() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the month schedule")

        val schedule = combo!!.schedule
        assertTrue(schedule.days.isNotEmpty(), "The month schedule should have bookable days")
        schedule.days.forEach { (day, windows) ->
            assertTrue(windows.isNotEmpty(), "Day $day should have bookable windows")
            windows.forEach { window ->
                assertTrue(window.start < window.end, "Window should start before it ends: $window")
            }
        }
    }

    @Test
    suspend fun getDaySchedule() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the day schedule")

        val day = combo!!.schedule.days.keys.max()
        val schedule = desks.availability.getDaySchedule(
            serviceId = combo.service.id,
            date = day,
            areaId = combo.area.id,
            durationSeconds = combo.service.durationSeconds
        )
        assertTrue(schedule.slots.isNotEmpty(), "The day schedule should have slots")
        schedule.slots.forEach { (window, slot) ->
            assertTrue(window.start < window.end, "Slot window should start before it ends: $window")
            assertTrue(slot.totalCount > 0, "Slot should be served by at least one resource: $window")
            assertTrue(slot.availableCount in 0..slot.totalCount, "Available count should be within the total: $window")
        }
        assertTrue(
            schedule.slots.values.any { it.availableCount > 0 },
            "A day reported by the month schedule should have at least one free slot"
        )
    }

    @Test
    suspend fun getFirstAvailability() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the first availability")

        val result = desks.availability.getFirstAvailability(
            serviceId = combo!!.service.id,
            areaId = combo.area.id
        )
        assertTrue(result.success, "A combination with availability should acknowledge it")
    }

    @Test
    suspend fun getResourceAvailability() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the resource availability")

        val day = combo!!.schedule.days.keys.max()
        val resources = desks.availability.getResourceAvailability(
            serviceId = combo.service.id,
            areaId = combo.area.id,
            date = day,
            durationSeconds = combo.service.durationSeconds
        )
        assertTrue(resources.isNotEmpty(), "An open day should have at least one resource")
        resources.forEach { availability ->
            assertTrue(availability.resource.id > 0, "Resource id should be positive")
            assertEquals(combo.area.id, availability.resource.areaId, "Resource should belong to the queried area")
            availability.openWindows.values.flatten().forEach { window ->
                assertTrue(window.start <= window.end, "Open window should not be inverted: $window")
            }
        }
    }

    @Test
    suspend fun getReservationWithBogusCodeThrows() {
        val error = runCatching {
            desks.reservations.getReservation(reservationCode = "BOGUSCODE", primaryValue = TEST_EMAIL)
        }.exceptionOrNull()
        val planningError = assertInstanceOf<ApiRequestException>(error, "Bogus codes should be reported as ApiRequestException")
        assertEquals("not_found", planningError.message, "Error message should identify the missing reservation")
    }

    @Test
    suspend fun getReservationServiceIdWithBogusCodeThrows() {
        val error = runCatching {
            desks.reservations.getReservationServiceId(reservationCode = "BOGUSCODE", primaryValue = TEST_EMAIL)
        }.exceptionOrNull()
        val planningError = assertInstanceOf<ApiRequestException>(error, "Bogus codes should be reported as ApiRequestException")
        assertEquals("reservation_not_found", planningError.message, "Error message should identify the missing reservation")
    }

    /**
     * Exercises the full anonymous lifecycle the way the web front-end rolls back an
     * abandoned booking session: the provisional hold is created, inspected, edited, and
     * force-deleted without ever being confirmed (even when an assertion fails), so no real
     * appointment is registered. The booked slot is the last free slot of the farthest open
     * day, to minimize interference with real users.
     */
    @Test
    suspend fun reservationStoreUpdateDeleteRoundTrip() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the reservation lifecycle")
        val (service, area, monthSchedule) = combo!!

        val form = desks.portals.getServiceForm(service.id)
        assumeTrue(form.any { it.primary }, "The booking form has no primary field, cannot test the reservation lifecycle")

        val day = monthSchedule.days.keys.max()
        val daySchedule = desks.availability.getDaySchedule(
            serviceId = service.id,
            date = day,
            areaId = area.id,
            durationSeconds = service.durationSeconds
        )
        val slot = daySchedule.slots.entries
            .filter { (_, availability) -> availability.availableCount > 0 }
            .maxByOrNull { it.key.start }
        assumeTrue(slot != null, "No free slot found, cannot test the reservation lifecycle")

        val values = form.asSequence()
            .filter { it.required }
            .mapNotNull { field -> testValueFor(field)?.let { field to it } }
            .toList()
        val start = LocalDateTime.of(day, slot!!.key.start).atZone(CAMPUS_TIME_ZONE)
        val request = EasyStaffPlanningReservationRequest(
            reservationNumber = 0,
            portalCode = EasyStaffPlanningPortal.INFORMATION_DESKS.code,
            startTimeEpochSeconds = start.toEpochSecond(),
            endTimeEpochSeconds = start.plusSeconds(service.durationSeconds.toLong()).toEpochSecond(),
            durationSeconds = service.durationSeconds,
            serviceId = service.id,
            areaId = area.id,
            primaryValue = TEST_EMAIL,
            userFields = values.filter { (field) -> field.position == EasyStaffPlanningFieldPosition.User }.associate { (field, value) -> field.code to value },
            serviceFields = values.filter { (field) -> field.position == EasyStaffPlanningFieldPosition.Service }.associate { (field, value) -> field.code to value },
            backofficeFields = values.filter { (field) -> field.position == EasyStaffPlanningFieldPosition.Backoffice }.associate { (field, value) -> field.code to value },
            resourceId = null,
            recaptchaToken = null,
            timeZone = CAMPUS_TIME_ZONE.id
        )

        val result = desks.reservations.createReservation(request)
        val reservationCode = result.reservationCode
        assertNotNull(reservationCode, "The created reservation should have a code")
        try {
            assertEquals(TEST_EMAIL, result.primaryValue, "The reservation should be keyed by the test email")
            assertNotNull(result.entryId, "The created reservation should have an entry id")
            assertTrue(result.fieldLabels.isNotEmpty(), "The created reservation should report its field labels")

            val serviceId = desks.reservations.getReservationServiceId(reservationCode = reservationCode, primaryValue = TEST_EMAIL)
            assertEquals(service.id, serviceId, "The reservation should reference the booked service")

            val reservation = desks.reservations.getReservation(reservationCode = reservationCode, primaryValue = TEST_EMAIL)
            assertTrue(reservation.success, "The reservation should be manageable")
            assertEquals(reservationCode, reservation.reservationCode, "The managed reservation should echo its code")
            assertEquals(result.entryId, reservation.entry?.id, "The managed reservation should reference the created entry")
            assertEquals(TEST_NAME, reservation.publicFields["cognome_nome"]?.jsonPrimitive?.content, "The managed reservation should echo the submitted name")
            assertFalse(reservation.confirmed, "The presence at the appointment should not be confirmed yet")

            val editableField = form.firstOrNull { field ->
                field.position == EasyStaffPlanningFieldPosition.Service && !field.primary && !field.identifying
            }
            if (editableField != null) {
                val update = desks.reservations.updateReservation(
                    reservationCode = reservationCode,
                    body = EasyStaffPlanningReservationUpdate(publicFields = mapOf(editableField.code to UPDATED_TEST_NOTE)),
                    primaryValue = TEST_EMAIL
                )
                assertTrue(update.success, "The reservation update should succeed")
            }
        } finally {
            val deletion = desks.reservations.deleteReservation(
                reservationCode = reservationCode,
                body = EasyStaffPlanningReservationDeletion(force = true),
                primaryValue = TEST_EMAIL
            )
            assertTrue(deletion.success, "The reservation rollback should succeed")
        }
    }

    private fun testValueFor(field: EasyStaffPlanningFormField): String? = when {
        field.position == EasyStaffPlanningFieldPosition.Gdpr -> null
        field.type == EasyStaffPlanningFieldType.Email -> TEST_EMAIL
        field.type == EasyStaffPlanningFieldType.TextArea -> TEST_NOTE
        field.type == EasyStaffPlanningFieldType.Select -> field.options?.firstOrNull()?.value
        field.type == EasyStaffPlanningFieldType.Phone -> TEST_PHONE
        field.type == EasyStaffPlanningFieldType.Checkbox -> "true"
        else -> TEST_NAME
    }
}
