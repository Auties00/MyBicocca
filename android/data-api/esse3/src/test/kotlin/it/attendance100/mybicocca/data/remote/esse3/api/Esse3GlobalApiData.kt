package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.*

object Esse3GlobalApiData : BeforeAllCallback, AutoCloseable {
    private const val TIMEOUT_MS = 30_000L

    val username: String
        get() = System.getenv("ESSE3_USERNAME")
            ?: throw IllegalStateException("Missing username: set ESSE3_USERNAME environment variable")

    val password: String
        get() = System.getenv("ESSE3_PASSWORD")
            ?: throw IllegalStateException("Missing password: set ESSE3_PASSWORD environment variable")

    val api: Esse3Api by lazy {
        Esse3Api {
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
                val credentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
                header("Authorization", "Basic $credentials")
            }
        }
    }

    var session: Esse3AuthSession? = null
    var studentProfile: Esse3StudentProfile? = null

    override fun beforeAll(context: ExtensionContext) {
        if (session == null) {
            runBlocking {
                session = performLogin()
            }
        }

        if (studentProfile == null) {
            runBlocking {
                studentProfile = fetchStudentProfile()
            }
        }
    }

    private suspend fun performLogin(): Esse3AuthSession {
        val result = api.auth.login()
        return Esse3AuthSession(
            authToken = result.authToken,
            internalAuthToken = result.internalAuthToken,
            jwt = result.jwt,
            userId = result.user.userId,
            fiscalCode = result.user.fiscalCode
        )
    }

    // FIXME: Could fail if the user is not a STUDENT
    private suspend fun fetchStudentProfile(): Esse3StudentProfile {
        val careers = api.careers.getCareers()
        val career = careers.firstOrNull()
            ?: throw IllegalStateException("No careers found for user")
        return Esse3StudentProfile(
            personId = career.personId ?: throw IllegalStateException("No personId in career"),
            studentId = career.studentId ?: throw IllegalStateException("No studentId in career"),
            enrollmentId = career.enrollmentId ?: throw IllegalStateException("No enrollmentId in career"),
            matricola = career.enrollmentId.toString(),
            matId = career.matId ?: throw IllegalStateException("No matId in career"),
            degreeCourseId = career.courseOfStudyId ?: throw IllegalStateException("No degreeCourseId in career"),
            userId = session?.userId ?: throw IllegalStateException("No userId in session")
        )
    }

    override fun close() {
        api.close()
    }
}
