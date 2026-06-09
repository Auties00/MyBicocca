package it.attendance100.mybicocca.domain.model.library

// A bookable seat area of a library (an Affluences "resource type"). At Bicocca these are the
// colored seating zones plus the individual-study carrels.
data class LibraryZone(
    val resourceTypeId: Int,
    val name: String,
    val subdescription: String?,
    val imageUrl: String?,
    val color: LibraryZoneColor,
)

// The named zone color drives the accent palette. Carrels and anything unrecognized fall back.
enum class LibraryZoneColor { Rossa, Gialla, Verde, Lilla, Blu, Arancio, Carrels, Other }
