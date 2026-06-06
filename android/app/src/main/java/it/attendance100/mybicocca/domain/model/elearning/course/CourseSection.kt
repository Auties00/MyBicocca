package it.attendance100.mybicocca.domain.model.elearning.course

data class CourseSection(
    val id: Int,
    val sectionNumber: Int,
    val name: String,
    val summary: String?,
    val visible: Boolean,
    // Set when this section is owned by a module plugin. "mod_subsection" sections are the
    // child content of a subsection placeholder module and must not render as top-level cards.
    val component: String?,
    // For mod_subsection child sections: the owning subsection module's instance id.
    val itemId: Int?,
    val modules: List<CourseModule>,
) {
    val isSubsectionChild: Boolean
        get() = component == "mod_subsection"
}
