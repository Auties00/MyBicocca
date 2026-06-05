package it.attendance100.mybicocca.domain.model.document

// A single qualification held by the student, unified across the three Esse3 title families
// (high-school diploma, Italian university title, foreign title). The UI groups by [category].
data class AcademicTitle(
    val category: TitleCategory,
    // Human-readable title type, e.g. "Maturità classica", "Laurea", "PHILOSOPHIAE DOCTOR (PhD)".
    val typeDescription: String?,
    // Specific subject/course where applicable, e.g. "INFORMATICA".
    val subject: String?,
    // Awarding institution (school or university), already free-text from Esse3.
    val institution: String?,
    val status: TitleStatus,
    val year: Int?,
    val grade: String?,
    val cumLaude: Boolean,
    // Country, only meaningful for foreign titles.
    val country: String?,
    // Whether a "dichiarazione di valore" was filed — only relevant for foreign titles.
    val valueDeclarationFiled: Boolean,
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
