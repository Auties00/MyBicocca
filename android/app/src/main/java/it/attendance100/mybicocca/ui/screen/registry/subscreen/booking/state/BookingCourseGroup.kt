package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

import it.attendance100.mybicocca.domain.model.exam.ExamCall

/** All bookable calls of one exam, rendered as one section of the booking calendar list. */
data class BookingCourseGroup(
    val courseKey: String,
    val courseTitle: String,
    val courseCode: String?,
    val courseOfStudy: String?,
    val calls: List<ExamCall>,
)

/**
 * Groups calls per exam, keyed by activity code (falling back to the description, then the
 * activity id, when missing); calls sort by date within a group, groups by title. The key
 * doubles as the deep-link focus key the libretto course sheet targets.
 */
fun List<ExamCall>.groupByCourse(): List<BookingCourseGroup> =
    groupBy { call ->
        call.activityCode?.takeIf { it.isNotBlank() }
            ?: call.activityDescription?.takeIf { it.isNotBlank() }
            ?: call.key.activityId.toString()
    }.map { (key, calls) ->
        val first = calls.first()
        BookingCourseGroup(
            courseKey = key,
            courseTitle = first.activityDescription?.takeIf { it.isNotBlank() } ?: "Esame",
            courseCode = first.activityCode?.takeIf { it.isNotBlank() },
            courseOfStudy = first.courseOfStudyDescription?.takeIf { it.isNotBlank() },
            calls = calls.sortedWith(compareBy(nullsLast()) { it.callDate }),
        )
    }.sortedBy { it.courseTitle.lowercase() }
