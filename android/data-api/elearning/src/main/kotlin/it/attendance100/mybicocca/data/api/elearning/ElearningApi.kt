package it.attendance100.mybicocca.data.api.elearning

/**
 * # Elearning API
 *
 * Unified access point for all Moodle e-learning REST API services. This interface
 * aggregates the various API modules into a single injectable dependency.
 *
 * ## Available APIs
 *
 * ### Authentication
 * - [authEmail]: Email-based authentication flows
 *
 * ### Blocks
 * - [blockAccessreview]: Accessibility review block
 * - [blockIomadCompanyAdmin]: IOMAD company administration block
 * - [blockRecentlyaccesseditems]: Recently accessed items block
 * - [blockStarredcourses]: Starred courses block
 *
 * ### Enrollment
 * - [enrolGuest]: Guest enrollment
 * - [enrolLicense]: License-based enrollment
 * - [enrolManual]: Manual enrollment
 * - [enrolMeta]: Meta enrollment
 * - [enrolSelf]: Self enrollment
 *
 * ### Grade Reports
 * - [gradereportGrader]: Grader report
 * - [gradereportOverview]: Overview report
 * - [gradereportSingleview]: Single view report
 * - [gradereportUser]: User grade report
 *
 * ### Grading Forms
 * - [gradingformGuide]: Marking guide grading form
 * - [gradingformRubric]: Rubric grading form
 *
 * ### Local Plugins
 * - [localIomadLearningpath]: IOMAD learning path
 *
 * ### Media
 * - [mediaVideojs]: VideoJS media player
 *
 * ### Messages
 * - [messageAirnotifier]: Airnotifier push notifications
 * - [messagePopup]: Popup notifications
 *
 * ### Modules
 * - [modAssign]: Assignment module
 * - [modBigbluebuttonbn]: BigBlueButton video conferencing
 * - [modBook]: Book module
 * - [modChat]: Chat module
 * - [modChoice]: Choice/poll module
 * - [modData]: Database module
 * - [modFeedback]: Feedback module
 * - [modFolder]: Folder module
 * - [modForum]: Forum module
 * - [modGlossary]: Glossary module
 * - [modH5pactivity]: H5P activity module
 * - [modImscp]: IMS content package module
 * - [modIomadcertificate]: IOMAD certificate module
 * - [modLabel]: Label module
 * - [modLesson]: Lesson module
 * - [modLti]: LTI module
 * - [modPage]: Page module
 * - [modQuiz]: Quiz module
 * - [modResource]: Resource/file module
 * - [modScorm]: SCORM module
 * - [modSurvey]: Survey module
 * - [modUrl]: URL module
 * - [modWiki]: Wiki module
 * - [modWorkshop]: Workshop module
 *
 * ### Core Moodle
 * - [moodle]: Core Moodle API functions
 *
 * ### Payment Gateways
 * - [paygwPaypal]: PayPal payment gateway
 *
 * ### Question Bank
 * - [qbankColumnsortorder]: Column sort order
 * - [qbankEditquestion]: Question editing
 * - [qbankTagquestion]: Question tagging
 * - [qbankViewquestiontext]: Question text viewing
 *
 * ### Quiz Access
 * - [quizaccessSeb]: Safe Exam Browser access rules
 *
 * ### Reports
 * - [reportCompetency]: Competency report
 * - [reportInsights]: Analytics insights report
 *
 * ### TinyMCE Editor
 * - [tinyAutosave]: Autosave functionality
 * - [tinyEquation]: Equation editor
 * - [tinyPremium]: Premium features
 *
 * ### Tools
 * - [toolAnalytics]: Analytics tool
 * - [toolBehat]: Behat testing tool
 * - [toolDataprivacy]: Data privacy tool
 * - [toolIomadpolicy]: IOMAD policy tool
 * - [toolLp]: Learning plans tool
 * - [toolMobile]: Mobile app tool
 * - [toolMoodlenet]: MoodleNet integration
 * - [toolPolicy]: Policy tool
 * - [toolTemplatelibrary]: Template library
 * - [toolUsertours]: User tours tool
 * - [toolXmldb]: XML database tool
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
 *     fun loadQuizzes() {
 *         viewModelScope.launch {
 *             val quizzes = elearningApi.modQuiz.modQuizGetQuizzesByCourses(request)
 *             // ...
 *         }
 *     }
 *
 *     fun loadForums() {
 *         viewModelScope.launch {
 *             val forums = elearningApi.modForum.modForumGetForumsByCourses(request)
 *             // ...
 *         }
 *     }
 * }
 * ```
 *
 * ## Architecture
 *
 * Each sub-API is a Retrofit interface that handles a specific Moodle module:
 *
 * ```
 * ElearningApi
 * ├── auth          → Authentication flows
 * ├── block         → Dashboard blocks
 * ├── enrol         → Enrollment methods
 * ├── gradereport   → Grade reports
 * ├── gradingform   → Grading forms
 * ├── message       → Messaging & notifications
 * ├── mod           → Activity modules (quiz, forum, assign, etc.)
 * ├── moodle        → Core Moodle functions
 * ├── qbank         → Question bank
 * ├── report        → Reports
 * ├── tiny          → TinyMCE editor plugins
 * └── tool          → Admin tools
 * ```
 *
 * @see ElearningMoodleApi
 * @see ElearningModQuizApi
 * @see ElearningModForumApi
 * @see ElearningModAssignApi
 */
