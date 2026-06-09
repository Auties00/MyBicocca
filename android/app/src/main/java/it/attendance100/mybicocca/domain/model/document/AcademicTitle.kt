package it.attendance100.mybicocca.domain.model.document

// A single qualification held by the student, unified across the three Esse3 title families
// (high-school diploma, Italian university title, foreign title). The list groups by [category]
// and renders the promoted fields; the detail page renders the full [attributes] list, which
// the mapper fills with every populated field for that family.
data class AcademicTitle(
    // Stable, family-prefixed identifier — drives detail navigation and list keys.
    val id: String,
    val category: TitleCategory,
    val status: TitleStatus,
    // Human-readable title type, e.g. "Maturità classica", "Laurea", "PHILOSOPHIAE DOCTOR (PhD)".
    val typeDescription: String?,
    // Specific subject/course where applicable, e.g. "INFORMATICA".
    val subject: String?,
    // Awarding institution (school or university), already free-text from Esse3.
    val institution: String?,
    // Country, only meaningful for foreign titles (and foreign-issued diplomas).
    val country: String?,
    // Display year — the academic-year string ("2023/2024") when Esse3 provides one, else the
    // calendar graduation year.
    val year: String?,
    val grade: String?,
    val cumLaude: Boolean,
    // Whether a "dichiarazione di valore" was filed — only relevant for foreign titles.
    val valueDeclarationFiled: Boolean,
    // Full ordered list of populated fields, for the detail page.
    val attributes: List<TitleAttribute>,
)

enum class TitleCategory {
    HighSchool,
    Italian,
    Foreign,
}

enum class TitleStatus {
    Awarded,
    Hypothesised,
    Unknown,
}

// One labeled fact about a title. [field] is the semantic key (the UI maps it to an Italian
// label, an icon, and a section); [value] is the already-formatted, display-ready text.
data class TitleAttribute(
    val field: TitleField,
    val value: String,
)

// Every semantic field a title can expose in the detail page. The list/header consume the
// promoted fields on [AcademicTitle] instead (type, subject, status, grade, year), so those
// don't appear here.
enum class TitleField {
    AcademicYear,
    GraduationYear,
    AwardDate,
    Session,
    AchievementYears,
    DurationYears,
    GradeAverage,
    Credits,
    Institution,
    City,
    Province,
    Country,
    SameUniversity,
    InstitutionCode,
    Language,
    ThesisTitle,
    StudyPath,
    EquivalentPath,
    DepositType,
    ValueDeclaration,
    Evaluated,
    Recognized,
}
