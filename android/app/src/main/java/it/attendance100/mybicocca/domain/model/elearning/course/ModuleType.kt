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
    Other("other");

    companion object {
        fun fromRaw(raw: String?): ModuleType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: Other
    }
}
