package it.attendance100.mybicocca.data.remote.esse3.scraper.api

import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.util.date.*
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

object Esse3GlobalApiData : BeforeAllCallback, AutoCloseable {
    private val AUTH_TIMEOUT = Duration.ofSeconds(300)

    var api: Esse3Api? = null

    override fun beforeAll(context: ExtensionContext) {
        if (api == null) {
            val cookies = performLogin()
            api = Esse3Api(cookies) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }
        }
    }

    private fun performLogin(): List<Cookie> {
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

        return try {
            driver.get(ESSE3_LOGIN_URL)

            val wait = WebDriverWait(driver, AUTH_TIMEOUT)
            wait.until { d ->
                d.currentUrl?.contains("auth/studente/HomePageStudente.do") == true
            }

            driver.manage().cookies.map {
                Cookie(
                    name = it.name,
                    value = it.value,
                    encoding = CookieEncoding.URI_ENCODING,
                    maxAge = null,
                    expires = it.expiry?.time?.let { timestamp -> GMTDate(timestamp) },
                    domain = it.domain,
                    path = it.path,
                    secure = it.isSecure,
                    httpOnly = it.isHttpOnly
                )
            }
        } finally {
            runCatching {
                driver.quit()
            }
        }
    }

    override fun close() {
        api?.close()
    }
}
