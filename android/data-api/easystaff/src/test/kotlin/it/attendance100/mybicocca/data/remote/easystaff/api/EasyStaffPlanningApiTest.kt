package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningLoginState
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningMonthSchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import java.time.LocalDateTime
import java.time.YearMonth

class EasyStaffPlanningApiTest : EasyStaffTestBase() {
    companion object {
        // Cap the services scanned for availability to keep the suite fast
        private const val MAX_AVAILABILITY_COMBOS = 10

        // Clearly-marked synthetic identity: the write test creates a provisional hold that
        // is force-deleted before ever being confirmed, mirroring the web front-end rollback
        private const val TEST_EMAIL = "mybicocca.apitest@example.com"
        private const val TEST_NAME = "MYBICOCCA TEST AUTOMATICO"
        private const val TEST_NOTE = "Prenotazione di test automatica, annullata immediatamente. Si prega di ignorare."
        private const val TEST_PHONE = "+39 333 0000000"
        private const val UPDATED_TEST_NOTE = "Prenotazione di test automatica (aggiornata), annullata immediatamente. Si prega di ignorare."
    }

    private val informationDesks get() = api.planning.informationDesks
    private val studyRooms get() = api.planning.studyRooms

    // A service/area pair with at least one bookable day, discovered once per class
    private data class AvailabilityCombo(
        val service: EasyStaffPlanningService,
        val area: EasyStaffPlanningArea,
        val schedule: EasyStaffPlanningMonthSchedule
    )

    private var cachedCombo: AvailabilityCombo? = null
    private var comboSearched = false

