package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.RequestWillBeSent
import java.util.Optional
import kotlin.io.encoding.Base64

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ElearningTestBase {
    companion object {
        private const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    protected val wsToken: String
        get() = AuthTokens.wsToken
            ?: throw IllegalStateException("Authentication failed: no wstoken")

    protected lateinit var api: ElearningApi

    @BeforeAll
    fun setup() {
        api = ElearningApi()
        if(!AuthTokens.isLoggedIn()) {
            runBlocking {
                login()
            }
        }
    }

    @AfterAll
    fun tearDown() {
        api.close()
    }

    private suspend fun login() {
        println("Starting authentication flow...")
        val loginUrl = api.site.getAuthUrl()

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

        var token: String? = null

        try {
            devTools.createSession()
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

            var foundTokens = false

            devTools.addListener(Network.requestWillBeSent()) { response: RequestWillBeSent ->
                val url = response.request.url
                if (url.startsWith("moodlemobile://")) {
                    println("\n[DETECTED] Redirect URL with tokens: $url")
                    val base64Tokens = url.substringAfter("token=")
                    try {
                        val decoded = Base64.decode(base64Tokens).decodeToString()
                        token = decoded.split(":::")[1]
                        println("[DETECTED] Parsed token: $token")
                        foundTokens = true
                    } catch (e: Exception) {
                        println("Error parsing token: ${e.message}")
                    }
                }
            }

            driver.get(loginUrl)

            println("\n${"=".repeat(60)}")
            println("BROWSER OPENED - Please complete the login process")
            println("=".repeat(60))
            println("\nWaiting for authentication...")

            val startTime = System.currentTimeMillis()

            while (!foundTokens && System.currentTimeMillis() - startTime < AUTH_TIMEOUT_MS) {
                Thread.sleep(500)
            }

            if (token != null) {
                println("\n${"=".repeat(60)}")
                println("LOGIN SUCCESSFUL!")
                println("=".repeat(60))
                AuthTokens.wsToken = token
            } else {
                throw IllegalStateException("Authentication failed or timed out")
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
     * Authentication token storage for API requests.
     */
    protected object AuthTokens {
        var wsToken: String? = null

        fun isLoggedIn(): Boolean = wsToken != null
    }
}
