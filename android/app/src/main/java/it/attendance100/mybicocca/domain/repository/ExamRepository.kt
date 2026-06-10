package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamResult

/**
 * Exam calls, bookings and published outcomes for a career.
 *
 * Reads are live-first, a deliberate departure from the Room-SSOT default: volatility
 * (seat counts, opening windows) makes a silently stale read dangerous, so every call
 * hits Esse3 while connectivity exists. The list reads keep an offline snapshot of their
 * last success purely for display when the device has no network — booking actions are
 * disabled in that state. All methods throw on failure unless noted.
 */
interface ExamRepository {

    /**
     * The exam calls the student can book (bookable and future). Each call carries
     * everything the booking sheet renders (notes, president, booking mode): the
     * per-appello detail endpoint costs 1.7–4s cold server-side, while the list response
     * already includes those fields for free, so no detail call is ever made.
     */
    suspend fun getExamCalls(careerId: CareerId): List<ExamCall>

    suspend fun bookExam(
        careerId: CareerId,
        key: ExamCallKey,
        request: BookExamRequest,
    ): ExamBooking

    /**
     * The student's full booking history, upcoming and past, in one call; past rows
     * carry their published outcome inline.
     */
    suspend fun getBookings(careerId: CareerId): List<BookedExam>

    /**
     * Bookings whose outcome has been published (`pubblId > 0`) — what the user thinks
     * of as the "Bacheca Esiti". Broader than Esse3's strict `q=BACHECA_ESITI` filter,
     * which only includes outcomes whose acknowledgment deadline has passed.
     */
    suspend fun getExamResults(careerId: CareerId): List<ExamResult>

    /**
     * Accepts (`presaVisione=A`) or rejects (`R`) a published outcome.
     * applicationListId is the booking's prenotazione id carried through
     * [getExamResults] into the exam result.
     */
    suspend fun acknowledgeExamResult(careerId: CareerId, applicationListId: Long, accept: Boolean)

    /** studentId here is the booking's `stuId`, not the user's `matId`. */
    suspend fun cancelBooking(careerId: CareerId, key: ExamCallKey, studentId: Long)

    /**
     * The booking slip PDF (statino di prenotazione) as raw bytes, available for any
     * booking. studentId is the booking's `stuId`, as for [cancelBooking].
     */
    suspend fun getBookingSlip(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray

    /**
     * The attendance certificate PDF (attestato di presenza) as raw bytes, available
     * only once the outcome is published (Esse3 responds 422 otherwise). studentId is
     * the booking's `stuId`, as for [cancelBooking].
     */
    suspend fun getPresenceCertificate(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray
}
