package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived
import java.net.URLDecoder
import java.util.Optional

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BicoccappApiTestBase {

    companion object {
        private const val BASE_URL = "https://backoffice-app.unimib.it/api/v1/"
        private const val TIMEOUT = 30_000L

        private const val HEADER_ACCESS_TOKEN = "access-token"
        private const val HEADER_CLIENT = "client"
        private const val HEADER_UID = "uid"

        private const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    /**
     * Authentication token storage for API requests.
     */
    protected object AuthTokens {
        var accessToken: String? = null
        var client: String? = null
        var uid: String? = null
        var fiscalCode: String? = null
        var matricId: Int? = null

        // User profile identifiers - fetched after login
        var personId: String? = null
        var enrollmentId: String? = null
        var studentId: String? = null

        fun isLoggedIn(): Boolean = accessToken != null && client != null && uid != null && fiscalCode != null

        fun hasUserProfile(): Boolean = personId != null && enrollmentId != null && studentId != null
    }

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            followRedirects = false

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }

            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT
                socketTimeoutMillis = TIMEOUT
                requestTimeoutMillis = TIMEOUT
            }

            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }

            defaultRequest {
                if (AuthTokens.isLoggedIn()) {
                    header(HEADER_ACCESS_TOKEN, AuthTokens.accessToken ?: "")
                    header(HEADER_CLIENT, AuthTokens.client ?: "")
                    header(HEADER_UID, AuthTokens.uid ?: "")
                }
            }
        }
    }

    private val ktorfitClient: Ktorfit by lazy {
        Ktorfit.Builder()
            .baseUrl(BASE_URL)
            .httpClient(httpClient)
            .converterFactories(ResponseConverterFactory())
            .build()
    }

    // API instances
    protected val authApi: BicoccappAuthApi by lazy {
        ktorfitClient.createBicoccappAuthApi()
    }

    protected val userApi: BicoccappUserApi by lazy {
        ktorfitClient.createBicoccappUserApi()
    }

    protected val calendarApi: BicoccappCalendarApi by lazy {
        ktorfitClient.createBicoccappCalendarApi()
    }

    protected val wizardApi: BicoccappWizardApi by lazy {
        ktorfitClient.createBicoccappWizardApi()
    }

    protected val messagesApi: BicoccappMessagesApi by lazy {
        ktorfitClient.createBicoccappMessagesApi()
    }

    protected val campusApi: BicoccappCampusApi by lazy {
        ktorfitClient.createBicoccappCampusApi()
    }

    @BeforeAll
    fun setupAuthentication() {
        if (!AuthTokens.isLoggedIn()) {
            performSeleniumLogin()
        }
        require(AuthTokens.isLoggedIn()) {
            "Authentication failed. Cannot proceed with tests."
        }

        // Fetch user profile to get personId, enrollmentId, studentId
        if (!AuthTokens.hasUserProfile()) {
            fetchUserProfile()
        }
        require(AuthTokens.hasUserProfile()) {
            "Failed to fetch user profile. Cannot proceed with tests."
        }
    }

    /**
     * Fetches the user profile to obtain personId, enrollmentId, and studentId.
     */
    private fun fetchUserProfile() {
        println("\n${"=".repeat(60)}")
        println("FETCHING USER PROFILE")
        println("=".repeat(60))

        try {
            val response = kotlinx.coroutines.runBlocking {
                userApi.getProfile(AuthTokens.fiscalCode!! )
            }

            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    // personId comes from the user object
                    AuthTokens.personId = profile.user?.personId?.toString()

                    // Get the first (active) career from the careers list
                    val career = profile.careers.firstOrNull()
                    if (career != null) {
                        AuthTokens.enrollmentId = career.enrollmentId?.toString()
                        AuthTokens.studentId = career.studentId?.toString()
                    }

                    println("User profile fetched successfully:")
                    println("  personId: ${AuthTokens.personId}")
                    println("  enrollmentId: ${AuthTokens.enrollmentId}")
                    println("  studentId: ${AuthTokens.studentId}")
                } else {
                    println("User profile response body is null")
                }
            } else {
                println("Failed to fetch user profile: ${response.code} ${response.message}")
                println("Error body: ${response.errorBody()}")
            }
        } catch (e: Exception) {
            println("Exception while fetching user profile: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Performs login using Selenium with Chrome DevTools to capture auth tokens.
     */
    private fun performSeleniumLogin() {
        println("\n${"#".repeat(60)}")
        println("# SELENIUM BROWSER LOGIN")
        println("#".repeat(60))

        println("\nSetting up Chrome for Testing...")

        val options = ChromeOptions().apply {
            browserVersion = "131"
            addArguments("--start-maximized")
            addArguments("--disable-blink-features=AutomationControlled")
            addArguments("--remote-allow-origins=*")
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
            setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        }

        println("\nStarting Chrome browser...")
        val driver = ChromeDriver(options)
        val devTools = driver.devTools

        try {
            devTools.createSession()
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

            var foundTokens = false

            devTools.addListener(Network.responseReceived()) { response: ResponseReceived ->
                val url = response.response.url
                if (url.startsWith("https://backoffice-app.unimib.it/inappbrowser")) {
                    println("\n[DETECTED] Redirect URL with tokens: $url")
                    extractTokensFromUrl(url)
                    if (AuthTokens.isLoggedIn()) {
                        foundTokens = true
                    }
                }
            }

            val loginUrl = BASE_URL + BICOCCAPP_AUTH_URL
            println("Redirect URL: $loginUrl")
            driver.get(loginUrl)

            println("\n${"=".repeat(60)}")
            println("BROWSER OPENED - Please complete the login process")
            println("=".repeat(60))
            println("\nWaiting for authentication...")

            val startTime = System.currentTimeMillis()

            while (!foundTokens && System.currentTimeMillis() - startTime < AUTH_TIMEOUT_MS) {
                Thread.sleep(500)
            }

            if (AuthTokens.isLoggedIn()) {
                println("\n${"=".repeat(60)}")
                println("LOGIN SUCCESSFUL!")
                println("=".repeat(60))
                println("\nCaptured tokens:")
                println("  access-token: ${AuthTokens.accessToken?.take(20)}...")
                println("  client: ${AuthTokens.client}")
                println("  uid: ${AuthTokens.uid}")
                println("  matricId: ${AuthTokens.matricId}")
            } else {
                println("\nTimeout or login not completed.")
                throw IllegalStateException("Authentication failed")
            }

        } finally {
            println("\nClosing browser...")
            try {
                devTools.close()
                driver.quit()
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Extracts auth tokens from a redirect URL.
     */
    private fun extractTokensFromUrl(url: String) {
        val params = parseQueryParams(url)

        params["access_token"]?.let {
            AuthTokens.accessToken = it
            println("  Found access_token")
        }
        params["client"]?.let {
            AuthTokens.client = it
            println("  Found client")
        }
        params["uid"]?.let {
            AuthTokens.uid = URLDecoder.decode(it, "UTF-8")
            println("  Found uid: ${AuthTokens.uid}")
        }
        params["favourite_career"]?.let {
            AuthTokens.matricId = it.toIntOrNull()
            println("  Found favourite_career (matricId): ${AuthTokens.matricId}")
        }
        params["fiscal_code"]?.let {
            AuthTokens.fiscalCode = it
            println("  Found fiscal_code (fiscalCode): ${AuthTokens.fiscalCode}")
        }
    }

    /**
     * Parses URL query parameters.
     */
    private fun parseQueryParams(url: String): Map<String, String> {
        val queryStart = url.indexOf('?')
        if (queryStart == -1) return emptyMap()

        return url.substring(queryStart + 1)
            .split('&')
            .mapNotNull { param ->
                val parts = param.split('=', limit = 2)
                if (parts.size == 2) {
                    URLDecoder.decode(parts[0], "UTF-8") to
                            URLDecoder.decode(parts[1], "UTF-8")
                } else null
            }
            .toMap()
    }

    /**
     * Pretty prints a response body as JSON for debugging.
     */
    protected fun <T> printResponse(name: String, response: Response<T>) {
        println("\n${"=".repeat(60)}")
        println("API: $name")
        println("Status: ${response.code} ${response.message}")
        println("=".repeat(60))

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                println("Response Body:")
                println(body)
            } else {
                println("Response body is null")
            }
        } else {
            println("Error Body:")
            println(response.errorBody() ?: "No error body")
        }
    }

    /**
     * Asserts that a response is successful (2xx status code).
     */
    protected fun <T> assertSuccessfulResponse(response: Response<T>, apiName: String) {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody() ?: "No error body"
            throw AssertionError(
                "$apiName failed with status ${response.code}: $errorBody"
            )
        }
    }

    /**
     * Asserts that a response has a non-null body.
     */
    protected fun <T> assertNonNullBody(response: Response<T>, apiName: String): T {
        assertSuccessfulResponse(response, apiName)
        return response.body() ?: throw AssertionError("$apiName returned null body")
    }
}
