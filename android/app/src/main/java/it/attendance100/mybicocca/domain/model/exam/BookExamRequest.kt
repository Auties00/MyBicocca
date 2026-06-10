package it.attendance100.mybicocca.domain.model.exam

/**
 * Payload the student submits when booking a seat on an exam call (appello) from the
 * booking sheet in the registry tab.
 *
 * @property activityChoiceId Esse3 `adsceId` of the libretto row being booked; the
 *   enrollment POST requires it to tie the booking to the student's transcript activity.
 * @property studentNote Optional free-text note attached to the booking and visible to
 *   the examiners.
 */
data class BookExamRequest(
    val activityChoiceId: Long,
    val studentNote: String? = null,
)
