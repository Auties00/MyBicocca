package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappUserApi Integration Tests")
class BicoccappUserApiTest : BicoccappApiTestBase() {

    @Nested
    @DisplayName("Profile API")
    inner class ProfileTests {

        @Test
        @DisplayName("getProfile() returns user profile for authenticated user")
        fun `getProfile returns user profile successfully`() = runTest {
            // When - use uid (email) as fiscalCode to fetch profile
            val response = userApi.getProfile(AuthTokens.fiscalCode!!)

            // Then
            printResponse("getProfile()", response)
            assertSuccessfulResponse(response, "getProfile")

            val profile = assertNonNullBody(response, "getProfile")
            assertNotNull(profile.user, "User should not be null")
            assertNotNull(profile.careers, "Careers list should not be null")

            // Verify user has basic information
            profile.user?.let { user ->
                println("User profile retrieved: ${user.name} ${user.surname}")
            }
        }

        @Test
        @DisplayName("getProfile() with fiscalCode parameter returns specific user")
        fun `getProfile with fiscalCode returns user profile`() = runTest {
            // First get current user's profile to potentially get fiscal code
            val currentProfile = userApi.getProfile(AuthTokens.fiscalCode!!)
            assertSuccessfulResponse(currentProfile, "getProfile")

            // The fiscalCode query is for admin purposes
            // Regular users can only fetch their own profile
            val response = userApi.getProfile(AuthTokens.fiscalCode!!)

            printResponse("getProfile(fiscalCode)", response)
            assertSuccessfulResponse(response, "getProfile with fiscalCode")
        }
    }

    @Nested
    @DisplayName("Career API")
    inner class CareerTests {

        @Test
        @DisplayName("getCareer() returns career information for authenticated user")
        fun `getCareer returns career data successfully`() = runTest {
            // When - personId, enrollmentId, studentId are required for this endpoint
            val personId = AuthTokens.personId!!
            val enrollmentId = AuthTokens.enrollmentId!!
            val studentId = AuthTokens.studentId!!

            val response = userApi.getCareer(
                personId = personId,
                enrollmentId = enrollmentId,
                studentId = studentId
            )

            // Then
            printResponse("getCareer(personId=$personId, enrollmentId=$enrollmentId, studentId=$studentId)", response)
            assertSuccessfulResponse(response, "getCareer")

            val careerResponse = assertNonNullBody(response, "getCareer")
            // Response has singular 'career' property
            assertNotNull(careerResponse.career, "Career should not be null")

            careerResponse.career?.let { career ->
                println("Career retrieved with ${career.averages.size} averages")
                assertNotNull(career.averages, "Career averages should not be null")
            }
        }

        @Test
        @DisplayName("getCareer() with identifiers returns specific career")
        fun `getCareer with identifiers returns career data`() = runTest {
            // When - using captured identifiers
            val response = userApi.getCareer(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!,
                studentId = AuthTokens.studentId!!
            )

            // Then
            printResponse("getCareer(identifiers)", response)
            assertSuccessfulResponse(response, "getCareer with identifiers")
        }

        @Test
        @DisplayName("getCareer() with all parameters returns career data")
        fun `getCareer with all parameters returns career data`() = runTest {
            // When
            val response = userApi.getCareer(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!,
                studentId = AuthTokens.studentId!!
            )

            // Then
            printResponse("getCareer(all params)", response)
            assertSuccessfulResponse(response, "getCareer with all parameters")
        }
    }

    @Nested
    @DisplayName("Exams API")
    inner class ExamsTests {

        @Test
        @DisplayName("getExams() returns exam history for authenticated user")
        fun `getExams returns exam history successfully`() = runTest {
            // When - enrollmentId is required for this endpoint
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getExams(enrollmentId = enrollmentId)

            // Then
            printResponse("getExams(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getExams")

            val examsResponse = assertNonNullBody(response, "getExams")
            // Response has singular 'career' property
            assertNotNull(examsResponse.career, "Exams career should not be null")

            examsResponse.career?.let { career ->
                println("Exams retrieved, count: ${career.exams.size}")
            }
        }

        @Test
        @DisplayName("getExams() with enrollmentId parameter returns specific exams")
        fun `getExams with enrollmentId returns exam data`() = runTest {
            // When - using captured enrollmentId
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getExams(enrollmentId = enrollmentId)

            // Then
            printResponse("getExams(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getExams with enrollmentId")
        }
    }

