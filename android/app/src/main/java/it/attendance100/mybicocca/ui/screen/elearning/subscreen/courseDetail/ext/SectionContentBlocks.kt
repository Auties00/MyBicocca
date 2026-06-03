package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContentBlock

// Real courses use labels two ways (surveyed 457 labels across 37 courses): short ones
// ("Prima settimana", "VI Appello (4 feb 2026)") head the modules that follow, long ones
// are inline teacher notes. Splitting sections on them is what keeps the 50+ module
// sections that actually exist on elearning.unimib.it scannable.
private const val HEADER_MAX_CHARS = 60

fun CourseSection.contentBlocks(): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    var title: String? = null
    val run = mutableListOf<CourseModule>()

    fun flush() {
        if (run.isNotEmpty()) {
            blocks += ContentBlock.Group(title, run.toList())
            run.clear()
        }
    }

    for (module in modules) {
        if (!module.visible) continue
        if (module.type == ModuleType.Label) {
            val name = module.name.htmlToPlainText().stripCopySuffix()
            if (name.length in 1..HEADER_MAX_CHARS) {
                flush()
                title = name
            } else {
                flush()
                // After a note the run resumes without repeating the header.
                title = null
                val note = (module.description?.takeIf { it.isNotBlank() } ?: module.name)
                    .htmlToPlainText()
                    .stripCopySuffix()
                if (note.isNotBlank()) blocks += ContentBlock.Note(note)
            }
            continue
        }
        run += module
    }
    flush()
    return blocks
}

fun CourseSection.summaryPlainText(): String? =
    summary?.htmlToPlainText()?.takeIf { it.isNotBlank() }

// Teachers duplicate activities and leave the default "(copia)"/"(copy)" suffixes behind;
// they carry no meaning for students.
fun String.stripCopySuffix(): String {
    var out = trim()
    val suffix = Regex("""\s*\((copia|copy)\)$""", RegexOption.IGNORE_CASE)
    while (true) {
        val next = out.replace(suffix, "")
        if (next == out) return out
        out = next.trim()
    }
}

fun String.htmlToPlainText(): String =
    android.text.Html.fromHtml(this, android.text.Html.FROM_HTML_MODE_COMPACT)
        .toString()
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