interface ElearningApi {
    /**
     * Email authentication API.
     *
     * Handles email-based user registration and authentication flows.
     *
     * @see ElearningAuthEmailApi
     */
    val authEmail: ElearningAuthEmailApi

    /**
     * Accessibility review block API.
     *
     * Provides accessibility checking and review functionality.
     *
     * @see ElearningBlockAccessreviewApi
     */
    val blockAccessreview: ElearningBlockAccessreviewApi

    /**
     * IOMAD company administration block API.
     *
     * Handles company-level administration for IOMAD multi-tenancy.
     *
     * @see ElearningBlockIomadCompanyAdminApi
     */
    val blockIomadCompanyAdmin: ElearningBlockIomadCompanyAdminApi

    /**
     * Recently accessed items block API.
     *
     * Provides access to recently viewed courses and activities.
     *
     * @see ElearningBlockRecentlyaccesseditemsApi
     */
    val blockRecentlyaccesseditems: ElearningBlockRecentlyaccesseditemsApi

    /**
     * Starred courses block API.
     *
     * Manages user's favorite/starred courses.
     *
     * @see ElearningBlockStarredcoursesApi
     */
    val blockStarredcourses: ElearningBlockStarredcoursesApi

    /**
     * Guest enrollment API.
     *
     * Handles guest access to courses without authentication.
     *
     * @see ElearningEnrolGuestApi
     */
    val enrolGuest: ElearningEnrolGuestApi

    /**
     * License enrollment API.
     *
     * Manages license-based course enrollment.
     *
     * @see ElearningEnrolLicenseApi
     */
    val enrolLicense: ElearningEnrolLicenseApi

    /**
     * Manual enrollment API.
     *
     * Handles manual user enrollment by administrators.
     *
     * @see ElearningEnrolManualApi
     */
    val enrolManual: ElearningEnrolManualApi

    /**
     * Meta enrollment API.
     *
     * Manages course meta linking for automatic enrollment.
     *
     * @see ElearningEnrolMetaApi
     */
    val enrolMeta: ElearningEnrolMetaApi

    /**
     * Self enrollment API.
     *
     * Handles self-enrollment with optional enrollment keys.
     *
     * @see ElearningEnrolSelfApi
     */
    val enrolSelf: ElearningEnrolSelfApi

    /**
     * Grader report API.
     *
     * Provides the grader report view for instructors.
     *
     * @see ElearningGradereportGraderApi
     */
    val gradereportGrader: ElearningGradereportGraderApi

    /**
     * Overview report API.
     *
     * Provides grade overview across courses.
     *
     * @see ElearningGradereportOverviewApi
     */
    val gradereportOverview: ElearningGradereportOverviewApi

    /**
     * Single view report API.
     *
     * Provides single user or item grade view.
     *
     * @see ElearningGradereportSingleviewApi
     */
    val gradereportSingleview: ElearningGradereportSingleviewApi

    /**
     * User grade report API.
     *
     * Provides individual user grade reports.
     *
     * @see ElearningGradereportUserApi
     */
    val gradereportUser: ElearningGradereportUserApi

    /**
     * Marking guide API.
     *
     * Handles marking guide grading form operations.
     *
     * @see ElearningGradingformGuideApi
     */
    val gradingformGuide: ElearningGradingformGuideApi

    /**
     * Rubric API.
     *
     * Handles rubric grading form operations.
     *
     * @see ElearningGradingformRubricApi
     */
    val gradingformRubric: ElearningGradingformRubricApi

    /**
     * IOMAD learning path API.
     *
     * Manages learning paths for IOMAD.
     *
     * @see ElearningLocalIomadLearningpathApi
     */
    val localIomadLearningpath: ElearningLocalIomadLearningpathApi

    /**
     * VideoJS media player API.
     *
     * Handles VideoJS media player functionality.
     *
     * @see ElearningMediaVideojsApi
     */
    val mediaVideojs: ElearningMediaVideojsApi

    /**
     * Airnotifier API.
     *
     * Handles mobile push notifications via Airnotifier.
     *
     * @see ElearningMessageAirnotifierApi
     */
    val messageAirnotifier: ElearningMessageAirnotifierApi

