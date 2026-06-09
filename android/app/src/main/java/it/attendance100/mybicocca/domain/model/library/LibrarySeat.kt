package it.attendance100.mybicocca.domain.model.library

import java.time.LocalTime

// A single bookable seat (an Affluences "resource") of a zone, with its open slots for a day.
data class LibrarySeat(
    val resourceId: Int,
    val name: String,
    // The seat label without the trailing "- Posto con presa elettrica" note (e.g. "R 100").
    val shortName: String,
    val hasPowerOutlet: Boolean,
    val description: String?,
    val noteRequired: Boolean,
    // What the required note should contain (e.g. "Cognome e nome").
    val noteLabel: String?,
    val slots: List<LibrarySlot>,
)

data class LibrarySlot(
    val start: LocalTime,
    val durationMinutes: Int,
    val placesBookable: Int,
)

fun LibrarySeat.isBookableAt(start: LocalTime): Boolean =
    slots.any { it.start == start && it.placesBookable > 0 }
