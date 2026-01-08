package it.attendance100.mybicocca.data.api.easystaff

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class EasyStaffAttendanceApiTest : EasyStaffTestBase() {
    companion object {
        private const val MOCK_STUDENT_ID = "909697"
        private const val MOCK_LESSON_CODE = "TEST_LESSON"
        private const val MOCK_DEVICE_TOKEN = "TEST_DEVICE"
        private const val MOCK_LONGITUDE = 9.2094
        private const val MOCK_LATITUDE = 45.5175
    }

    @Test
    suspend fun getAttendanceHistory() {
        val history = api.attendance.getAttendanceHistory(MOCK_STUDENT_ID)
        assertNotNull(history)
    }

    @Test
    suspend fun certifyAttendance() {
        val result = api.attendance.certifyAttendance(
            studentId = MOCK_STUDENT_ID,
            lessonCode = MOCK_LESSON_CODE,
            deviceToken = MOCK_DEVICE_TOKEN,
            longitude = MOCK_LONGITUDE,
            latitude = MOCK_LATITUDE
        )
        assertNotNull(result)
    }
}
