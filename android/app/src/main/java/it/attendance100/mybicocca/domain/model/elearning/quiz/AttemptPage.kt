package it.attendance100.mybicocca.domain.model.elearning.quiz

/**
 * One page of questions within a quiz attempt, loaded from Moodle's mod_quiz_get_attempt_data
 * web service while an attempt is in progress and reassembled from the attempt review once it
 * is finished. Rendered as one step of the attempt wizard in the quiz detail sheet.
 *
 * @property attemptId Attempt the page belongs to.
 * @property pageIndex 0-based index of the page within the attempt.
 * @property nextPage Index of the following page; null on the last page (Moodle's -1 end
 * marker) and on review pages, which arrive all at once.
 * @property questions Questions on the page, in slot order.
 */
data class AttemptPage(
    val attemptId: AttemptId,
    val pageIndex: Int,
    val nextPage: Int?,
    val questions: List<AttemptQuestion>,
)
