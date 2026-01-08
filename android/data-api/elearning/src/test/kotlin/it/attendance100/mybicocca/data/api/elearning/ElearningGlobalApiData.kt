package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.RequestWillBeSent
import java.util.Optional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ElearningGlobalApiData : BeforeAllCallback, AutoCloseable {
    private const val AUTH_TIMEOUT_SEC = 300L

    var session: ElearningAuthSession? = null
    var profile: ElearningUserProfile? = null
    var api: ElearningApi? = null

    override fun beforeAll(context: ExtensionContext) {
        if (api == null) {
            api = ElearningApi(
                enableLogging = true
            )
        }

        if (session == null) {
            runBlocking {
                session = performLogin(api!!)
            }
        }

        if (profile == null) {
            runBlocking {
                profile = fetchUserProfile(api!!, session!!)
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun performLogin(api: ElearningApi): ElearningAuthSession {
        val loginUrl = api.site.getAuthUrl()

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
        val tokenRef = AtomicReference<String>()
        val latch = CountDownLatch(1)

        return try {
            driver.devTools.apply {
                createSession()
                send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))
                addListener(Network.requestWillBeSent()) { response: RequestWillBeSent ->
                    val url = response.request.url
                    if (url.startsWith("moodlemobile://")) {
                        runCatching {
                            val base64Tokens = url.substringAfter("token=")
                            val decoded = Base64.decode(base64Tokens).decodeToString()
                            val token = decoded.split(":::")[1]
                            tokenRef.set(token)
                            latch.countDown()
                        }
                    }
                }
            }

            driver.get(loginUrl)

            val success = latch.await(AUTH_TIMEOUT_SEC, TimeUnit.SECONDS)
            if (!success) throw IllegalStateException("Authentication timed out")

            val token = tokenRef.get() ?: throw IllegalStateException("Failed to capture authentication token")
            ElearningAuthSession(wsToken = token)
        } finally {
            runCatching {
                driver.devTools.close()
                driver.quit()
            }
        }
    }

    private suspend fun fetchUserProfile(api: ElearningApi, session: ElearningAuthSession): ElearningUserProfile {
        val siteInfo = api.site.getSiteInfo(session.wsToken)
        return ElearningUserProfile(
            userId = siteInfo.userId,
            username = siteInfo.username,
            fullName = siteInfo.fullName
        )
    }

    override fun close() {
        api?.close()
    }
}
