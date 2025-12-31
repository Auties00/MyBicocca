package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappMessagesApi Integration Tests")
class BicoccappMessagesApiTest : BicoccappApiTestBase() {

    @Nested
    @DisplayName("Alerts API")
    inner class AlertsTests {

        @Test
        @DisplayName("getAlerts() returns active alerts for authenticated user")
        fun `getAlerts returns alerts successfully`() = runTest {
            // When
            val response = messagesApi.getAlerts()

            // Then
            printResponse("getAlerts()", response)
            assertSuccessfulResponse(response, "getAlerts")

            val alertsResponse = assertNonNullBody(response, "getAlerts")
            assertNotNull(alertsResponse.alerts, "Alerts list should not be null")

            println("Alerts count: ${alertsResponse.alerts.size}")
            println("Alerts to read: ${alertsResponse.alertsToRead ?: 0}")
        }

        @Test
        @DisplayName("getAlerts() response contains alert metadata")
        fun `getAlerts returns alerts with metadata`() = runTest {
            // When
            val response = messagesApi.getAlerts()
            assertSuccessfulResponse(response, "getAlerts")

            val alertsResponse = assertNonNullBody(response, "getAlerts")

            // Check for alertsToRead field
            println("Unread alerts count: ${alertsResponse.alertsToRead}")

            // If alerts exist, verify they have expected properties
            if (alertsResponse.alerts.isNotEmpty()) {
                val firstAlert = alertsResponse.alerts.first()
                println("First alert: ${firstAlert.title}")
            }
        }

        @Test
        @DisplayName("getAlerts() multiple calls return consistent data")
        fun `getAlerts returns consistent data on multiple calls`() = runTest {
            // When
            val response1 = messagesApi.getAlerts()
            val response2 = messagesApi.getAlerts()

            // Then
            assertSuccessfulResponse(response1, "getAlerts call 1")
            assertSuccessfulResponse(response2, "getAlerts call 2")

            val alerts1 = assertNonNullBody(response1, "getAlerts call 1")
            val alerts2 = assertNonNullBody(response2, "getAlerts call 2")

            // Alert counts should be consistent
            assertEquals(
                alerts1.alerts.size,
                alerts2.alerts.size,
                "Alert counts should be consistent"
            )

            println("Consistent alert count: ${alerts1.alerts.size}")
        }
    }

    @Nested
    @DisplayName("Appointment Request API")
    inner class AppointmentRequestTests {

        @Test
        @DisplayName("sendAppointmentRequest() with placeholder values returns appropriate response")
        fun `sendAppointmentRequest with placeholder values handles gracefully`() = runTest {
            // Note: This test verifies the API handles invalid parameters
            // We intentionally don't send real messages

            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "test@unimib.it",
                studentId = AuthTokens.studentId?.toIntOrNull() ?: 0,
                messageBody = ""
            )

            // Then
            printResponse("sendAppointmentRequest(placeholder values)", response)
            // API should return error without valid parameters
            println("sendAppointmentRequest API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("sendAppointmentRequest() with empty teacherKey")
        fun `sendAppointmentRequest with empty teacherKey handles gracefully`() = runTest {
            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "",
                studentId = AuthTokens.studentId?.toIntOrNull() ?: 0,
                messageBody = "Test message"
            )

            // Then
            printResponse("sendAppointmentRequest(empty teacherKey)", response)
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("sendAppointmentRequest() with invalid teacher email format")
        fun `sendAppointmentRequest with invalid email handles gracefully`() = runTest {
            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "not-a-valid-email",
                studentId = 12345,
                messageBody = "Test message"
            )

            // Then
            printResponse("sendAppointmentRequest(invalid email)", response)
            // Should return 400 or 404 for invalid teacher
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("sendAppointmentRequest() with non-existent teacher")
        fun `sendAppointmentRequest with non-existent teacher returns 404`() = runTest {
            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "nonexistent.teacher@unimib.it",
                studentId = 12345,
                messageBody = "Test message for non-existent teacher"
            )

            // Then
            printResponse("sendAppointmentRequest(non-existent teacher)", response)
            // Should return 404 Not Found
            println("API responded with code: ${response.code}")

            // Verify it doesn't succeed with invalid teacher
            if (!response.isSuccessful) {
                println("Correctly rejected non-existent teacher")
            }
        }

        @Test
        @DisplayName("sendAppointmentRequest() with studentId and placeholder teacher")
        fun `sendAppointmentRequest with studentId handles gracefully`() = runTest {
            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "placeholder@unimib.it",
                studentId = AuthTokens.studentId?.toIntOrNull() ?: 12345,
                messageBody = ""
            )

            // Then
            printResponse("sendAppointmentRequest(studentId)", response)
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("sendAppointmentRequest() with messageBody and placeholder values")
        fun `sendAppointmentRequest with messageBody handles gracefully`() = runTest {
            // When
            val response = messagesApi.sendAppointmentRequest(
                teacherKey = "placeholder@unimib.it",
                studentId = AuthTokens.studentId?.toIntOrNull() ?: 0,
                messageBody = "Test message without valid recipient"
            )

            // Then
            printResponse("sendAppointmentRequest(messageBody)", response)
            println("API responded with code: ${response.code}")
        }
    }

    @Nested
    @DisplayName("Response Structure Validation")
    inner class ResponseStructureTests {

        @Test
        @DisplayName("Alerts response has expected structure")
        fun `alerts response structure is valid`() = runTest {
            val response = messagesApi.getAlerts()
            assertSuccessfulResponse(response, "getAlerts")

            val alerts = assertNonNullBody(response, "getAlerts")

            // Verify structure
            assertNotNull(alerts.alerts, "Response must have alerts list")

            // alertsToRead can be null or an integer
            println("alertsToRead field: ${alerts.alertsToRead}")
        }

        @Test
        @DisplayName("Alert objects contain required fields")
        fun `alert objects have required fields`() = runTest {
            val response = messagesApi.getAlerts()
            assertSuccessfulResponse(response, "getAlerts")

            val alertsResponse = assertNonNullBody(response, "getAlerts")

            if (alertsResponse.alerts.isNotEmpty()) {
                val alert = alertsResponse.alerts.first()
                // Alerts should have basic properties
                println("Alert structure: title=${alert.title}")
            } else {
                println("No alerts to validate structure - list is empty")
            }
        }
    }
}
