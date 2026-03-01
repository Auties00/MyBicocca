package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.plugins.logging.*
import it.attendance100.mybicocca.data.dto.elearning.ElearningLoginResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object ElearningGlobalApiData : BeforeAllCallback, AutoCloseable {
    val username: String
        get() = System.getenv("ELEARNING_USERNAME")
            ?: throw IllegalStateException("Missing username: set ELEARNING_USERNAME environment variable")

    val password: String
        get() = System.getenv("ELEARNING_PASSWORD")
            ?: throw IllegalStateException("Missing username: set ELEARNING_PASSWORD environment variable")

    var session: ElearningAuthSession? = null
    var profile: ElearningUserProfile? = null
    var api: ElearningApi? = null

    override fun beforeAll(context: ExtensionContext) {
        if (api == null) {
            api = ElearningApi {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }
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

    private suspend fun performLogin(api: ElearningApi): ElearningAuthSession {
        return when (val result = api.auth.login(username, password)) {
            is ElearningLoginResponse.Error -> fail(result.message)
            is ElearningLoginResponse.Success -> result.toSession()
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

fun ElearningLoginResponse.Success.toSession(): ElearningAuthSession = ElearningAuthSession(wsToken)

