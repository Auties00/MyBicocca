package it.attendance100.mybicocca.domain.model.career

/**
 * Lifecycle state of a career, mapped from the Esse3 status code (`staStuCod`). Interruption-like
 * states (interrupted, transferred, withdrawn) collapse into [INTERRUPTED]; codes the mapper does
 * not recognize fall back to [OTHER].
 */
enum class CareerStatus {
    ACTIVE,
    SUSPENDED,

    /**
     * Esse3 "Ipotesi": a career opened ahead of its requirements, e.g. a master's started before
     * the bachelor's is awarded. The student operates in it like in an active one.
     */
    PROVISIONAL,
    GRADUATED,
    INTERRUPTED,
    OTHER,
}

/**
 * Whether the student can still operate in this career (active, provisional or merely
 * suspended). Ended careers stay pickable; this drives how the pickers group and style them, the
 * default selection, whether sign-in asks for a pick, and the reconciliation events fired when a
 * selected career ends.
 */
val CareerStatus.isOpen: Boolean
    get() = this == CareerStatus.ACTIVE ||
        this == CareerStatus.SUSPENDED ||
        this == CareerStatus.PROVISIONAL
