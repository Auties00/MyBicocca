package it.attendance100.mybicocca.domain.model.library

import java.time.LocalTime

/**
 * A single bookable seat — an Affluences "resource" — of a zone, with its open slots for a day.
 * Listed in the seat-picker step of the Biblioteca booking flow.
 *
 * @property resourceId Affluences resource id, sent when creating the reservation.
 * @property name Full seat label as published, including any power-outlet note.
 * @property shortName The seat label without the trailing "- Posto con presa elettrica" note
 * (e.g. "R 100").
 * @property hasPowerOutlet Whether the label marks the seat as having a power outlet.
 * @property description Free-form seat description; null when blank or absent.
 * @property noteRequired Whether a booking note is mandatory for this seat.
 * @property noteLabel What the required note should contain (e.g. "Cognome e nome"); null when
 * absent.
 * @property slots The seat's bookable time slots for the requested day.
 */
data class LibrarySeat(
    val resourceId: Int,
    val name: String,
    val shortName: String,
    val hasPowerOutlet: Boolean,
    val description: String?,
    val noteRequired: Boolean,
    val noteLabel: String?,
    val slots: List<LibrarySlot>,
)

/**
 * One bookable time slot of a seat.
 *
 * @property start Slot start time.
 * @property durationMinutes Slot granularity, in minutes.
 * @property placesBookable How many places remain bookable at this start time; 0 means full.
 */
data class LibrarySlot(
    val start: LocalTime,
    val durationMinutes: Int,
    val placesBookable: Int,
)

/** Whether the seat has a slot starting exactly at [start] with at least one place left. */
fun LibrarySeat.isBookableAt(start: LocalTime): Boolean =
    slots.any { it.start == start && it.placesBookable > 0 }
