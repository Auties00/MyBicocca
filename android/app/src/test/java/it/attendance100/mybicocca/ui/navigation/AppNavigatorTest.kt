package it.attendance100.mybicocca.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.scene.ModalLayout
import org.junit.Test

/**
 * The navigator's guarantees that the modal architecture relies on: unique entry identities,
 * single-top pushes, id-addressed idempotent pops and a root that can never be removed — the
 * properties that rule out the duplicate-key crash (#44) and the empty-stack race (#22).
 */
class AppNavigatorTest {

    private fun navigator(vararg routes: Route): AppNavigator {
        val stack = NavBackStack<NavKey>(Destination(AppRoute.TabRoot, AppNavigator.ROOT_ID))
        return AppNavigator(stack).apply { routes.forEach { navigate(it) } }
    }

    private val AppNavigator.routes get() = entries.map { it.route }

    @Test
    fun `every push gets a unique id even for equal routes`() {
        val nav = navigator(SheetRoute.Isee)
        nav.back()
        nav.navigate(SheetRoute.Isee)
        val ids = nav.entries.map { it.id }
        assertThat(ids).containsNoDuplicates()

        nav.navigate(AppRoute.CourseDetail(1))
        nav.navigate(SheetRoute.Isee)
        assertThat(nav.entries.map { it.id }).containsNoDuplicates()
    }

    @Test
    fun `navigate is single top`() {
        val nav = navigator()
        assertThat(nav.navigate(SheetRoute.Isee)).isTrue()
        assertThat(nav.navigate(SheetRoute.Isee)).isFalse()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot, SheetRoute.Isee).inOrder()
    }

    @Test
    fun `back never removes the root`() {
        val nav = navigator(AppRoute.Profile)
        assertThat(nav.back()).isTrue()
        assertThat(nav.back()).isFalse()
        assertThat(nav.back()).isFalse()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot)
    }

    @Test
    fun `pop by id is idempotent and never over-pops`() {
        val nav = navigator(AppRoute.CourseDetail(1), SheetRoute.Isee, SheetRoute.IseeDetail(2024))
        val sheetRoot = nav.entries[2].id

        // A sheet reporting its dismissal twice (e.g. from two Material dismiss paths).
        assertThat(nav.pop(sheetRoot)).isTrue()
        assertThat(nav.pop(sheetRoot)).isFalse()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot, AppRoute.CourseDetail(1)).inOrder()
    }

    @Test
    fun `pop of the root id is refused`() {
        val nav = navigator(SheetRoute.Isee)
        assertThat(nav.pop(AppNavigator.ROOT_ID)).isFalse()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot, SheetRoute.Isee).inOrder()
    }

    @Test
    fun `currentPage skips sheets`() {
        val nav = navigator(AppRoute.CourseDetail(7), SheetRoute.QuizDetail(1, 7))
        assertThat(nav.currentPage.route).isEqualTo(AppRoute.CourseDetail(7))
        assertThat(nav.top.route).isEqualTo(SheetRoute.QuizDetail(1, 7))
    }

    @Test
    fun `dismissSheetOf closes the whole sheet a page belongs to`() {
        val nav = navigator(SheetRoute.CalendarEvent("e1"), SheetRoute.CourseEditionPicker("ABC"))
        val picker = nav.top.id
        assertThat(nav.dismissSheetOf(picker)).isTrue()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot)
    }

    @Test
    fun `popToRoot clears everything above the root`() {
        val nav = navigator(AppRoute.Profile, SheetRoute.Appelli)
        nav.popToRoot()
        assertThat(nav.routes).containsExactly(AppRoute.TabRoot)
    }

    @Test
    fun `in-sheet pages join the sheet beneath them`() {
        val routes = listOf<Route>(
            AppRoute.TabRoot,
            SheetRoute.Isee,
            SheetRoute.IseeDetail(2024),
            SheetRoute.FileOpenChooser(AppRoute.FileViewer("a.pdf")),
        )
        assertThat(ModalLayout.sheetStart(routes, 3)).isEqualTo(1)
        assertThat(ModalLayout.sheetRange(routes, 1)).isEqualTo(1..3)
    }

    @Test
    fun `a regular sheet route stacks a new sheet`() {
        val routes = listOf<Route>(
            AppRoute.TabRoot,
            SheetRoute.CalendarEvent("e1"),
            SheetRoute.Appelli,
        )
        assertThat(ModalLayout.sheetStart(routes, 2)).isEqualTo(2)
        assertThat(ModalLayout.sheetRange(routes, 1)).isEqualTo(1..1)
    }

    @Test
    fun `an in-sheet page pushed over a full-screen page opens its own sheet`() {
        val routes = listOf<Route>(
            AppRoute.TabRoot,
            AppRoute.CourseDetail(1),
            SheetRoute.FileOpenChooser(AppRoute.FileViewer("a.pdf")),
        )
        assertThat(ModalLayout.sheetStart(routes, 2)).isEqualTo(2)
    }

    @Test
    fun `a sheet is shown only while nothing full-screen covers it`() {
        val nav = navigator(SheetRoute.Appointments)
        val sheet = nav.top.id
        assertThat(ModalLayout.isSheetShown(nav.entries, sheet)).isTrue()

        nav.navigate(SheetRoute.FileOpenChooser(AppRoute.FileViewer("a.pdf")))
        assertThat(ModalLayout.isSheetShown(nav.entries, sheet)).isTrue()

        nav.navigate(AppRoute.FileViewer("a.pdf"))
        assertThat(ModalLayout.isSheetShown(nav.entries, sheet)).isFalse()

        nav.back()
        assertThat(ModalLayout.isSheetShown(nav.entries, sheet)).isTrue()
    }
}
