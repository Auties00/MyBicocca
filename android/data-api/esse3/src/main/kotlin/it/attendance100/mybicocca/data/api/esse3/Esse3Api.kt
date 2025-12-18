package it.attendance100.mybicocca.data.api.esse3

/**
 * # Esse3 API
 *
 * Unified access point for all Esse3 REST API services. This interface
 * aggregates the various API modules into a single injectable dependency,
 * mirroring the structure of `BicoccappApi`.
 *
 * ## Available APIs
 *
 * - [auth]: Authentication and Login
 * - [user]: User profile, Anagrafica, Address Book, Identity Docs
 * - [career]: Career, Enrollment, Study Plans, Fees, Certificates
 * - [exams]: Exam sessions, Bookings, Calendar
 * - [internship]: Internships, Companies, Opportunities
 * - [admission]: Admissions, Competitions, Right to Study
 * - [questionnaire]: Questionnaires (Didactic/Generic)
 * - [common]: System utils, Checklists, File Uploads
 *
 * ## Usage
 *
 * Inject this interface via Hilt:
 *
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val esse3Api: Esse3Api
 * ) : ViewModel() {
 *     // ...
 * }
 * ```
 *
 * @see Esse3AuthApi
 * @see Esse3UserApi
 * @see Esse3CareerApi
 * @see Esse3ExamsApi
 * @see Esse3InternshipApi
 * @see Esse3AdmissionApi
 * @see Esse3QuestionnaireApi
 * @see Esse3CommonApi
 */
interface Esse3Api {

    /**
     * Authentication API.
     *
     * Handles login and session management.
     */
    val auth: Esse3AuthApi

    /**
     * User API.
     *
     * Manage personal data, contacts, and identity documents.
     */
    val user: Esse3UserApi

    /**
     * Career API.
     *
     * Manage academic career, enrollment, and fees.
     */
    val career: Esse3CareerApi

    /**
     * Exams API.
     *
     * Manage exam sessions and bookings.
     */
    val exams: Esse3ExamsApi

    /**
     * Internship API.
     *
     * Search and apply for internships.
     */
    val internship: Esse3InternshipApi

    /**
     * Admission API.
     *
     * Manage admissions and right to study.
     */
    val admission: Esse3AdmissionApi

    /**
     * Questionnaire API.
     *
     * Fill and submit questionnaires.
     */
    val questionnaire: Esse3QuestionnaireApi

    /**
     * Common API.
     *
     * System utilities and common processes.
     */
    val common: Esse3CommonApi
}