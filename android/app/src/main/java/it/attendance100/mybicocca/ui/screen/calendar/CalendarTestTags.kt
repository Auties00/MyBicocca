package it.attendance100.mybicocca.ui.screen.calendar

import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode

/**
 * Stable `testTag` identifiers for [CalendarScreen], its segmented mode control and the
 * event detail sheet, referenced by both the production composables and their UI tests so a
 * user-visible copy change never breaks a test and a tag rename is a compile error rather
 * than a silently missed node. Composables apply them with `Modifier.testTag`; tests anchor
 * on the constants instead of on Italian text.
 */
object CalendarTestTags {
    const val ROOT = "calendar:root"
    const val STATE_CONTENT = "calendar:state:content"
    const val STATE_ERROR = "calendar:state:error"

    /** Tag of the Day/Week/Month segment for [mode], used to drive the view switch. */
    fun segment(mode: CalendarViewMode): String = "calendar:segment:${mode.name}"

    const val EVENT_TITLE = "calendar:event:title"
    const val EVENT_ACTIVITY_LABEL = "calendar:event:activityLabel"
    const val EVENT_CONTENT = "calendar:event:content"
    const val EVENT_PRIMARY_ACTION = "calendar:event:primaryAction"
    const val EVENT_SECONDARY_ACTION = "calendar:event:secondaryAction"
}
