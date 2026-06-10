package it.attendance100.mybicocca.domain.model.calendar

/**
 * Physical place a [CalendarEvent] takes place at. Events whose source provides no location
 * information carry null instead of an empty instance.
 *
 * @property room Room name or code; sources without a room concept reuse the slot for their
 *   closest equivalent (appointments store the office street address, library reservations
 *   the seat label). Null when unknown.
 * @property building Building name or code; appointments store the office area name here and
 *   library reservations the library's secondary name. Null when unknown.
 * @property mapsUrl Link to an external map of the venue; provided by the EasyStaff lesson
 *   feed only, null for every other source.
 */
data class EventLocation(
    val room: String?,
    val building: String?,
    val mapsUrl: String? = null,
)
