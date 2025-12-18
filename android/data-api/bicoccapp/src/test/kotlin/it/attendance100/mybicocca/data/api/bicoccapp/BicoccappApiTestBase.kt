package it.attendance100.mybicocca.data.api.bicoccapp

import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.util.Optional
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BicoccappApiTestBase {

    companion object {
        private const val BASE_URL = "https://backoffice-app.unimib.it/api/v1/"
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L

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

    /**
     * In-memory cookie jar that persists cookies across requests.
     */
    protected class InMemoryCookieJar : CookieJar {
        private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.host
            cookieStore.getOrPut(host) { mutableListOf() }.apply {
                cookies.forEach { newCookie ->
                    removeAll { it.name == newCookie.name }
                    add(newCookie)
                }
            }
            if (host.contains(".")) {
                val baseDomain = host.substringAfter(".")
                cookieStore.getOrPut(baseDomain) { mutableListOf() }.apply {
                    cookies.forEach { newCookie ->
                        removeAll { it.name == newCookie.name }
                        add(newCookie)
                    }
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val host = url.host
            val cookies = mutableListOf<Cookie>()

            cookieStore[host]?.let { cookies.addAll(it) }

            if (host.contains(".")) {
                val baseDomain = host.substringAfter(".")
                cookieStore[baseDomain]?.let { cookies.addAll(it) }
            }

            val now = System.currentTimeMillis()
            return cookies.filter { it.expiresAt > now }.distinctBy { it.name }
        }

        fun getAllCookies(): Map<String, List<Cookie>> = cookieStore.toMap()

        fun clear() = cookieStore.clear()
    }

    protected val gson = GsonBuilder().setPrettyPrinting().create()
    protected val cookieJar = InMemoryCookieJar()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                if (!AuthTokens.isLoggedIn()) {
                    chain.proceed(originalRequest)
                } else {
                    val authenticatedRequest = originalRequest.newBuilder().apply {
                        AuthTokens.accessToken?.let { header(HEADER_ACCESS_TOKEN, it) }
                        AuthTokens.client?.let { header(HEADER_CLIENT, it) }
                        AuthTokens.uid?.let { header(HEADER_UID, it) }
                    }.build()
                    chain.proceed(authenticatedRequest)
                }
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val retrofitClient: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API instances
    protected val authApi: BicoccappAuthApi by lazy {
        retrofitClient.create(BicoccappAuthApi::class.java)
    }

    protected val userApi: BicoccappUserApi by lazy {
        retrofitClient.create(BicoccappUserApi::class.java)
    }

    protected val calendarApi: BicoccappCalendarApi by lazy {
        retrofitClient.create(BicoccappCalendarApi::class.java)
    }

    protected val wizardApi: BicoccappWizardApi by lazy {
        retrofitClient.create(BicoccappWizardApi::class.java)
    }

    protected val messagesApi: BicoccappMessagesApi by lazy {
        retrofitClient.create(BicoccappMessagesApi::class.java)
    }

    protected val campusApi: BicoccappCampusApi by lazy {
        retrofitClient.create(BicoccappCampusApi::class.java)
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
                        AuthTokens.enrollmentId = career.enrollmentId
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
                println("Failed to fetch user profile: ${response.code()} ${response.message()}")
                println("Error body: ${response.errorBody()?.string()}")
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

                if (url.contains("access_token=") || url.contains("inappbrowser")) {
                    println("\n[DETECTED] Redirect URL with tokens: $url")
                    extractTokensFromUrl(url)
                    if (AuthTokens.isLoggedIn()) {
                        foundTokens = true
                    }
                }

                val headers = response.response.headers
                val accessToken = headers["access-token"] as? String
                val client = headers["client"] as? String
                val uid = headers["uid"] as? String

                if (accessToken != null && client != null && uid != null) {
                    AuthTokens.accessToken = accessToken
                    AuthTokens.client = client
                    AuthTokens.uid = uid
                    foundTokens = true
                    println("\n[CAPTURED] Auth tokens from headers: $url")
                }
            }

            val loginUrl = "https://backoffice-app.unimib.it/api/v1/auth/openid_connect"
            println("\nOpening browser and navigating to login...")
            println("URL: $loginUrl")
            driver.get(loginUrl)

            println("\n${"=".repeat(60)}")
            println("BROWSER OPENED - Please complete the login process")
            println("=".repeat(60))
            println("\nWaiting for authentication...")

            val startTime = System.currentTimeMillis()

            while (!foundTokens && System.currentTimeMillis() - startTime < AUTH_TIMEOUT_MS) {
                try {
                    val currentUrl = driver.currentUrl ?: continue

                    if (currentUrl.contains("access_token=") || currentUrl.contains("inappbrowser?")) {
                        println("\n[DETECTED] Current URL contains tokens!")
                        println("URL: $currentUrl")
                        extractTokensFromUrl(currentUrl)
                        if (AuthTokens.isLoggedIn()) {
                            foundTokens = true
                            break
                        }
                    }
                } catch (_: Exception) {
                    println("\nBrowser was closed.")
                    break
                }

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
        println("Status: ${response.code()} ${response.message()}")
        println("=".repeat(60))

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                println("Response Body:")
                println(gson.toJson(body))
            } else {
                println("Response body is null")
            }
        } else {
            println("Error Body:")
            println(response.errorBody()?.string() ?: "No error body")
        }
    }

    /**
     * Asserts that a response is successful (2xx status code).
     */
    protected fun <T> assertSuccessfulResponse(response: Response<T>, apiName: String) {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "No error body"
            throw AssertionError(
                "$apiName failed with status ${response.code()}: $errorBody"
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
