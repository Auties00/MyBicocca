package it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.state

import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationStage

// The ordered checklist the graduation hub renders. Each step is derived from the hub and
// carries its own completed / enabled / locked status so the UI stays declarative.
enum class GraduationStep(val title: String, val subtitle: String) {
    Application("Domanda di laurea", "Scegli l'appello e presenta la domanda"),
    Thesis("Tesi", "Titolo, abstract, tipologia e parole chiave"),
    Supervisors("Relatori", "Relatore e correlatori della tesi"),
    Attachments("Allegati tesi", "Elaborato definitivo e antiplagio"),
    Consultation("Consultazione", "Modalità di accesso pubblico o embargo"),
}

enum class StepStatus {
    // The step is finished — its data is present on the hub.
    Done,

    // The step is the next actionable one.
    Current,

    // Not yet reachable (a prior step must be completed first).
    Locked,
}

data class GraduationStepState(
    val step: GraduationStep,
    val status: StepStatus,
    val summary: String?,
)

// Build the checklist from the current hub. The flow is strictly sequential: domanda →
// tesi → relatori → allegati → consultazione. A step unlocks only once the previous one is
// done, which mirrors the irreversible secretariat process.
fun GraduationHub.steps(): List<GraduationStepState> {
    val hasApplication = application != null && stage != GraduationStage.NotOpen &&
        stage != GraduationStage.Cancelled
    val hasThesis = thesis != null && thesis.id.value != 0L
    val hasSupervisors = thesis?.supervisors?.isNotEmpty() == true
    val hasAttachments = thesis?.attachments?.isNotEmpty() == true
    val hasConsultation = !thesis?.discussionModeCode.isNullOrBlank()

    fun status(done: Boolean, prevDone: Boolean): StepStatus = when {
        done -> StepStatus.Done
        prevDone -> StepStatus.Current
        else -> StepStatus.Locked
    }

    return listOf(
        GraduationStepState(
            step = GraduationStep.Application,
            status = if (hasApplication) StepStatus.Done else StepStatus.Current,
            summary = application?.let { app ->
                app.callDescription ?: app.sessionDescription ?: app.stateLabel
            },
        ),
        GraduationStepState(
            step = GraduationStep.Thesis,
            status = status(hasThesis, hasApplication),
            summary = thesis?.titleItalian,
        ),
        GraduationStepState(
            step = GraduationStep.Supervisors,
            status = status(hasSupervisors, hasThesis),
            summary = thesis?.supervisors
                ?.mapNotNull { it.displayName ?: it.relationTypeLabel }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", "),
        ),
        GraduationStepState(
            step = GraduationStep.Attachments,
            status = status(hasAttachments, hasSupervisors),
            summary = thesis?.attachments?.firstOrNull()?.let { it.fileName ?: it.title ?: it.stateLabel },
        ),
        GraduationStepState(
            step = GraduationStep.Consultation,
            status = status(hasConsultation, hasAttachments),
            summary = thesis?.discussionModeCode,
        ),
    )
}