    private suspend fun findAvailabilityCombo(): AvailabilityCombo? {
        if (comboSearched) {
            return cachedCombo
        }
        comboSearched = true
        val services = informationDesks.getServices()
        for (service in services.take(MAX_AVAILABILITY_COMBOS)) {
            val area = informationDesks.getAreas(service.id).firstOrNull() ?: continue
            var month = YearMonth.now()
            repeat(2) {
                val schedule = informationDesks.getMonthSchedule(
                    serviceId = service.id,
                    areaId = area.id,
                    month = month,
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
        val portals = api.planning.getPortals()
        assertTrue(portals.isNotEmpty(), "The portal index should not be empty")
        portals.forEach { portal ->
            assertTrue(portal.id > 0, "Portal id should be positive")
            assertTrue(portal.name.isNotBlank(), "Portal name should not be blank")
            assertTrue(portal.code.isNotBlank(), "Portal code should not be blank")
        }
    }

    @Test
    suspend fun getInformationDesksPortal() {
        val portal = informationDesks.getPortal()
        assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.id, portal.id, "Portal id should match the enum")
        assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.code, portal.code, "Portal code should match the enum")
        assertTrue(portal.name.isNotBlank(), "Portal name should not be blank")
        assertFalse(portal.settings.general.captchaRequired, "The information desks portal should not require a captcha")
        assertEquals(EasyStaffPlanningLoginState.Optional, portal.settings.publicLogin.state, "Public login should be optional")
        assertFalse(portal.settings.publicLogin.authBlocked, "Authentication should not be blocked")
        assertNotNull(portal.colors.textColor, "The portal should have an accent color")
    }

    @Test
    suspend fun getStudyRoomsPortalRequiresLogin() {
        val portal = studyRooms.getPortal()
        assertEquals(EasyStaffPlanningPortal.STUDY_ROOMS.id, portal.id, "Portal id should match the enum")
        assertEquals(EasyStaffPlanningPortal.STUDY_ROOMS.code, portal.code, "Portal code should match the enum")
        assertEquals(EasyStaffPlanningLoginState.Mandatory, portal.settings.publicLogin.state, "Public login should be mandatory")
        assertTrue(portal.settings.publicLogin.authBlocked, "Authentication should be blocked on the legacy portal")
    }

    @Test
    suspend fun getStudyRoomsListingsAreEmptyForAnonymousUsers() {
        val services = studyRooms.getServices()
        assertTrue(services.isEmpty(), "The study rooms portal should hide services from anonymous users")
        val areas = studyRooms.getAreas()
        assertTrue(areas.isEmpty(), "The study rooms portal should hide areas from anonymous users")
    }

    @Test
    suspend fun getServiceGroups() {
        val groups = informationDesks.getServiceGroups()
        assertTrue(groups.isNotEmpty(), "The information desks portal should have service groups")
        groups.forEach { group ->
            assertTrue(group.isNotBlank(), "Service group name should not be blank")
        }
    }

    @Test
    suspend fun getServices() {
        val services = informationDesks.getServices()
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
        val groups = informationDesks.getServiceGroups()
        assumeTrue(groups.isNotEmpty(), "No service group available, cannot test the filtered service listing")

        val group = groups.first()
        val services = informationDesks.getServices(group)
        assertTrue(services.isNotEmpty(), "The group should have at least one service")
        services.forEach { service ->
            assertEquals(group, service.group, "Filtered services should belong to the requested group")
        }
    }

    @Test
    suspend fun getAreaGroups() {
        val groups = informationDesks.getAreaGroups()
        assertTrue(groups.isNotEmpty(), "The information desks portal should have area groups")
        groups.forEach { group ->
            assertTrue(group.id > 0, "Area group id should be positive")
            assertTrue(group.name.isNotBlank(), "Area group name should not be blank")
        }
    }

    @Test
    suspend fun getAreas() {
        val areas = informationDesks.getAreas()
        assertTrue(areas.isNotEmpty(), "The information desks portal should have areas")
        assertTrue(areas.any { it.code == "U17" }, "The U17 building should be among the areas")
        areas.forEach { area ->
            assertTrue(area.id > 0, "Area id should be positive")
            assertEquals(EasyStaffPlanningPortal.INFORMATION_DESKS.id, area.portalId, "Area should belong to the queried portal")
            assertTrue(area.name.isNotBlank(), "Area name should not be blank")
            assertTrue(area.services.isEmpty(), "Unfiltered areas should not embed services")
        }
    }

    @Test
    suspend fun getAreasFilteredByServiceEmbedsServices() {
        val services = informationDesks.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the filtered area listing")

        val service = services.first()
        val areas = informationDesks.getAreas(service.id)
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
        val services = informationDesks.getServices()
        assumeTrue(services.isNotEmpty(), "No service available, cannot test the booking form")

        val form = informationDesks.getServiceForm(services.first().id)
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
    suspend fun getPrimaryField() {
        val field = informationDesks.getPrimaryField()
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
        val schedule = informationDesks.getDaySchedule(
            serviceId = combo.service.id,
            areaId = combo.area.id,
            date = day,
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

        val result = informationDesks.getFirstAvailability(
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
        val resources = informationDesks.getResourceAvailability(
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
            informationDesks.getReservation(
                reservationCode = "BOGUSCODE",
                primaryValue = TEST_EMAIL
            )
        }.exceptionOrNull()
        val planningError = assertInstanceOf<ApiRequestException>(error, "Bogus codes should be reported as ApiRequestException")
        assertEquals("not_found", planningError.message, "Error message should identify the missing reservation")
    }

    @Test
    suspend fun getReservationServiceIdWithBogusCodeThrows() {
        val error = runCatching {
            informationDesks.getReservationServiceId(
                reservationCode = "BOGUSCODE",
                primaryValue = TEST_EMAIL
            )
        }.exceptionOrNull()
        val planningError = assertInstanceOf<ApiRequestException>(error, "Bogus codes should be reported as ApiRequestException")
        assertEquals("reservation_not_found", planningError.message, "Error message should identify the missing reservation")
    }

    // Exercises the full anonymous lifecycle the way the web front-end rolls back an
    // abandoned booking session: the provisional hold is created, inspected, edited, and
    // force-deleted without ever being confirmed, so no real appointment is registered
    @Test
    suspend fun reservationStoreUpdateDeleteRoundTrip() {
        val combo = findAvailabilityCombo()
        assumeTrue(combo != null, "No availability found, cannot test the reservation lifecycle")
        val (service, area, monthSchedule) = combo!!

        val form = informationDesks.getServiceForm(service.id)
        assumeTrue(form.any { it.primary }, "The booking form has no primary field, cannot test the reservation lifecycle")

        // Book the last free slot of the farthest open day to minimize interference with real users
        val day = monthSchedule.days.keys.max()
        val daySchedule = informationDesks.getDaySchedule(
            serviceId = service.id,
            areaId = area.id,
            date = day,
            durationSeconds = service.durationSeconds
        )
        val slot = daySchedule.slots.entries
            .filter { (_, availability) -> availability.availableCount > 0 }
            .maxByOrNull { it.key }
        assumeTrue(slot != null, "No free slot found, cannot test the reservation lifecycle")

        val request = informationDesks.buildReservationRequest(
            serviceId = service.id,
            areaId = area.id,
            slotStart = LocalDateTime.of(day, slot!!.key.start),
            durationSeconds = service.durationSeconds,
            formFields = form,
            values = form.asSequence()
                .filter { it.required }
                .mapNotNull { field -> testValueFor(field)?.let { field.code to it } }
                .toMap()
        )
        assertEquals(TEST_EMAIL, request.primaryValue, "The request should be keyed by the test email")

        val result = informationDesks.createReservation(request)
        val reservationCode = result.reservationCode
        assertNotNull(reservationCode, "The created reservation should have a code")
        try {
            assertEquals(TEST_EMAIL, result.primaryValue, "The reservation should be keyed by the test email")
            assertNotNull(result.entryId, "The created reservation should have an entry id")
            assertTrue(result.fieldLabels.isNotEmpty(), "The created reservation should report its field labels")

            val serviceId = informationDesks.getReservationServiceId(
                reservationCode = reservationCode,
                primaryValue = TEST_EMAIL
            )
            assertEquals(service.id, serviceId, "The reservation should reference the booked service")

            val reservation = informationDesks.getReservation(
                reservationCode = reservationCode,
                primaryValue = TEST_EMAIL
            )
            assertTrue(reservation.success, "The reservation should be manageable")
            assertEquals(reservationCode, reservation.reservationCode, "The managed reservation should echo its code")
            assertEquals(result.entryId, reservation.entry?.id, "The managed reservation should reference the created entry")
            assertEquals(TEST_NAME, reservation.publicFields["cognome_nome"], "The managed reservation should echo the submitted name")
            assertFalse(reservation.confirmed, "The presence at the appointment should not be confirmed yet")

            val editableField = form.firstOrNull { field ->
                field.position == EasyStaffPlanningFieldPosition.Service && !field.primary && !field.identifying
            }
            if (editableField != null) {
                val update = informationDesks.updateReservationFields(
                    reservationCode = reservationCode,
                    primaryValue = TEST_EMAIL,
                    publicFields = mapOf(editableField.code to UPDATED_TEST_NOTE)
                )
                assertTrue(update.success, "The reservation update should succeed")
            }
        } finally {
            // Always roll back the provisional hold, even when an assertion above fails
            val deletion = informationDesks.deleteReservation(
                reservationCode = reservationCode,
                primaryValue = TEST_EMAIL,
                force = true
            )
            assertTrue(deletion.success, "The reservation rollback should succeed")
        }
    }

    private fun testValueFor(field: EasyStaffPlanningFormField): String? = when {
        field.position == EasyStaffPlanningFieldPosition.Gdpr -> null
        field.type == EasyStaffPlanningFieldType.Email -> TEST_EMAIL
        field.type == EasyStaffPlanningFieldType.TextArea -> TEST_NOTE
        field.type == EasyStaffPlanningFieldType.Select -> field.values?.firstOrNull()?.value
        field.type == EasyStaffPlanningFieldType.Phone -> TEST_PHONE
        field.type == EasyStaffPlanningFieldType.Checkbox -> "true"
        else -> TEST_NAME
    }
}
