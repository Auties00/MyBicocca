package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

import it.attendance100.mybicocca.domain.model.exam.ExamCall

/**
 * The appello a booking flow is open on. [activityChoiceId] is the Esse3 adsceId the booking
 * call requires; when the list response carries none, [canBook] turns the flow read-only.
 */
data class BookingTarget(
    val call: ExamCall,
    val activityChoiceId: Long?,
) {
    val canBook: Boolean get() = activityChoiceId != null
}
