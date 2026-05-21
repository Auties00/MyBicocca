package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

sealed interface CourseFilter {
    data object All : CourseFilter
    data object Favourites : CourseFilter
    data class ByYear(val year: StudyYear) : CourseFilter
}