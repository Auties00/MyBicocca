package it.attendance100.mybicocca.domain.model.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate

data class TranscriptRow(
    val id: Long,
    val careerId: CareerId,
    val activityCode: String?,
    val activityName: String,
    val courseYear: Int,
    val credits: Float,
    val state: TranscriptRowState,
    val grade: Int?,
    val cumLaude: Boolean,
    val examDate: LocalDate?,
    val academicYear: Int?,
    val inStudyPlan: Boolean,
    // Esse3 tipoEsaDes (e.g. "Scritto", "Orale", "Scritto e Orale Congiunti").
    val examType: String?,
    // numAppelliPrenotabili at the last sync: how many calls can be booked right now.
    val bookableCallsCount: Int,
) {
    val passed: Boolean get() = state == TranscriptRowState.Passed
}
