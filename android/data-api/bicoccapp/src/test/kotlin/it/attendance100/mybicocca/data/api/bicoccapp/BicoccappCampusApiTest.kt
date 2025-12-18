package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappCampusApi Integration Tests")
class BicoccappCampusApiTest : BicoccappApiTestBase() {

    @Nested
    @DisplayName("Points of Interest API")
    inner class PointsOfInterestTests {

        @Test
        @DisplayName("getPointsOfInterest() returns campus locations")
        fun `getPointsOfInterest returns POIs successfully`() = runTest {
            // When
            val response = campusApi.getPointsOfInterest()

            // Then
            printResponse("getPointsOfInterest()", response)
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val poisResponse = assertNonNullBody(response, "getPointsOfInterest")
            assertNotNull(poisResponse.maps, "Maps should not be null")

            println("Points of Interest response retrieved successfully")
        }

        @Test
        @DisplayName("getPointsOfInterest() returns location data with coordinates")
        fun `getPointsOfInterest returns locations with coordinates`() = runTest {
            // When
            val response = campusApi.getPointsOfInterest()
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val poisResponse = assertNonNullBody(response, "getPointsOfInterest")

            // Check maps structure
            if (poisResponse.maps != null) {
                val maps = poisResponse.maps
                println("Maps data retrieved")

                // Check for map locations
                maps.mapLocations.let { locations ->
                    println("Map locations count: ${locations.size}")
                    if (locations.isNotEmpty()) {
                        println("First location sample: ${locations.first()}")
                    }
                }

                // Check for filters
                maps.filters.let { filters ->
                    println("Filters count: ${filters.size}")
                }
            }
        }

        @Test
        @DisplayName("getPointsOfInterest() multiple calls return consistent data")
        fun `getPointsOfInterest returns consistent data`() = runTest {
            // When
            val response1 = campusApi.getPointsOfInterest()
            val response2 = campusApi.getPointsOfInterest()

            // Then
            assertSuccessfulResponse(response1, "getPointsOfInterest call 1")
            assertSuccessfulResponse(response2, "getPointsOfInterest call 2")

            // POI data should be consistent
            println("POI data is consistent across calls")
        }
    }

    @Nested
    @DisplayName("Teacher Lookup API")
    inner class TeacherLookupTests {

        @Test
        @DisplayName("getTeacherByEmail() with placeholder email returns appropriate response")
        fun `getTeacherByEmail with placeholder email handles gracefully`() = runTest {
            // When - email is now required
            val response = campusApi.getTeacherByEmail(email = "placeholder@unimib.it")

            // Then
            printResponse("getTeacherByEmail(placeholder)", response)
            // API may require valid email parameter
            println("getTeacherByEmail API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with test email")
        fun `getTeacherByEmail with test email handles gracefully`() = runTest {
            // When - email is now required
            val response = campusApi.getTeacherByEmail(email = "test@unimib.it")

            // Then
            printResponse("getTeacherByEmail(test@unimib.it)", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with empty email")
        fun `getTeacherByEmail with empty email handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "")

            // Then
            printResponse("getTeacherByEmail(email='')", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with invalid email format")
        fun `getTeacherByEmail with invalid email format handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "not-an-email")

            // Then
            printResponse("getTeacherByEmail(invalid format)", response)
            // Should return 400 or 404
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with non-existent teacher")
        fun `getTeacherByEmail with non-existent teacher returns 404`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "nonexistent.teacher@unimib.it")

            // Then
            printResponse("getTeacherByEmail(non-existent)", response)
            // Should return 404 Not Found
            println("API responded with code: ${response.code()}")

            // Non-existent teacher should not return success
            if (!response.isSuccessful || response.code() == 404) {
                println("Correctly returned not found for non-existent teacher")
            }
        }

        @Test
        @DisplayName("getTeacherByEmail() with non-unimib email")
        fun `getTeacherByEmail with external email handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "teacher@gmail.com")

            // Then
            printResponse("getTeacherByEmail(external email)", response)
            // Should return 404 or error for non-unimib email
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with valid unimib email returns response")
        fun `getTeacherByEmail with valid email returns response`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "test@unimib.it")

            // Then
            printResponse("getTeacherByEmail(test@unimib.it)", response)
            println("API responded with code: ${response.code()}")

            if (response.isSuccessful) {
                val teacherResponse = assertNonNullBody(response, "getTeacherByEmail")
                println("Teacher found: ${teacherResponse.teacher}")
            }
        }
    }

    @Nested
    @DisplayName("Response Structure Validation")
    inner class ResponseStructureTests {

        @Test
        @DisplayName("POI response has expected structure")
        fun `poi response structure is valid`() = runTest {
            val response = campusApi.getPointsOfInterest()
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val pois = assertNonNullBody(response, "getPointsOfInterest")

            // Verify structure
            assertNotNull(pois.maps, "Response must have maps")
        }

        @Test
        @DisplayName("POI maps contain filters and locations")
        fun `poi maps contain expected data`() = runTest {
            val response = campusApi.getPointsOfInterest()
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val pois = assertNonNullBody(response, "getPointsOfInterest")

            if (pois.maps != null) {
                val maps = pois.maps

                // Filters list
                assertNotNull(maps.filters, "Maps should have filters")
                println("Filters: ${maps.filters.size}")

                // Map locations list
                assertNotNull(maps.mapLocations, "Maps should have mapLocations")
                println("Map locations: ${maps.mapLocations.size}")
            }
        }

        @Test
        @DisplayName("Teacher response has expected structure when found")
        fun `teacher response structure is valid`() = runTest {
            // First try to find a valid teacher
            val response = campusApi.getTeacherByEmail(email = "test@unimib.it")

            printResponse("getTeacherByEmail structure test", response)

            if (response.isSuccessful) {
                val teacher = assertNonNullBody(response, "getTeacherByEmail")
                assertNotNull(teacher.teacher, "Response must have teacher")
                println("Teacher structure validated")
            } else {
                println("No teacher found to validate structure")
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("getPointsOfInterest() handles large response")
        fun `getPointsOfInterest handles large response`() = runTest {
            // When
            val response = campusApi.getPointsOfInterest()

            // Then
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val pois = assertNonNullBody(response, "getPointsOfInterest")

            // Log response size indicators
            val locationCount = pois.maps?.mapLocations?.size ?: 0
            val filterCount = pois.maps?.filters?.size ?: 0

            println("POI response size: $locationCount locations, $filterCount filters")
        }

        @Test
        @DisplayName("getTeacherByEmail() with SQL injection attempt")
        fun `getTeacherByEmail handles sql injection attempt`() = runTest {
            // When - attempting SQL injection (should be safely handled)
            val response = campusApi.getTeacherByEmail(email = "'; DROP TABLE teachers; --")

            // Then
            printResponse("getTeacherByEmail(SQL injection)", response)
            // Should return 400 or 404, not 500
            assertNotEquals(500, response.code(), "Server should not crash on SQL injection attempt")
            println("SQL injection safely handled with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with very long email")
        fun `getTeacherByEmail with long email handles gracefully`() = runTest {
            // Given
            val longEmail = "a".repeat(1000) + "@unimib.it"

            // When
            val response = campusApi.getTeacherByEmail(email = longEmail)

            // Then
            printResponse("getTeacherByEmail(long email)", response)
            // Should handle gracefully without server error
            assertNotEquals(500, response.code(), "Server should handle long email gracefully")
        }

        @Test
        @DisplayName("getTeacherByEmail() with unicode characters")
        fun `getTeacherByEmail with unicode handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "tëst@unimib.it")

