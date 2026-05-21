package it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state

import java.time.LocalDate

data class ChartPoint(
    val rowId: Long,
    val x: Float,
    val y: Float,
    val date: LocalDate,
)
