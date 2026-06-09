package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

// One page of the Biblioteca modal's in-sheet pager.
sealed interface LibraryPage {
    val key: String

    // Landing: my (server-synced) bookings + the bookable libraries.
    data object Home : LibraryPage {
        override val key = "home"
    }

    // Email-validation login (enter email -> open link -> verify).
    data object Login : LibraryPage {
        override val key = "login"
    }

    // The bookable libraries list, opened from the "Prenota" footer.
    data object Libraries : LibraryPage {
        override val key = "libraries"
    }

    // A booking referenced by its server id, resolved against the live list.
    data class ReservationDetail(val reservationId: Int) : LibraryPage {
        override val key = "reservation:$reservationId"
    }

    data class LibraryDetail(val libraryId: String) : LibraryPage {
        override val key = "library:$libraryId"
    }

    // Booking wizard.
    data object Zones : LibraryPage {
        override val key = "zones"
    }

    data object DateTime : LibraryPage {
        override val key = "datetime"
    }

    data object Seats : LibraryPage {
        override val key = "seats"
    }

    data object Confirm : LibraryPage {
        override val key = "confirm"
    }

    data object Done : LibraryPage {
        override val key = "done"
    }
}
