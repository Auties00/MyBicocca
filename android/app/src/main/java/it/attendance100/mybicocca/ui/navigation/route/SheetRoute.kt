package it.attendance100.mybicocca.ui.navigation.route

import it.attendance100.mybicocca.ui.component.file.OfficeApp
import it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear.ExamValueMode
import kotlinx.serialization.Serializable

/**
 * Back-stack keys for modal bottom-sheet pages. Unlike [AppRoute] these are NOT full-screen
 * sub-pages: [ModalSceneStrategy] renders them in a modal sheet floating over the page beneath.
 *
 * Which sheet a page lands in is decided by the route itself, so it holds wherever the page is
 * pushed from:
 * - a regular sheet route opens a NEW sheet — stacked above any sheet already open;
 * - a route with [joinsParentSheet] is an in-sheet page: pushed while a sheet is on top it becomes
 *   the next page INSIDE that sheet (morphing the sheet rather than opening a window above it);
 *   pushed from a full-screen page it opens as its own sheet.
 */
@Serializable
sealed interface SheetRoute : Route {

    /** Whether this page continues the sheet beneath it instead of opening a new one. */
    val joinsParentSheet: Boolean get() = false

    /** Marker for in-sheet pages ([joinsParentSheet] = true). */
    @Serializable
    sealed interface InSheetPage : SheetRoute {
        override val joinsParentSheet: Boolean get() = true
    }

    /** Iscrizioni: the annual-enrollment timeline page. */
    @Serializable
    data object Enrollments : SheetRoute

    /** One enrollment year's detail, an in-sheet page over [Enrollments]. */
    @Serializable
    data class EnrollmentDetail(val enrollmentId: Long) : InSheetPage

    /** Titoli: the titles list page. */
    @Serializable
    data object Titles : SheetRoute

    /** One title's detail, an in-sheet page over [Titles]. */
    @Serializable
    data class TitleDetail(val titleId: String) : InSheetPage

    /** Certificati: a single list page, with an in-page download outcome. */
    @Serializable
    data object Certificates : SheetRoute

    /** Rimborsi: the refunds list page. */
    @Serializable
    data object Refunds : SheetRoute

    /** One refund's detail, an in-sheet page over [Refunds]. */
    @Serializable
    data class RefundDetail(val refundKey: Long) : InSheetPage

    /** ISEE: the declarations list page. */
    @Serializable
    data object Isee : SheetRoute

    /** One academic year's ISEE declaration, an in-sheet page over [Isee]. */
    @Serializable
    data class IseeDetail(val year: Long) : InSheetPage

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
     * In-app/external chooser for a tapped file: pushed while a sheet is open it renders as a page
     * INSIDE that sheet; pushed from a full screen it becomes its own standalone sheet.
     */
    @Serializable
    data class FileOpenChooser(val file: AppRoute.FileViewer) : InSheetPage

    /** One calendar event in full (calendar tab, search hits). */
    @Serializable
    data class CalendarEvent(val eventId: String) : SheetRoute

    /**
     * Which e-learning edition "Apri corso" opens when an event's activity code matches several:
     * a page inside the event sheet, or its own sheet from the month agenda.
     */
    @Serializable
    data class CourseEditionPicker(val activityCode: String) : InSheetPage

    /** Account switcher (avatar in the top bar): accounts list plus the add-account sign-in. */
    @Serializable
    data object AccountSwitcher : SheetRoute

    /** Office hand-off for a tapped Word/Excel/PowerPoint file: open in the Microsoft app. */
    @Serializable
    data class OfficeOpen(val app: OfficeApp, val file: AppRoute.FileViewer) : SheetRoute

    /** Scadenzario: upcoming registry deadlines (Segreterie tab). */
    @Serializable
    data object Deadlines : SheetRoute

    /** Hypothetical average calculator (Profilo), [weighted] picking the average it projects. */
    @Serializable
    data class HypotheticalGrade(val weighted: Boolean) : SheetRoute

    /** Passed exams by year (Profilo), as grades or as credits. */
    @Serializable
    data class ExamsByYear(val mode: ExamValueMode) : SheetRoute

    /** Map building filter (top-bar filter toggle on the Mappa tab). */
    @Serializable
    data object MapFilter : SheetRoute

    /** E-learning catalog: browse and enrol in a course. */
    @Serializable
    data object AddCourse : SheetRoute

    /** Settings: theme and appearance. */
    @Serializable
    data object SettingsAppearance : SheetRoute

    /** Settings: app language. */
    @Serializable
    data object SettingsLanguage : SheetRoute

    /** Settings: haptic feedback. */
    @Serializable
    data object SettingsHaptic : SheetRoute

    /** Settings: app lock and biometrics. */
    @Serializable
    data object SettingsSecurity : SheetRoute

    /** Settings: remembered in-app/external choice per file kind. */
    @Serializable
    data object FileAssociations : SheetRoute

    /** Edits the remembered open choice of one file kind (by its preference key). */
    @Serializable
    data class FileAssociationChooser(val preferenceKey: String) : InSheetPage

    /** App information, release notes and update channel settings. */
    @Serializable
    data object AppInfo : SheetRoute
}
