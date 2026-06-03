package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallDetail
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamResult

// Exam booking data is intentionally NOT cached locally — every call hits Esse3.
// Volatility (seat counts, opening windows) makes stale reads dangerous; the spinner
// on a fresh fetch is preferable to acting on stale state. See project_booking_no_cache.md.
interface ExamRepository {

    suspend fun getExamCalls(careerId: CareerId): List<ExamCall>

    suspend fun getExamCallDetail(careerId: CareerId, key: ExamCallKey): ExamCallDetail

    suspend fun bookExam(
        careerId: CareerId,
        key: ExamCallKey,
        request: BookExamRequest,
    ): ExamBooking

    // Upcoming bookings only (server-filtered by exam date >= today).
    suspend fun getBookings(careerId: CareerId): List<BookedExam>

    // Bookings whose outcome has been published (pubblId > 0) — what the user thinks
    // of as the "Bacheca Esiti". Broader than Esse3's strict q=BACHECA_ESITI filter,
    // which only includes outcomes whose acknowledgment deadline has passed.
    suspend fun getExamResults(careerId: CareerId): List<ExamResult>

    // Accept (presaVisione=A) or reject (R) a published outcome. applicationListId is the
    // booking's prenotazione id carried through getExamResults → ExamResult.
    suspend fun acknowledgeExamResult(careerId: CareerId, applicationListId: Long, accept: Boolean)

    // studentId here is the booking's `stuId`, not the user's matId.
    suspend fun cancelBooking(careerId: CareerId, key: ExamCallKey, studentId: Long)
}
