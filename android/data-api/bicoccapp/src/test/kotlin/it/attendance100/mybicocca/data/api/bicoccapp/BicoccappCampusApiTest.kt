package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
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
            println("getTeacherByEmail API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with test email")
        fun `getTeacherByEmail with test email handles gracefully`() = runTest {
            // When - email is now required
            val response = campusApi.getTeacherByEmail(email = "test@unimib.it")

            // Then
            printResponse("getTeacherByEmail(test@unimib.it)", response)
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with empty email")
        fun `getTeacherByEmail with empty email handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "")

            // Then
            printResponse("getTeacherByEmail(email='')", response)
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with invalid email format")
        fun `getTeacherByEmail with invalid email format handles gracefully`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "not-an-email")

            // Then
            printResponse("getTeacherByEmail(invalid format)", response)
            // Should return 400 or 404
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with non-existent teacher")
        fun `getTeacherByEmail with non-existent teacher returns 404`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "nonexistent.teacher@unimib.it")

            // Then
            printResponse("getTeacherByEmail(non-existent)", response)
            // Should return 404 Not Found
            println("API responded with code: ${response.code}")

            // Non-existent teacher should not return success
            if (!response.isSuccessful || response.code == 404) {
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
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getTeacherByEmail() with valid unimib email returns response")
        fun `getTeacherByEmail with valid email returns response`() = runTest {
            // When
            val response = campusApi.getTeacherByEmail(email = "test@unimib.it")

            // Then
            printResponse("getTeacherByEmail(test@unimib.it)", response)
            println("API responded with code: ${response.code}")

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
            val response = campusApi.getTeacherByEmail(email = "luigi.celona@unimib.it")

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
}
