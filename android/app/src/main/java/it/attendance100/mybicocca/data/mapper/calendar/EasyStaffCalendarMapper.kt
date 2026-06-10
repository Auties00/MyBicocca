package it.attendance100.mybicocca.data.mapper.calendar

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId

/**
 * Maps an EasyStaff weekly-grid cell to a calendar lesson. Derived fields are computed here,
 * at map time: the compact short label comes from the lesson name, the booking status
 * collapses to confirmed/cancelled, and the curriculum path string becomes the event notes.
 * The activity code is supplied by the caller, which resolves it from the study plan via the
 * cell's EasyStaff subject code; the feed carries no credit information, so cfu stays null.
 */
internal fun EasyStaffScheduleCell.Lesson.toDomain(careerId: CareerId, activityCode: String?): CalendarEvent.Lesson =
    CalendarEvent.Lesson(
        id = CalendarEventId.of(EventSource.LESSON, id),
        careerId = careerId,
        date = date,
        start = startTime,
        end = endTime,
        title = name,
        shortLabel = shortLabelFor(name),
        location = locationFrom(roomCode, buildingCode, mapsUrl),
        status = if (status == EasyStaffBookingStatus.CONFIRMED) EventStatus.CONFIRMED else EventStatus.CANCELLED,
        notes = curriculumPath.takeIf { it.isNotBlank() },
        activityCode = activityCode,
        subjectCode = subjectCode,
        teachers = teacherNames,
        cfu = null,
    )

/** Builds a location only when at least one component is usable, null otherwise. */
private fun locationFrom(room: String?, building: String?, mapsUrl: String?): EventLocation? {
    if (room.isNullOrBlank() && building.isNullOrBlank() && mapsUrl.isNullOrBlank()) return null
    return EventLocation(
        room = room?.takeIf { it.isNotBlank() },
        building = building?.takeIf { it.isNotBlank() },
        mapsUrl = mapsUrl,
    )
}
