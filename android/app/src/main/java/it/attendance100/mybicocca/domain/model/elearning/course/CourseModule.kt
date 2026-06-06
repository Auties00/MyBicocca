package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

data class CourseModule(
    val cmId: Int,
    val instanceId: Int?,
    val name: String,
    val type: ModuleType,
    // Server-provided plural label for the module type ("Wooclap", "Open Forums", "Webex").
    // Used as a graceful fallback meta label for types without a hand-written Italian string.
    val typeLabel: String?,
    val description: String?,
    val url: String?,
    val iconUrl: String?,
    // Teacher-visible (Moodle `visible != 0`). A module can be teacher-visible but gated for
    // the current user (see [accessible]) — that is rendered as a locked row, not dropped.
    val visible: Boolean,
    // Visible to the current user right now. False means access is restricted (a password,
    // a date, a grade condition); [availabilityInfo] explains why.
    val accessible: Boolean,
    val availabilityInfo: String?,
    // Whether Moodle shows this on the main course page. False = "stealth" module, reachable
    // only through a link elsewhere; we hide it from the flat list.
    val onCoursePage: Boolean,
    // Visual nesting level set by the teacher (0 = top level).
    val indent: Int,
    // Plain-texted `afterlink` HTML (e.g. "7 unread posts", upload date) shown as a meta suffix.
    val afterLink: String?,
    // For subsection placeholders: the id of the child section that holds the real content.
    val linkedSectionId: Int?,
    val contents: List<ModuleContent>,
    val completion: CompletionState?,
    val dates: List<ModuleDate>,
) {
    // The submission/closing deadline, classified by stable dataId rather than the localized
    // label. Falls back across the keys teachers actually use on this site.
    val dueAt: Instant?
        get() = dates.firstOrNull { it.dataId == "duedate" }?.instant
            ?: dates.firstOrNull { it.dataId == "timeclose" }?.instant
            ?: dates.firstOrNull { it.isDeadline }?.instant

    val opensAt: Instant?
        get() = dates.firstOrNull { it.isOpening }?.instant
}
