package it.attendance100.mybicocca.domain.model.search

// Navigable in-app targets the unified search can land on. The UI layer maps these to
// routes/tabs; domain stays ignorant of navigation types.
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
    Internships,
    Questionnaires,
    DegreeAward,
    Reservations,
    Messaging,
}
