package it.attendance100.mybicocca.data.api.elearning

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.RequestWillBeSent
import java.util.Optional
import kotlin.io.encoding.Base64

private const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes

// The token we get looks like this:
// someBase64Payload
// Then we decode it and it becomes:
// someHexString:::anotherHexString:::someOtherBase64String
// anotherHexString is the wstoken we need, don't know about what the other things are
suspend fun main() {
    val api = ElearningApi()
    val loginUrl = api.getAuthUrl()

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
                println("[DETECTED] Captured tokens to parse: $base64Tokens")
                token = Base64.decode(base64Tokens).decodeToString().split(":::")[1]
                println("[DETECTED] Parsed token: $token")
                foundTokens = true
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
            println("\nCaptured tokens:")
            println("  token: ${token}...")
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

    api.close()
}