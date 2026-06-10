package it.attendance100.mybicocca.domain.model.map

/**
 * Rich detail of a single room, from EasyStaff's room showcase (vetrina_aule census). Fetched on
 * demand when a room is opened in the map tab; never cached, as it is secondary, volatile
 * detail. The showcase cards carry no room identity, so the showcase is queried one room at a
 * time and the returned card unambiguously belongs to that room.
 *
 * @property floor Floor level, 0 being the ground floor; null when unknown.
 * @property capacity Seating capacity; null when unknown.
 * @property isAccessible Whether the room is officially marked as accessible.
 * @property accessibilityNotes Free-form notes (internal steps, amphitheater layout,
 * accessible-table booking info); null when absent.
 * @property isInclusionValidated Whether the room is marked as validated by the university's
 * "Spazio B.Inclusion" accessibility service.
 * @property roomType Room category label (e.g. "Aula Magna"); null when absent.
 * @property address Street address of the room's building; null when absent.
 * @property description Free-form description; null when absent.
 * @property equipment User-facing equipment labels, in Italian.
 */
data class MapRoomDetail(
    val floor: Int?,
    val capacity: Int?,
    val isAccessible: Boolean,
    val accessibilityNotes: String?,
    val isInclusionValidated: Boolean,
    val roomType: String?,
    val address: String?,
    val description: String?,
    val equipment: List<String>,
)
