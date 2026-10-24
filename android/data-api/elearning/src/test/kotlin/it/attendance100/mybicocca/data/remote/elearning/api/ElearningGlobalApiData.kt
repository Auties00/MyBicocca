package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.plugins.logging.*
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Shared state for the live suites. Logging in is the app's job, so the suites take an already
 * minted web service token from `ELEARNING_WS_TOKEN`.
 */
object ElearningGlobalApiData : BeforeAllCallback, AutoCloseable {
    val wsToken: String
        get() = System.getenv("ELEARNING_WS_TOKEN")
            ?: throw IllegalStateException("Missing token: set ELEARNING_WS_TOKEN environment variable")

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
            session = ElearningAuthSession(wsToken)
        }

        if (profile == null) {
            runBlocking {
                profile = fetchUserProfile(api!!, session!!)
            }
        }
    }

    private suspend fun fetchUserProfile(api: ElearningApi, session: ElearningAuthSession): ElearningUserProfile {
        val siteInfo = api.moodle.site.getSiteInfo(auth = MoodleTokenAuth(session.wsToken))
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

