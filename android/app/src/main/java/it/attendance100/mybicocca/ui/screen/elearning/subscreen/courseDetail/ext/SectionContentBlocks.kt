package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.ext

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSection
import it.attendance100.mybicocca.domain.model.elearning.course.ModuleType
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state.ContentBlock

/** Maximum plain-text length for a bodyless label to count as a sub-header. */
private const val HEADER_MAX_CHARS = 60

/**
 * Derives the section's render units: module runs grouped under label sub-headers, teacher
 * notes, and inlined subsections, skipping modules the student can't see. Stealth modules
 * (visibleoncoursepage=0) are reachable only through a link elsewhere; Moodle hides them from
 * the course page and so does this.
 *
 * Real courses use mod_label two ways (surveyed 458 labels across 37 courses): short ones with
 * no body ("Prima settimana", "VI Appello") head the modules that follow; ones carrying a real
 * description are content (lesson syllabi, links, exam rules). The split keys on the body, not
 * the name length: a label with a meaningful description is a [ContentBlock.Note] (rendered as
 * rich HTML, links intact), a short bodyless label is a header, and a long bodyless name is
 * itself the note (it may carry a link). Keying on name length alone would hide multi-paragraph
 * syllabi behind a short header.
 */
fun CourseSection.contentBlocks(
    resolveSubsection: (CourseModule) -> CourseSection? = { null },
): List<ContentBlock> {
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
        if (!module.onCoursePage) continue

        if (module.type == ModuleType.Subsection) {
            val child = resolveSubsection(module)
            flush()
            title = null
            val childTitle = module.name.htmlToPlainText().stripCopySuffix()
                .ifBlank { child?.name?.htmlToPlainText()?.stripCopySuffix().orEmpty() }
            if (child != null) {
                blocks += ContentBlock.Subsection(
                    title = childTitle,
                    summaryHtml = child.summary?.takeIf { it.containsRenderableHtml() },
                    blocks = child.contentBlocks(resolveSubsection),
                )
            } else if (childTitle.isNotBlank()) {
                blocks += ContentBlock.Subsection(title = childTitle, summaryHtml = null, blocks = emptyList())
            }
            continue
        }

        if (module.type == ModuleType.Label) {
            val descHtml = module.description?.takeIf { it.containsRenderableHtml() }
            val namePlain = module.name.htmlToPlainText().stripCopySuffix()
            when {
                descHtml != null -> {
                    flush()
                    title = null
                    blocks += ContentBlock.Note(descHtml)
                }
                namePlain.length in 1..HEADER_MAX_CHARS -> {
                    flush()
                    title = namePlain
                }
                module.name.containsRenderableHtml() || namePlain.isNotBlank() -> {
                    flush()
                    title = null
                    blocks += ContentBlock.Note(module.name)
                }
            }
            continue
        }
        run += module
    }
    flush()
    return blocks
}

/** The section summary flattened to one plain-text line, or null when it has no visible text. */
fun CourseSection.summaryPlainText(): String? =
    summary?.htmlToPlainText()?.takeIf { it.isNotBlank() }

/**
 * Drops trailing "(copia)"/"(copy)" runs: teachers duplicate activities and leave the default
 * suffixes behind, and they carry no meaning for students.
 */
fun String.stripCopySuffix(): String {
    var out = trim()
    val suffix = Regex("""\s*\((copia|copy)\)$""", RegexOption.IGNORE_CASE)
    while (true) {
        val next = out.replace(suffix, "")
        if (next == out) return out
        out = next.trim()
    }
}

/**
 * Whether an HTML fragment is worth rendering as rich content: it has visible text, or it
 * carries a link / image / table that flattening to plain text would destroy.
 */
fun String.containsRenderableHtml(): Boolean {
    if (isBlank()) return false
    if (htmlToPlainText().isNotBlank()) return true
    val lower = lowercase()
    return "<a " in lower || "href=" in lower || "<img" in lower || "<table" in lower
}

/** Flattens HTML to plain text collapsed onto a single space-joined line. */
fun String.htmlToPlainText(): String =
    android.text.Html.fromHtml(this, android.text.Html.FROM_HTML_MODE_COMPACT)
        .toString()
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
