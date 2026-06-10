package it.attendance100.mybicocca.domain.model.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate

/**
 * One row of the student's libretto (transcript): a teaching activity with its state,
 * grade and credits. Sourced from Esse3's record-book rows, cached in Room, and rendered
 * by the profile screen's exam list, averages and course-detail entry points.
 *
 * @property id Esse3 `adsceId` of the libretto row.
 * @property careerId Career the row belongs to.
 * @property activityCode University activity code, e.g. "E3101Q123".
 * @property activityName Name of the teaching activity.
 * @property courseYear Year of the course the activity sits in; 0 marks year-less
 *   activities such as prerequisites.
 * @property credits CFU weight of the activity.
 * @property state Whether the activity is planned, attended or passed.
 * @property grade Numeric grade for passed exams; null for judgment-only outcomes or
 *   activities not yet passed.
 * @property cumLaude True when the grade carries the lode.
 * @property examDate Day the exam was verbalised.
 * @property academicYear Academic year the outcome (or attendance) refers to.
 * @property inStudyPlan False for supernumerary (sovrannumerarie) activities not counted
 *   in the study plan.
 * @property examType Esse3 `tipoEsaDes` (e.g. "Scritto", "Orale", "Scritto e Orale
 *   Congiunti").
 * @property bookableCallsCount Esse3 `numAppelliPrenotabili` at the last sync: how many
 *   calls can be booked.
 */
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
    val examType: String?,
    val bookableCallsCount: Int,
) {
    val passed: Boolean get() = state == TranscriptRowState.Passed
}