    /**
     * Popup notifications API.
     *
     * Handles in-browser popup notifications.
     *
     * @see ElearningMessagePopupApi
     */
    val messagePopup: ElearningMessagePopupApi

    /**
     * Assignment module API.
     *
     * Handles assignment submissions, grading, and feedback.
     *
     * @see ElearningModAssignApi
     */
    val modAssign: ElearningModAssignApi

    /**
     * BigBlueButton API.
     *
     * Handles BigBlueButton video conferencing integration.
     *
     * @see ElearningModBigbluebuttonbnApi
     */
    val modBigbluebuttonbn: ElearningModBigbluebuttonbnApi

    /**
     * Book module API.
     *
     * Handles multi-page book resources.
     *
     * @see ElearningModBookApi
     */
    val modBook: ElearningModBookApi

    /**
     * Chat module API.
     *
     * Handles real-time chat functionality.
     *
     * @see ElearningModChatApi
     */
    val modChat: ElearningModChatApi

    /**
     * Choice module API.
     *
     * Handles single-question polls and choices.
     *
     * @see ElearningModChoiceApi
     */
    val modChoice: ElearningModChoiceApi

    /**
     * Database module API.
     *
     * Handles database activity for collaborative data collection.
     *
     * @see ElearningModDataApi
     */
    val modData: ElearningModDataApi

    /**
     * Feedback module API.
     *
     * Handles feedback and survey collection.
     *
     * @see ElearningModFeedbackApi
     */
    val modFeedback: ElearningModFeedbackApi

    /**
     * Folder module API.
     *
     * Handles folder resources for organizing files.
     *
     * @see ElearningModFolderApi
     */
    val modFolder: ElearningModFolderApi

    /**
     * Forum module API.
     *
     * Handles forum discussions and posts.
     *
     * @see ElearningModForumApi
     */
    val modForum: ElearningModForumApi

    /**
     * Glossary module API.
     *
     * Handles glossary entries and definitions.
     *
     * @see ElearningModGlossaryApi
     */
    val modGlossary: ElearningModGlossaryApi

    /**
     * H5P activity API.
     *
     * Handles H5P interactive content.
     *
     * @see ElearningModH5pactivityApi
     */
    val modH5pactivity: ElearningModH5pactivityApi

    /**
     * IMS content package API.
     *
     * Handles IMS content package resources.
     *
     * @see ElearningModImscpApi
     */
    val modImscp: ElearningModImscpApi

    /**
     * IOMAD certificate API.
     *
     * Handles certificate generation for IOMAD.
     *
     * @see ElearningModIomadcertificateApi
     */
    val modIomadcertificate: ElearningModIomadcertificateApi

    /**
     * Label module API.
     *
     * Handles text and media labels on course page.
     *
     * @see ElearningModLabelApi
     */
    val modLabel: ElearningModLabelApi

    /**
     * Lesson module API.
     *
     * Handles lesson activities with branching paths.
     *
     * @see ElearningModLessonApi
     */
    val modLesson: ElearningModLessonApi

    /**
     * LTI module API.
     *
     * Handles Learning Tools Interoperability integration.
     *
     * @see ElearningModLtiApi
     */
    val modLti: ElearningModLtiApi

    /**
     * Page module API.
     *
     * Handles single-page text/HTML resources.
     *
     * @see ElearningModPageApi
     */
    val modPage: ElearningModPageApi

    /**
     * Quiz module API.
     *
     * Handles quiz attempts, questions, and grading.
     *
     * @see ElearningModQuizApi
     */
    val modQuiz: ElearningModQuizApi

    /**
     * Resource module API.
     *
     * Handles file resources.
     *
     * @see ElearningModResourceApi
     */
    val modResource: ElearningModResourceApi

    /**
     * SCORM module API.
     *
     * Handles SCORM package playback and tracking.
     *
     * @see ElearningModScormApi
     */
    val modScorm: ElearningModScormApi

    /**
     * Survey module API.
     *
     * Handles standard course evaluation surveys.
     *
     * @see ElearningModSurveyApi
     */
    val modSurvey: ElearningModSurveyApi

    /**
     * URL module API.
     *
     * Handles external URL resources.
     *
     * @see ElearningModUrlApi
     */
    val modUrl: ElearningModUrlApi

    /**
     * Wiki module API.
     *
     * Handles collaborative wiki pages.
     *
     * @see ElearningModWikiApi
     */
    val modWiki: ElearningModWikiApi

    /**
     * Workshop module API.
     *
     * Handles peer assessment workshops.
     *
     * @see ElearningModWorkshopApi
     */
    val modWorkshop: ElearningModWorkshopApi

