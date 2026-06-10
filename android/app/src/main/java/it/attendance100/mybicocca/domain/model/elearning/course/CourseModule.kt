package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

/**
 * One activity or resource inside a course section, from the Moodle course-contents
 * web service. Rendered as a row of the course detail content list.
 *
 * @property cmId The course-module id, unique across the whole site and the key for
 * completion, deadlines, and deep links.
 * @property instanceId The plugin-scoped instance id (e.g. the assignment id for an
 * assign module), used to open the matching detail screen.
 * @property name Module title, with any {mlang} markup resolved.
 * @property type The plugin kind the module is an instance of.
 * @property typeLabel Server-provided plural label for the module type ("Wooclap",
 * "Open Forums", "Webex"); a graceful fallback meta label for types without a
 * hand-written Italian string.
 * @property description Module description HTML, when authored.
 * @property url The module's view page on the Moodle site, when it has one.
 * @property iconUrl The activity icon Moodle serves for the module.
 * @property visible Teacher-visibility (Moodle `visible != 0`). A module can be
 * teacher-visible yet gated for the current user (see [accessible]) — that renders as
 * a locked row, not dropped.
 * @property accessible Whether the current user can access the module right now. False
 * means access is restricted (a password, a date, a grade condition);
 * [availabilityInfo] explains why.
 * @property availabilityInfo Human-readable restriction summary for inaccessible
 * modules.
 * @property onCoursePage Whether Moodle shows the module on the main course page.
 * False marks a "stealth" module reachable only through a link elsewhere; those are
 * hidden from the flat list.
 * @property indent Visual nesting level set by the teacher (0 = top level).
 * @property afterLink Plain-texted `afterlink` HTML (e.g. "7 unread posts", upload
 * date) shown as a meta suffix.
 * @property linkedSectionId For subsection placeholders: the id of the child section
 * that holds the real content.
 * @property contents Files and links bundled with the module (a resource's file, a
 * folder's entries).
 * @property completion Completion status, when tracked and resolved.
 * @property dates Dated events attached to the module (opens, due, closes).
 */
data class CourseModule(
    val cmId: Int,
    val instanceId: Int?,
    val name: String,
    val type: ModuleType,
    val typeLabel: String?,
    val description: String?,
    val url: String?,
    val iconUrl: String?,
    val visible: Boolean,
    val accessible: Boolean,
    val availabilityInfo: String?,
    val onCoursePage: Boolean,
    val indent: Int,
    val afterLink: String?,
    val linkedSectionId: Int?,
    val contents: List<ModuleContent>,
    val completion: CompletionState?,
    val dates: List<ModuleDate>,
) {
    /**
     * The submission/closing deadline, classified by stable dataId rather than the
     * localized label, falling back across the keys teachers actually use on this site.
     */
    val dueAt: Instant?
        get() = dates.firstOrNull { it.dataId == "duedate" }?.instant
            ?: dates.firstOrNull { it.dataId == "timeclose" }?.instant
            ?: dates.firstOrNull { it.isDeadline }?.instant

    /** When the activity opens for participation, when such a date is declared. */
    val opensAt: Instant?
        get() = dates.firstOrNull { it.isOpening }?.instant
}
