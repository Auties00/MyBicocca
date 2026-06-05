package it.attendance100.mybicocca.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Title shown by the global top bar, modelled as a sealed type so "this is the tab root"
// is distinct from "this is a sub-page that happens to have no title" (e.g. the video
// player draws its own chrome). `isSubPage` is derived rather than stored — see the
// extension below.
sealed interface AppTitle {
    data object TopLevel : AppTitle
    data class SubPage(val title: String = "") : AppTitle
}

val AppRoute.isSubPage: Boolean
    get() = appTitle is AppTitle.SubPage

// The inner NavHost only knows tab + sub-pages. Auth/career-pick is handled upstream of
// MainShell via RootPhase, so Splash and Login are intentionally absent.
sealed interface AppRoute : NavKey {

    val appTitle: AppTitle

    // True when the destination draws its content edge-to-edge behind the global top bar:
    // the bar keeps a see-through background until the page publishes a runtime title, so
    // the page's own hero scrolls visibly through the bar region instead of being clipped
    // at its bottom edge.
    val extendsBehindTopBar: Boolean get() = false

    // Idle destination — the active tab content renders behind the NavHost when this entry
    // is on top of the back stack. Switching tabs pops back to here.
    @Serializable data object TabRoot : AppRoute {
        override val appTitle = AppTitle.TopLevel
    }

    // Top-level destinations reachable from the avatar / switcher.
    @Serializable data object Profile : AppRoute {
        override val appTitle = AppTitle.SubPage("Profilo")
    }
    @Serializable data object Settings : AppRoute {
        override val appTitle = AppTitle.SubPage("Impostazioni")
    }
    @Serializable data object SettingsAppearance : AppRoute {
        override val appTitle = AppTitle.SubPage("Aspetto")
    }
    @Serializable data object SettingsGeneral : AppRoute {
        override val appTitle = AppTitle.SubPage("Generale")
    }
    @Serializable data object SettingsBehaviour : AppRoute {
        override val appTitle = AppTitle.SubPage("Comportamento")
    }
    @Serializable data object SettingsSecurity : AppRoute {
        override val appTitle = AppTitle.SubPage("Sicurezza")
    }
    @Serializable data object SettingsDeveloper : AppRoute {
        override val appTitle = AppTitle.SubPage("Sviluppatore")
    }
    @Serializable data object AppInfo : AppRoute {
        override val appTitle = AppTitle.SubPage("Info App")
    }

    // Registry (Segreterie) sub-screens.
    @Serializable
    data object BookedExams : AppRoute {
        override val appTitle = AppTitle.SubPage("Esami")
    }

    @Serializable
    data object Taxes : AppRoute {
        override val appTitle = AppTitle.SubPage("Tasse")
    }
    @Serializable data class TaxDetail(val chargeId: Long) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Dettaglio Tassa")
    }

    @Serializable data object StudyPlan : AppRoute {
        override val appTitle = AppTitle.SubPage("Piano di Studi")
    }
    @Serializable data class StudyPlanEdit(
        val studentId: Long,
        val choiceRegulationId: Long,
        val schemaId: Long,
        val planId: Long,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Modifica Piano")
    }
    @Serializable data class Transcript(val careerId: Long) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Carriera")
    }
    @Serializable data object ExamResults : AppRoute {
        override val appTitle = AppTitle.SubPage("Esiti")
    }
    @Serializable data object Attendance : AppRoute {
        override val appTitle = AppTitle.SubPage("Presenze")
    }
    @Serializable data object Internships : AppRoute {
        override val appTitle = AppTitle.SubPage("Stage")
    }
    @Serializable data object Questionnaires : AppRoute {
        override val appTitle = AppTitle.SubPage("Questionari")
    }
    // Carries the full compilation target so the screen can start the server-side
    // session without re-fetching the activity's questionnaire config.
    @Serializable data class QuestionnaireCompilation(
        val activityChoiceId: Long,
        val questionnaireId: Int,
        val questionnaireConfigId: Int,
        val anonymous: Boolean,
        val tags: String,
        val activityName: String,
        val lecturerName: String? = null,
        val partitionName: String? = null,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Valutazione")
    }
    @Serializable data object DegreeAward : AppRoute {
        override val appTitle = AppTitle.SubPage("Conseguimento Titolo")
    }
    @Serializable data object SelfCertificates : AppRoute {
        override val appTitle = AppTitle.SubPage("Autocertificazioni")
    }
    @Serializable data object Reservations : AppRoute {
        override val appTitle = AppTitle.SubPage("Prenotazioni")
    }

    // Elearning sub-screens.
    // Empty: CourseDetailScreen overrides the title once the hero scrolls past.
    @Serializable data class CourseDetail(val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("")
        override val extendsBehindTopBar get() = true
    }
    @Serializable data class QuizDetail(val quizId: Int, val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Quiz")
    }
    @Serializable data class AssignmentDetail(val assignId: Int, val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Compito")
    }
    @Serializable data class ForumDetail(val forumId: Int, val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Forum")
    }
    @Serializable data class DiscussionDetail(val discussionId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Discussione")
    }
    @Serializable data object Messaging : AppRoute {
        override val appTitle = AppTitle.SubPage("Messaggi")
    }
    @Serializable data class ConversationDetail(val conversationId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Conversazione")
    }
    // Player draws its own chrome — global top bar fades out, but the title carries the
    // video name during the entry morph.
    @Serializable data class VideoPlayback(
        val courseId: Int,
        val cmId: Int,
        val title: String,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage(title)
    }

    // Map sub-screens.
    @Serializable data class Room360View(val url: String, val roomName: String) : AppRoute {
        override val appTitle get() = AppTitle.SubPage(roomName)
    }

    // Cross-tab detail screens.
    @Serializable data class TeacherDetail(val teacherCode: String) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Docente")
    }
}
