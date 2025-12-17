package it.attendance100.mybicocca.domain.model

import java.time.LocalDate
import java.time.YearMonth

sealed interface CourseEventSelector {
    data class ByDay(val day: LocalDate): CourseEventSelector
    data class ByMonth(val month: YearMonth) : CourseEventSelector
}