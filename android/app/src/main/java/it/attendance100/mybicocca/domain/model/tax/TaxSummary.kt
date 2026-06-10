package it.attendance100.mybicocca.domain.model.tax

/**
 * Esse3 tax traffic light ("semaforo tasse"): [GREEN] means the student is in good standing,
 * [YELLOW] that late fees can accrue, [RED] that blocking overdue charges are past tolerance.
 */
enum class TaxLight { GREEN, YELLOW, RED, UNKNOWN }

/**
 * Aggregate payment standing of the student, sourced live from the Esse3 traffic-light
 * (semaforo) endpoint and shown as the header of the registry "Tasse" sub-screen.
 *
 * @property light Overall standing.
 * @property dueAmount Total outstanding amount in euro.
 * @property expiredCount Number of overdue charges.
 * @property dueCount Number of charges currently due.
 */
data class TaxSummary(
    val light: TaxLight,
    val dueAmount: Double,
    val expiredCount: Int,
    val dueCount: Int,
)
