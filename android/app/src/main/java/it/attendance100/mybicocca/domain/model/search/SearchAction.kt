package it.attendance100.mybicocca.domain.model.search

/**
 * Things a user can DO from the unified search, as opposed to places to land on. The UI
 * layer maps each action to a guided navigation plan (tab, then page, then sheet, animated
 * step by step); domain stays ignorant of navigation types.
 */
enum class SearchAction {
    BookExam,
    MarkPresence,
    ReserveLibrarySeat,
    BookAppointment,
    AddCourse,
    PayTaxes,
    CompileQuestionnaire,
    EditStudyPlan,
    HypotheticalAverage,
    ChangeTheme,
    ChangeLanguage,
    AddAccount,
}
