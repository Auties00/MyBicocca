package it.attendance100.mybicocca.data.sync

import java.time.YearMonth

object SyncKeys {
    const val USER = "user"
    const val CAREERS = "careers"
    const val CAMPUS_BUILDINGS = "campus_buildings"
    const val APPOINTMENTS_STUDENT = "appointments:student"
    const val ISEE = "isee:latest"

    fun transcript(careerId: Long) = "transcript:$careerId"
    fun taxes(careerId: Long) = "taxes:$careerId"
    fun studyPlan(careerId: Long) = "study_plan:$careerId"
    fun internships(careerId: Long) = "internships:$careerId"
    fun questionnaires(careerId: Long) = "questionnaires:$careerId"
    fun examCalls(careerId: Long) = "exam_calls:$careerId"
    fun examBookings(matricolaId: Long) = "exam_bookings:$matricolaId"
    fun attendance(studentId: String) = "attendance:$studentId"
    fun calendar(month: YearMonth) = "calendar:$month"
}
