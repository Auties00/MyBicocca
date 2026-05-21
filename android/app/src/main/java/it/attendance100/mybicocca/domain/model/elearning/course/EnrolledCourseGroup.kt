package it.attendance100.mybicocca.domain.model.elearning.course

// A set of EnrolledCourse editions that share the same activity code (Esse3
// activityTranscriptCode = elearning idNumber tail segment). Editions are sorted
// latest-first by their parsed academic year; entries with no year sort to the
// bottom. A group with a single edition is rendered the same as today.
data class EnrolledCourseGroup(
    val editions: List<EnrolledCourse>,
) {
    init {
        require(editions.isNotEmpty()) { "Group must have at least one edition." }
    }

    val latest: EnrolledCourse get() = editions.first()
    val sharedCode: String? get() = latest.courseCode().code
    val isFavourite: Boolean get() = editions.any { it.isFavourite }
}
