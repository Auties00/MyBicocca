package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state

/**
 * Everything the plan compiler needs to start, captured when the in-sheet edit page is
 * pushed; it also keys the wizard's ViewModel so a new compilation target gets a fresh
 * instance.
 */
data class StudyPlanEditRequest(
    val studentId: Long,
    val choiceRegulationId: Long,
    /**
     * The schema to start from (the student's current one); 0 when the career has no
     * plan yet and the wizard's percorso step must pick one.
     */
    val schemaId: Long,
    /** 0 when compiling a first plan. */
    val planId: Long,
)
