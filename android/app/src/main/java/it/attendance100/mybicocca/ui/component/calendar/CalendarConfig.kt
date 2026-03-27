package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Centralized configuration for the calendar module.
 */
object CalendarConfig {

    object TimeSettings {
        const val START_HOUR = 7
        const val END_HOUR = 22
        const val TOTAL_HOURS = END_HOUR - START_HOUR
    }

    object Animation {
        const val PULSE_DURATION_MS = 2000
    }

    object Gesture {
        const val SWIPE_THRESHOLD = 0.25f
        const val VELOCITY_THRESHOLD = 400f
    }

    object Zoom {
        const val MAX_ZOOM = 1.5f
        const val DEFAULT_ZOOM = 1.0f
    }

    object Dimensions {
        // Timeline
        val HOUR_SLOT_HEIGHT = 80.dp
        val TIME_COLUMN_WIDTH = 50.dp
        val CURRENT_TIME_DOT_SIZE = 12.dp
        val CURRENT_TIME_LINE_HEIGHT = 2.dp

        // Cards
        val EVENT_CARD_CORNER_RADIUS = 16.dp
        val COLOR_BAR_WIDTH = 4.dp

        // Stack
        val STACK_OFFSET_X = 10.dp
        val STACK_OFFSET_Y = 6.dp
        const val MAX_VISIBLE_STACK_CARDS = 3

        // Event rendering
        const val MIN_EVENT_VISUAL_DURATION_MINUTES = 15

        // Day selector
        val DAY_SELECTOR_HEIGHT = 72.dp
        const val WEEK_DAYS = 7
    }

    object Pager {
        const val INITIAL_PAGE_OFFSET = Int.MAX_VALUE / 2
        const val PAGE_COUNT = Int.MAX_VALUE

        fun calculateWeekPage(referenceDate: LocalDate, targetDate: LocalDate): Int {
            val weeksBetween = ChronoUnit.WEEKS.between(
                referenceDate.with(DayOfWeek.MONDAY),
                targetDate.with(DayOfWeek.MONDAY)
            )
            return INITIAL_PAGE_OFFSET + weeksBetween.toInt()
        }

        fun calculateDayPage(referenceDate: LocalDate, targetDate: LocalDate): Int {
            val daysBetween = ChronoUnit.DAYS.between(referenceDate, targetDate)
            return INITIAL_PAGE_OFFSET + daysBetween.toInt()
        }
    }

    object Heights {
        val COMPACT_THRESHOLD = 80.dp
        val SHOW_PROFESSOR_THRESHOLD = 120.dp
    }
}
