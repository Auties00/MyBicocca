package it.attendance100.mybicocca.ui.screen.calendar.ext

import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import java.text.Normalizer


fun resolveEnrolledCourseId(event: CalendarEvent, courses: List<EnrolledCourse>): CourseId? {
    if (courses.isEmpty()) return null

    val subjectCode = (event as? CalendarEvent.Lesson)
        ?.subjectCode
        ?.trim()
        ?.takeIf { it.isNotBlank() }
    if (subjectCode != null) {
        courses.firstOrNull { it.courseCode().code?.equals(subjectCode, ignoreCase = true) == true }
            ?.let { return it.id }
    }

    val normalizedTitle = normalizeCourseName(event.title)
    if (normalizedTitle.isNotBlank()) {
        courses.firstOrNull {
            normalizeCourseName(it.fullName) == normalizedTitle ||
                normalizeCourseName(it.displayName) == normalizedTitle ||
                normalizeCourseName(it.shortName) == normalizedTitle
        }?.let { return it.id }
    }

    return null
}

private val DiacriticsRegex = Regex("\\p{Mn}+")
private val NonAlphanumericRegex = Regex("[^a-z0-9]+")

private fun normalizeCourseName(raw: String): String =
    Normalizer.normalize(raw, Normalizer.Form.NFD)
        .replace(DiacriticsRegex, "")
        .lowercase()
        .replace(NonAlphanumericRegex, " ")
        .trim()
