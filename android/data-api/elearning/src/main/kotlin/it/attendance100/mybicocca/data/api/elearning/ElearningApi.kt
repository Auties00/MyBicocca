package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Main entry point for the Elearning (Moodle) API.
 *
 * This class serves as a facade that provides access to all specialized API classes
 * for interacting with a Moodle learning management system.
 *
 */
class ElearningApi(enableLogging: Boolean = false) : AutoCloseable {
    /**
     * JSON serializer configured for Moodle API responses.
     */
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Shared HTTP client for all API requests.
     */
    private val client = HttpClient {
        if(enableLogging) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * API for site-level operations and authentication.
     */
    val site: ElearningSiteApi = ElearningSiteApi(client, json)

    /**
     * API for user-related operations.
     */
    val users: ElearningUserApi = ElearningUserApi(client, json)

    /**
     * API for course-related operations.
     */
    val courses: ElearningCourseApi = ElearningCourseApi(client, json)

    /**
     * API for quiz-related operations.
     */
    val quizzes: ElearningQuizApi = ElearningQuizApi(client, json)

    /**
     * API for assignment-related operations.
     */
    val assignments: ElearningAssignApi = ElearningAssignApi(client, json)

    /**
     * API for forum-related operations.
     */
    val forums: ElearningForumApi = ElearningForumApi(client, json)

    /**
     * API for calendar-related operations.
     */
    val calendar: ElearningCalendarApi = ElearningCalendarApi(client, json)

    /**
     * API for badge-related operations.
     */
    val badges: ElearningBadgeApi = ElearningBadgeApi(client, json)

    /**
     * API for completion-related operations.
     */
    val completion: ElearningCompletionApi = ElearningCompletionApi(client, json)

    /**
     * API for grade-related operations.
     */
    val grades: ElearningGradeApi = ElearningGradeApi(client, json)

    /**
     * API for messaging operations.
     */
    val messages: ElearningMessageApi = ElearningMessageApi(client, json)

    /**
     * Closes the underlying HTTP client and releases resources.
     *
     * After calling this method, the API instance should not be used.
     * All API calls will fail after closing.
     *
     * This method is idempotent - calling it multiple times has no effect.
     */
    override fun close() {
        client.close()
    }
}
