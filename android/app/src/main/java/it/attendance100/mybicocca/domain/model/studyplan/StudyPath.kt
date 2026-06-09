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
    // The choice regulation the options hang off. Also resolved when the career has no
    // plan yet (looked up by course + cohort) so a first compilation can be offered.
    val choiceRegulationId: Long?,
    // The option matching the student's active schema; null when no plan exists yet or
    // the current schema isn't among the offered options.
    val currentSchemaId: Long?,
    // Selectable path alternatives drawn from the regulation's plan schemas, already
    // de-duplicated by their pds/orient/prof/apt tuple. Empty when there's nothing to
    // pick (single-percorso career) or the regulation couldn't be read.
    val options: List<StudyPathOption>,
    // True while a compilation window for the plan's choice regulation is open,
    // regardless of how many options exist. Drives the plan-edit action.
    val editingOpen: Boolean,
    // True only when a compilation window is open AND more than one distinct option
    // exists. When false the section renders read-only ("percorso unico").
    val choiceAvailable: Boolean,
)

// How Esse3 approves a plan compiled on a given schema (tipoApprovazione).
enum class PlanApprovalType {
    // 0 — approved as soon as it is submitted.
    Automatic,
    // 1 — waits for a manual evaluation by the department.
    Manual,
    // 2 — approved automatically only when conforming to the choice regulation.
    AutomaticIfCompliant,
    Unknown,
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
    val approval: PlanApprovalType = PlanApprovalType.Unknown,
    // Esse3 may gate a schema behind a server-side SQL condition that can't be evaluated
    // client-side; when present, this is its human description shown as a caveat.
    val conditionNote: String? = null,
    // Teaching languages of the percorso, from the public structure catalog.
    val languages: List<String> = emptyList(),
) {
    val title: String
        get() = percorso?.label?.takeIf { it.isNotBlank() }
            ?: schemaDescription?.takeIf { it.isNotBlank() }
            ?: schemaCode?.takeIf { it.isNotBlank() }
            ?: "Percorso"
}
