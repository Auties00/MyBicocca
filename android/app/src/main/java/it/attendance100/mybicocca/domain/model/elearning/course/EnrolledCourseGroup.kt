package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Editions of the same teaching grouped into one course-list row. Editions share an
 * activity code (the Esse3 transcript code embedded in the e-learning idNumber tail)
 * or an identical course name, and are ordered latest academic year first with
 * year-less entries at the bottom. A single-edition group renders as a plain course row.
 *
 * @property editions The grouped courses, latest edition first; never empty.
 */
data class EnrolledCourseGroup(
    val editions: List<EnrolledCourse>,
) {
    init {
        require(editions.isNotEmpty()) { "Group must have at least one edition." }
    }

    /** The most recent edition, used as the group's face in the course list. */
    val latest: EnrolledCourse get() = editions.first()

    /** The activity code shared by the editions, or null when none parses. */
    val sharedCode: String? get() = latest.courseCode().code

    /** Whether any edition is starred; the group row shows the star if so. */
    val isFavourite: Boolean get() = editions.any { it.isFavourite }
}
