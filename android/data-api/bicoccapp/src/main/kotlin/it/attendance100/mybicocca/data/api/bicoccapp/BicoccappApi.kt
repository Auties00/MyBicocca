package it.attendance100.mybicocca.data.api.bicoccapp

/**
 * Unified access point for all BicoccApp REST API services.
 */
interface BicoccappApi {
    /** Authentication and OAuth2/OpenID Connect flows. */
    val auth: BicoccappAuthApi

    /** Student profile, career, exams, fees, and registrations. */
    val user: BicoccappUserApi

    /** Academic calendar and course scheduling. */
    val calendar: BicoccappCalendarApi

    /** Course selection wizard for calendar setup. */
    val wizard: BicoccappWizardApi

    /** Notifications and appointment requests. */
    val messages: BicoccappMessagesApi

    /** Points of interest and faculty directory. */
    val campus: BicoccappCampusApi
}
