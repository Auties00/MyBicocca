package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

import it.attendance100.mybicocca.domain.model.exam.ExamCall

data class BookingTarget(
    val call: ExamCall,
    val activityChoiceId: Long?,
) {
    val canBook: Boolean get() = activityChoiceId != null
}
