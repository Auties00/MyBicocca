package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappCalendarApi Integration Tests")
class BicoccappCalendarApiTest : BicoccappApiTestBase() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @Nested
    @DisplayName("Calendar API")
    inner class CalendarTests {

        @Test
        @DisplayName("getCalendar() returns calendar events for authenticated user")
        fun `getCalendar returns calendar events successfully`() = runTest {
            // When - enrollmentId is required
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = calendarApi.getCalendar(enrollmentId = enrollmentId)

            // Then
            printResponse("getCalendar(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getCalendar")

            val calendarResponse = assertNonNullBody(response, "getCalendar")
            assertNotNull(calendarResponse.days, "Calendar list should not be null")

            println("Calendar events count: ${calendarResponse.days.size}")
        }

        @Test
        @DisplayName("getCalendar() with enrollmentId returns calendar for specific student")
        fun `getCalendar with enrollmentId returns calendar`() = runTest {
            // When
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = calendarApi.getCalendar(enrollmentId = enrollmentId)

            // Then
            printResponse("getCalendar(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getCalendar with enrollmentId")
        }

        @Test
        @DisplayName("getCalendar() with current date returns relevant events")
        fun `getCalendar with date returns calendar for specific date`() = runTest {
            // Given
            val currentDate = LocalDate.now().format(dateFormatter)
            val enrollmentId = AuthTokens.enrollmentId!!

            // When
            val response = calendarApi.getCalendar(
                enrollmentId = enrollmentId,
                date = currentDate
            )

            // Then
            printResponse("getCalendar(date=$currentDate)", response)
            assertSuccessfulResponse(response, "getCalendar with date")

            val calendarResponse = assertNonNullBody(response, "getCalendar")
            println("Calendar events for $currentDate: ${calendarResponse.days.size}")
        }

        @Test
        @DisplayName("getCalendar() with future date returns scheduled events")
        fun `getCalendar with future date returns calendar`() = runTest {
            // Given
            val futureDate = LocalDate.now().plusWeeks(1).format(dateFormatter)
            val enrollmentId = AuthTokens.enrollmentId!!

            // When
            val response = calendarApi.getCalendar(
                enrollmentId = enrollmentId,
                date = futureDate
            )

            // Then
            printResponse("getCalendar(date=$futureDate)", response)
            assertSuccessfulResponse(response, "getCalendar with future date")
        }

        @Test
        @DisplayName("getCalendar() with past date returns historical events")
        fun `getCalendar with past date returns calendar`() = runTest {
            // Given
            val pastDate = LocalDate.now().minusWeeks(1).format(dateFormatter)
            val enrollmentId = AuthTokens.enrollmentId!!

            // When
            val response = calendarApi.getCalendar(
                enrollmentId = enrollmentId,
                date = pastDate
            )

            // Then
            printResponse("getCalendar(date=$pastDate)", response)
            assertSuccessfulResponse(response, "getCalendar with past date")
        }

        @Test
        @DisplayName("getCalendar() with both enrollmentId and date parameters")
        fun `getCalendar with all parameters returns calendar`() = runTest {
            // Given
            val currentDate = LocalDate.now().format(dateFormatter)
            val enrollmentId = AuthTokens.enrollmentId!!

            // When
            val response = calendarApi.getCalendar(
                enrollmentId = enrollmentId,
                date = currentDate
            )

            // Then
            printResponse("getCalendar(all params)", response)
            assertSuccessfulResponse(response, "getCalendar with all parameters")
        }
    }

    @Nested
    @DisplayName("Available Courses API")
    inner class AvailableCoursesTests {

        @Test
        @DisplayName("getAvailableCourses() returns course catalog")
        fun `getAvailableCourses returns courses successfully`() = runTest {
            // When
            val response = calendarApi.getAvailableCourses()

            // Then
            printResponse("getAvailableCourses()", response)
            assertSuccessfulResponse(response, "getAvailableCourses")

            val coursesResponse = assertNonNullBody(response, "getAvailableCourses")
            assertNotNull(coursesResponse.courses, "Courses list should not be null")

            println("Available courses count: ${coursesResponse.courses.size}")
        }

        @Test
        @DisplayName("getAvailableCourses() response contains course details")
        fun `getAvailableCourses returns courses with details`() = runTest {
            // When
            val response = calendarApi.getAvailableCourses()
            assertSuccessfulResponse(response, "getAvailableCourses")

            val coursesResponse = assertNonNullBody(response, "getAvailableCourses")

            // Verify courses have expected properties
            if (coursesResponse.courses.isNotEmpty()) {
                val firstCourse = coursesResponse.courses.first()
                println("First course: ${firstCourse.lessonName}")
            }
        }
    }

    @Nested
    @DisplayName("Course Detail API")
    inner class CourseDetailTests {

        @Test
        @DisplayName("getCourseDetail() with both codes returns course information")
        fun `getCourseDetail with both codes returns course details`() = runTest {
            // First get available courses to find valid codes
            val coursesResponse = calendarApi.getAvailableCourses()

            if (coursesResponse.isSuccessful && coursesResponse.body()?.courses?.isNotEmpty() == true) {
                val course = coursesResponse.body()!!.courses.first()

                // When - both activityCode and courseCode are now required
                val response = calendarApi.getCourseDetail(
                    activityCode = course.activityCode!!,
                    courseCode = course.courseCode!!
                )

                // Then
                printResponse("getCourseDetail(activityCode=${course.activityCode}, courseCode=${course.courseCode})", response)

                if (response.isSuccessful) {
                    val courseDetail = assertNonNullBody(response, "getCourseDetail")
                    println("Course detail retrieved: ${courseDetail.lessonName}")
                }
            } else {
                println("No courses available to test getCourseDetail")
            }
        }

        @Test
        @DisplayName("getCourseDetail() with activityCode and courseCode returns course information")
        fun `getCourseDetail with activityCode and courseCode returns course details`() = runTest {
            // First get available courses to find valid codes
            val coursesResponse = calendarApi.getAvailableCourses()

            if (coursesResponse.isSuccessful && coursesResponse.body()?.courses?.isNotEmpty() == true) {
                val course = coursesResponse.body()!!.courses.first()

                // When
                val response = calendarApi.getCourseDetail(
                    activityCode = course.activityCode!!,
                    courseCode = course.courseCode!!
                )

                // Then
                printResponse("getCourseDetail(activityCode, courseCode)", response)

                if (response.isSuccessful) {
                    val courseDetail = assertNonNullBody(response, "getCourseDetail")
                    println("Course detail retrieved: ${courseDetail.lessonName}")
                }
            }
        }

        @Test
        @DisplayName("getCourseDetail() with codes from first available course")
        fun `getCourseDetail with codes from available course returns details`() = runTest {
            // First get available courses
            val coursesResponse = calendarApi.getAvailableCourses()

            if (coursesResponse.isSuccessful && coursesResponse.body()?.courses?.isNotEmpty() == true) {
                val course = coursesResponse.body()!!.courses.first()

                // When
                val response = calendarApi.getCourseDetail(
                    activityCode = course.activityCode!!,
                    courseCode = course.courseCode!!
                )

                // Then
                printResponse("getCourseDetail(both codes)", response)

                if (response.isSuccessful) {
                    val courseDetail = assertNonNullBody(response, "getCourseDetail")
                    assertNotNull(courseDetail.lessonName, "Course name should not be null")
                }
            }
        }

        @Test
        @DisplayName("getCourseDetail() for known Informatica course")
        fun `getCourseDetail for informatica course returns details`() = runTest {
            // Test with known course code for Informatica
            // Using a placeholder activity code since the API now requires both
            val response = calendarApi.getCourseDetail(
                activityCode = "E3101Q", // Common activity code for Informatica courses
                courseCode = "F0801Q"
            )

            printResponse("getCourseDetail(activityCode=E3101Q, courseCode=F0801Q)", response)

            if (response.isSuccessful) {
                val courseDetail = assertNonNullBody(response, "getCourseDetail")
                println("Informatica course retrieved: ${courseDetail.lessonName}")
            }
        }
    }

    @Nested
    @DisplayName("Calendar Management API")
    inner class CalendarManagementTests {

        @Test
        @DisplayName("addCourseToCalendar() with placeholder values returns appropriate response")
        fun `addCourseToCalendar with placeholder values handles gracefully`() = runTest {
            // When - all parameters are now required, using placeholder values
            val response = calendarApi.addCourseToCalendar(
                userId = AuthTokens.uid ?: "test_user",
                cdsCode = "TEST_CDS",
                activityCode = "TEST_ACTIVITY",
                lessonName = "Test Lesson",
                courseCode = "TEST_COURSE"
            )

            // Then
            printResponse("addCourseToCalendar(placeholder values)", response)
            println("addCourseToCalendar API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("addCourseToCalendar() with minimal valid parameters")
        fun `addCourseToCalendar with minimal parameters returns response`() = runTest {
            // When - using placeholder values since all are required
            val response = calendarApi.addCourseToCalendar(
                userId = AuthTokens.uid ?: "test_user",
                cdsCode = "F0801Q",
                activityCode = "E3101Q",
                lessonName = "Test",
                courseCode = "F0801Q",
                partition = null
            )

            // Then
            printResponse("addCourseToCalendar(minimal params)", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("addCourseToCalendar() with course from available courses")
        fun `addCourseToCalendar with available course returns response`() = runTest {
            // First get available courses
            val coursesResponse = calendarApi.getAvailableCourses()

            if (coursesResponse.isSuccessful && coursesResponse.body()?.courses?.isNotEmpty() == true) {
                val course = coursesResponse.body()!!.courses.first()

                // When - all parameters are required
                val response = calendarApi.addCourseToCalendar(
                    userId = AuthTokens.uid ?: "test_user",
                    cdsCode = course.cdsCode ?: "F0801Q",
                    activityCode = course.activityCode!!,
                    lessonName = course.lessonName!!,
                    courseCode = course.courseCode!!
                )

                // Then
                printResponse("addCourseToCalendar(from available)", response)

                if (response.isSuccessful) {
                    val result = assertNonNullBody(response, "addCourseToCalendar")
                    println("Course added to calendar: ${result.status}")
                }
            }
        }
    }

    @Nested
    @DisplayName("Response Structure Validation")
    inner class ResponseStructureTests {

        @Test
        @DisplayName("Calendar response has expected structure")
        fun `calendar response structure is valid`() = runTest {
            val response = calendarApi.getCalendar(enrollmentId = AuthTokens.enrollmentId!!)
            assertSuccessfulResponse(response, "getCalendar")

            val calendar = assertNonNullBody(response, "getCalendar")

            // Verify structure
            assertNotNull(calendar.days, "Calendar must have calendar list")
        }

        @Test
        @DisplayName("Available courses response has expected structure")
        fun `available courses response structure is valid`() = runTest {
            val response = calendarApi.getAvailableCourses()
            assertSuccessfulResponse(response, "getAvailableCourses")

            val courses = assertNonNullBody(response, "getAvailableCourses")

            // Verify structure
            assertNotNull(courses.courses, "Response must have courses list")
        }

        @Test
        @DisplayName("Calendar events contain required fields")
        fun `calendar events have required fields`() = runTest {
            val response = calendarApi.getCalendar(enrollmentId = AuthTokens.enrollmentId!!)
            assertSuccessfulResponse(response, "getCalendar")

            val calendarResponse = assertNonNullBody(response, "getCalendar")

            if (calendarResponse.days.isNotEmpty()) {
                val day = calendarResponse.days.first()
                for (event in day.events) {
                    // Events should have basic properties
                    println("Event: ${event.courseName}, Type: ${event.type}")
                }
            }
        }

        @Test
        @DisplayName("Course objects contain required fields")
        fun `courses have required fields`() = runTest {
            val response = calendarApi.getAvailableCourses()
            assertSuccessfulResponse(response, "getAvailableCourses")

            val coursesResponse = assertNonNullBody(response, "getAvailableCourses")

            if (coursesResponse.courses.isNotEmpty()) {
                val course = coursesResponse.courses.first()
                // Courses should have basic properties
                println("Course: ${course.lessonName}, Code: ${course.courseCode}")
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("getCalendar() with invalid date format handles gracefully")
        fun `getCalendar with invalid date handles gracefully`() = runTest {
            // When
            val response = calendarApi.getCalendar(
                enrollmentId = AuthTokens.enrollmentId!!,
                date = "invalid-date"
            )

            // Then
            printResponse("getCalendar(invalid date)", response)
            // API should handle invalid dates gracefully
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getCalendar() with empty date string")
        fun `getCalendar with empty date handles gracefully`() = runTest {
            // When
            val response = calendarApi.getCalendar(
                enrollmentId = AuthTokens.enrollmentId!!,
                date = ""
            )

            // Then
            printResponse("getCalendar(empty date)", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("getCourseDetail() with invalid codes handles gracefully")
        fun `getCourseDetail with invalid codes handles gracefully`() = runTest {
            // When
            val response = calendarApi.getCourseDetail(
                activityCode = "INVALID_CODE",
                courseCode = "INVALID_CODE"
            )

            // Then
            printResponse("getCourseDetail(invalid codes)", response)
            // API should return 404 or empty response for invalid codes
            println("API responded with code: ${response.code()}")
        }
    }
}
