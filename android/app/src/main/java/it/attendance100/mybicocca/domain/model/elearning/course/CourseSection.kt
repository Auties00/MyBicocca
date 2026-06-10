package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * One titled block of a Moodle course page grouping a run of modules, from the
 * course-contents web service. Rendered as a card of the course detail content list.
 *
 * @property id The section id, unique across the site.
 * @property sectionNumber Position of the section on the course page (0 = the general
 * top section).
 * @property name Section title, with any {mlang} markup resolved.
 * @property summary Section description HTML, when authored.
 * @property visible Whether the teacher is showing the section to students.
 * @property component Owning module plugin when the section is plugin-managed.
 * "mod_subsection" sections are the child content of a subsection placeholder module
 * and must not render as top-level cards.
 * @property itemId For mod_subsection child sections: the owning subsection module's
 * instance id.
 * @property modules The section's activities and resources in page order.
 */
data class CourseSection(
    val id: Int,
    val sectionNumber: Int,
    val name: String,
    val summary: String?,
    val visible: Boolean,
    val component: String?,
    val itemId: Int?,
    val modules: List<CourseModule>,
) {
    /** Whether this section is the inlined content of a subsection placeholder module. */
    val isSubsectionChild: Boolean
        get() = component == "mod_subsection"
}
