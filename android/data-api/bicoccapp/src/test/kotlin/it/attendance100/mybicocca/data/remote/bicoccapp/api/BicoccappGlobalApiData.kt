package it.attendance100.mybicocca.data.remote.bicoccapp.api

import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappLoginResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object BicoccappGlobalApiData : BeforeAllCallback, AutoCloseable {
    private const val TIMEOUT_MS = 30_000L

    val username: String
        get() = System.getenv("BICOCCA_USERNAME")
            ?: throw IllegalStateException("Missing username: set BICOCCA_USERNAME environment variable")

    val password: String
        get() = System.getenv("BICOCCA_PASSWORD")
            ?: throw IllegalStateException("Missing username: set BICOCCA_PASSWORD environment variable")

    val api: BicoccappApi by lazy {
        BicoccappApi {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
                requestTimeoutMillis = TIMEOUT_MS
            }

            defaultRequest {
                if(session != null) {
                    header("access-token", session?.accessToken)
                    header("client", session?.client)
                    header("uid", session?.uid)
                }
            }
        }
    }

    var session: BicoccappAuthSession? = null
    var profile: BicoccappUserProfile? = null

    override fun beforeAll(context: ExtensionContext) {
        if (session == null) {
            runBlocking {
                session = performLogin()
            }
        }

        if (profile == null) {
            runBlocking {
                profile = fetchUserProfile(api, session!!)
            }
        }
    }

    private suspend fun performLogin(): BicoccappAuthSession {
        return when (val result = api.auth.login(username, password)) {
            is BicoccappLoginResponse.Error -> fail(result.message)
            is BicoccappLoginResponse.Success -> result.toSession()
        }
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
        api.close()
    }
}

fun BicoccappLoginResponse.Success.toSession(): BicoccappAuthSession = BicoccappAuthSession(
    accessToken,
    client,
    uid,
    fiscalCode,
    favouriteCareer
)
