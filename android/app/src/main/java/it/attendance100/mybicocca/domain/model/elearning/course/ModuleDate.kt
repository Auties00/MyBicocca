package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

// A dated event attached to a module. `dataId` is the stable machine key Moodle ships
// (e.g. "duedate", "timeclose", "timeopen", "allowsubmissionsfromdate") and is the only
// language-independent way to classify the date — the human `label` is server-localized
// Italian on this site ("Data limite:", "Aperto:", "Chiuso:").
data class ModuleDate(
    val label: String?,
    val instant: Instant,
    val dataId: String?,
) {
    val isDeadline: Boolean
        get() = dataId == "duedate" || dataId == "timeclose" || dataId == "close"

    val isOpening: Boolean
        get() = dataId == "allowsubmissionsfromdate" || dataId == "timeopen" || dataId == "open"
}
