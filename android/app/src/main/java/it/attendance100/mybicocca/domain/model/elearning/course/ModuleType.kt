package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * The Moodle activity/resource plugin a course module is an instance of, including the
 * Bicocca-specific plugins observed on the site. Distinct values keep each module kind's
 * identity in the course content list (icon, label, tap behaviour); unrecognized plugins
 * collapse to [Other] and render as a generic activity row.
 *
 * @property raw The Moodle plugin name as reported in the course-contents web service
 * `modname` field.
 */
enum class ModuleType(val raw: String) {
    Resource("resource"),
    Url("url"),
    Page("page"),
    Folder("folder"),
    Label("label"),
    Quiz("quiz"),
    Assign("assign"),
    Forum("forum"),
    Choice("choice"),
    Workshop("workshop"),
    Lesson("lesson"),
    Scorm("scorm"),
    Glossary("glossary"),
    Wiki("wiki"),
    Book("book"),
    Feedback("feedback"),
    H5p("h5pactivity"),
    Kalvidres("kalvidres"),
    Wooclap("wooclap"),

    /**
     * The Moodle "Open Forum" plugin. Forum-shaped, but its instance ids live in
     * mod_hsuforum rather than mod_forum, so it must never be routed to the in-app
     * forum screens.
     */
    HsuForum("hsuforum"),
    Lti("lti"),
    Reservation("reservation"),
    Webex("webexunimib"),
    ChoiceGroup("choicegroup"),
    Database("data"),
    Attendance("attendance"),
    Scheduler("scheduler"),

    /**
     * A placeholder module whose real content is a separate section linked via
     * customdata.sectionid; rendered inline rather than as an activity row.
     */
    Subsection("subsection"),
    Other("other");

    companion object {
        fun fromRaw(raw: String?): ModuleType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
