package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

/**
 * Stable `testTag` identifiers for the pure-Compose surfaces of the course detail subscreen,
 * referenced by both the components and their UI tests so a user-visible copy change never breaks
 * a test and a tag rename is a compile error rather than a silently missed node.
 *
 * Scope: only the Scheda (syllabus) tab is covered here — it is a stateless, pure-Compose page
 * driven entirely by a [it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails].
 * The collapsing hero, the horizontal pager, the continue-watching media card and the
 * file/video-viewer surfaces of the course detail are out of unit-test scope (custom measure-time
 * geometry and native/media rendering). [SYLLABUS_EMPTY] vs [SYLLABUS_CONTENT] are mutually
 * exclusive: the empty marker shows when the course publishes no syllabus.
 */
object CourseDetailTestTags {
    const val SYLLABUS_EMPTY = "courseDetail:syllabus:empty"
    const val SYLLABUS_CONTENT = "courseDetail:syllabus:content"
}
