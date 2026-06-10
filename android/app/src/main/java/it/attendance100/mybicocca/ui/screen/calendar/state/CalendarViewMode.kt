package it.attendance100.mybicocca.ui.screen.calendar.state

/**
 * Granularity of the calendar's main content — a single day's timeline, a seven-column
 * week timeline, or the month overview grid. Persisted in saved state so the chosen
 * layout survives process death.
 */
enum class CalendarViewMode { DAY, WEEK, MONTH }
