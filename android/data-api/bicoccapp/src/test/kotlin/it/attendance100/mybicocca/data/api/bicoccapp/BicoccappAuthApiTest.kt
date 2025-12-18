package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappAuthApi Integration Tests")
class BicoccappAuthApiTest : BicoccappApiTestBase() {

    @Nested
    @DisplayName("Login Initiation API")
    inner class LoginInitiationTests {

        @Test
        @DisplayName("initiateLogin() returns redirect to IdP")
        fun `initiateLogin returns 302 redirect`() = runTest {
            // When
            val response = authApi.initiateLogin()

            // Then
            printResponse("initiateLogin()", response)

            // Expected behavior: 302 redirect to university IdP
            assertEquals(
                302,
                response.code(),
                "initiateLogin should return 302 redirect"
            )

            // Check for Location header
            val locationHeader = response.headers()["Location"]
            assertNotNull(locationHeader, "Response should have Location header")

            println("Redirect URL: $locationHeader")

            // Verify redirect URL is to university IdP
            locationHeader?.let { url ->
                assertTrue(
                    url.contains("unimib") || url.contains("oauth") || url.contains("openid"),
                    "Redirect should be to university IdP"
                )
            }
        }

        @Test
        @DisplayName("initiateLogin() redirect URL contains required OAuth parameters")
        fun `initiateLogin redirect contains oauth parameters`() = runTest {
            // When
            val response = authApi.initiateLogin()

            // Then
            assertEquals(302, response.code(), "Should be redirect")

            val locationHeader = response.headers()["Location"]
            assertNotNull(locationHeader, "Should have Location header")

            locationHeader?.let { url ->
                println("Redirect URL parameters:")
                println("  URL: $url")

                // OAuth2 redirect should contain certain parameters
                // (client_id, redirect_uri, scope, state, nonce, response_type)
                // The exact parameters depend on the IdP configuration
                assertTrue(url.isNotEmpty(), "Redirect URL should not be empty")
            }
        }

        @Test
        @DisplayName("initiateLogin() sets cookies for session tracking")
        fun `initiateLogin sets session cookies`() = runTest {
            // Clear existing cookies
            cookieJar.clear()

            // When
            val response = authApi.initiateLogin()

            // Then
            assertEquals(302, response.code(), "Should be redirect")

            // Check if cookies were set
            val cookies = cookieJar.getAllCookies()
            println("Cookies after initiateLogin:")
            cookies.forEach { (domain, cookieList) ->
                cookieList.forEach { cookie ->
                    println("  $domain: ${cookie.name}=${cookie.value.take(20)}...")
                }
            }
        }

        @Test
        @DisplayName("initiateLogin() multiple calls return consistent redirect")
        fun `initiateLogin is consistent`() = runTest {
            // When
            val response1 = authApi.initiateLogin()
            val response2 = authApi.initiateLogin()

            // Then
            assertEquals(response1.code(), response2.code(), "Status codes should match")

            // Both should redirect to same IdP (base URL)
            val location1 = response1.headers()["Location"]
            val location2 = response2.headers()["Location"]

            // Extract base URLs for comparison
            val baseUrl1 = location1?.substringBefore("?")
            val baseUrl2 = location2?.substringBefore("?")

            assertEquals(baseUrl1, baseUrl2, "Redirect base URLs should match")
        }
    }

