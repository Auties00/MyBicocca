package it.attendance100.mybicocca.domain.model.studyplan

// The student's current path-level configuration of a study plan: percorso (pds),
// orientamento (orient), profilo (prof) and impegno / part-time (apt). Esse3 keeps
// these on the plan header and offers alternatives through the choice regulation's
// plan schemas (each schema = one distinct pds/orient/prof/apt combination).
//
// All fields are nullable because Esse3 frequently returns only codes (or nothing) and
// omits the human descriptions; the UI falls back to the code, then to a generic label.
data class StudyPath(
    val percorso: StudyPathFacet?,
    val orientamento: StudyPathFacet?,
    val profilo: StudyPathFacet?,
    val partTime: StudyPathFacet?,
    // Selectable path alternatives drawn from the regulation's plan schemas, already
    // de-duplicated by their pds/orient/prof/apt tuple. Empty when there's nothing to
    // pick (single-percorso career) or the regulation couldn't be read.
    val options: List<StudyPathOption>,
    // True only when a compilation window is open AND more than one distinct option
    // exists. When false the section renders read-only ("percorso unico").
    val choiceAvailable: Boolean,
) {
    val hasAnyFacet: Boolean
        get() = percorso != null || orientamento != null || profilo != null || partTime != null
}

// A single path facet (percorso / orientamento / profilo / part-time). At least one of
// code or description is non-null when the facet is present.
data class StudyPathFacet(
    val code: String?,
    val description: String?,
) {
    // Human label: description first, then code. Never blank when the facet exists.
    val label: String
        get() = description?.takeIf { it.isNotBlank() }
            ?: code?.takeIf { it.isNotBlank() }
            ?: ""
}

// One selectable path alternative, backed by an Esse3 plan schema.
data class StudyPathOption(
    val schemaId: Long,
    val schemaCode: String?,
    val schemaDescription: String?,
    val percorso: StudyPathFacet?,
    val orientamento: StudyPathFacet?,
    val profilo: StudyPathFacet?,
    val partTime: StudyPathFacet?,
    // Whether this option matches the student's currently active schema.
    val isCurrent: Boolean,
) {
    val title: String
        get() = percorso?.label?.takeIf { it.isNotBlank() }
            ?: schemaDescription?.takeIf { it.isNotBlank() }
            ?: schemaCode?.takeIf { it.isNotBlank() }
            ?: "Percorso"
}
