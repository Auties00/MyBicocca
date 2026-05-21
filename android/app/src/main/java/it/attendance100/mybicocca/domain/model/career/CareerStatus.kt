package it.attendance100.mybicocca.domain.model.career

enum class CareerStatus {
    ACTIVE,
    SUSPENDED,
    GRADUATED,
    INTERRUPTED,
    OTHER,
}

val CareerStatus.isSelectable: Boolean
    get() = this == CareerStatus.ACTIVE || this == CareerStatus.SUSPENDED