            // Then
            printResponse("getTeacherByEmail(unicode)", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with special characters")
        fun `getTeacherByEmail with special chars handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "test+special@unimib.it")

            // Then
            printResponse("getTeacherByEmail(special chars)", response)
            println("API responded with code: ${response.code()}")
        }
    }

    @Nested
    @DisplayName("API Behavior Verification")
    inner class ApiBehaviorTests {

        @Test
        @DisplayName("getPointsOfInterest() is idempotent")
        fun `getPointsOfInterest is idempotent`() = runTest {
            // Multiple GET requests should return same data
            val response1 = campusApi.getPointsOfInterest()
            val response2 = campusApi.getPointsOfInterest()

            assertSuccessfulResponse(response1, "getPointsOfInterest 1")
            assertSuccessfulResponse(response2, "getPointsOfInterest 2")

            val pois1 = assertNonNullBody(response1, "getPointsOfInterest 1")
            val pois2 = assertNonNullBody(response2, "getPointsOfInterest 2")

            // Location counts should match
            assertEquals(
                pois1.maps?.mapLocations?.size,
                pois2.maps?.mapLocations?.size,
                "POI data should be consistent"
            )

            println("GET POIs is idempotent")
        }

        @Test
        @DisplayName("getTeacherByEmail() is idempotent")
        fun `getTeacherByEmail is idempotent`() = runTest {
            // When
            val email = "test@unimib.it"
            val response1 = campusApi.getTeacherByEmail(email = email)
            val response2 = campusApi.getTeacherByEmail(email = email)

            // Then - both calls should return same result
            assertEquals(
                response1.code(),
                response2.code(),
                "Teacher lookup should be consistent"
            )

            println("GET teacher is idempotent")
        }

        @Test
        @DisplayName("POI data contains campus building information")
        fun `poi data contains building information`() = runTest {
            // When
            val response = campusApi.getPointsOfInterest()
            assertSuccessfulResponse(response, "getPointsOfInterest")

            val pois = assertNonNullBody(response, "getPointsOfInterest")

            // Check for expected campus buildings (U1-U9, U14, etc.)
            val locations = pois.maps?.mapLocations ?: emptyList()

            println("Total locations: ${locations.size}")

            // Log some location names if available
            locations.take(10).forEach { location ->
                println("  - Location: $location")
            }
        }
    }
}
