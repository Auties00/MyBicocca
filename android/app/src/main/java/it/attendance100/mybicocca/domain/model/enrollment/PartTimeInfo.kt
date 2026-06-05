package it.attendance100.mybicocca.domain.model.enrollment

// Present only when the year was enrolled part-time (ptFlg == 1). Credits is the CFU cap
// chosen at enrollment; extraCredits are CFU added during the year; locked blocks further
// changes to the part-time choice for that year.
data class PartTimeInfo(
    val credits: Int?,
    val extraCredits: Int?,
    val locked: Boolean,
)
