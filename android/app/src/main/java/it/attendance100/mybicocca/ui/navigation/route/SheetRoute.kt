package it.attendance100.mybicocca.ui.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Back-stack keys for modal bottom-sheet pages. Unlike [AppRoute] these are NOT full-screen
 * sub-pages: the bottom-sheet scene strategy renders a run of consecutive same-group entries as
 * one persistent overlay sheet floating over the current tab. The metadata `group` set per entry
 * in [MainShell]'s entryProvider decides which pages belong to the same sheet; pushing/popping
 * these keys navigates within (or opens/closes) that sheet.
 */
sealed interface SheetRoute : NavKey {

    /** Iscrizioni: the annual-enrollment timeline page. */
    @Serializable
    data object Enrollments : SheetRoute

    /** One enrollment year's detail, an in-sheet page over [Enrollments]. */
    @Serializable
    data class EnrollmentDetail(val enrollmentId: Long) : SheetRoute

    /** Titoli: the titles list page. */
    @Serializable
    data object Titles : SheetRoute

    /** One title's detail, an in-sheet page over [Titles]. */
    @Serializable
    data class TitleDetail(val titleId: String) : SheetRoute

    /** Certificati: a single list page, with an in-page download outcome. */
    @Serializable
    data object Certificates : SheetRoute

    /** Rimborsi: the refunds list page. */
    @Serializable
    data object Refunds : SheetRoute

    /** One refund's detail, an in-sheet page over [Refunds]. */
    @Serializable
    data class RefundDetail(val refundKey: Long) : SheetRoute

    /** ISEE: the declarations list page. */
    @Serializable
    data object Isee : SheetRoute

    /** One academic year's ISEE declaration, an in-sheet page over [Isee]. */
    @Serializable
    data class IseeDetail(val year: Long) : SheetRoute

    /**
     * Esiti: a self-contained state machine (feed -> detail -> reject confirm / result), kept as
     * one entry that owns its own morphing header.
     */
    @Serializable
    data object ExamResults : SheetRoute

    /** Tasse: a single pager page, plus an external hero detail dialog. */
    @Serializable
    data object Taxes : SheetRoute

    /**
     * Quiz detail sheet. Carries its target ids because the page's ViewModel is assisted-injected
     * (Navigation3 does not populate SavedStateHandle).
     */
    @Serializable
    data class QuizDetail(val quizId: Int, val courseId: Int) : SheetRoute

    /**
     * Forum: discussions list -> thread -> composer, all in one entry's internal page machine.
     * [initialDiscussionId] opens straight onto a thread (e.g. the latest announcement), with the
     * discussions list still behind it for back navigation.
     */
    @Serializable
    data class Forum(
        val forumId: Int,
        val courseId: Int,
        val initialDiscussionId: Int? = null,
    ) : SheetRoute

    /**
     * Compito detail sheet. Carries its target ids because the page's ViewModel is
     * assisted-injected (Navigation3 does not populate SavedStateHandle).
     */
    @Serializable
    data class AssignmentDetail(val assignId: Int, val courseId: Int) : SheetRoute

    /** Presenze: a self-contained state machine (courses -> course detail / rileva flow). */
    @Serializable
    data object Attendance : SheetRoute

    /** Appelli: prenotazioni list -> detail / cancel-confirm, plus the booking sub-flow. */
    @Serializable
    data object Appelli : SheetRoute

    /** Percorso e piano: year list -> year courses, plus the plan-compiler wizard. */
    @Serializable
    data object StudyPlan : SheetRoute

    /** Questionari: activities -> units -> the compilation wizard. */
    @Serializable
    data object Questionnaires : SheetRoute

    /** Appuntamenti: reservations plus the booking wizard (sections -> types -> slots -> form). */
    @Serializable
    data object Appointments : SheetRoute

    /** Biblioteca: reservations / login / the seat-booking wizard. */
    @Serializable
    data object Library : SheetRoute

    /**
     * In-app/external chooser for a tapped file. Its sheet group is resolved dynamically in
     * [MainShell]: pushed while another sheet is open it inherits that sheet's group and renders
     * as a sub-page INSIDE it; pushed from a full screen it becomes its own standalone sheet.
     */
    @Serializable
    data class FileOpenChooser(val file: AppRoute.FileViewer) : SheetRoute
}
