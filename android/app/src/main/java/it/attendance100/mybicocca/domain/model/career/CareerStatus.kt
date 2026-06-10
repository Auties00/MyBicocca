package it.attendance100.mybicocca.domain.model.career

/**
 * Lifecycle state of a career, mapped from the Esse3 status code (`staStuCod`). Interruption-like
 * states (interrupted, transferred, withdrawn) collapse into [INTERRUPTED]; codes the mapper does
 * not recognize fall back to [OTHER].
 */
enum class CareerStatus {
    ACTIVE,
    SUSPENDED,
    GRADUATED,
    INTERRUPTED,
    OTHER,
}

/**
 * Whether the student can still operate in this career (active or merely suspended). Drives
 * which careers the picker offers, the default selection, and the reconciliation events fired
 * when a selected career ends.
 */
val CareerStatus.isSelectable: Boolean
    get() = this == CareerStatus.ACTIVE || this == CareerStatus.SUSPENDED
