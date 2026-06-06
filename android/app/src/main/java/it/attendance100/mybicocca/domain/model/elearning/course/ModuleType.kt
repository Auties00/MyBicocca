package it.attendance100.mybicocca.domain.model.elearning.course

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
    // Bicocca-specific / less common types that previously collapsed to Other and lost
    // their identity (rendered as a generic "Attività" row). See the e-learning content audit.
    Wooclap("wooclap"),
    // Moodle "Open Forum" plugin — forum-shaped but its instance ids live in mod_hsuforum,
    // NOT mod_forum, so it must never be routed to the in-app forum screens.
    HsuForum("hsuforum"),
    Lti("lti"),
    Reservation("reservation"),
    Webex("webexunimib"),
    ChoiceGroup("choicegroup"),
    Database("data"),
    Attendance("attendance"),
    Scheduler("scheduler"),
    // A placeholder module whose real content is a separate section linked via
    // customdata.sectionid; rendered inline rather than as an activity row.
    Subsection("subsection"),
    Other("other");

    companion object {
        fun fromRaw(raw: String?): ModuleType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
