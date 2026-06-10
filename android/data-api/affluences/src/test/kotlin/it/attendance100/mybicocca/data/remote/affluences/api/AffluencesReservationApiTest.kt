package it.attendance100.mybicocca.data.remote.affluences.api

import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeFilters
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import java.time.LocalDate
import java.time.LocalTime

class AffluencesReservationApiTest : AffluencesTestBase() {
    companion object {
        private val TIME_REGEX = Regex("""\d{2}:\d{2}""")
        private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
    }

    private suspend fun firstResourceTypeFilters(): AffluencesResourceTypeFilters {
        val info = api.reservations.getReservationInfo(CENTRAL_LIBRARY_ID)
        assertTrue(info.types.isNotEmpty(), "The central library should have bookable resource types")
        return api.reservations.getResourceTypeFilters(
            siteIdentifier = CENTRAL_LIBRARY_ID,
            resourceTypeId = info.types.first().resourceTypeId,
            date = LocalDate.now()
        )
    }

    @Test
    suspend fun getReservationInfo() {
        val info = api.reservations.getReservationInfo(CENTRAL_LIBRARY_ID)
        assertTrue(info.types.isNotEmpty(), "The central library should have bookable resource types")
        info.types.forEach { type ->
            assertTrue(type.resourceTypeId > 0, "Resource type id should be positive")
            assertTrue(!type.description.isNullOrBlank(), "Resource type description should not be blank")
        }
    }

    @Test
    suspend fun getResourceTypeFilters() {
        val filters = firstResourceTypeFilters()
        assertTrue(filters.resourceTypeId > 0, "Resource type id should be positive")
        assertTrue(filters.resources.isNotEmpty(), "The type should have bookable resources")
        filters.resources.forEach { resource ->
            assertTrue(resource.resourceId > 0, "Resource id should be positive")
        }
        filters.openDays.forEach { day ->
            assertTrue(DATE_REGEX.matches(day), "Open day should be a date: $day")
        }
        filters.openHours.forEach { hour ->
            assertTrue(TIME_REGEX.matches(hour), "Open hour should be a time: $hour")
        }
        filters.durations.forEach { duration ->
            assertTrue(duration > 0, "Duration should be positive: $duration")
        }
    }

    @Test
    suspend fun getAvailableResources() {
        val filters = firstResourceTypeFilters()
        assumeTrue(filters.openDays.isNotEmpty(), "No bookable day available, cannot test availability")

        val date = LocalDate.parse(filters.openDays.first())
        val resources = api.reservations.getAvailableResources(
            siteIdentifier = CENTRAL_LIBRARY_ID,
            date = date,
            resourceTypeId = filters.resourceTypeId
        )
        assertTrue(resources.isNotEmpty(), "The type should have resources on an open day")
        resources.forEach { resource ->
            assertTrue(resource.resourceId > 0, "Resource id should be positive")
            resource.granularityMinutes?.let { granularity ->
                assertTrue(granularity > 0, "Granularity should be positive")
            }
            resource.hours.forEach { slot ->
                assertTrue(TIME_REGEX.matches(slot.hour), "Slot hour should be a time: ${slot.hour}")
                assertTrue(!slot.state.isNullOrBlank(), "Slot state should not be blank")
            }
        }
    }

    @Test
    suspend fun getAvailableResourcesWithUnknownTypeReturnsNothing() {
        val resources = api.reservations.getAvailableResources(
            siteIdentifier = CENTRAL_LIBRARY_ID,
            date = LocalDate.now(),
            resourceTypeId = -1
        )
        assertTrue(resources.isEmpty(), "Unknown types should have no resources")
    }

    @Test
    suspend fun getAgreements() {
        val agreements = api.reservations.getAgreements(CENTRAL_LIBRARY_ID)
        assertTrue(agreements.isNotEmpty(), "The central library should have at least one agreement")
        agreements.forEach { agreement ->
            assertTrue(agreement.agreementId > 0, "Agreement id should be positive")
            assertTrue(!agreement.name.isNullOrBlank(), "Agreement name should not be blank")
        }
    }

    @Test
    suspend fun getResourceEmailPolicy() {
        val filters = firstResourceTypeFilters()
        val policy = api.reservations.getResourceEmailPolicy(filters.resources.first().resourceId)
        assertNotNull(policy.filters, "Email filters should be present, even when empty")
    }

