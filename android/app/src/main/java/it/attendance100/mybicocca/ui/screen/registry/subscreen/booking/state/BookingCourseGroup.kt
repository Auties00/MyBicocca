package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

import it.attendance100.mybicocca.domain.model.exam.ExamCall

data class BookingCourseGroup(
    val courseKey: String,
    val courseTitle: String,
    val courseCode: String?,
    val courseOfStudy: String?,
    val calls: List<ExamCall>,
)

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
