package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state

/** One page of the Biblioteca modal's in-sheet pager. */
sealed interface LibraryPage {
    /** Stable identity used as the pager's content key. */
    val key: String

    /** Landing hub: libraries CTA or next-booking split box, bookings row, services row. */
    data object Home : LibraryPage {
        override val key = "home"
    }

    /** Bookable libraries list, ordered with the student's preferred campus first. */
    data object Libraries : LibraryPage {
        override val key = "libraries"
    }

    /** Email-validation login (enter email -> open link -> verify). */
    data class Login(val fromBooking: Boolean = false) : LibraryPage {
        override val key = if (fromBooking) "login:booking" else "login"
    }

    /** Server-synced bookings list (upcoming and history) for the linked account. */
    data object Bookings : LibraryPage {
        override val key = "bookings"
    }

    /** Linked library account details and logout entry. */
    data object Account : LibraryPage {
        override val key = "account"
    }

    /** Library web services directory. */
    data object Services : LibraryPage {
        override val key = "services"
    }

    /** A booking referenced by its server id, resolved against upcoming and past lists. */
    data class ReservationDetail(val reservationId: Int) : LibraryPage {
        override val key = "reservation:$reservationId"
    }

    /** One library's photo, facilities, crowding chart and practical info. */
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

/** Pager depth governing horizontal transition direction for every [LibraryPage]. */
fun LibraryPage.depth(): Int = when (this) {
    LibraryPage.Home -> 0
    LibraryPage.Libraries -> 1
    LibraryPage.Bookings -> 1
    LibraryPage.Account -> 1
    LibraryPage.Services -> 1
    is LibraryPage.Login -> if (fromBooking) 7 else 1
    is LibraryPage.LibraryDetail -> 2
    is LibraryPage.ReservationDetail -> 2
    LibraryPage.Zones -> 3
    LibraryPage.DateTime -> 4
    LibraryPage.Seats -> 5
    LibraryPage.Confirm -> 6
    LibraryPage.Done -> 8
}
