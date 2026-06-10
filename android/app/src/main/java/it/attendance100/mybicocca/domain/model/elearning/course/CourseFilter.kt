package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

/**
 * The user-selected filter for the e-learning course list, persisted across sessions.
 * Years come from each course's idNumber, so [ByYear] works fully offline.
 */
sealed interface CourseFilter {
    /** No filtering: every non-hidden enrolled course is listed. */
    data object All : CourseFilter

    /** Only courses the user starred as favourites. */
    data object Favourites : CourseFilter

    /**
     * Only courses whose idNumber resolves to the given year of study.
     *
     * @property year The year of study (1-based) to keep.
     */
    data class ByYear(val year: StudyYear) : CourseFilter
}