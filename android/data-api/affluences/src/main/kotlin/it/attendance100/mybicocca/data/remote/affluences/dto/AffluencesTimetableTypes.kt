package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The opening hours of a site for one week, as returned by the app API v4
 * (`GET /sites/{site}/timetables?weekOffset=N`).
 *
 * @property startDate The first day of the week (Monday), as a `yyyy-MM-dd` date string.
 * @property endDate The last day of the week (Sunday), as a `yyyy-MM-dd` date string.
 * @property entries One entry per day of the week, in chronological order.
 * @property information Localized free-form information about the timetable, when provided.
 */
@Serializable
data class AffluencesWeekTimetable(
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("entries")
    val entries: List<AffluencesDayTimetable> = emptyList(),
    @SerialName("information")
    val information: String? = null
)

/**
 * The opening hours of a site for a single day.
 *
 * @property day The day, as a `yyyy-MM-dd` date string.
 * @property isToday Whether [day] is the current day in the site time zone.
 * @property openingHours The opening ranges of the day. An empty list means the site is closed.
 */
@Serializable
data class AffluencesDayTimetable(
    @SerialName("day")
    val day: String,
    @SerialName("isToday")
    val isToday: Boolean = false,
    @SerialName("openingHours")
    val openingHours: List<AffluencesOpeningHours> = emptyList()
)

/**
 * A continuous opening range within a day.
 *
 * @property openingHour When the range starts, as a `yyyy-MM-dd HH:mm:ss` date-time string
 * local to the site time zone.
 * @property closingHour When the range ends, as a `yyyy-MM-dd HH:mm:ss` date-time string
 * local to the site time zone.
 */
@Serializable
data class AffluencesOpeningHours(
    @SerialName("openingHour")
    val openingHour: String,
    @SerialName("closingHour")
    val closingHour: String
)
