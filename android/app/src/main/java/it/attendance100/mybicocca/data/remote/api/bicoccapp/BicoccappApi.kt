package it.attendance100.mybicocca.data.remote.api.bicoccapp

/**
 * # BicoccApp API
 *
 * Unified access point for all BicoccApp REST API services. This interface
 * aggregates the various API modules into a single injectable dependency.
 *
 * ## Available APIs
 *
 * - [auth]: Authentication and OAuth2/OpenID Connect flows
 * - [user]: Student profile, career, exams, fees, and registrations
 * - [calendar]: Academic calendar and course scheduling
 * - [wizard]: Course selection wizard for calendar setup
 * - [messages]: Notifications, conversations, and appointments
 * - [campus]: Points of interest and faculty directory
 *
 * ## Usage
 *
 * Inject this interface via Hilt to access any BicoccApp API:
 *
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val bicoccappApi: BicoccappApi
 * ) : ViewModel() {
 *
 *     fun loadProfile() {
 *         viewModelScope.launch {
 *             val profile = bicoccappApi.user.getProfile()
 *             // ...
 *         }
 *     }
 *
 *     fun loadCalendar() {
 *         viewModelScope.launch {
 *             val calendar = bicoccappApi.calendar.getCalendar()
 *             // ...
 *         }
 *     }
 * }
 * ```
 *
 * ## Architecture
 *
 * Each sub-API is a Retrofit interface that handles a specific domain:
 *
 * ```
 * BicoccappApi
 * ├── auth       → Authentication flows
 * ├── user       → Student data
 * ├── calendar   → Schedule management
 * ├── wizard     → Course browsing
 * ├── messages   → Communications
 * └── campus     → Location services
 * ```
 *
 * @see BicoccappAuthApi
 * @see BicoccappUserApi
 * @see BicoccappCalendarApi
 * @see BicoccappWizardApi
 * @see BicoccappMessagesApi
 * @see BicoccappCampusApi
 */
interface BicoccappApi {

    /**
     * Authentication API for OAuth2/OpenID Connect flows.
     *
     * Handles user login and token management through the university's
     * identity provider.
     *
     * @see BicoccappAuthApi
     */
    val auth: BicoccappAuthApi

    /**
     * User API for student academic data.
     *
     * Provides access to profile, career progress, exams, fees,
     * and registration information.
     *
     * @see BicoccappUserApi
     */
    val user: BicoccappUserApi

    /**
     * Calendar API for schedule management.
     *
     * Handles academic calendar events, course schedules, and
     * personal calendar customization.
     *
     * @see BicoccappCalendarApi
     */
    val calendar: BicoccappCalendarApi

    /**
     * Wizard API for course selection.
     *
     * Provides a guided flow for browsing and selecting courses
     * to add to the calendar.
     *
     * @see BicoccappWizardApi
     */
    val wizard: BicoccappWizardApi

    /**
     * Messages API for communications.
     *
     * Handles notifications, teacher conversations, and
     * appointment requests.
     *
     * @see BicoccappMessagesApi
     */
    val messages: BicoccappMessagesApi

    /**
     * Campus API for location services.
     *
     * Provides points of interest, building information,
     * and faculty directory.
     *
     * @see BicoccappCampusApi
     */
    val campus: BicoccappCampusApi
}
