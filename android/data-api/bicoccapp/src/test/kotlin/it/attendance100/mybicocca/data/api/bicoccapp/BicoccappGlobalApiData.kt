package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object BicoccappGlobalApiData : BeforeAllCallback, AutoCloseable {
    private const val BASE_URL = "https://backoffice-app.unimib.it/api/v1/"
    private const val REDIRECT_TRIGGER = "https://backoffice-app.unimib.it/inappbrowser"
    private const val TIMEOUT_MS = 30_000L
    private const val AUTH_TIMEOUT_SEC = 300L

    var session: BicoccappAuthSession? = null
    var profile: BicoccappUserProfile? = null
    var httpClient: HttpClient? = null
    var api: BicoccappApi? = null

    private fun createApi(httpClient: HttpClient): BicoccappApi {
        val ktorfit = Ktorfit.Builder()
            .baseUrl(BASE_URL)
            .httpClient(httpClient)
            .build()

        return BicoccappApi(
            auth = ktorfit.createBicoccappAuthApi(),
            profile = ktorfit.createBicoccappProfileApi(),
            career = ktorfit.createBicoccappCareerApi(),
            exams = ktorfit.createBicoccappExamsApi(),
            taxes = ktorfit.createBicoccappTaxesApi(),
            calendar = ktorfit.createBicoccappCalendarApi(),
            wizard = ktorfit.createBicoccappWizardApi(),
            campus = ktorfit.createBicoccappCampusApi()
        )
    }

    private fun createHttpClient(session: BicoccappAuthSession): HttpClient {
        val httpClient = HttpClient(OkHttp) {
            followRedirects = false

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json(Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }

            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
                requestTimeoutMillis = TIMEOUT_MS
            }

            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }

            defaultRequest {
                header("access-token", session.accessToken)
                header("client", session.client)
                header("uid", session.uid)
            }
        }
        return httpClient
    }

    override fun beforeAll(context: ExtensionContext) {
        if (session == null) {
            session = performLogin()
        }

        if (httpClient == null) {
            httpClient = createHttpClient(session!!)
        }

        if (api == null) {
            api = createApi(httpClient!!)
        }

        if (profile == null) {
            runBlocking {
                profile = fetchUserProfile(api!!, session!!)
            }
        }
    }

    private fun performLogin(): BicoccappAuthSession {
        val options = ChromeOptions().apply {
            browserVersion = "131"
            addArguments(
                "--start-maximized",
                "--disable-blink-features=AutomationControlled",
                "--remote-allow-origins=*",
                "--no-sandbox",
                "--disable-dev-shm-usage"
            )
            setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        }

        val driver = ChromeDriver(options)
        val sessionRef = AtomicReference<BicoccappAuthSession>()
        val latch = CountDownLatch(1)

        return try {
            driver.devTools.apply {
                createSession()
                send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))
                addListener(Network.responseReceived()) { response: ResponseReceived ->
                    val urlString = response.response.url
                    if (urlString.startsWith(REDIRECT_TRIGGER)) {
                        runCatching {
                            val session = parseSessionFromUrl(urlString)
                            sessionRef.set(session)
                            latch.countDown()
                        }
                    }
                }
            }

            driver.get("${BASE_URL}${BICOCCAPP_AUTH_PATH}")

            val success = latch.await(AUTH_TIMEOUT_SEC, TimeUnit.SECONDS)
            if (!success) throw IllegalStateException("Authentication timed out")

            sessionRef.get() ?: throw IllegalStateException("Failed to capture authentication tokens")
        } finally {
            runCatching {
                driver.devTools.close()
                driver.quit()
            }
        }
    }

    private fun parseSessionFromUrl(urlString: String): BicoccappAuthSession {
        val url = Url(urlString)
        val params = url.parameters

        val accessToken = requireNotNull(params["access_token"]) {
            "Missing access_token"
        }

        val client = requireNotNull(params["client"]) {
            "Missing client"
        }

        val uid = requireNotNull(params["uid"]) {
            "Missing uid"
        }

        val fiscalCode = requireNotNull(params["fiscal_code"]) {
            "Missing fiscal_code"
        }

        val matricId = requireNotNull(params["favourite_career"]?.toIntOrNull()) {
            "Missing favourite_career"
        }

        return BicoccappAuthSession(
            accessToken = accessToken,
            client = client,
            uid = uid,
            fiscalCode = fiscalCode,
            matricId = matricId
        )
    }

    private suspend fun fetchUserProfile(api: BicoccappApi, session: BicoccappAuthSession): BicoccappUserProfile {
        val userResponse = api.profile.getProfile(session.fiscalCode)
        val personId = userResponse.user.personId.toString()
        val appUserId = userResponse.user.appUserId.toString()

        val activeCareer = userResponse.careers.firstOrNull { it.isSelected }
            ?: userResponse.careers.firstOrNull()
            ?: throw IllegalStateException("No careers found for user")

        val enrollmentId = activeCareer.enrollmentId.toString()
        val studentId = activeCareer.studentId.toString()

        return BicoccappUserProfile(
            personId = personId,
            enrollmentId = enrollmentId,
            studentId = studentId,
            appUserId = appUserId
        )
    }

    override fun close() {
        httpClient?.close()
    }
}
