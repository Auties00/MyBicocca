package it.attendance100.mybicocca.data.remote.api.elearning

/**
 * # Elearning API
 *
 * Unified access point for all Elearning (Moodle) REST API services. This interface
 * aggregates the various API modules into a single injectable dependency.
 *
 * ## Available APIs
 *
 * - [auth]: Authentication and token management (e.g., QR login, subscription keys)
 * - [user]: User profiles, preferences, files, and device management
 * - [course]: Course contents, categories, and enrollment information
 * - [calendar]: Calendar events, deadlines, and schedule views
 * - [message]: Instant messaging, conversations, and contact requests
 * - [assignment]: Assignment modules, submissions, and grading
 * - [forum]: Discussion forums, threads, and posts
 * - [quiz]: Quiz modules, attempts, and reviews
 * - [common]: Common tools, configuration, and plugin support
 *
 * ## Usage
 *
 * Inject this interface via Hilt to access any Elearning API:
 *
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val elearningApi: ElearningApi
 * ) : ViewModel() {
 *
 *     fun loadCourses() {
 *         viewModelScope.launch {
 *             val courses = elearningApi.course.getCourses(request)
 *             // ...
 *         }
 *     }
 *
 *     fun getMessages() {
 *         viewModelScope.launch {
 *             val messages = elearningApi.message.getConversations(request)
 *             // ...
 *         }
 *     }
 * }
 * ```
 *
 * ## Architecture
 *
 * Each sub-API is a Retrofit interface that handles a specific Moodle Web Service domain:
 *
 * ```
 * ElearningApi
 * ├── auth       → Authentication & Tokens
 * ├── user       → User Data & Preferences
 * ├── course     → Courses & Contents
 * ├── calendar   → Events & Schedule
 * ├── message    → Chat & Notifications
 * ├── assignment → Assignments (mod_assign)
 * ├── forum      → Forums (mod_forum)
 * ├── quiz       → Quizzes (mod_quiz)
 * └── common     → Tool Mobile & Config
 * ```
 *
 * @see ElearningAuthApi
 * @see ElearningUserApi
 * @see ElearningCourseApi
 * @see ElearningCalendarApi
 * @see ElearningMessageApi
 * @see ElearningAssignmentApi
 * @see ElearningForumApi
 * @see ElearningQuizApi
 * @see ElearningCommonApi
 */
interface ElearningApi {

    /**
     * Authentication API.
     *
     * Handles authentication, password resets, and token management for the Elearning platform.
     *
     * @see ElearningAuthApi
     */
    val auth: ElearningAuthApi

    /**
     * User API.
     *
     * Handles user profiles, preferences, devices, and private files.
     *
     * @see ElearningUserApi
     */
    val user: ElearningUserApi

    /**
     * Course API.
     *
     * Handles course listings, contents, categories, and user enrolments.
     *
     * @see ElearningCourseApi
     */
    val course: ElearningCourseApi

    /**
     * Calendar API.
     *
     * Handles calendar events, views (monthly, upcoming, day), and event creation.
     *
     * @see ElearningCalendarApi
     */
    val calendar: ElearningCalendarApi

    /**
     * Message API.
     *
     * Handles messaging, conversations, contacts, and notifications.
     *
     * @see ElearningMessageApi
     */
    val message: ElearningMessageApi

    /**
     * Assignment API.
     *
     * Handles assignment modules, submissions, status views, and grading.
     *
     * @see ElearningAssignmentApi
     */
    val assignment: ElearningAssignmentApi

    /**
     * Forum API.
     *
     * Handles forum discussions, posts, and viewing forum content.
     *
     * @see ElearningForumApi
     */
    val forum: ElearningForumApi

    /**
     * Quiz API.
     *
     * Handles quiz modules, attempts, reviews, and summaries.
     *
     * @see ElearningQuizApi
     */
    val quiz: ElearningQuizApi

    /**
     * Common API.
     *
     * Handles common tools, configuration, and mobile plugin checks (Tool Mobile).
     *
     * @see ElearningCommonApi
     */
    val common: ElearningCommonApi
}