    /**
     * Core Moodle API.
     *
     * Provides core Moodle web services including user, course,
     * calendar, and content management functions.
     *
     * @see ElearningMoodleApi
     */
    val moodle: ElearningMoodleApi

    /**
     * PayPal payment gateway API.
     *
     * Handles PayPal payment processing.
     *
     * @see ElearningPaygwPaypalApi
     */
    val paygwPaypal: ElearningPaygwPaypalApi

    /**
     * Column sort order API.
     *
     * Manages question bank column ordering.
     *
     * @see ElearningQbankColumnsortorderApi
     */
    val qbankColumnsortorder: ElearningQbankColumnsortorderApi

    /**
     * Edit question API.
     *
     * Handles question editing operations.
     *
     * @see ElearningQbankEditquestionApi
     */
    val qbankEditquestion: ElearningQbankEditquestionApi

    /**
     * Tag question API.
     *
     * Handles question tagging operations.
     *
     * @see ElearningQbankTagquestionApi
     */
    val qbankTagquestion: ElearningQbankTagquestionApi

    /**
     * View question text API.
     *
     * Handles question text preview operations.
     *
     * @see ElearningQbankViewquestiontextApi
     */
    val qbankViewquestiontext: ElearningQbankViewquestiontextApi

    /**
     * Safe Exam Browser API.
     *
     * Handles Safe Exam Browser access rules for quizzes.
     *
     * @see ElearningQuizaccessSebApi
     */
    val quizaccessSeb: ElearningQuizaccessSebApi

    /**
     * Competency report API.
     *
     * Provides competency-based progress reports.
     *
     * @see ElearningReportCompetencyApi
     */
    val reportCompetency: ElearningReportCompetencyApi

    /**
     * Insights report API.
     *
     * Provides analytics insights and predictions.
     *
     * @see ElearningReportInsightsApi
     */
    val reportInsights: ElearningReportInsightsApi

    /**
     * TinyMCE autosave API.
     *
     * Handles editor content autosaving.
     *
     * @see ElearningTinyAutosaveApi
     */
    val tinyAutosave: ElearningTinyAutosaveApi

    /**
     * TinyMCE equation editor API.
     *
     * Handles mathematical equation editing.
     *
     * @see ElearningTinyEquationApi
     */
    val tinyEquation: ElearningTinyEquationApi

    /**
     * TinyMCE premium API.
     *
     * Handles premium TinyMCE features.
     *
     * @see ElearningTinyPremiumApi
     */
    val tinyPremium: ElearningTinyPremiumApi

    /**
     * Analytics tool API.
     *
     * Provides learning analytics functionality.
     *
     * @see ElearningToolAnalyticsApi
     */
    val toolAnalytics: ElearningToolAnalyticsApi

    /**
     * Behat testing tool API.
     *
     * Handles Behat acceptance testing operations.
     *
     * @see ElearningToolBehatApi
     */
    val toolBehat: ElearningToolBehatApi

    /**
     * Data privacy tool API.
     *
     * Handles GDPR data privacy requests.
     *
     * @see ElearningToolDataprivacyApi
     */
    val toolDataprivacy: ElearningToolDataprivacyApi

    /**
     * IOMAD policy tool API.
     *
     * Handles IOMAD policy management.
     *
     * @see ElearningToolIomadpolicyApi
     */
    val toolIomadpolicy: ElearningToolIomadpolicyApi

    /**
     * Learning plans tool API.
     *
     * Handles competency-based learning plans.
     *
     * @see ElearningToolLpApi
     */
    val toolLp: ElearningToolLpApi

    /**
     * Mobile app tool API.
     *
     * Provides mobile app specific functionality.
     *
     * @see ElearningToolMobileApi
     */
    val toolMobile: ElearningToolMobileApi

    /**
     * MoodleNet tool API.
     *
     * Handles MoodleNet resource sharing.
     *
     * @see ElearningToolMoodlenetApi
     */
    val toolMoodlenet: ElearningToolMoodlenetApi

    /**
     * Policy tool API.
     *
     * Handles site policy management.
     *
     * @see ElearningToolPolicyApi
     */
    val toolPolicy: ElearningToolPolicyApi

    /**
     * Template library tool API.
     *
     * Handles Mustache template operations.
     *
     * @see ElearningToolTemplatelibraryApi
     */
    val toolTemplatelibrary: ElearningToolTemplatelibraryApi

    /**
     * User tours tool API.
     *
     * Handles guided user tours.
     *
     * @see ElearningToolUsertoursApi
     */
    val toolUsertours: ElearningToolUsertoursApi

    /**
     * XML database tool API.
     *
     * Handles database schema operations.
     *
     * @see ElearningToolXmldbApi
     */
    val toolXmldb: ElearningToolXmldbApi
}
