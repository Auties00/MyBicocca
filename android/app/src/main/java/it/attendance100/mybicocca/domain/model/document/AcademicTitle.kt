package it.attendance100.mybicocca.domain.model.document

/**
 * A single qualification held by the student, unified across the three Esse3 title families
 * (high-school diploma, Italian university title, foreign title). The registry "Titoli"
 * sub-screen groups the list by [category] and renders the promoted fields; the detail page
 * renders the full [attributes] list, which the mapper fills with every populated field for
 * that family.
 *
 * @property id Stable, family-prefixed identifier — drives detail navigation and list keys.
 * @property category Title family the qualification belongs to.
 * @property status Whether the title is awarded or only hypothesised.
 * @property typeDescription Human-readable title type, e.g. "Maturità classica", "Laurea",
 *   "PHILOSOPHIAE DOCTOR (PhD)".
 * @property subject Specific subject/course where applicable, e.g. "INFORMATICA".
 * @property institution Awarding institution (school or university), free-text from Esse3.
 * @property country Country, only meaningful for foreign titles (and foreign-issued diplomas).
 * @property year Display year — the academic-year string ("2023/2024") when Esse3 provides
 *   one, else the calendar graduation year.
 * @property grade Final grade, formatted as "grade/base" when a base grade exists.
 * @property cumLaude Whether the title was awarded cum laude.
 * @property valueDeclarationFiled Whether a "dichiarazione di valore" was filed — only
 *   relevant for foreign titles.
 * @property attributes Full ordered list of populated fields, for the detail page.
 */
data class AcademicTitle(
    val id: String,
    val category: TitleCategory,
    val status: TitleStatus,
    val typeDescription: String?,
    val subject: String?,
    val institution: String?,
    val country: String?,
    val year: String?,
    val grade: String?,
    val cumLaude: Boolean,
    val valueDeclarationFiled: Boolean,
    val attributes: List<TitleAttribute>,
)

/** Esse3 title family a qualification belongs to. */
enum class TitleCategory {
    HighSchool,
    Italian,
    Foreign,
}

/** Award state of a title: "conseguito" maps to [Awarded], "in ipotesi" to [Hypothesised]. */
enum class TitleStatus {
    Awarded,
    Hypothesised,
    Unknown,
}

/**
 * One labeled fact about a title.
 *
 * @property field Semantic key; the UI maps it to an Italian label, an icon, and a section.
 * @property value Already-formatted, display-ready text.
 */
data class TitleAttribute(
    val field: TitleField,
    val value: String,
)

/**
 * Every semantic field a title can expose in the detail page. The list/header consume the
 * promoted fields on [AcademicTitle] instead (type, subject, status, grade, year), so those
 * don't appear here.
 */
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
