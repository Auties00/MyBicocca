package it.attendance100.mybicocca.domain.model.transcript

/**
 * Outcome of the propedeuticità (prerequisite) check for a not-yet-passed libretto
 * activity, shown on the profile screen's course detail.
 *
 * Live-validated semantics of the Esse3 `/libretti/{matId}/righe/{adsceId}/prop`
 * endpoint: it returns `{"esito": 1}` when prerequisites ARE satisfied, and responds
 * HTTP 422 for an activity that is already passed (prerequisites cannot be computed on
 * it). The DTO field doc confirms: "il valore 1 indica controllo superato". So
 * `esito == 1` means OK; any other value means the prerequisites are NOT satisfied.
 *
 * [Unknown] is the sentinel for a check that was never run (passed activities, where the
 * endpoint 422s by design) or that failed for an uninterpretable reason — in those cases
 * no misleading warning is surfaced.
 */
enum class PrerequisiteStatus {
    Satisfied,
    NotSatisfied,
    Unknown,
}
