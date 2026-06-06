package it.attendance100.mybicocca.data.mapper.calendar

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffBookingStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffExamType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduledExam
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId

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

internal fun EasyStaffScheduledExam.toDomain(careerId: CareerId, activityCode: String?): CalendarEvent.Exam =
    CalendarEvent.Exam(
        id = CalendarEventId.of(EventSource.EXAM, eventId),
        careerId = careerId,
        date = date,
        start = startTime,
        end = endTime,
        title = subjectName,
        shortLabel = shortLabelFor(subjectName),
        location = locationFrom(room, building, null),
        status = if (isCancelled) EventStatus.CANCELLED else EventStatus.CONFIRMED,
        notes = notes.takeIf { it.isNotBlank() },
        activityCode = activityCode,
        examiners = examiners.map { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() }
            .filter { it.isNotBlank() },
        examTypeLabel = examTypeLabel(examType),
    )

private fun examTypeLabel(type: EasyStaffExamType): String? = when (type) {
    EasyStaffExamType.Written -> "Scritto"
    EasyStaffExamType.Oral -> "Orale"
    EasyStaffExamType.OralAndWritten -> "Scritto e orale"
    is EasyStaffExamType.Other -> type.value.takeIf { it.isNotBlank() }
}

private fun locationFrom(room: String?, building: String?, mapsUrl: String?): EventLocation? {
    if (room.isNullOrBlank() && building.isNullOrBlank() && mapsUrl.isNullOrBlank()) return null
    return EventLocation(
        room = room?.takeIf { it.isNotBlank() },
        building = building?.takeIf { it.isNotBlank() },
        mapsUrl = mapsUrl,
    )
}
