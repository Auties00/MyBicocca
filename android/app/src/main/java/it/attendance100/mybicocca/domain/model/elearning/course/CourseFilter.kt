package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

/**
 * The user-selected filter for the e-learning course list, persisted across sessions.
 * Years and careers come from each course's idNumber (see [CourseCareers]), so every filter
 * works fully offline.
 */
sealed interface CourseFilter {
    /** No filtering: every non-hidden enrolled course is listed. */
    data object All : CourseFilter

    /** Only courses the user starred as favourites. */
    data object Favourites : CourseFilter

    /**
     * Only the selected career's courses whose idNumber resolves to the given year of study.
     *
     * @property year The year of study (1-based) to keep.
     */
    data class ByYear(val year: StudyYear) : CourseFilter

    /**
     * Only the courses of one of the student's other careers, e.g. the bachelor's of a student
     * now in a master's.
     */
    data class ByCareer(val careerId: CareerId) : CourseFilter
}
