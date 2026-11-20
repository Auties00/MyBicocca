package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state

import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService

/**
 * Whether what the menu shows (place, menu, day, meal) can be ordered. Browsing never needs an
 * account, but booking does, and the booking data says which days and meals are open.
 */
sealed interface MensaOrderAvailability {
    /** The booking data has not arrived yet. */
    data object Checking : MensaOrderAvailability

    /** Ordering needs an account. */
    data object SignedOut : MensaOrderAvailability

    /** The menu has no booking service, or it is closed for this day, meal or place. */
    data object Unavailable : MensaOrderAvailability

    /** The canteen takes one booking per day and meal, and the user already has it. */
    data class AlreadyBooked(val reservation: MensaReservation) : MensaOrderAvailability

    data class Available(val service: MensaTakeawayService) : MensaOrderAvailability
}
