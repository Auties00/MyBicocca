package it.attendance100.mybicocca.domain.model.library

/**
 * A bookable seat area of a library — an Affluences "resource type". At Bicocca these are the
 * colored seating zones plus the individual-study carrels. Listed in the zone-picker step of the
 * Biblioteca booking flow.
 *
 * @property resourceTypeId Affluences resource-type id, used in availability and filter calls.
 * @property name Zone display name; "Posto" when the server provides no description.
 * @property subdescription Secondary descriptive line; null when blank or absent.
 * @property imageUrl URL of the zone's illustration; null when absent.
 * @property color Named zone color recognized from the description.
 */
data class LibraryZone(
    val resourceTypeId: Int,
    val name: String,
    val subdescription: String?,
    val imageUrl: String?,
    val color: LibraryZoneColor,
)

/**
 * The named zone color, which drives the zone's accent palette. Carrels and anything
 * unrecognized fall back to their dedicated entries.
 */
enum class LibraryZoneColor { Rossa, Gialla, Verde, Lilla, Blu, Arancio, Carrels, Other }
