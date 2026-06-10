package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state

/**
 * One page of the Appuntamenti modal's in-sheet pager. The modal hosts a single morphing
 * header over this stack; depth is the page's index in the back stack, which drives the
 * header morph and the forward/back page transition (mirrors the Edifici sheet).
 */
sealed interface AppointmentsPage {
    val key: String

    data object Reservations : AppointmentsPage {
        override val key = "reservations"
    }

    /**
     * The reservation is referenced by code and resolved against the live Room list, so a
     * cancellation elsewhere can pop the page.
     */
    data class ReservationDetail(val code: String) : AppointmentsPage {
        override val key = "reservation:$code"
    }

    data object Sections : AppointmentsPage {
        override val key = "sections"
    }

    data class Types(val sectionName: String) : AppointmentsPage {
        override val key = "types:$sectionName"
    }

    data object Slots : AppointmentsPage {
        override val key = "slots"
    }

    data object Form : AppointmentsPage {
        override val key = "form"
    }

    data object Done : AppointmentsPage {
        override val key = "done"
    }
}