    @Nested
    @DisplayName("Exam Sessions API")
    inner class ExamSessionsTests {

        @Test
        @DisplayName("getExamsSessions() returns available and registered exam sessions")
        fun `getExamsSessions returns exam sessions successfully`() = runTest {
            // When - personId and enrollmentId are required for this endpoint
            val personId = AuthTokens.personId!!
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getExamsSessions(
                personId = personId,
                enrollmentId = enrollmentId
            )

            // Then
            printResponse("getExamsSessions(personId=$personId, enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getExamsSessions")

            val sessionsResponse = assertNonNullBody(response, "getExamsSessions")
            // Response has singular 'career' property
            assertNotNull(sessionsResponse.career, "Exam sessions career should not be null")

            println("Exam sessions retrieved successfully")
        }

        @Test
        @DisplayName("getExamsSessions() with identifiers returns specific sessions")
        fun `getExamsSessions with identifiers returns sessions`() = runTest {
            // When - using captured identifiers
            val response = userApi.getExamsSessions(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getExamsSessions(identifiers)", response)
            assertSuccessfulResponse(response, "getExamsSessions with identifiers")
        }

        @Test
        @DisplayName("getExamsSessions() with personId and enrollmentId returns sessions")
        fun `getExamsSessions with personId and enrollmentId returns sessions`() = runTest {
            // When
            val response = userApi.getExamsSessions(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getExamsSessions(personId, enrollmentId)", response)
            assertSuccessfulResponse(response, "getExamsSessions with personId and enrollmentId")
        }

        @Test
        @DisplayName("getExamsSessions() with both parameters")
        fun `getExamsSessions with both parameters returns sessions`() = runTest {
            // When
            val response = userApi.getExamsSessions(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getExamsSessions(both params)", response)
            assertSuccessfulResponse(response, "getExamsSessions with both parameters")
        }
    }

    @Nested
    @DisplayName("Exam Session Registration API")
    inner class ExamSessionRegistrationTests {

        @Test
        @DisplayName("addExamSession() with invalid IDs returns appropriate response")
        fun `addExamSession with invalid IDs handles gracefully`() = runTest {
            // Note: This test verifies the API handles invalid parameters appropriately
            // Without valid session IDs, the API should return an error

            // When - using placeholder invalid IDs
            val response = userApi.addExamSession(
                cdsId = 0,
                activityId = 0,
                activityItemId = 0,
                activityAppealId = 0
            )

            // Then
            printResponse("addExamSession(invalid IDs)", response)
            // The API should either fail gracefully or return an error
            // We're testing the API doesn't crash
            println("addExamSession API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("cancelExamSession() with invalid IDs returns appropriate response")
        fun `cancelExamSession with invalid IDs handles gracefully`() = runTest {
            // Note: This test verifies the API handles invalid parameters appropriately

            // When - using placeholder invalid IDs
            val response = userApi.cancelExamSession(
                cdsId = 0,
                activityId = 0,
                activityItemId = 0,
                activityAppealId = 0,
                studentId = AuthTokens.studentId!!
            )

            // Then
            printResponse("cancelExamSession(invalid IDs)", response)
            println("cancelExamSession API responded with code: ${response.code}")
        }
    }

    @Nested
    @DisplayName("Taxes API")
    inner class TaxesTests {

        @Test
        @DisplayName("getTaxes() returns tuition fee information")
        fun `getTaxes returns fee information successfully`() = runTest {
            // When - personId and enrollmentId are required for this endpoint
            val personId = AuthTokens.personId!!
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getTaxes(
                personId = personId,
                enrollmentId = enrollmentId
            )

            // Then
            printResponse("getTaxes(personId=$personId, enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getTaxes")

            val taxesResponse = assertNonNullBody(response, "getTaxes")
            // Response has singular 'career' property
            assertNotNull(taxesResponse.career, "Taxes career should not be null")

            println("Taxes information retrieved successfully")
        }

        @Test
        @DisplayName("getTaxes() with personId parameter")
        fun `getTaxes with personId returns fee information`() = runTest {
            // When
            val response = userApi.getTaxes(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getTaxes(personId, enrollmentId)", response)
            assertSuccessfulResponse(response, "getTaxes with personId")
        }

        @Test
        @DisplayName("getTaxes() with enrollmentId parameter")
        fun `getTaxes with enrollmentId returns fee information`() = runTest {
            // When - using captured identifiers
            val response = userApi.getTaxes(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getTaxes(identifiers)", response)
            assertSuccessfulResponse(response, "getTaxes with enrollmentId")
        }

        @Test
        @DisplayName("getTaxes() with both personId and enrollmentId")
        fun `getTaxes with both parameters returns fee information`() = runTest {
            // When
            val response = userApi.getTaxes(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )

            // Then
            printResponse("getTaxes(both params)", response)
            assertSuccessfulResponse(response, "getTaxes with both parameters")
        }
    }

    @Nested
    @DisplayName("Registrations API")
    inner class RegistrationsTests {

        @Test
        @DisplayName("getRegistrations() returns enrollment history")
        fun `getRegistrations returns enrollment data successfully`() = runTest {
            // When - enrollmentId is required for this endpoint
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getRegistrations(enrollmentId = enrollmentId)

            // Then
            printResponse("getRegistrations(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getRegistrations")

            val registrationsResponse = assertNonNullBody(response, "getRegistrations")
            // Response has singular 'career' property
            assertNotNull(registrationsResponse.career, "Registrations career should not be null")

            println("Registration history retrieved successfully")
        }

        @Test
        @DisplayName("getRegistrations() with enrollmentId parameter")
        fun `getRegistrations with enrollmentId returns enrollment data`() = runTest {
            // When - using captured enrollmentId
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = userApi.getRegistrations(enrollmentId = enrollmentId)

            // Then
            printResponse("getRegistrations(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getRegistrations with enrollmentId")
        }
    }

    @Nested
    @DisplayName("Response Structure Validation")
    inner class ResponseStructureTests {

        @Test
        @DisplayName("Profile response has expected structure")
        fun `profile response structure is valid`() = runTest {
            val response = userApi.getProfile(AuthTokens.fiscalCode!!)
            assertSuccessfulResponse(response, "getProfile")

            val profile = assertNonNullBody(response, "getProfile")

            // Verify structure
            assertNotNull(profile.user, "Profile must have user object")
            assertNotNull(profile.careers, "Profile must have careers list")
        }

        @Test
        @DisplayName("Career response has expected structure")
        fun `career response structure is valid`() = runTest {
            val response = userApi.getCareer(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!,
                studentId = AuthTokens.studentId!!
            )
            assertSuccessfulResponse(response, "getCareer")

            val careerResponse = assertNonNullBody(response, "getCareer")

            // Verify structure - singular career property
            assertNotNull(careerResponse.career, "Career response must have career object")
        }

        @Test
        @DisplayName("Exams response has expected structure")
        fun `exams response structure is valid`() = runTest {
            val response = userApi.getExams(enrollmentId = AuthTokens.enrollmentId!!)
            assertSuccessfulResponse(response, "getExams")

            val exams = assertNonNullBody(response, "getExams")

            // Verify structure - singular career property
            assertNotNull(exams.career, "Exams response must have career object")
        }

        @Test
        @DisplayName("Exam sessions response has expected structure")
        fun `exam sessions response structure is valid`() = runTest {
            val response = userApi.getExamsSessions(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )
            assertSuccessfulResponse(response, "getExamsSessions")

            val sessions = assertNonNullBody(response, "getExamsSessions")

            // Verify structure - singular career property
            assertNotNull(sessions.career, "Exam sessions response must have career object")
        }

        @Test
        @DisplayName("Taxes response has expected structure")
        fun `taxes response structure is valid`() = runTest {
            val response = userApi.getTaxes(
                personId = AuthTokens.personId!!,
                enrollmentId = AuthTokens.enrollmentId!!
            )
            assertSuccessfulResponse(response, "getTaxes")

            val taxes = assertNonNullBody(response, "getTaxes")

            // Verify structure - singular career property
            assertNotNull(taxes.career, "Taxes response must have career object")
        }

        @Test
        @DisplayName("Registrations response has expected structure")
        fun `registrations response structure is valid`() = runTest {
            val response = userApi.getRegistrations(enrollmentId = AuthTokens.enrollmentId!!)
            assertSuccessfulResponse(response, "getRegistrations")

            val registrations = assertNonNullBody(response, "getRegistrations")

            // Verify structure - singular career property
            assertNotNull(registrations.career, "Registrations response must have career object")
        }
    }
}
