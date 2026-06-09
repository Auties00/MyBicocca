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

    // Elearning sub-screens.
    // Empty: CourseDetailScreen overrides the title once the hero scrolls past.
    @Serializable data class CourseDetail(val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("")
        override val extendsBehindTopBar get() = true
    }
    // The overview is a modal sheet (QuizDetailSheet); this route is the full-screen attempt /
    // review, entered with exactly one action set so its view model knows what to open.
    @Serializable data class QuizDetail(
        val quizId: Int,
        val courseId: Int,
        val startNew: Boolean = false,
        val resumeAttemptId: Int? = null,
        val reviewAttemptId: Int? = null,
    ) : AppRoute {
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
    // In-app viewer for course files. Carries either a remote pluginfile URL (normal case,
    // downloaded on open) or an already-local path (a file extracted from a zip archive).
    @Serializable data class FileViewer(
        val fileName: String,
        val fileUrl: String? = null,
        val localPath: String? = null,
        val mimeType: String? = null,
        val sizeBytes: Long? = null,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage(fileName)
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
