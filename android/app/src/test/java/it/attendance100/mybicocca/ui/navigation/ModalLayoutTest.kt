package it.attendance100.mybicocca.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import org.junit.Test

/** How the back stack splits into sheets: which pages join a sheet and when a sheet is on screen. */
class ModalLayoutTest {

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
        val nav = AppNavigator(NavBackStack<NavKey>(Destination(AppRoute.TabRoot, AppNavigator.ROOT_ID)))
        nav.navigate(SheetRoute.Appointments)
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
