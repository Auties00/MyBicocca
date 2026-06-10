package it.attendance100.mybicocca.domain.model.transcript

/**
 * Lifecycle state of a libretto row, from Esse3's record-book state: still to attend
 * ([Planned]), attended but not yet passed ([Frequented]), or verbalised ([Passed]).
 * Unrecognised Esse3 states are folded into [Planned].
 */
enum class TranscriptRowState {
    Planned,
    Frequented,
    Passed,
}
