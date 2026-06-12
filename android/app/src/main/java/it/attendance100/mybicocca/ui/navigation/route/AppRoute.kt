package it.attendance100.mybicocca.ui.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Title shown by the global top bar, modelled as a sealed type so "this is the tab root" stays
 * distinct from "this is a sub-page that happens to have no title" (e.g. the video player draws
 * its own chrome). A route's sub-page-ness is derived from it via [isSubPage] rather than stored.
 */
sealed interface AppTitle {
    data object TopLevel : AppTitle
    data class SubPage(val title: String = "") : AppTitle
}

val AppRoute.isSubPage: Boolean
    get() = appTitle is AppTitle.SubPage

/**
 * Full-screen destinations of [MainShell]'s inner NavDisplay: the tab root plus the sub-pages
 * pushed over it. Auth and career pick are handled upstream of the shell via [RootPhase], so
 * splash and login routes are intentionally absent.
 */
sealed interface AppRoute : NavKey {

    val appTitle: AppTitle

    /**
     * True when the destination draws its content edge-to-edge behind the global top bar: the
     * bar keeps a see-through background until the page publishes a runtime title, so the page's
     * own hero scrolls visibly through the bar region instead of being clipped at its bottom edge.
     */
    val extendsBehindTopBar: Boolean get() = false

    /**
     * Idle destination — the active tab content renders behind the NavDisplay while this entry
     * is on top of the back stack. Switching tabs pops back to here.
     */
    @Serializable data object TabRoot : AppRoute {
        override val appTitle = AppTitle.TopLevel
    }

    /** Profile page (with the libretto), reached from the avatar / account switcher. */
    @Serializable data object Profile : AppRoute {
        override val appTitle = AppTitle.SubPage("Profilo") // TODO localize
    }

    /** Settings page, reached from the account switcher; every section lives inline on it. */
    @Serializable data object Settings : AppRoute {
        override val appTitle = AppTitle.SubPage("Impostazioni") // TODO localize
    }

    /**
     * Elearning course detail. The static title is empty on purpose: the page extends behind the
     * see-through top bar and overrides the title at runtime once its hero headline scrolls past.
     */
    @Serializable data class CourseDetail(val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("")
        override val extendsBehindTopBar get() = true
    }
    @Serializable data class AssignmentDetail(val assignId: Int, val courseId: Int) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Compito") // TODO localize
    }
    /**
     * In-app viewer for course files. Carries either a remote pluginfile URL (the common case,
     * downloaded on open) or an already-local path (e.g. a file extracted from a zip archive).
     */
    @Serializable data class FileViewer(
        val fileName: String,
        val fileUrl: String? = null,
        val localPath: String? = null,
        val mimeType: String? = null,
        val sizeBytes: Long? = null,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage(fileName)
    }

    /**
     * Immersive video player. It draws its own chrome — the global top bar fades out — but the
     * title still carries the video name so the bar reads correctly during the entry morph.
     */
    @Serializable data class VideoPlayback(
        val courseId: Int,
        val cmId: Int,
        val title: String,
    ) : AppRoute {
        override val appTitle get() = AppTitle.SubPage(title)
    }

    /** Teacher detail page, reachable from multiple tabs. */
    @Serializable data class TeacherDetail(val teacherCode: String) : AppRoute {
        override val appTitle get() = AppTitle.SubPage("Docente") // TODO localize
    }
}
