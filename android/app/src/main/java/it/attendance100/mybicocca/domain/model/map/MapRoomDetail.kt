package it.attendance100.mybicocca.domain.model.map

// Fetched on demand for a single room. EasyStaff's room showcase carries no room identity,
// so we query it one room at a time (the returned card then unambiguously belongs to that
// room). Not cached — it is secondary, volatile detail surfaced only when a room is opened.
data class MapRoomDetail(
    val floor: Int?,
    val capacity: Int?,
    val isAccessible: Boolean,
    // Free-form notes (internal steps, amphitheater layout, accessible-table booking info).
    val accessibilityNotes: String?,
    // Marked as validated by the university's "Spazio B.Inclusion" accessibility service.
    val isInclusionValidated: Boolean,
    val roomType: String?,
    val interactive360Url: String?,
    val address: String?,
    val description: String?,
    val equipment: List<String>,
)
