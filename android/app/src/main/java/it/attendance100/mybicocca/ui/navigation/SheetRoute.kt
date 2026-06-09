package it.attendance100.mybicocca.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Back-stack keys for modal bottom-sheet pages. Unlike AppRoute these are NOT full-screen
// sub-pages: BottomSheetSceneStrategy renders a run of consecutive same-group entries as one
// persistent overlay sheet floating over the current tab. The metadata `group` set per entry in
// MainShell's entryProvider decides which pages belong to the same sheet; pushing/popping these
// keys navigates within (or opens/closes) that sheet.
sealed interface SheetRoute : NavKey {

    // Iscrizioni: annual-enrollment timeline -> one year's detail.
    @Serializable
    data object Enrollments : SheetRoute

    @Serializable
    data class EnrollmentDetail(val enrollmentId: Long) : SheetRoute

    // Titoli: titles list -> one title's detail.
    @Serializable
    data object Titles : SheetRoute

    @Serializable
    data class TitleDetail(val titleId: String) : SheetRoute

    // Certificati: a single list page (with an in-page download outcome).
    @Serializable
    data object Certificates : SheetRoute

    // Rimborsi: refunds list -> one refund's detail.
    @Serializable
    data object Refunds : SheetRoute

    @Serializable
    data class RefundDetail(val refundKey: Long) : SheetRoute

    // ISEE: declarations list -> one year's declaration.
    @Serializable
    data object Isee : SheetRoute

    @Serializable
    data class IseeDetail(val year: Long) : SheetRoute

    // Esiti: a self-contained state machine (feed -> detail -> reject confirm / result), kept
    // as one entry that owns its own morphing header.
    @Serializable
    data object ExamResults : SheetRoute

    // Tasse: a single pager page (+ an external hero detail dialog).
    @Serializable
    data object Taxes : SheetRoute

    // Elearning detail sheets (carry their target's ids; their own VM is assisted-injected).
    @Serializable
    data class QuizDetail(val quizId: Int, val courseId: Int) : SheetRoute

    @Serializable
    data class AssignmentDetail(val assignId: Int, val courseId: Int) : SheetRoute

    // Presenze: a self-contained state machine (courses -> course detail / rileva flow).
    @Serializable
    data object Attendance : SheetRoute

    // Appelli: prenotazioni list -> detail / cancel-confirm, plus the booking sub-flow.
    @Serializable
    data object Appelli : SheetRoute

    // Percorso e piano: year list -> year courses, plus the plan-compiler wizard.
    @Serializable
    data object StudyPlan : SheetRoute

    // Questionari: activities -> units -> the compilation wizard.
    @Serializable
    data object Questionnaires : SheetRoute

    // Appuntamenti: reservations + the booking wizard (sections -> types -> slots -> form).
    @Serializable
    data object Appointments : SheetRoute

    // Biblioteca: reservations / login / the seat-booking wizard.
    @Serializable
    data object Library : SheetRoute
}