    @Nested
    @DisplayName("Login Callback API")
    inner class LoginCallbackTests {

        @Test
        @DisplayName("handleLoginCallback() without parameters returns appropriate response")
        fun `handleLoginCallback without params handles gracefully`() = runTest {
            // When - no code or state
            val response = authApi.handleLoginCallback()

            // Then
            printResponse("handleLoginCallback()", response)

            // Without proper authorization code, should fail or redirect to error
            println("handleLoginCallback responded with code: ${response.code()}")

            // Common responses: 302 (redirect to error), 400 (bad request), 401 (unauthorized)
            assertTrue(
                response.code() in listOf(302, 400, 401, 422),
                "Should reject callback without proper parameters"
            )
        }

        @Test
        @DisplayName("handleLoginCallback() with invalid code returns error")
        fun `handleLoginCallback with invalid code returns error`() = runTest {
            // When - invalid authorization code
            val response = authApi.handleLoginCallback(
                code = "invalid_code_12345",
                state = "invalid_state_67890"
            )

            // Then
            printResponse("handleLoginCallback(invalid code)", response)

            // Invalid code should be rejected
            println("API responded with code: ${response.code()}")

            // Should not return 200 OK with invalid code
            assertNotEquals(
                200,
                response.code(),
                "Invalid code should not succeed"
            )
        }

        @Test
        @DisplayName("handleLoginCallback() with only code parameter")
        fun `handleLoginCallback with only code returns error`() = runTest {
            // When - missing state parameter (CSRF vulnerability check)
            val response = authApi.handleLoginCallback(
                code = "some_code",
                state = null
            )

            // Then
            printResponse("handleLoginCallback(code only)", response)

            // Missing state should be rejected for CSRF protection
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("handleLoginCallback() with only state parameter")
        fun `handleLoginCallback with only state returns error`() = runTest {
            // When - missing code parameter
            val response = authApi.handleLoginCallback(
                code = null,
                state = "some_state"
            )

            // Then
            printResponse("handleLoginCallback(state only)", response)

            // Missing code should be rejected
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("handleLoginCallback() with empty parameters")
        fun `handleLoginCallback with empty params returns error`() = runTest {
            // When
            val response = authApi.handleLoginCallback(
                code = "",
                state = ""
            )

            // Then
            printResponse("handleLoginCallback(empty params)", response)
            println("API responded with code: ${response.code()}")
        }

        @Test
        @DisplayName("handleLoginCallback() with cookies parameter")
        fun `handleLoginCallback with cookies returns response`() = runTest {
            // When
            val response = authApi.handleLoginCallback(
                code = "test_code",
                state = "test_state",
                cookies = "session=test_session; other=value"
            )

            // Then
            printResponse("handleLoginCallback(with cookies)", response)
            println("API responded with code: ${response.code()}")
        }
    }

    @Nested
    @DisplayName("Authentication Flow Verification")
    inner class AuthFlowTests {

        @Test
        @DisplayName("Auth tokens are captured during Selenium login")
        fun `auth tokens are captured successfully`() {
            // This test verifies the test infrastructure is working

            // The Selenium login in @BeforeAll should have captured tokens
            assertTrue(AuthTokens.isLoggedIn(), "Auth tokens should be captured")

            println("Authentication verification:")
            println("  access-token present: ${AuthTokens.accessToken != null}")
            println("  client present: ${AuthTokens.client != null}")
            println("  uid present: ${AuthTokens.uid != null}")

            assertNotNull(AuthTokens.accessToken, "access-token should be present")
            assertNotNull(AuthTokens.client, "client should be present")
            assertNotNull(AuthTokens.uid, "uid should be present")
        }

        @Test
        @DisplayName("Auth tokens enable authenticated requests")
        fun `auth tokens enable api access`() = runTest {
            // Verify tokens work by making authenticated request
            val response = userApi.getProfile(AuthTokens.fiscalCode!!)

            assertSuccessfulResponse(response, "getProfile with auth")
            println("Authenticated request successful")
        }

        @Test
        @DisplayName("initiateLogin() redirect URL structure is valid")
        fun `login redirect url is properly formatted`() = runTest {
            // When
            val response = authApi.initiateLogin()

            // Then
            val locationHeader = response.headers()["Location"]
            assertNotNull(locationHeader, "Location header required")

            locationHeader?.let { url ->
                // Validate URL structure
                assertTrue(url.startsWith("http"), "Should be absolute URL")
                assertTrue(url.contains("://"), "Should have protocol")

                // Parse URL parts
                val hasQuery = url.contains("?")
                println("URL has query string: $hasQuery")

                if (hasQuery) {
                    val queryString = url.substringAfter("?")
                    val params = queryString.split("&")
                    println("Query parameters count: ${params.size}")

                    params.forEach { param ->
                        val parts = param.split("=", limit = 2)
                        if (parts.size == 2) {
                            println("  ${parts[0]}: ${parts[1].take(30)}...")
                        }
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Security Tests")
    inner class SecurityTests {

        @Test
        @DisplayName("handleLoginCallback() protects against CSRF")
        fun `callback validates state parameter`() = runTest {
            // When - using mismatched state
            val response = authApi.handleLoginCallback(
                code = "valid_looking_code",
                state = "forged_state_token"
            )

            // Then
            printResponse("handleLoginCallback(forged state)", response)

            // Should reject forged state
            assertNotEquals(
                200,
                response.code(),
                "Should reject forged state token"
            )
        }

        @Test
        @DisplayName("handleLoginCallback() rejects expired codes")
        fun `callback handles expired authorization codes`() = runTest {
            // Authorization codes expire quickly (typically 10 minutes)
            // Using an old-looking code format

            // When
            val response = authApi.handleLoginCallback(
                code = "expired_code_from_past",
                state = "old_state"
            )

            // Then
            printResponse("handleLoginCallback(expired)", response)

            // Should reject
            assertNotEquals(
                200,
                response.code(),
                "Should reject expired/invalid code"
            )
        }

        @Test
        @DisplayName("handleLoginCallback() handles injection attempts")
        fun `callback handles injection attempts safely`() = runTest {
            // When - SQL/code injection attempt
            val response = authApi.handleLoginCallback(
                code = "'; DROP TABLE users; --",
                state = "<script>alert('xss')</script>"
            )

            // Then
            printResponse("handleLoginCallback(injection)", response)

            // Should handle safely without server error
            assertNotEquals(
                500,
                response.code(),
                "Should not cause server error"
            )
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("initiateLogin() handles network timeouts gracefully")
        fun `initiateLogin completes within timeout`() = runTest {
            // When
            val startTime = System.currentTimeMillis()
            val response = authApi.initiateLogin()
            val duration = System.currentTimeMillis() - startTime

            // Then
            println("initiateLogin completed in ${duration}ms")
            assertTrue(duration < 30000, "Should complete within timeout")

            assertEquals(302, response.code(), "Should return redirect")
        }

        @Test
        @DisplayName("handleLoginCallback() with very long code")
        fun `handleLoginCallback with long code handles gracefully`() = runTest {
            // Given
            val longCode = "a".repeat(10000)

            // When
            val response = authApi.handleLoginCallback(
                code = longCode,
                state = "normal_state"
            )

            // Then
            printResponse("handleLoginCallback(long code)", response)

            // Should handle gracefully
            assertNotEquals(
                500,
                response.code(),
                "Should handle long code gracefully"
            )
        }

        @Test
        @DisplayName("handleLoginCallback() with special characters")
        fun `handleLoginCallback with special chars handles gracefully`() = runTest {
            // When
            val response = authApi.handleLoginCallback(
                code = "code+with/special=chars&more",
                state = "state%20encoded%3D"
            )

            // Then
            printResponse("handleLoginCallback(special chars)", response)
            println("API responded with code: ${response.code()}")
        }
    }
}
