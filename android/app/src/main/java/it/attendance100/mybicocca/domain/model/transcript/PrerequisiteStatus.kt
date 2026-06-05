package it.attendance100.mybicocca.domain.model.transcript

// Outcome of the propedeuticità (prerequisite) check for a not-yet-passed activity.
//
// Live semantics (validated against the real Esse3 /libretti/{matId}/righe/{adsceId}/prop
// endpoint, see scripts/esse3_activity_detail_probe.py): the endpoint returns
// {"esito": 1} when prerequisites ARE satisfied, and responds HTTP 422 for an
// activity that is already passed (prereqs can't be computed on it). The DTO field
// doc confirms: "il valore 1 indica controllo superato". So esito == 1 means OK;
// anything else (or a non-422 failure) means the prerequisites are NOT satisfied.
//
// Unknown is used when the check wasn't run at all (passed activities, where /prop
// 422s by design) or when it failed for a reason we can't interpret — in those
// cases we don't surface a misleading warning.
enum class PrerequisiteStatus {
    Satisfied,
    NotSatisfied,
    Unknown,
}
