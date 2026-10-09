package it.attendance100.mybicocca.demo.capture

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import org.junit.Test
import java.io.File

/**
 * Keeps `screenshots.json` in step with the app's navigation: adding a route without giving it a
 * shot or an exemption fails here, as does a shot naming a route that no longer exists.
 */
class ScreenshotCatalogTest {

    private val catalog = ScreenshotCatalog.parse(File("src/demo/assets/${ScreenshotCatalog.ASSET}").readText())

    @Test
    fun `every route has a shot or an exemption`() {
        val shown = catalog.shots.flatMap { it.routes }.map { it::class.java }.toSet()
        val exempt = catalog.exempt.map { Class.forName(binaryName(it.type)) }.toSet()

        val missing = (concreteRoutes(AppRoute::class.java) + concreteRoutes(SheetRoute::class.java))
            .filterNot { it in shown || it in exempt }
            .map { it.name.removePrefix(ScreenshotCatalog.ROUTE_PACKAGE).replace('$', '.') }

        assertWithMessage("Routes with no shot and no exemption in screenshots.json").that(missing).isEmpty()
    }

    @Test
    fun `every tab has a shot of its own`() {
        val tabsShown = catalog.shots.filter { it.routes.isEmpty() && it.tap.isEmpty() }.map { it.tab }
        assertThat(tabsShown).containsAtLeastElementsIn(ShellTab.entries)
    }

    @Test
    fun `shot names are unique`() {
        assertThat(catalog.shots.map { it.name }).containsNoDuplicates()
    }

    @Test
    fun `no route is both shown and exempt`() {
        val shown = catalog.shots.flatMap { it.routes }.map { it::class.java.name }.toSet()
        val both = catalog.exempt.map { binaryName(it.type) }.filter { it in shown }
        assertThat(both).isEmpty()
    }

    @Test
    fun `every tap names a string resource`() {
        val strings = File("src/main/res/values").listFiles { file -> file.name.startsWith("strings") }.orEmpty()
            .flatMap { STRING_NAME.findAll(it.readText()).map { match -> match.groupValues[1] } }
            .toSet()
        val unknown = catalog.shots.flatMap { it.tap }.filterNot { it in strings }
        assertWithMessage("Tap steps that are not string resource names").that(unknown).isEmpty()
    }

    private fun binaryName(type: String): String = ScreenshotCatalog.ROUTE_PACKAGE + type.replace('.', '$')

    private companion object {
        val STRING_NAME = Regex("""<string name="([^"]+)"""")
    }

    // Sealed interfaces nest (SheetRoute.InSheetPage), so walk down to the classes that can be instantiated.
    private fun concreteRoutes(type: Class<out Route>): List<Class<*>> =
        type.permittedSubclasses.orEmpty().flatMap { sub ->
            @Suppress("UNCHECKED_CAST")
            if (sub.isInterface) concreteRoutes(sub as Class<out Route>) else listOf(sub)
        }
}
