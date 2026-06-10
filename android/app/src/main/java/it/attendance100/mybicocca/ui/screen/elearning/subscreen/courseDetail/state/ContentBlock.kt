package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

import it.attendance100.mybicocca.domain.model.elearning.course.CourseModule

/** One render unit inside an expanded section. */
sealed interface ContentBlock {
    /**
     * A teacher note (a content label). [html] is the raw Moodle HTML so links and emphasis
     * survive — it is rendered as rich HTML, not flattened to plain text.
     */
    data class Note(val html: String) : ContentBlock

    /**
     * A run of modules under an optional sub-header. [title] is null for the leading run
     * before any label, and for runs that resume after an interleaved note (the header is not
     * repeated).
     */
    data class Group(val title: String?, val modules: List<CourseModule>) : ContentBlock

    /**
     * An inlined mod_subsection: the placeholder module's child section, rendered as a
     * nested expander with its own content blocks.
     */
    data class Subsection(
        val title: String,
        val summaryHtml: String?,
        val blocks: List<ContentBlock>,
    ) : ContentBlock
}
