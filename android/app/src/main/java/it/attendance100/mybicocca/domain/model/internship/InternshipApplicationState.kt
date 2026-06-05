package it.attendance100.mybicocca.domain.model.internship

// Mirrors Esse3's domanda-di-tirocinio lifecycle: PRE, CON, AVV, CHI, ANN, RIF, NAS.
enum class InternshipApplicationState {
    Submitted,
    Confirmed,
    Started,
    Closed,
    Cancelled,
    Rejected,
    NotAssigned,
    Unknown,
}
