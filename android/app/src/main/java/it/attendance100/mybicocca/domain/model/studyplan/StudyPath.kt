package it.attendance100.mybicocca.domain.model.studyplan

/**
 * The student's current path-level configuration of a study plan: percorso (pds),
 * orientamento (orient), profilo (prof) and impegno / part-time (apt). Esse3 keeps these
 * on the plan header and offers alternatives through the choice regulation's plan
 * schemas (each schema = one distinct pds/orient/prof/apt combination). Rendered by the
 * study-plan sub-screen in the registry tab, where changing percorso happens by
 * compiling the plan against a different schema.
 *
 * All facets are nullable because Esse3 frequently returns only codes (or nothing) and
 * omits the human descriptions; the UI falls back to the code, then to a generic label.
 *
 * @property percorso The curriculum facet (piano di studi / percorso).
 * @property orientamento The orientation facet.
 * @property profilo The profile facet.
 * @property partTime The impegno / part-time facet.
 * @property choiceRegulationId The choice regulation the options hang off. Also resolved
 *   when the career has no plan yet (looked up by course + cohort) so a first
 *   compilation can be offered.
 * @property currentSchemaId The option matching the student's active schema; null when
 *   no plan exists yet or the current schema is not among the offered options.
 * @property options Selectable path alternatives drawn from the regulation's plan
 *   schemas, de-duplicated by their pds/orient/prof/apt tuple. Empty when there is
 *   nothing to pick (single-percorso career) or the regulation could not be read.
 * @property editingOpen True while a compilation window for the plan's choice regulation
 *   is open, regardless of how many options exist. Drives the plan-edit action.
 * @property choiceAvailable True only when a compilation window is open AND more than
 *   one distinct option exists. When false the section renders read-only ("percorso
 *   unico").
 */
data class StudyPath(
    val percorso: StudyPathFacet?,
    val orientamento: StudyPathFacet?,
    val profilo: StudyPathFacet?,
    val partTime: StudyPathFacet?,
    val choiceRegulationId: Long?,
    val currentSchemaId: Long?,
    val options: List<StudyPathOption>,
    val editingOpen: Boolean,
    val choiceAvailable: Boolean,
)

/**
 * How Esse3 approves a plan compiled on a given schema (`tipoApprovazione`):
 * 0 = [Automatic] (approved as soon as it is submitted), 1 = [Manual] (waits for a
 * manual evaluation by the department), 2 = [AutomaticIfCompliant] (approved
 * automatically only when conforming to the choice regulation). [Unknown] covers any
 * unrecognised value.
 */
enum class PlanApprovalType {
    Automatic,
    Manual,
    AutomaticIfCompliant,
    Unknown,
}

/**
 * A single path facet (percorso / orientamento / profilo / part-time). At least one of
 * code or description is non-null when the facet is present.
 *
 * @property code Esse3 code of the facet value.
 * @property description Human description, when Esse3 publishes one.
 */
data class StudyPathFacet(
    val code: String?,
    val description: String?,
) {
    /** Human label: description first, then code. Never blank when the facet exists. */
    val label: String
        get() = description?.takeIf { it.isNotBlank() }
            ?: code?.takeIf { it.isNotBlank() }
            ?: ""
}

/**
 * One selectable path alternative, backed by an Esse3 plan schema. Choosing it in the
 * plan-edit wizard compiles the plan against that schema, which is how Esse3 records a
 * percorso change.
 *
 * @property schemaId Esse3 id of the plan schema backing this option.
 * @property schemaCode Esse3 code of the schema.
 * @property schemaDescription Human description of the schema.
 * @property percorso The curriculum facet the schema selects.
 * @property orientamento The orientation facet the schema selects.
 * @property profilo The profile facet the schema selects.
 * @property partTime The impegno / part-time facet the schema selects.
 * @property isCurrent Whether this option matches the student's currently active schema.
 * @property approval How a plan compiled on this schema gets approved.
 * @property conditionNote Esse3 may gate a schema behind a server-side SQL condition
 *   that cannot be evaluated client-side; when present, this is its human description
 *   shown as a caveat.
 * @property languages Teaching languages of the percorso, from the public structure
 *   catalog.
 */
data class StudyPathOption(
    val schemaId: Long,
    val schemaCode: String?,
    val schemaDescription: String?,
    val percorso: StudyPathFacet?,
    val orientamento: StudyPathFacet?,
    val profilo: StudyPathFacet?,
    val partTime: StudyPathFacet?,
    val isCurrent: Boolean,
    val approval: PlanApprovalType = PlanApprovalType.Unknown,
    val conditionNote: String? = null,
    val languages: List<String> = emptyList(),
) {
    /**
     * Display title: percorso label, then schema description, then schema code; null when
     * none is available, so the UI can supply a localized fallback.
     */
    val title: String?
        get() = percorso?.label?.takeIf { it.isNotBlank() }
            ?: schemaDescription?.takeIf { it.isNotBlank() }
            ?: schemaCode?.takeIf { it.isNotBlank() }
}
