package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state

import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome

sealed interface MarkUiState {
    data object Idle : MarkUiState
    data object Submitting : MarkUiState
    data class Done(val outcome: PresenceMarkOutcome) : MarkUiState
}
