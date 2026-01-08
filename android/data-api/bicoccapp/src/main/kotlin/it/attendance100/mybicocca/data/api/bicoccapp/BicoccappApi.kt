package it.attendance100.mybicocca.data.api.bicoccapp

/**
 * Unified access point for all BicoccApp REST API services.
 */
data class BicoccappApi(
    /** Authentication and OAuth2/OpenID Connect flows. */
    val auth: BicoccappAuthApi,

    /** Student profile. */
    val profile: BicoccappProfileApi,

    /** Student career and registrations. */
    val career: BicoccappCareerApi,

    /** Student exams and exam sessions. */
    val exams: BicoccappExamsApi,

    /** Student tuition fees. */
    val taxes: BicoccappTaxesApi,

    /** Academic calendar and course scheduling. */
    val calendar: BicoccappCalendarApi,

    /** Course selection wizard for calendar setup. */
    val wizard: BicoccappWizardApi,

    /** Points of interest and faculty directory. */
    val campus: BicoccappCampusApi
)