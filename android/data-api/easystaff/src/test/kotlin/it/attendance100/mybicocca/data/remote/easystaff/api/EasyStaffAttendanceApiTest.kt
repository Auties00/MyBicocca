package it.attendance100.mybicocca.data.remote.easystaff.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Integration tests for EasyStaffAttendanceApi.
 *
 * Note: These tests use mock data and may fail if the API requires valid credentials.
 * The tests are designed to verify the API structure and error handling.
 */
class EasyStaffAttendanceApiTest : EasyStaffTestBase() {
    companion object {
        // Mock student ID - this is a test value and will likely fail on production
        private const val MOCK_STUDENT_ID = "909697"
        private const val MOCK_LESSON_CODE = "TEST_LESSON"
        private const val MOCK_DEVICE_TOKEN = "TEST_DEVICE"

        // Bicocca campus coordinates (roughly)
        private const val MOCK_LONGITUDE = 9.2094
        private const val MOCK_LATITUDE = 45.5175
    }

    @Test
    suspend fun getAttendanceHistoryWithInvalidStudent() {
        val response = api.attendance.getAttendanceHistory(MOCK_STUDENT_ID)
        assertTrue(response.isEmpty())
    }

    @Test
    @Disabled
    suspend fun getAttendanceHistoryWithValidData() {
        TODO("Create a test with valid data to get attendance history")
    }

    @Test
    suspend fun certifyAttendanceWithInvalidData() {
        val result = api.attendance.certifyAttendance(
            studentId = MOCK_STUDENT_ID,
            lessonCode = MOCK_LESSON_CODE,
            deviceToken = MOCK_DEVICE_TOKEN,
            longitude = MOCK_LONGITUDE,
            latitude = MOCK_LATITUDE
        )

        assertNotNull(result, "Result should not be null")
        assertFalse(result.success)
        assertNotNull(result.message, "Result message should not be null")
    }

    @Test
    @Disabled
    suspend fun certifyAttendanceWithValidData() {
        TODO("Create a test with valid data to certify attendance")
    }
}
