package it.attendance100.mybicocca.data.api.esse3

import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

/**
 * Base test class for Esse3 API integration tests.
 *
 * This class handles the Shibboleth SSO authentication flow using Selenium.
 * It opens a Chrome browser window for manual login and captures the session cookies.
 *
 * All test classes should extend this base class to get automatic authentication.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class Esse3TestBase {
    companion object {
        private const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes

        @JvmStatic
        protected val api: Esse3Api by lazy {
            println("Starting Esse3 authentication flow...")

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

            try {
                // Navigate to login page
                driver.get(ESSE3_LOGIN_URL)

                println("\n${"=".repeat(60)}")
                println("BROWSER OPENED - Please complete the Shibboleth login process")
                println("=".repeat(60))
                println("\nWaiting for authentication...")

                val wait = WebDriverWait(driver, Duration.ofMillis(AUTH_TIMEOUT_MS))

                // Wait until we're on the authenticated home page
                // This happens after successful Shibboleth SSO
                wait.until { d ->
                    val currentUrl = d.currentUrl ?: ""
                    val pageSource = d.pageSource ?: ""

                    // Check if we're on the authenticated area
                    (currentUrl.contains("/auth/") || currentUrl.contains("Home")) &&
                            !pageSource.contains("Logon.do") &&
                            (pageSource.contains("studente", ignoreCase = true) ||
                                    pageSource.contains("Esci", ignoreCase = true) ||
                                    pageSource.contains("Logout", ignoreCase = true))
                }

                println("\nAuthentication detected! Extracting cookies...")

                val cookies = driver.manage().cookies.map { cookie ->
                    Cookie(
                        name=cookie.name,
                        value=cookie.value,
                        encoding = CookieEncoding.URI_ENCODING,
                        maxAge = null,
                        expires = cookie.expiry?.time?.let {
                                timestamp -> GMTDate(timestamp)
                        },
                        domain = cookie.domain,
                        path = cookie.path,
                        secure = cookie.isSecure,
                        httpOnly = cookie.isHttpOnly
                    )
                }

                println("\n${"=".repeat(60)}")
                println("LOGIN SUCCESSFUL!")

                Esse3Api(cookies)
            } finally {
                println("\nClosing browser...")
                try {
                    driver.quit()
                } catch (_: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }
}
