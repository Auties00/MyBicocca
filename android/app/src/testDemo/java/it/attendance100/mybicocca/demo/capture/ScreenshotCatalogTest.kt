package it.attendance100.mybicocca.demo.capture

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.data.local.map.BuildingCatalogFile
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.demo.repository.DemoCalendarRepository
import it.attendance100.mybicocca.demo.repository.DemoDocumentRepository
import it.attendance100.mybicocca.demo.repository.DemoEnrollmentRepository
import it.attendance100.mybicocca.demo.repository.DemoTaxRepository
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import java.time.YearMonth

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

    @Test
    fun `every route target and building exists in demo data`() = runBlocking {
        val buildings = buildingJson.decodeFromString<BuildingCatalogFile>(
            File("src/main/assets/buildings.json").readText(),
        ).buildings.map { it.code }.toSet()
        val calendarEvents = (
            DemoCalendarRepository()
                .observeMonth(DemoStudent.careerId, YearMonth.from(DemoClock.now))
                .first() as Loadable.Loaded
            ).value.map { it.id.value }.toSet()
        val enrollmentIds = DemoEnrollmentRepository().getHistory(DemoStudent.careerId).years.map { it.id.value }.toSet()
        val titleIds = DemoDocumentRepository().getTitles(DemoStudent.careerId).map { it.id }.toSet()
        val taxRepository = DemoTaxRepository()
        val iseeYears = taxRepository.getIseeDeclarations(DemoStudent.careerId).map { it.academicYearEnrollmentId }.toSet()
        val refundKeys = taxRepository.getRefunds(DemoStudent.careerId).map { it.invoiceId }.toSet()
        val demoFiles = DemoCourses.all
            .flatMap { DemoElearning.details(it).sections }
            .flatMap { it.modules }
            .flatMap { it.contents }

        val invalid = buildList {
            for (shot in catalog.shots) {
                if (shot.building != null && shot.building !in buildings) {
                    add("${shot.name}: unknown building ${shot.building}")
                }
                for (route in shot.routes) {
                    val valid = when (route) {
                        is AppRoute.CourseDetail -> DemoElearning.course(CourseId(route.courseId)) != null
                        is AppRoute.FileViewer -> demoFiles.any { it.fileName == route.fileName && it.fileUrl == route.fileUrl }
                        is AppRoute.VideoPlayback -> DemoElearning.course(CourseId(route.courseId)) != null
                        is AppRoute.TeacherDetail -> route.teacherCode.isNotBlank()
                        is SheetRoute.CalendarEvent -> route.eventId in calendarEvents
                        is SheetRoute.CourseEditionPicker -> DemoCourses.all.any { it.code == route.activityCode }
                        is SheetRoute.QuizDetail ->
                            DemoElearning.course(CourseId(route.courseId))?.let { DemoElearning.quizId(it) == route.quizId } == true
                        is SheetRoute.AssignmentDetail ->
                            DemoElearning.allAssignments.any { it.id == route.assignId && it.course.moodleCourseId == route.courseId }
                        is SheetRoute.Forum ->
                            DemoElearning.course(CourseId(route.courseId))?.let { DemoElearning.forumId(it) == route.forumId } == true
                        is SheetRoute.EnrollmentDetail -> route.enrollmentId in enrollmentIds
                        is SheetRoute.TitleDetail -> route.titleId in titleIds
                        is SheetRoute.IseeDetail -> route.year in iseeYears
                        is SheetRoute.RefundDetail -> route.refundKey in refundKeys
                        is SheetRoute.FileOpenChooser -> demoFiles.any { it.fileName == route.file.fileName && it.fileUrl == route.file.fileUrl }
                        is SheetRoute.OfficeOpen -> demoFiles.any { it.fileName == route.file.fileName && it.fileUrl == route.file.fileUrl }
                        is SheetRoute.FileAssociationChooser -> route.preferenceKey.isNotBlank()
                        is SheetRoute.HypotheticalGrade, is SheetRoute.ExamsByYear -> true
                        else -> route.javaClass.declaredFields.any { it.name == "INSTANCE" }
                    }
                    if (!valid) add("${shot.name}: $route")
                }
            }
        }

        assertWithMessage("Shots referencing data absent from the demo build").that(invalid).isEmpty()
    }

    private fun binaryName(type: String): String = ScreenshotCatalog.ROUTE_PACKAGE + type.replace('.', '$')

    private companion object {
        val STRING_NAME = Regex("""<string name="([^"]+)"""")
        val buildingJson = Json { ignoreUnknownKeys = true }
    }

    // Sealed interfaces nest (SheetRoute.InSheetPage), so walk down to the classes that can be instantiated.
    private fun concreteRoutes(type: Class<out Route>): List<Class<*>> =
        type.permittedSubclasses.orEmpty().flatMap { sub ->
            @Suppress("UNCHECKED_CAST")
            if (sub.isInterface) concreteRoutes(sub as Class<out Route>) else listOf(sub)
        }
}
