package it.attendance100.mybicocca.domain.model.tax

// Esse3 "semaforo tasse": GREEN = in regola, YELLOW = late fees accruable,
// RED = blocking overdue charges past tolerance.
enum class TaxLight { GREEN, YELLOW, RED, UNKNOWN }

data class TaxSummary(
    val light: TaxLight,
    val dueAmount: Double,
    val expiredCount: Int,
    val dueCount: Int,
)
