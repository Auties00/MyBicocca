package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.state

/**
 * A resolved web link surfaced to the link sheet (open / copy). For mod/url modules the
 * url is the fully-resolved external target from core_course_get_contents, not the
 * internal mod/url/view.php page.
 */
data class LinkTarget(
    val title: String,
    val url: String,
)
