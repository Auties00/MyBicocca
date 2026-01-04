package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.Cookie


/**
 * Main entry point for the Esse3 API.
 *
 * @see Esse3AuthApi for authentication operations
 */
class Esse3Api(sessionCookies: List<Cookie>) : AutoCloseable {
    companion object {
        /**
         * HTTP timeout in milliseconds.
         */
        private const val TIMEOUT = 30_000L
    }

    /**
     * Shared HTTP client for all API requests.
     *
     * Configured with:
     * - Cookie storage for session management
     * - Extended timeouts for slow university servers
     */
    private val client = HttpClient {
        install(HttpCookies) {
            default {
                sessionCookies.forEach { cookie ->
                    cookie.domain?.let { domain ->
                        addCookie(domain, cookie)
                    }
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT
            connectTimeoutMillis = TIMEOUT
            socketTimeoutMillis = TIMEOUT
        }
        engine {
            followRedirects = true
        }
        followRedirects = true
    }

    /**
     * API for authentication operations.
     *
     * Handles Shibboleth SSO login flow and session management.
     */
    val auth: Esse3AuthApi = Esse3AuthApi(client)

    /**
     * API for student profile operations.
     *
     * Provides access to:
     * - Student homepage with summary data
     * - Profile photo
     * - Personal information
     */
    val profile: Esse3ProfileApi = Esse3ProfileApi(client)

    /**
     * API for academic career operations.
     *
     * Provides access to:
     * - Libretto (academic record)
     * - Study plan
     * - Career acts
     * - Course evaluations
     */
    val career: Esse3CareerApi = Esse3CareerApi(client)

    /**
     * API for exam operations.
     *
     * Provides access to:
     * - Available exam sessions
     * - Exam reservations
     * - Exam results
     * - Reservation printing
     */
    val exams: Esse3ExamsApi = Esse3ExamsApi(client)

    /**
     * API for tax and payment operations.
     *
     * Provides access to:
     * - Tax bill list
     * - Bill details
     * - Payment receipts
     * - MAV/PagoPA documents
     */
    val taxes: Esse3TaxesApi = Esse3TaxesApi(client)

    /**
     * API for internship and stage operations.
     *
     * Provides access to:
     * - Search internship opportunities
     * - View company information
     * - Manage applications
     * - View active internships
     * - Save/unsave opportunities
     */
    val internships: Esse3InternshipApi = Esse3InternshipApi(client)

    /**
     * API for address book and contact operations.
     *
     * Provides access to:
     * - Residence/domicile addresses
     * - Contact information
     * - Privacy consents
     * - Disability declarations
     */
    val address: Esse3ProfileApi = Esse3ProfileApi(client)

    /**
     * API for questionnaire operations.
     *
     * Provides access to:
     * - Pending questionnaires
     * - Course evaluations
     * - Fill and submit questionnaires
     */
    val questionnaires: Esse3QuestionnaireApi = Esse3QuestionnaireApi(client)

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
