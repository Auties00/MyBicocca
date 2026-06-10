package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

/** One page of the Biblioteca modal's in-sheet pager. */
sealed interface LibraryPage {
    /** Stable identity used as the pager's content key. */
    val key: String

    /** Landing: my (server-synced) bookings, with the login or booking entry pinned as a footer. */
    data object Home : LibraryPage {
        override val key = "home"
    }

    /** Email-validation login (enter email -> open link -> verify). */
    data object Login : LibraryPage {
        override val key = "login"
    }

    /** The bookable libraries list, opened from the "Prenota" footer. */
    data object Libraries : LibraryPage {
        override val key = "libraries"
    }

    /** A booking referenced by its server id, resolved against the live list. */
    data class ReservationDetail(val reservationId: Int) : LibraryPage {
        override val key = "reservation:$reservationId"
    }

    /** One library's live occupancy, forecast and opening hours. */
    data class LibraryDetail(val libraryId: String) : LibraryPage {
        override val key = "library:$libraryId"
    }

    /** Booking wizard, step 1: pick the seating zone. */
    data object Zones : LibraryPage {
        override val key = "zones"
    }

    /** Booking wizard, step 2: pick day, duration and start time. */
    data object DateTime : LibraryPage {
        override val key = "datetime"
    }

    /** Booking wizard, step 3: pick the seat. */
    data object Seats : LibraryPage {
        override val key = "seats"
    }

    /** Booking wizard, step 4: recap and submission. */
    data object Confirm : LibraryPage {
        override val key = "confirm"
    }

    /** Booking wizard, final step: success acknowledgement. */
    data object Done : LibraryPage {
        override val key = "done"
    }
}