    /** The central library resources have no custom form, but the call must still succeed. */
    @Test
    suspend fun getResourceForms() {
        val filters = firstResourceTypeFilters()
        api.reservations.getResourceForms(filters.resources.first().resourceId)
    }

    @Test
    suspend fun getResourceIdentityProviders() {
        val filters = firstResourceTypeFilters()
        val providers = api.reservations.getResourceIdentityProviders(filters.resources.first().resourceId)
        providers.forEach { provider ->
            assertTrue(!provider.type.isNullOrBlank(), "Identity provider type should not be blank")
        }
    }

    @Test
    suspend fun getResourceStatus() {
        val filters = firstResourceTypeFilters()
        val resourceId = filters.resources.first().resourceId
        val status = api.reservations.getResourceStatus(resourceId)
        assertTrue(!status.currentState.isNullOrBlank(), "Current state should not be blank")
        assertNotNull(status.resource, "Resource detail should be present")
        assertEquals(resourceId, status.resource.resourceId, "Resource detail should match the requested resource")
        assertEquals(CENTRAL_LIBRARY_ID, status.resource.siteUuid, "Resource should belong to the central library")
    }

    @Test
    suspend fun createReservationWithInvalidEmailThrows() {
        val filters = firstResourceTypeFilters()
        assumeTrue(filters.openDays.isNotEmpty(), "No bookable day available, cannot test reservation")
        assumeTrue(filters.openHours.isNotEmpty(), "No bookable hour available, cannot test reservation")

        val startTime = LocalTime.parse(filters.openHours.first())
        val error = runCatching {
            api.reservations.createReservation(
                resourceId = filters.resources.first().resourceId,
                email = "not-an-email",
                date = LocalDate.parse(filters.openDays.first()),
                startTime = startTime,
                endTime = startTime.plusMinutes(filters.durations.firstOrNull()?.toLong() ?: 60L)
            )
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Invalid emails should be reported as AffluencesException")
        assertEquals("email_invalid", affluencesError.errorCode, "Error code should identify the invalid email")
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    @Test
    suspend fun validateReservationWithBogusCodeThrows() {
        val error = runCatching {
            api.reservations.validateReservation(validationCode = "000000", email = "test@example.com")
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Bogus codes should be reported as AffluencesException")
        assertEquals("invalid_validation_code", affluencesError.errorCode, "Error code should identify the invalid code")
    }

    @Test
    suspend fun confirmReservationWithBogusTokenThrows() {
        val error = runCatching {
            api.reservations.confirmReservation("bogus-token")
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Bogus tokens should be reported as AffluencesException")
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    /** The backend rejects unknown tokens without a structured error body. */
    @Test
    suspend fun getReservationToCancelWithBogusTokenThrows() {
        val error = runCatching {
            api.reservations.getReservationToCancel("bogus-token")
        }.exceptionOrNull()
        val requestError = assertInstanceOf<ApiRequestException>(error, "Bogus tokens should be reported as ApiRequestException")
        assertTrue(requestError.statusCode >= 400, "Status code should be an error")
    }

    /** The backend rejects unknown tokens without a structured error body. */
    @Test
    suspend fun cancelReservationWithBogusTokenThrows() {
        val error = runCatching {
            api.reservations.cancelReservation("bogus-token")
        }.exceptionOrNull()
        val requestError = assertInstanceOf<ApiRequestException>(error, "Bogus tokens should be reported as ApiRequestException")
        assertTrue(requestError.statusCode >= 400, "Status code should be an error")
    }

    @Test
    suspend fun confirmEmailWithBogusTokenReportsNotFound() {
        val confirmation = api.reservations.confirmEmail("bogus-token")
        assertTrue(confirmation.notFound, "Bogus tokens should be reported as not found")
        assertTrue(!confirmation.alreadyConfirmed, "Bogus tokens should not be reported as already confirmed")
    }

    @Test
    suspend fun trustDeviceWithBogusTokenThrows() {
        val error = runCatching {
            api.reservations.trustDevice(email = "test@example.com", token = "bogus-token")
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(error, "Bogus tokens should be reported as AffluencesException")
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }
}
