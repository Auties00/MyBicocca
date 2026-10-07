package it.attendance100.mybicocca.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.data.mapper.calendar.examCalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.component.bar.BottomBarItem
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaBottomBar
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaTopBar
import it.attendance100.mybicocca.ui.component.bar.TopBarSearchState
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarHost
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.rememberAppSnackbarController
import it.attendance100.mybicocca.ui.component.file.FileKind
import it.attendance100.mybicocca.ui.component.file.OfficeApp
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.navigation.route.AppRoute
import it.attendance100.mybicocca.ui.navigation.route.AppTitle
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import it.attendance100.mybicocca.ui.navigation.route.isSubPage
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.navigation.scene.SheetDismissControl
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeader
import it.attendance100.mybicocca.ui.navigation.scene.sheetHeaderInPage
import it.attendance100.mybicocca.ui.navigation.scene.LocalModalEntries
import it.attendance100.mybicocca.ui.navigation.scene.ModalSceneStrategy
import it.attendance100.mybicocca.ui.navigation.transitions.LocalAnimatedContentScope
import it.attendance100.mybicocca.ui.navigation.transitions.LocalSharedTransitionScope
import it.attendance100.mybicocca.ui.navigation.transitions.defaultEnterTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultExitTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultPopEnterTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultPopExitTransition
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.state.AccountEvent
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherPage
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherSheetStyle
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.CourseEditionPickerPage
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailPage
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.eventDetailHeader
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker.courseEditionPickerHeader
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.officeOpenHeader
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.fileOpenChooserHeader
import it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter.mapFilterHeader
import it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter.MapFilterPage
import it.attendance100.mybicocca.ui.navigation.scene.sheetStyle
import it.attendance100.mybicocca.ui.screen.calendar.CalendarScreen
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewModel
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.teacherDetail.TeacherDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCourseViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.ExternalFileLauncher
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.OfficeOpenPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.FileOpenChooserContent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.ForumSheetPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerViewModel
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.map.MapViewModel
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.profile.ProfileViewModel
import it.attendance100.mybicocca.ui.screen.registry.RegistryScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.AppelliPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendancePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendanceViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.EnrollmentsTimelinePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.EnrollmentsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.enrollmentsHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.academicYearLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.courseYearLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.statusLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.subscreen.yearDetail.EnrollmentDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeDeclarationsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeDetailSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeDetailTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundsListPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.RefundsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundHeaderTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundKey
import it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds.refundsHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxesHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitleDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesListPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.headline
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.headlineSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.titlesHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.search.SearchOverlay
import it.attendance100.mybicocca.ui.screen.search.SearchViewModel
import it.attendance100.mybicocca.ui.screen.settings.SettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.settingsSheetEntries
import it.attendance100.mybicocca.ui.screen.profile.profileSheetEntries
import it.attendance100.mybicocca.ui.screen.registry.registrySheetEntries
import it.attendance100.mybicocca.ui.screen.elearning.elearningSheetEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * The signed-in shell: a Scaffold whose global chrome (morphing top bar, bottom tab bar, snackbar
 * host) frames one Navigation3 NavDisplay. [AppRoute.TabRoot] is always the root entry and hosts
 * the four-tab pager; full-screen sub-pages push over it, and modal sheets ([SheetRoute]) ride the
 * SAME back stack as overlay scenes via [BottomSheetSceneStrategy], floating over the current
 * page. Hosting the pager INSIDE the TabRoot entry puts a list ticket and its detail page in the
 * same NavDisplay AnimatedContent, which is what makes list-to-detail shared-element morphs seek
 * with the predictive-back gesture; a SharedTransitionLayout around the NavDisplay provides the
 * scope.
 *
 * Tab selection always pops the sub-stack back to TabRoot first (switching — or re-tapping — a
 * tab must never land deep on a stale sub-page) and then jumps the pager without scrolling
 * through intermediate pages: all tabs stay composed, so the jump is a cheap show/hide. Settling
 * on a different tab resets search and filter state. Two independent fractions drive the chrome
 * morph — sub-page cover and search-field expansion — documented on `navProgress` below.
 * Immersive destinations (video playback, file viewer) hide the global chrome entirely and draw
 * their own.
 *
 * The full-screen search overlay is drawn after (over) the NavDisplay but under the Scaffold's
 * top bar, so the bar's search field stays interactive above it; it rides the search fraction and
 * is only composed while open or animating. Opening a hit commits the query and pick to the
 * adaptive search memory, then plays the resulting [SearchNavStep] plan one step per beat so the
 * user can watch the route unfold; plans made purely of page pushes keep the search overlay alive
 * underneath (popping back restores query, results and scroll), while plans that switch tab or
 * open a sheet close it.
 *
 * Tab and sheet ViewModels are hoisted at shell level so a sheet's pages share one owner that
 * outlives the sheet and eager fetches start on shell load; sheet detail pages resolve their item
 * from the live ViewModel stream against the top back-stack key, so an item evicted underneath an
 * open detail (e.g. by a career switch) collapses the header and pops the page back to its list
 * instead of rendering a stale snapshot.
 *
 * External entry points land here as well: an Affluences confirm/cancel email link opens the
 * Biblioteca sheet so its snackbar can report the outcome, a mod_attendance QR scanned outside
 * the app opens the Presenze sheet to run the marking flow, and a libretto course deep-links into
 * exam booking by arming a focus request on the bookable-exams ViewModel, landing on the Servizi
 * tab and opening the Appelli sheet over it — the sheet then enters its booking flow on the
 * pending focus and scrolls to that exam's section.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
    adminMessageViewModel: it.attendance100.mybicocca.ui.screen.admin.AdminMessageViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner" }
    )
) {
    val strShellSessionExpired = stringResource(R.string.shell_session_expired)
    val strShellUpdateAvailable = stringResource(R.string.shell_update_available)
    val strShellAccountRemoved = stringResource(R.string.shell_account_removed)
    val strShellCareerMissing = stringResource(R.string.shell_career_missing)
    val strShellNewCareerAvailable = stringResource(R.string.shell_new_career_available)
    val strShellCareerEnded = stringResource(R.string.shell_career_ended)
    val strShellSignOutFailed = stringResource(R.string.shell_signout_failed)

    /**
     * Source of truth for the selected tab. One pager hosts all four tabs and keeps them composed
     * (beyondViewportPageCount), so switching is instant; user swipe is disabled because Registry
     * hosts its own pager and the map pans horizontally, leaving the bottom bar as the only page
     * driver. The state lives here in the shell body (NOT inside the TabRoot entry) so it
     * survives the entry being disposed and recomposed while a sub-page is on top.
     */
    val pagerState = rememberPagerState(
        initialPage = ShellTab.Calendar.ordinal,
        pageCount = { ShellTab.entries.size },
    )
    val scope = rememberCoroutineScope()

    /**
     * The one back stack of the signed-in shell — full-screen pages, modal sheets and their
     * in-sheet pages — and the only way to change it. See [AppNavigator].
     */
    val navigator = rememberAppNavigator()

    val tab = ShellTab.entries[pagerState.currentPage]
    val photo by accountViewModel.userPhoto.collectAsStateWithLifecycle()

    val adminMessage by adminMessageViewModel.message.collectAsStateWithLifecycle()
    adminMessage?.let { msg ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { adminMessageViewModel.dismiss(msg.id) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { adminMessageViewModel.dismiss(msg.id) }) {
                    androidx.compose.material3.Text("OK")
                }
            },
            title = { androidx.compose.material3.Text(msg.title) },
            text = { androidx.compose.material3.Text(msg.message) }
        )
    }

    /**
     * Every stored account's avatar, observed to warm Coil's cache as soon as the shell loads so
     * the account switcher renders photos with no placeholder flash.
     */
    val accountPhotos by accountViewModel.photos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(accountPhotos) {
        accountPhotos.values.forEach { file ->
            if (file != null) {
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(file)
                        .size(Size.ORIGINAL)
                        .build(),
                )
            }
        }
    }

    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val elearningViewModel: ElearningViewModel = hiltViewModel()
    val addCourseViewModel: AddCourseViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val bookedExamsViewModel: BookedExamsViewModel = hiltViewModel()
    val bookableExamsViewModel: BookableExamsViewModel = hiltViewModel()

    /**
     * Hoisted so the Segreterie landing can derive its status badges and the scadenzario
     * deadline spine from the exam outcomes, and the Esiti sub-page shares the same fetch.
     */
    val examResultsViewModel: ExamResultsViewModel = hiltViewModel()

    /**
     * Hoisted so the tax fetch starts on shell load and the list / detail / ISEE destinations
     * share one in-memory result (taxes are not cached to Room).
     */
    val taxesViewModel: TaxesViewModel = hiltViewModel()

    /**
     * Hoisted so the compilation sub-page can refresh the questionnaire list after a confirmed
     * submission (questionnaires are not cached to Room).
     */
    val questionnairesViewModel: QuestionnairesViewModel = hiltViewModel()

    /**
     * Hoisted so the whole Appuntamenti modal (reservations + booking wizard) shares one owner;
     * opened as a shell sheet rather than a back-stack route.
     */
    val appointmentsViewModel: AppointmentsViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()

    /**
     * Seat totals per exam event, merged off the main thread from the bookable calls, the
     * bookings' persisted numIscritti and fresh lazy fetches (in increasing precedence);
     * distinctUntilChanged keeps the instance stable when nothing changed. Shared by the calendar
     * tab and the event sheet.
     */
    val examBookingTotalsFlow = remember(bookableExamsViewModel, bookedExamsViewModel) {
        combine(
            bookableExamsViewModel.examCalls,
            bookedExamsViewModel.bookings,
            bookedExamsViewModel.callTotals,
        ) { calls, bookings, lazyTotals ->
            buildMap {
                calls.valueOrNull().orEmpty().forEach { call ->
                    call.enrolledNumber?.let { put(examCalendarEventId(call.key), it) }
                }
                bookings.valueOrNull().orEmpty().forEach { booking ->
                    booking.totalBookings?.let { put(examCalendarEventId(booking.key), it) }
                }
                lazyTotals.forEach { (key, total) -> put(examCalendarEventId(key), total) }
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }
    val examBookingTotals by examBookingTotalsFlow.collectAsStateWithLifecycle(
        initialValue = emptyMap(),
    )

    /** An exam event's detail became visible: lazily fetch its call's live seat total. */
    val onExamEventShown: (CalendarEvent.Exam) -> Unit = { examEvent ->
        bookedExamsViewModel.bookings.value.valueOrNull()
            .orEmpty()
            .firstOrNull { examCalendarEventId(it.key) == examEvent.id }
            ?.let(bookedExamsViewModel::loadTotalBookings)
    }

    /**
     * "Vai alla prenotazione" on a calendar event: the managing sheet (appelli / appuntamenti /
     * biblioteca) opens as a sheet STACKED over the still-open event sheet, so dismissing it lands
     * back on the event.
     */
    val openReservation: (CalendarEvent) -> Unit = { event ->
        when (event) {
            is CalendarEvent.Exam -> navigator.navigate(SheetRoute.Appelli)
            is CalendarEvent.Appointment -> navigator.navigate(SheetRoute.Appointments)
            is CalendarEvent.LibraryReservation -> navigator.navigate(SheetRoute.Library)
            else -> Unit
        }
    }

    /**
     * Hoisted so the sheet entries share one ViewModel that outlives the sheet, like the other
     * shell-scoped sheet ViewModels.
     */
    val enrollmentsViewModel: EnrollmentsViewModel = hiltViewModel()
    val titlesViewModel: TitlesViewModel = hiltViewModel()
    val certificatesViewModel: CertificatesViewModel = hiltViewModel()
    val refundsViewModel: RefundsViewModel = hiltViewModel()
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val studyPlanViewModel: StudyPlanViewModel = hiltViewModel()

    /**
     * Hoisted so the transcript refresh (kicked off in the ViewModel's init) starts on shell
     * load, not when the Profile sub-page is first opened — the stats/badge are already warm.
     */
    val profileViewModel: ProfileViewModel = hiltViewModel()

    /**
     * Unified search: one ViewModel feeds both the bar's text field and the full-screen overlay
     * body, so it is hoisted at shell level like the tab ViewModels.
     */
    val searchViewModel: SearchViewModel = hiltViewModel()

    /**
     * The topmost full-screen destination, not the top of the stack: a modal sheet
     * ([SheetRoute]) rides the same stack as an overlay floating OVER its page, so the page
     * underneath is still the current destination. Reading the last entry blindly would flip
     * this whenever a sheet opens, dropping the page's title / actions / back arrow from
     * the chrome (which sits dimmed behind the sheet) and animating them away.
     */
    val currentRoute = navigator.currentPage.route as AppRoute

    /** Renders sheet routes as modal overlay scenes; remembered so scenes stay stable. */
    val sceneStrategies = remember(navigator) {
        listOf(ModalSceneStrategy(navigator), SinglePaneSceneStrategy())
    }

    val presenceDeepLinkViewModel: PresenceDeepLinkViewModel = hiltViewModel()
    val pendingPresenceScan by presenceDeepLinkViewModel.pending.collectAsStateWithLifecycle()

    val isOnSubPage = currentRoute?.isSubPage == true
    val subPageTitle = (currentRoute?.appTitle as? AppTitle.SubPage)?.title

    /**
     * Video playback and the file viewer are immersive: the global chrome is hidden and the page
     * goes edge to edge (the file viewer draws its own Custom-Tab-style top bar).
     */
    val immersive = currentRoute is AppRoute.VideoPlayback || currentRoute is AppRoute.FileViewer

    val motion = MaterialTheme.motionScheme
    val enterTransition = remember(motion) { defaultEnterTransition(motion) }
    val exitTransition = remember(motion) { defaultExitTransition(motion) }
    val popEnterTransition = remember(motion) { defaultPopEnterTransition(motion) }
    val popExitTransition = remember(motion) { defaultPopExitTransition(motion) }

    /**
     * How far a sub-page covers the tab root (0 = on a tab, 1 = sub-page on top); one of the two
     * independent drivers of the chrome morph, which consumers that must react to either cover —
     * the bars, the calendar's popup chrome — combine with the search fraction as max(). It is
     * driven by the NavDisplay's OWN TabRoot<->sub-page transition (published from the TabRoot
     * entry via animateFloat on that entry's transition), so the bar expand and the bottom-bar
     * slide-off seek in lockstep with the page slide — including while the predictive-back
     * gesture is scrubbing it, which a commit-time spring could never track. Seeded from the
     * restored back stack: after an activity recreation (process death, or a config change not
     * declared in the manifest, e.g. fontScale/density) the stack can come back with a sub-page
     * already on top and NO transition — the TabRoot entry (which publishes this fraction) never
     * composes, so a 0f initial would leave the bar collapsed on a sub-page.
     */
    val navProgress = remember { mutableFloatStateOf(if (isOnSubPage) 1f else 0f) }

    /**
     * The search field open/close fraction, scrubbed by the bar's own predictive-back handler.
     * Search is page-only, so this and [navProgress] never both drive the morph at the same time.
     */
    val searchProgress = remember { Animatable(0f) }



    /**
     * External hand-off (download + ACTION_VIEW): PDFs go to the default reader, Office to the
     * installed app, and any file the user chose to open externally.
     */
    var externalFile by remember { mutableStateOf<AppRoute.FileViewer?>(null) }

    LaunchedEffect(Unit) {
        libraryViewModel.openSheetRequests.collect {
            navigator.navigate(SheetRoute.Library)
        }
    }
    LaunchedEffect(pendingPresenceScan) {
        if (pendingPresenceScan != null) navigator.navigate(SheetRoute.Attendance)
    }

    val fileOpenViewModel: FileOpenPreferenceViewModel = hiltViewModel()
    val fileOpenChoices by fileOpenViewModel.choices.collectAsStateWithLifecycle()

    /**
     * Decides how a tapped file opens, including files re-dispatched from inside another viewer
     * (e.g. zip entries). In-app-capable kinds honour a remembered choice or, when none (or on a
     * long-press force), show the chooser — a back-stack sheet page that joins an already-open
     * sheet as a sub-page, or opens as its own sheet from a full screen. Unknown kinds have no
     * in-app viewer so they hand off externally. Office always goes through the hand-off sheet:
     * there is no in-app viewer and ACTION_VIEW doesn't reliably open it, so the sheet opens the
     * file directly in the Microsoft app via the documented ms-*:ofv protocol (offering install /
     * another app as fallbacks).
     */
    val openFile: (AppRoute.FileViewer, Boolean) -> Unit = { route, forceChooser ->
        when (val kind = FileKind.classify(route.fileName, route.mimeType)) {
            is FileKind.Office -> navigator.navigate(SheetRoute.OfficeOpen(kind.app, route))
            FileKind.Unknown -> externalFile = route
            else -> {
                val remembered = kind.preferenceKey?.let { fileOpenChoices[it] }
                when {
                    forceChooser || remembered == null ->
                        navigator.navigate(SheetRoute.FileOpenChooser(route))

                    remembered == FileOpenChoice.InApp -> navigator.navigate(route)
                    remembered == FileOpenChoice.External -> externalFile = route
                }
            }
        }
    }
    var searchActive by rememberSaveable { mutableStateOf(false) }

    /**
     * Query and dictation live in the SearchViewModel (the query is SavedStateHandle-backed
     * there); the shell only owns the open/closed flag that drives the bar morph.
     */
    val searchQuery by searchViewModel.query.collectAsStateWithLifecycle()
    val searchDictating by searchViewModel.dictating.collectAsStateWithLifecycle()
    var filterToggle by remember { mutableStateOf<(() -> Unit)?>(null) }
    var filterActive by remember { mutableStateOf(false) }

    /** Null = use the route's static title; non-null = the sub-page is driving it at runtime. */
    var subPageTitleOverride by remember { mutableStateOf<String?>(null) }

    /**
     * The active sub-page's trailing action, hoisted so the global top bar can render it. The
     * lambda is published by the screen and captures the screen's own ViewModel, so it stays
     * correctly scoped even when invoked from the shell-level bar.
     */
    var subPageActions by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    /** Guards the reset of search/filter state to actual settled-tab changes after first composition. */
    var prevPage by remember { mutableIntStateOf(pagerState.settledPage) }
    LaunchedEffect(pagerState.settledPage) {
        if (prevPage != pagerState.settledPage) {
            searchActive = false
            searchViewModel.reset()
            filterActive = false
            subPageTitleOverride = null
            subPageActions = null
            prevPage = pagerState.settledPage
        }
    }

    /**
     * Dictation starts on mic tap once RECORD_AUDIO is granted; the system prompt fires on first
     * use and starts listening immediately on grant.
     */
    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) searchViewModel.startDictation() }

    val shellKeyboardController = LocalSoftwareKeyboardController.current

    /**
     * Wiring for the bar's search field. The mic tap hides the IME up front — voice replaces
     * typing, and the keyboard would just sit under the dictation dialog.
     */
    val searchState = TopBarSearchState(
        query = searchQuery,
        active = searchActive,
        dictating = searchDictating,
        onQueryChange = searchViewModel::setQuery,
        onActiveChange = { active ->
            searchActive = active
            if (!active) searchViewModel.reset()
        },
        onMicClick = {
            shellKeyboardController?.hide()
            when {
                searchDictating -> searchViewModel.stopDictation()
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED -> searchViewModel.startDictation()

                else -> recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onSubmit = searchViewModel::submit,
    )

    val bottomBarItems = ShellTab.entries.map {
        BottomBarItem(
            key = it,
            label = stringResource(it.labelRes),
            icon = it.icon
        )
    }

    val snackbarController = rememberAppSnackbarController()
    val updateEventsViewModel: UpdateEventsViewModel = hiltViewModel()

    val downloadState by updateEventsViewModel.downloadState.collectAsStateWithLifecycle()
    var showUpdateModal by remember { mutableStateOf<it.attendance100.mybicocca.domain.model.update.AppRelease?>(null) }

    showUpdateModal?.let { release ->
        it.attendance100.mybicocca.ui.component.modal.UpdateModalSheet(
            release = release,
            downloadStateFlow = updateEventsViewModel.downloadState,
            onDownload = {
                updateEventsViewModel.startDownload(release)
            },
            onInstall = { file ->
                // State deliberately left alone: the APK stays downloaded and ready, and the
                // downloader needs its pending-install marker to notice a dismissed dialog.
                updateEventsViewModel.installApk(file)
                showUpdateModal = null
            },
            onDismiss = {
                updateEventsViewModel.dismissDownloadError()
                showUpdateModal = null
            },
        )
    }

    val strInstallUpdate = stringResource(R.string.update_modal_install)

    // Both channels take the same path — announce, then either start the download straight away or
    // let the tap open the modal to download from. Only the event source differs.
    LaunchedEffect(updateEventsViewModel, snackbarController) {
        updateEventsViewModel.events.collect { release ->
            if (updateEventsViewModel.stableAutoDownload()) {
                snackbarController.showInfo(strShellUpdateAvailable)
                updateEventsViewModel.startDownload(release)
            } else {
                snackbarController.showInfo(strShellUpdateAvailable) {
                    showUpdateModal = release
                }
            }
        }
    }

    LaunchedEffect(updateEventsViewModel, snackbarController) {
        updateEventsViewModel.nightlyEvents.collect { release ->
            if (updateEventsViewModel.nightlyAutoDownload()) {
                snackbarController.showInfo(strShellUpdateAvailable)
                updateEventsViewModel.startDownload(release)
            } else {
                snackbarController.showInfo(strShellUpdateAvailable) {
                    showUpdateModal = release
                }
            }
        }
    }

    // A finished download is only ever offered, never acted on: the install starts from the tap.
    // Deliberately not gated on this shell having started the download — AppUpdateWorker starts
    // every auto-download, and gating on a shell-local flag silently swallowed the offer for it.
    LaunchedEffect(downloadState) {
        if (downloadState is it.attendance100.mybicocca.data.update.DownloadState.Success) {
            val file = (downloadState as it.attendance100.mybicocca.data.update.DownloadState.Success).file

            if (showUpdateModal == null) {
                snackbarController.showInfo(strInstallUpdate) {
                    updateEventsViewModel.installApk(file)
                }
            }
        }
    }

    LaunchedEffect(accountViewModel, snackbarController) {
        accountViewModel.events.collect { event ->
            when (event) {
                is AccountEvent.RequireReauth -> snackbarController.showError(
                    strShellSessionExpired,
                    event.cause
                )

                is AccountEvent.SignedOut -> snackbarController.showInfo(strShellAccountRemoved)
                is AccountEvent.NewCareerAvailable -> snackbarController.showInfo(
                    strShellNewCareerAvailable.format(event.career.description)
                )

                is AccountEvent.SelectedCareerEnded -> snackbarController.showInfo(
                    strShellCareerEnded.format(event.career.description)
                )

                is AccountEvent.SelectedCareerMissing -> snackbarController.showInfo(
                    strShellCareerMissing
                )

                is AccountEvent.SignOutFailed -> snackbarController.showError(
                    strShellSignOutFailed,
                    event.error
                )

                is AccountEvent.Switched -> Unit
            }
        }
    }

    CompositionLocalProvider(
        LocalAppSnackbarController provides snackbarController,
        LocalAppNavigator provides navigator,
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MyBicoccaTopBar(
                        navProgress = navProgress,
                        searchProgress = searchProgress,
                        canNavigateBack = isOnSubPage,
                        subPageTitle = subPageTitleOverride ?: subPageTitle,
                        searchState = searchState,
                        onProfileClick = { navigator.navigate(SheetRoute.AccountSwitcher) },
                        onNavigateBack = { navigator.pop(navigator.currentPage.id) },
                        photo = photo,
                        globalAlpha = if (immersive) 0f else 1f,
                        onFilterToggle = filterToggle,
                        filterActive = filterActive,
                        trailingActions = subPageActions,
                        transparentBackground = currentRoute?.extendsBehindTopBar == true &&
                                subPageTitleOverride == null,
                    )
                },
                bottomBar = {
                    MyBicoccaBottomBar(
                        items = bottomBarItems,
                        selected = tab,
                        onSelect = { selected ->
                            navigator.popToRoot()
                            scope.launch { pagerState.scrollToPage(selected.ordinal) }
                        },
                        translationY = maxOf(navProgress.floatValue, searchProgress.value) * 300f,
                    )
                },
                snackbarHost = { AppSnackbarHost(controller = snackbarController) },
            ) { innerPadding ->
                val topInset = innerPadding.calculateTopPadding()
                Box(modifier = Modifier.fillMaxSize()) {
                    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            /**
                             * The decorated entries of the whole stack (per-entry saveable state and
                             * ViewModel store, keyed by each push's unique id). Built here rather
                             * than inside NavDisplay so the modal sheets — which NavDisplay keeps
                             * as one stable overlay per sheet — can render their pages from the
                             * live stack through [LocalModalEntries].
                             */
                            val shellEntries = rememberDecoratedNavEntries(
                                backStack = navigator.keys,
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                    rememberEntryPopActionsDecorator(),
                                ),
                                entryProvider = destinationEntryProvider(entryProvider {
                                    entry<AppRoute.TabRoot> {
                                        /**
                                         * NavDisplay's AnimatedContentScope for this entry, bridged into
                                         * LocalAnimatedContentScope so the tabs' list tickets can be true
                                         * shared elements that seek into the detail entry.
                                         */
                                        val tabRootScope = LocalNavAnimatedContentScope.current

                                        /**
                                         * The bar/bottom-bar morph fraction published off THIS entry's
                                         * enter/exit. animateFloat rides the same (seekable) transition that
                                         * slides the page and seeks the shared elements, so the chrome tracks
                                         * the predictive-back gesture frame-for-frame. Presence is 1 when
                                         * TabRoot fully covers the screen and 0 once a sub-page has fully
                                         * replaced it.
                                         */
                                        val tabRootPresence = tabRootScope.transition.animateFloat(
                                            transitionSpec = { motion.defaultSpatialSpec() },
                                            label = "tabRootPresence",
                                        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
                                        LaunchedEffect(tabRootPresence) {
                                            snapshotFlow { tabRootPresence.value }
                                                .collect { navProgress.floatValue = 1f - it }
                                        }
                                        CompositionLocalProvider(
                                            LocalAnimatedContentScope provides tabRootScope,
                                        ) {
                                            HorizontalPager(
                                                state = pagerState,
                                                beyondViewportPageCount = ShellTab.entries.size - 1,
                                                userScrollEnabled = false,
                                                modifier = Modifier.fillMaxSize(),
                                            ) { page ->
                                                val pageTab = ShellTab.entries[page]
                                                val isActive = page == pagerState.settledPage
                                                val onProvideFilterToggle: ((() -> Unit)?) -> Unit =
                                                    { filterToggle = it }

                                                /** The map renders behind the floating top bar; other tabs inset under both bars. */
                                                val pagePadding = if (pageTab == ShellTab.Map) {
                                                    PaddingValues(bottom = innerPadding.calculateBottomPadding())
                                                } else {
                                                    innerPadding
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(pagePadding)
                                                ) {
                                                    when (pageTab) {
                                                        ShellTab.Calendar -> CalendarScreen(
                                                            viewModel = calendarViewModel,
                                                            isActive = isActive,
                                                            examBookingTotals = examBookingTotals,
                                                            onExamEventShown = onExamEventShown,
                                                            coverProgress = remember {
                                                                derivedStateOf {
                                                                    maxOf(
                                                                        navProgress.floatValue,
                                                                        searchProgress.value,
                                                                    )
                                                                }
                                                            },
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                            onOpenCourse = { courseId ->
                                                                navigator.navigate(
                                                                    AppRoute.CourseDetail(
                                                                        courseId.value
                                                                    )
                                                                )
                                                            },
                                                            onOpenAssignment = { assignmentId, courseId ->
                                                                navigator.navigate(
                                                                    SheetRoute.AssignmentDetail(
                                                                        assignId = assignmentId,
                                                                        courseId = courseId,
                                                                    )
                                                                )
                                                            },
                                                            onOpenReservation = openReservation,
                                                            bottomNavBarPadding = innerPadding,
                                                        )

                                                        ShellTab.Elearning -> ElearningScreen(
                                                            viewModel = elearningViewModel,
                                                            isActive = isActive,
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                            onOpenCourse = { courseId ->
                                                                navigator.navigate(
                                                                    AppRoute.CourseDetail(
                                                                        courseId.value
                                                                    )
                                                                )
                                                            },
                                                            onOpenAssignment = { courseId, assignmentId ->
                                                                navigator.navigate(
                                                                    SheetRoute.AssignmentDetail(
                                                                        assignId = assignmentId.value,
                                                                        courseId = courseId.value,
                                                                    )
                                                                )
                                                            },
                                                            onOpenQuiz = { courseId, quizId ->
                                                                navigator.navigate(
                                                                    SheetRoute.QuizDetail(
                                                                        quizId = quizId.value,
                                                                        courseId = courseId.value,
                                                                    )
                                                                )
                                                            },
                                                        )

                                                        ShellTab.Map -> MapScreen(
                                                            viewModel = mapViewModel,
                                                            isActive = isActive,
                                                            contentInsets = innerPadding,
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                        )

                                                        ShellTab.Registry -> RegistryScreen(
                                                            bookedExamsViewModel = bookedExamsViewModel,
                                                            bookableExamsViewModel = bookableExamsViewModel,
                                                            taxesViewModel = taxesViewModel,
                                                            examResultsViewModel = examResultsViewModel,
                                                            studyPlanViewModel = studyPlanViewModel,
                                                            isActive = isActive,
                                                            onOpenAppelli = {
                                                                navigator.navigate(SheetRoute.Appelli)
                                                            },
                                                            onOpenTaxes = {
                                                                navigator.navigate(SheetRoute.Taxes)
                                                            },
                                                            onOpenIsee = {
                                                                navigator.navigate(SheetRoute.Isee)
                                                            },
                                                            onOpenRefunds = {
                                                                navigator.navigate(SheetRoute.Refunds)
                                                            },
                                                            onOpenExamResults = {
                                                                navigator.navigate(SheetRoute.ExamResults)
                                                            },
                                                            onOpenStudyPlan = {
                                                                navigator.navigate(SheetRoute.StudyPlan)
                                                            },
                                                            onOpenQuestionnaires = {
                                                                navigator.navigate(SheetRoute.Questionnaires)
                                                            },
                                                            onOpenAppointments = {
                                                                navigator.navigate(SheetRoute.Appointments)
                                                            },
                                                            onOpenLibrary = {
                                                                navigator.navigate(SheetRoute.Library)
                                                            },
                                                            onOpenAttendance = {
                                                                navigator.navigate(SheetRoute.Attendance)
                                                            },
                                                            onOpenEnrollments = {
                                                                navigator.navigate(SheetRoute.Enrollments)
                                                            },
                                                            onOpenTitles = {
                                                                navigator.navigate(SheetRoute.Titles)
                                                            },
                                                            onOpenCertificates = {
                                                                navigator.navigate(SheetRoute.Certificates)
                                                            },
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    entry<AppRoute.Profile> {
                                        SubPage(topInset) {
                                            ProfileScreen(viewModel = profileViewModel)
                                        }
                                    }
                                    entry<AppRoute.Settings> {
                                        SubPage(topInset) { SettingsScreen() }
                                    }

                                    entry<AppRoute.CourseDetail> { key ->
                                        val vm =
                                            hiltViewModel<CourseDetailViewModel, CourseDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(
                                            topInset,
                                            extendBehindBar = key.extendsBehindTopBar
                                        ) {
                                            CourseDetailScreen(
                                                courseId = key.courseId,
                                                topBarInset = topInset,
                                                viewModel = vm,
                                                onProvideTitle = { subPageTitleOverride = it },
                                                onProvideActions = { subPageActions = it },
                                                onOpenAssignment = { id ->
                                                    navigator.navigate(
                                                        SheetRoute.AssignmentDetail(
                                                            assignId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenQuiz = { id ->
                                                    navigator.navigate(
                                                        SheetRoute.QuizDetail(
                                                            quizId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenForum = { id ->
                                                    navigator.navigate(
                                                        SheetRoute.Forum(
                                                            forumId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenDiscussion = { forumId, discussionId ->
                                                    navigator.navigate(
                                                        SheetRoute.Forum(
                                                            forumId = forumId.value,
                                                            courseId = key.courseId,
                                                            initialDiscussionId = discussionId.value,
                                                        )
                                                    )
                                                },
                                                onOpenVideo = { cmId, title ->
                                                    navigator.navigate(
                                                        AppRoute.VideoPlayback(
                                                            courseId = key.courseId,
                                                            cmId = cmId,
                                                            title = title
                                                        )
                                                    )
                                                },
                                                onOpenFile = openFile,
                                            )
                                        }
                                    }
                                    entry<AppRoute.FileViewer> { key ->
                                        val vm =
                                            hiltViewModel<FileViewerViewModel, FileViewerViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        val close = rememberPopSelf()
                                        SubPage(topInset, immersive = true) {
                                            FileViewerScreen(
                                                onOpenFile = openFile,
                                                onClose = close,
                                                viewModel = vm,
                                            )
                                        }
                                    }

                                    entry<AppRoute.VideoPlayback> { key ->
                                        val vm =
                                            hiltViewModel<VideoPlayerViewModel, VideoPlayerViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        val close = rememberPopSelf()
                                        SubPage(topInset, immersive = true) {
                                            VideoPlayerScreen(
                                                courseId = key.courseId,
                                                cmId = key.cmId,
                                                onBack = close,
                                                viewModel = vm,
                                            )
                                        }
                                    }
                                    entry<AppRoute.TeacherDetail> { key ->
                                        SubPage(topInset) { TeacherDetailScreen(teacherCode = key.teacherCode) }
                                    }

                                    entry<SheetRoute.Enrollments>(
                                        metadata = sheetHeader<SheetRoute.Enrollments> {
                                            val history by enrollmentsViewModel.history
                                                .collectAsStateWithLifecycle()
                                            enrollmentsHeader(history.valueOrNull())
                                        },
                                    ) {
                                        EnrollmentsTimelinePage(
                                            viewModel = enrollmentsViewModel,
                                            onOpenDetail = { id ->
                                                navigator.navigate(SheetRoute.EnrollmentDetail(id.value))
                                            },
                                        )
                                    }
                                    entry<SheetRoute.EnrollmentDetail>(
                                        metadata = sheetHeader<SheetRoute.EnrollmentDetail> {
                                            val top = it
                                            val history by enrollmentsViewModel.history
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                history.valueOrNull()?.years
                                                    ?.firstOrNull { it.id.value == k.enrollmentId }
                                            }?.let { enrollment ->
                                                SheetHeaderSpec(
                                                    title = stringResource(
                                                        R.string.enrollments_detail_title,
                                                        enrollment.academicYearLabel()
                                                    ),
                                                    subtitle = "${enrollment.courseYearLabel()} · ${enrollment.statusLabel()}",
                                                )
                                            } ?: SheetHeaderSpec(
                                                title = stringResource(R.string.registry_enrollments),
                                                subtitle = null,
                                            )
                                        },
                                    ) { key ->
                                        val history by enrollmentsViewModel.history
                                            .collectAsStateWithLifecycle()
                                        val enrollment = history.valueOrNull()?.years
                                            ?.firstOrNull { it.id.value == key.enrollmentId }
                                        PopWhenMissing(loaded = history.valueOrNull() != null, missing = enrollment == null)
                                        if (enrollment != null) {
                                            EnrollmentDetailPage(enrollment = enrollment)
                                        }
                                    }
                                    entry<SheetRoute.Titles>(
                                        metadata = sheetHeader<SheetRoute.Titles> {
                                            val titles by titlesViewModel.titles
                                                .collectAsStateWithLifecycle()
                                            SheetHeaderSpec(
                                                title = stringResource(R.string.registry_titles),
                                                subtitle = titles.valueOrNull()
                                                    ?.let { titlesHeaderSubtitle(it) },
                                            )
                                        },
                                    ) {
                                        TitlesListPage(
                                            viewModel = titlesViewModel,
                                            onOpenDetail = { id ->
                                                navigator.navigate(
                                                    SheetRoute.TitleDetail(
                                                        id
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.TitleDetail>(
                                        metadata = sheetHeader<SheetRoute.TitleDetail> {
                                            val top = it
                                            val titles by titlesViewModel.titles
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                titles.valueOrNull()
                                                    ?.firstOrNull { it.id == k.titleId }
                                            }?.let { title ->
                                                SheetHeaderSpec(
                                                    title = title.headline(),
                                                    subtitle = title.headlineSubtitle(),
                                                )
                                            } ?: SheetHeaderSpec(
                                                title = stringResource(R.string.registry_titles),
                                                subtitle = null,
                                            )
                                        },
                                    ) { key ->
                                        val titles by titlesViewModel.titles
                                            .collectAsStateWithLifecycle()
                                        val title = titles.valueOrNull()
                                            ?.firstOrNull { it.id == key.titleId }
                                        PopWhenMissing(loaded = titles.valueOrNull() != null, missing = title == null)
                                        if (title != null) TitleDetailPage(title = title)
                                    }
                                    entry<SheetRoute.Certificates>(metadata = sheetHeaderInPage()) {
                                        CertificatesPage(viewModel = certificatesViewModel)
                                    }
                                    entry<SheetRoute.Refunds>(
                                        metadata = sheetHeader<SheetRoute.Refunds> {
                                            val refunds by refundsViewModel.refunds
                                                .collectAsStateWithLifecycle()
                                            SheetHeaderSpec(
                                                title = stringResource(R.string.registry_refunds),
                                                subtitle = refunds.valueOrNull()
                                                    ?.let { refundsHeaderSubtitle(it) },
                                            )
                                        },
                                    ) {
                                        RefundsListPage(
                                            viewModel = refundsViewModel,
                                            onOpenDetail = { key ->
                                                navigator.navigate(
                                                    SheetRoute.RefundDetail(
                                                        key
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.RefundDetail>(
                                        metadata = sheetHeader<SheetRoute.RefundDetail> {
                                            val top = it
                                            val refunds by refundsViewModel.refunds
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                refunds.valueOrNull()
                                                    ?.firstOrNull { it.refundKey() == k.refundKey }
                                            }?.let { refund ->
                                                SheetHeaderSpec(
                                                    title = refundHeaderTitle(refund),
                                                    subtitle = refundHeaderSubtitle(refund),
                                                )
                                            } ?: SheetHeaderSpec(
                                                title = stringResource(R.string.registry_refunds),
                                                subtitle = null,
                                            )
                                        },
                                    ) { key ->
                                        val refunds by refundsViewModel.refunds
                                            .collectAsStateWithLifecycle()
                                        val refund = refunds.valueOrNull()
                                            ?.firstOrNull { it.refundKey() == key.refundKey }
                                        PopWhenMissing(loaded = refunds.valueOrNull() != null, missing = refund == null)
                                        if (refund != null) RefundDetailPage(refund = refund)
                                    }
                                    entry<SheetRoute.Isee>(
                                        metadata = sheetHeader<SheetRoute.Isee> {
                                            val state by taxesViewModel.isee
                                                .collectAsStateWithLifecycle()
                                            val declarations = state.valueOrNull()
                                                ?.filter { it.isee != null && it.academicYearEnrollmentId != null }
                                            iseeHeader(declarations)
                                        },
                                    ) {
                                        IseeDeclarationsPage(
                                            viewModel = taxesViewModel,
                                            onOpenDetail = { year ->
                                                navigator.navigate(
                                                    SheetRoute.IseeDetail(
                                                        year
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.IseeDetail>(
                                        metadata = sheetHeader<SheetRoute.IseeDetail> {
                                            val top = it
                                            val state by taxesViewModel.isee
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                state.valueOrNull()
                                                    ?.firstOrNull { it.academicYearEnrollmentId == k.year }
                                            }?.let { declaration ->
                                                SheetHeaderSpec(
                                                    title = iseeDetailTitle(declaration),
                                                    subtitle = iseeDetailSubtitle(declaration),
                                                )
                                            } ?: SheetHeaderSpec(
                                                title = stringResource(R.string.registry_isee),
                                                subtitle = null,
                                            )
                                        },
                                    ) { key ->
                                        val state by taxesViewModel.isee
                                            .collectAsStateWithLifecycle()
                                        val declaration = state.valueOrNull()
                                            ?.firstOrNull { it.academicYearEnrollmentId == key.year }
                                        PopWhenMissing(loaded = state.valueOrNull() != null, missing = declaration == null)
                                        if (declaration != null) IseeDetailPage(declaration = declaration)
                                    }
                                    entry<SheetRoute.ExamResults>(metadata = sheetHeaderInPage()) {
                                        ExamResultsPage(viewModel = examResultsViewModel)
                                    }
                                    entry<SheetRoute.Taxes>(
                                        metadata = sheetHeader<SheetRoute.Taxes> {
                                            val state by taxesViewModel.invoices
                                                .collectAsStateWithLifecycle()
                                            SheetHeaderSpec(
                                                title = stringResource(R.string.registry_fees),
                                                subtitle = state.valueOrNull()
                                                    ?.let { taxesHeaderSubtitle(it) },
                                            )
                                        },
                                    ) {
                                        TaxesPage(viewModel = taxesViewModel)
                                    }
                                    entry<SheetRoute.QuizDetail>(metadata = sheetHeaderInPage()) { key ->
                                        QuizDetailPage(
                                            quizId = key.quizId,
                                            courseId = key.courseId,
                                        )
                                    }
                                    entry<SheetRoute.Forum>(metadata = sheetHeaderInPage()) { key ->
                                        ForumSheetPage(
                                            forumId = key.forumId,
                                            courseId = key.courseId,
                                            initialDiscussionId = key.initialDiscussionId,
                                            onOpenFile = { fileName, fileUrl, mimeType, sizeBytes ->
                                                openFile(
                                                    AppRoute.FileViewer(
                                                        fileName = fileName,
                                                        fileUrl = fileUrl,
                                                        mimeType = mimeType,
                                                        sizeBytes = sizeBytes,
                                                    ),
                                                    false,
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.AssignmentDetail>(metadata = sheetHeaderInPage()) { key ->
                                        AssignmentDetailPage(
                                            assignId = key.assignId,
                                            courseId = key.courseId,
                                            onOpenFile = { fileName, fileUrl, mimeType, sizeBytes, forceChooser ->
                                                openFile(
                                                    AppRoute.FileViewer(
                                                        fileName = fileName,
                                                        fileUrl = fileUrl,
                                                        mimeType = mimeType,
                                                        sizeBytes = sizeBytes,
                                                    ),
                                                    forceChooser,
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.Attendance>(metadata = sheetHeaderInPage()) {
                                        AttendancePage(viewModel = attendanceViewModel)
                                    }
                                    entry<SheetRoute.Appelli>(metadata = sheetHeaderInPage()) {
                                        AppelliPage(
                                            bookableViewModel = bookableExamsViewModel,
                                            viewModel = bookedExamsViewModel,
                                        )
                                    }
                                    entry<SheetRoute.StudyPlan>(metadata = sheetHeaderInPage()) {
                                        StudyPlanPage(viewModel = studyPlanViewModel)
                                    }
                                    entry<SheetRoute.Questionnaires>(metadata = sheetHeaderInPage()) {
                                        QuestionnairesPage(viewModel = questionnairesViewModel)
                                    }
                                    entry<SheetRoute.Appointments>(metadata = sheetHeaderInPage()) {
                                        AppointmentsPage(
                                            viewModel = appointmentsViewModel,
                                            onOpenPdf = { path, name ->
                                                navigator.navigate(
                                                    AppRoute.FileViewer(
                                                        fileName = name,
                                                        localPath = path,
                                                        mimeType = "application/pdf",
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.Library>(metadata = sheetHeaderInPage()) {
                                        LibraryPage(viewModel = libraryViewModel)
                                    }
                                    entry<SheetRoute.CalendarEvent>(
                                        metadata = sheetHeader<SheetRoute.CalendarEvent> { route ->
                                            val monthEvents by calendarViewModel.events
                                                .collectAsStateWithLifecycle()
                                            val dayEvents by calendarViewModel.dayEvents
                                                .collectAsStateWithLifecycle()
                                            val live = monthEvents.valueOrNull()
                                                ?.firstOrNull { it.id.value == route.eventId }
                                                ?: dayEvents.valueOrNull()
                                                    ?.firstOrNull { it.id.value == route.eventId }
                                            // Keep the last header while the event closes.
                                            val last = remember { arrayOf<CalendarEvent?>(null) }
                                            if (live != null) last[0] = live
                                            eventDetailHeader(live ?: last[0])
                                        },
                                    ) { key ->
                                        val monthEvents by calendarViewModel.events
                                            .collectAsStateWithLifecycle()
                                        val dayEvents by calendarViewModel.dayEvents
                                            .collectAsStateWithLifecycle()
                                        val coursesByActivityCode by calendarViewModel
                                            .coursesByActivityCode.collectAsStateWithLifecycle()
                                        val liveEvent = monthEvents.valueOrNull()
                                            ?.firstOrNull { it.id.value == key.eventId }
                                            ?: dayEvents.valueOrNull()
                                                ?.firstOrNull { it.id.value == key.eventId }
                                        PopWhenMissing(
                                            loaded = monthEvents.valueOrNull() != null,
                                            missing = liveEvent == null,
                                            onlyAfterSeen = true,
                                        )
                                        // Keep showing the event while it closes (or while a
                                        // sheet stacked above it removed it, e.g. a cancelled
                                        // booking) instead of blanking the page.
                                        val lastEvent = remember { arrayOf<CalendarEvent?>(null) }
                                        if (liveEvent != null) lastEvent[0] = liveEvent
                                        val event = liveEvent ?: lastEvent[0]
                                        if (event != null) {
                                            LaunchedEffect(event.id) {
                                                (event as? CalendarEvent.Exam)?.let(onExamEventShown)
                                            }
                                            val closeEvent = rememberPopSelf()
                                            EventDetailPage(
                                                event = event,
                                                examTotalBookings = (event as? CalendarEvent.Exam)
                                                    ?.let { examBookingTotals[it.id] },
                                                elearningCourses = event.activityCode
                                                    ?.let(coursesByActivityCode::get).orEmpty(),
                                                onOpenCourse = { course ->
                                                    closeEvent()
                                                    navigator.navigate(AppRoute.CourseDetail(course.value))
                                                },
                                                onOpenAssignment = { assignmentId, courseId ->
                                                    closeEvent()
                                                    navigator.navigate(
                                                        SheetRoute.AssignmentDetail(assignmentId, courseId),
                                                    )
                                                },
                                                onOpenReservation = openReservation,
                                            )
                                        }
                                    }
                                    entry<SheetRoute.CourseEditionPicker>(
                                        metadata = sheetHeader<SheetRoute.CourseEditionPicker> { route ->
                                            val coursesByActivityCode by calendarViewModel
                                                .coursesByActivityCode.collectAsStateWithLifecycle()
                                            courseEditionPickerHeader(
                                                count = coursesByActivityCode[route.activityCode].orEmpty().size,
                                            )
                                        },
                                    ) { key ->
                                        val coursesByActivityCode by calendarViewModel
                                            .coursesByActivityCode.collectAsStateWithLifecycle()
                                        val self = checkNotNull(LocalDestination.current)
                                        CourseEditionPickerPage(
                                            courses = coursesByActivityCode[key.activityCode].orEmpty(),
                                            onPick = { courseId ->
                                                // Leave the whole sheet the picker belongs to (the
                                                // event sheet when it was opened from there).
                                                navigator.dismissSheetOf(self.id)
                                                navigator.navigate(AppRoute.CourseDetail(courseId.value))
                                            },
                                        )
                                    }
                                    entry<SheetRoute.AccountSwitcher>(
                                        metadata = sheetHeaderInPage() + sheetStyle(AccountSwitcherSheetStyle),
                                    ) {
                                        AccountSwitcherPage(
                                            onOpenProfile = { navigator.navigate(AppRoute.Profile) },
                                            onOpenSettings = { navigator.navigate(AppRoute.Settings) },
                                            viewModel = accountViewModel,
                                        )
                                    }
                                    entry<SheetRoute.OfficeOpen>(
                                        metadata = sheetHeader<SheetRoute.OfficeOpen> { route ->
                                            officeOpenHeader(app = route.app, file = route.file)
                                        },
                                    ) { key ->
                                        OfficeOpenPage(app = key.app, route = key.file)
                                    }
                                    entry<SheetRoute.MapFilter>(
                                        metadata = sheetHeader<SheetRoute.MapFilter> {
                                            val categoryFilter by mapViewModel.categoryFilter
                                                .collectAsStateWithLifecycle()
                                            mapFilterHeader(selectedCount = categoryFilter.size)
                                        },
                                    ) {
                                        val categoryFilter by mapViewModel.categoryFilter
                                            .collectAsStateWithLifecycle()
                                        MapFilterPage(
                                            selected = categoryFilter,
                                            onToggle = mapViewModel::toggleCategory,
                                            onClear = mapViewModel::clearCategories,
                                        )
                                    }
                                    settingsSheetEntries(fileOpenViewModel = fileOpenViewModel)
                                    profileSheetEntries(
                                        profileViewModel = profileViewModel,
                                        onOpenAppelli = { courseKey ->
                                            // A libretto course deep-links into booking: land on the
                                            // Servizi tab with the Appelli sheet focused on it.
                                            bookableExamsViewModel.requestFocus(courseKey)
                                            scope.launch {
                                                pagerState.scrollToPage(ShellTab.Registry.ordinal)
                                            }
                                            navigator.navigate(SheetRoute.Appelli)
                                        },
                                    )
                                    registrySheetEntries(
                                        bookedExamsViewModel = bookedExamsViewModel,
                                        bookableExamsViewModel = bookableExamsViewModel,
                                        taxesViewModel = taxesViewModel,
                                        examResultsViewModel = examResultsViewModel,
                                        studyPlanViewModel = studyPlanViewModel,
                                    )
                                    elearningSheetEntries(
                                        elearningViewModel = elearningViewModel,
                                        addCourseViewModel = addCourseViewModel,
                                        snackbarController = snackbarController,
                                        scope = scope,
                                    )
                                    entry<SheetRoute.FileOpenChooser>(
                                        metadata = sheetHeader<SheetRoute.FileOpenChooser> { route ->
                                            fileOpenChooserHeader(route.file)
                                        },
                                    ) { key ->
                                        val closeChooser = rememberPopSelf()
                                        val kind = remember(key) {
                                            FileKind.classify(key.file.fileName, key.file.mimeType)
                                        }
                                        FileOpenChooserContent(
                                            kind = kind,
                                            onChoose = { choice, rememberChoice ->
                                                kind.preferenceKey
                                                    ?.takeIf { rememberChoice }
                                                    ?.let { fileOpenViewModel.remember(it, choice) }
                                                closeChooser()
                                                when (choice) {
                                                    FileOpenChoice.InApp -> navigator.navigate(key.file)
                                                    FileOpenChoice.External -> externalFile =
                                                        key.file
                                                }
                                            },
                                        )
                                    }
                                }),
                            )
                            CompositionLocalProvider(LocalModalEntries provides shellEntries) {
                                NavDisplay(
                                    entries = shellEntries,
                                    onBack = { navigator.back() },
                                    modifier = Modifier.fillMaxSize(),
                                    sceneStrategies = sceneStrategies,
                                    transitionSpec = { enterTransition togetherWith exitTransition },
                                    popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                    predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                )
                            }
                        }
                    }

                    if (searchActive || searchProgress.value > 0f) {
                        val keyboardController = LocalSoftwareKeyboardController.current
                        fun closeSearch() {
                            keyboardController?.hide()
                            searchActive = false
                            searchViewModel.reset()
                        }

                        val searchNavHooks = remember {
                            SearchNavHooks(
                                selectCalendarDay = calendarViewModel::selectDay,
                                openCalendarEvent = calendarViewModel::openEventDetail,
                                selectBuilding = mapViewModel::selectBuilding,
                                selectRoom = mapViewModel::selectRoomByCode,
                                requestAddCourse = elearningViewModel::requestAddCourse,
                                requestHypotheticalCalculator = profileViewModel::requestHypotheticalCalculator,
                            )
                        }

                        SearchOverlay(
                            viewModel = searchViewModel,
                            progress = searchProgress.value,
                            subPageProgress = navProgress.floatValue,
                            topInset = topInset,
                            onOpenResult = { result ->
                                searchViewModel.commitPick(result)

                                /**
                                 * The guided steps to play. A switch to the tab already underneath
                                 * is a no-op step; dropping it lets same-tab plans start their
                                 * pushes at once.
                                 */
                                val plan = result.toNavPlan(searchNavHooks).filterNot { step ->
                                    step is SearchNavStep.SwitchTab && step.tab == tab && navigator.isAtRoot
                                }
                                if (plan.all { it is SearchNavStep.PushPage }) {
                                    keyboardController?.hide()
                                } else {
                                    closeSearch()
                                }
                                scope.launch {
                                    plan.forEachIndexed { index, step ->
                                        when (step) {
                                            is SearchNavStep.SwitchTab -> {
                                                navigator.popToRoot()
                                                pagerState.scrollToPage(step.tab.ordinal)
                                            }

                                            is SearchNavStep.PushPage -> navigator.navigate(step.route)
                                            is SearchNavStep.PushSheet -> navigator.navigate(step.route)

                                            SearchNavStep.OpenAccountSwitcher ->
                                                navigator.navigate(SheetRoute.AccountSwitcher)

                                            is SearchNavStep.Run -> step.action()
                                        }
                                        if (index < plan.lastIndex) delay(SEARCH_NAV_STEP_DELAY_MS)
                                    }
                                }
                            },
                        )
                    }
                }
            }

            externalFile?.let { route ->
                ExternalFileLauncher(
                    route = route,
                    onFinished = { externalFile = null },
                )
            }

        }
    }
}

/**
 * Pause between guided-search navigation steps: long enough for the previous transition (tab
 * landing, page slide, sheet rise) to read as its own beat, short enough to stay snappy.
 */
private const val SEARCH_NAV_STEP_DELAY_MS = 550L

/**
 * Sub-page container: an opaque surface that covers the tab pager, inset below the global top bar
 * (the bottom bar slides off on sub-pages). Immersive pages (video) go fully edge to edge;
 * [extendBehindBar] pages keep the opaque background but skip the top inset, scrolling their
 * content behind the see-through bar (they handle the inset themselves via contentPadding). It
 * also bridges NavDisplay's AnimatedContentScope into LocalAnimatedContentScope so shared
 * elements in the page (e.g. the tax detail ticket) seek with the page transition.
 */
@Composable
private fun SubPage(
    topInset: Dp,
    immersive: Boolean = false,
    extendBehindBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    /**
     * A page opened from a modal sheet (e.g. a PDF from Appuntamenti) sits directly above the
     * sheet's entries. NavDisplay's predictive back would seek the page transition toward the
     * sheet's overlay scene, which is its own window and cannot be scrubbed into, so in that layering
     * back commits a plain pop instead — gesture and button both return cleanly to the sheet, which
     * slides back in.
     */
    val navigator = requireAppNavigator()
    val self = checkNotNull(LocalDestination.current)
    val overSheet by remember(navigator, self) {
        derivedStateOf {
            val stack = navigator.entries
            val index = stack.indexOfFirst { it.id == self.id }
            index > 0 && stack[index - 1].route is SheetRoute
        }
    }
    BackHandler(enabled = overSheet) { navigator.pop(self.id) }
    CompositionLocalProvider(
        LocalAnimatedContentScope provides LocalNavAnimatedContentScope.current,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (immersive) Modifier else Modifier.background(MaterialTheme.colorScheme.surface))
                .padding(top = if (immersive || extendBehindBar) 0.dp else topInset),
        ) {
            content()
        }
    }
}

/**
 * Pops the calling entry (and anything above it) — the safe "close this page" action: addressed by
 * the entry's own id, it can neither pop an unrelated entry nor run twice.
 */
@Composable
private fun rememberPopSelf(): () -> Unit {
    val navigator = requireAppNavigator()
    val self = checkNotNull(LocalDestination.current)
    return remember(navigator, self) { { navigator.pop(self.id) } }
}

/**
 * Closes a detail sheet page whose item disappeared from its list (e.g. evicted by a career
 * switch), with three guards:
 * - only once the list has actually loaded: right after a process-death restore the page is back
 *   on the stack before its data is, and popping then would throw away the very page the user was on;
 * - only while the page is on top: a sheet stacked above it (e.g. the booking manager opened from a
 *   calendar event, where cancelling the booking removes the event) must not be torn down with it —
 *   the page closes once it is back on top;
 * - with [onlyAfterSeen], only after the item was present at least once, for lists that can briefly
 *   hold a previous query's rows (the calendar month list right after a month switch).
 */
@Composable
private fun PopWhenMissing(loaded: Boolean, missing: Boolean, onlyAfterSeen: Boolean = false) {
    val navigator = requireAppNavigator()
    val self = checkNotNull(LocalDestination.current)
    var seen by rememberSaveable { mutableStateOf(false) }
    if (!missing) seen = true
    val onTop = navigator.top.id == self.id
    val shouldPop = loaded && missing && onTop && (seen || !onlyAfterSeen)
    LaunchedEffect(shouldPop) {
        if (shouldPop) navigator.pop(self.id)
    }
}
