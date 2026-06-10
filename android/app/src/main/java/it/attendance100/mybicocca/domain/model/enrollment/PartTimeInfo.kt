package it.attendance100.mybicocca.domain.model.enrollment

/**
 * Part-time terms of an annual enrollment, present only when the year was enrolled
 * part-time (Esse3 `ptFlg == 1`).
 *
 * @property credits CFU cap chosen at enrollment.
 * @property extraCredits CFU added on top of the cap during the year; null when none.
 * @property locked True when further changes to the part-time choice are blocked for
 *   that year.
 */
data class PartTimeInfo(
    val credits: Int?,
    val extraCredits: Int?,
    val locked: Boolean,
)
