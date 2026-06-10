package it.attendance100.mybicocca.domain.model.search

/**
 * Navigable in-app targets the unified search can land on. The UI layer maps these to
 * routes and tabs; domain stays ignorant of navigation types. The Settings-prefixed
 * entries target individual pages of the settings modal — each lands with the pager
 * already on that page.
 */
enum class SearchDestination {
    TabCalendar,
    TabElearning,
    TabMap,
    TabRegistry,
    Profile,
    Settings,
    Taxes,
    ExamResults,
    BookedExams,
    StudyPlan,
    Attendance,
    Questionnaires,
    Appointments,
    Enrollments,
    Titles,
    Certificates,
    Library,
    Refunds,
    Isee,

    SettingsAppearance,
    SettingsSecurity,
    SettingsLanguage,
    SettingsFileAssociations,
    SettingsLicenses,
    SettingsAppInfo,
}
