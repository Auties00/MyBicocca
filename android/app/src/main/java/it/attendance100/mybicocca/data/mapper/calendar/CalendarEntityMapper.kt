package it.attendance100.mybicocca.data.mapper.calendar

import it.attendance100.mybicocca.data.local.calendar.CalendarEventDiscriminator
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate
import java.time.LocalTime

private const val LIST_DELIMITER = "\n"

internal fun CalendarEvent.toEntity(): CalendarEventEntity {
    val (discriminator, extras) = when (this) {
        is CalendarEvent.Lesson -> CalendarEventDiscriminator.LESSON to EntityExtras(
            subjectCode = subjectCode,
            teachersCsv = teachers.joinToString(LIST_DELIMITER).takeIf { it.isNotEmpty() },
            cfu = cfu,
        )
        is CalendarEvent.Exam -> CalendarEventDiscriminator.EXAM to EntityExtras(
            examinersCsv = examiners.joinToString(LIST_DELIMITER).takeIf { it.isNotEmpty() },
            examTypeLabel = examTypeLabel,
        )
    }
    return CalendarEventEntity(
        id = id.value,
        careerId = careerId.value,
        source = source.code,
        discriminator = discriminator,
        date = date.toString(),
        startTime = start.toString(),
        endTime = end.toString(),
        title = title,
        shortLabel = shortLabel,
        room = location?.room,
        building = location?.building,
        mapsUrl = location?.mapsUrl,
        status = status.name,
        notes = notes,
        subjectCode = extras.subjectCode,
        teachersCsv = extras.teachersCsv,
        cfu = extras.cfu,
        examinersCsv = extras.examinersCsv,
        examTypeLabel = extras.examTypeLabel,
    )
}

internal fun CalendarEventEntity.toDomain(): CalendarEvent? {
    val location = if (room != null || building != null || mapsUrl != null) {
        EventLocation(room = room, building = building, mapsUrl = mapsUrl)
    } else null
    val statusEnum = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.CONFIRMED)
    val date = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    val start = runCatching { LocalTime.parse(startTime) }.getOrNull() ?: return null
    val end = runCatching { LocalTime.parse(endTime) }.getOrNull() ?: return null
    val careerId = CareerId(careerId)
    val eventId = CalendarEventId(id)
    return when (discriminator) {
        CalendarEventDiscriminator.LESSON -> CalendarEvent.Lesson(
            id = eventId,
            careerId = careerId,
            date = date,
            start = start,
            end = end,
            title = title,
            shortLabel = shortLabel,
            location = location,
            status = statusEnum,
            notes = notes,
            subjectCode = subjectCode,
            teachers = teachersCsv?.split(LIST_DELIMITER).orEmpty().filter { it.isNotBlank() },
            cfu = cfu,
        )
        CalendarEventDiscriminator.EXAM -> CalendarEvent.Exam(
            id = eventId,
            careerId = careerId,
            date = date,
            start = start,
            end = end,
            title = title,
            shortLabel = shortLabel,
            location = location,
            status = statusEnum,
            notes = notes,
            examiners = examinersCsv?.split(LIST_DELIMITER).orEmpty().filter { it.isNotBlank() },
            examTypeLabel = examTypeLabel,
        )
        else -> null
    }
}

private data class EntityExtras(
    val subjectCode: String? = null,
    val teachersCsv: String? = null,
    val cfu: Int? = null,
    val examinersCsv: String? = null,
    val examTypeLabel: String? = null,
)
