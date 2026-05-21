package it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state

import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow

data class TranscriptPartition(
    val passed: List<TranscriptRow>,
    val pending: List<TranscriptRow>,
)
