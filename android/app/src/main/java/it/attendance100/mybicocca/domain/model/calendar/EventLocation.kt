package it.attendance100.mybicocca.domain.model.calendar

data class EventLocation(
    val room: String?,
    val building: String?,
    val mapsUrl: String? = null,
)
