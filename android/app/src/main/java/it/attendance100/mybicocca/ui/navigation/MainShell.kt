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
import androidx.navigation3.runtime.rememberNavBackStack
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
import it.attendance100.mybicocca.ui.navigation.scene.BottomSheetSceneStrategy
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.navigation.scene.SheetDismissControl
import it.attendance100.mybicocca.ui.navigation.scene.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.scene.sheetEntry
import it.attendance100.mybicocca.ui.navigation.transitions.LocalAnimatedContentScope
import it.attendance100.mybicocca.ui.navigation.transitions.LocalSharedTransitionScope
import it.attendance100.mybicocca.ui.navigation.transitions.defaultEnterTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultExitTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultPopEnterTransition
import it.attendance100.mybicocca.ui.navigation.transitions.defaultPopExitTransition
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.state.AccountEvent
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherSheet
import it.attendance100.mybicocca.ui.screen.calendar.CalendarScreen
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewModel
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.teacherDetail.TeacherDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.ExternalFileLauncher
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.OfficeOpenSheet
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.enrollmentsHeaderSubtitle
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.iseeHeaderSubtitle
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
    val tab = ShellTab.entries[pagerState.currentPage]
    val photo by accountViewModel.userPhoto.collectAsStateWithLifecycle()

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
     * "Vai alla prenotazione" on a calendar event: the managing page (appelli / appuntamenti /
     * biblioteca) opens as a modal NESTED over the still-open event-detail sheet — its dialog
     * window stacks above — rather than as a shell sheet that would replace it.
     */
    var calendarReservationEvent by remember { mutableStateOf<CalendarEvent?>(null) }

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

    val backStack = rememberNavBackStack(AppRoute.TabRoot)

    /**
     * The topmost full-screen destination — NOT `backStack.lastOrNull()`: a modal sheet
     * ([SheetRoute]) rides this same stack as an overlay floating OVER its page, so the page
     * underneath is still the current destination. Reading the last entry blindly would flip
     * this to null whenever a sheet opens, dropping the page's title / actions / back arrow from
     * the chrome (which sits dimmed behind the sheet) and animating them away.
     */
    val currentRoute = backStack.lastOrNull { it is AppRoute } as? AppRoute

    /** Renders sheet pages as overlay scenes; pop(n) closes or steps a sheet by removing n trailing entries. */
    val bottomSheetStrategy = remember {
        BottomSheetSceneStrategy<NavKey>(pop = { count -> repeat(count) { backStack.removeLastOrNull() } })
    }

    /**
     * The file-open chooser inherits the sheet group of whatever entry sits beneath it, so it
     * renders as a sub-page INSIDE an already-open sheet (same run) instead of stacking a second
     * modal window on top. From a full screen the fallback group makes it its own sheet. The
     * entryProvider re-evaluates on recomposition, so this tracks the stack live.
     */
    val chooserHostGroup = backStack
        .indexOfLast { it is SheetRoute.FileOpenChooser }
        .takeIf { it > 0 }
        ?.let { sheetGroupOf(backStack[it - 1]) }

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

    var showAccountSwitcher by remember { mutableStateOf(false) }

    /** The Office install prompt — only shown when the matching Microsoft app is missing. */
    var officeFile by remember { mutableStateOf<Pair<OfficeApp, AppRoute.FileViewer>?>(null) }

    /**
     * External hand-off (download + ACTION_VIEW): PDFs go to the default reader, Office to the
     * installed app, and any file the user chose to open externally.
     */
    var externalFile by remember { mutableStateOf<AppRoute.FileViewer?>(null) }

    LaunchedEffect(Unit) {
        libraryViewModel.openSheetRequests.collect {
            if (backStack.lastOrNull() != SheetRoute.Library) backStack.add(SheetRoute.Library)
        }
    }
    LaunchedEffect(pendingPresenceScan) {
        if (pendingPresenceScan != null && backStack.lastOrNull() != SheetRoute.Attendance) {
            backStack.add(SheetRoute.Attendance)
        }
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
            is FileKind.Office -> officeFile = kind.app to route
            FileKind.Unknown -> externalFile = route
            else -> {
                val remembered = kind.preferenceKey?.let { fileOpenChoices[it] }
                when {
                    forceChooser || remembered == null ->
                        backStack.add(SheetRoute.FileOpenChooser(route))

                    remembered == FileOpenChoice.InApp -> backStack.add(route)
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

    LaunchedEffect(updateEventsViewModel, snackbarController) {
        updateEventsViewModel.events.collect {
            snackbarController.showInfo(strShellUpdateAvailable)
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

    CompositionLocalProvider(LocalAppSnackbarController provides snackbarController) {
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
                        onProfileClick = { showAccountSwitcher = true },
                        onNavigateBack = { backStack.removeLastOrNull() },
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
                            while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
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
                            NavDisplay(
                                backStack = backStack,
                                onBack = { backStack.removeLastOrNull() },
                                modifier = Modifier.fillMaxSize(),
                                sceneStrategies = listOf(
                                    bottomSheetStrategy,
                                    SinglePaneSceneStrategy()
                                ),
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                ),
                                transitionSpec = { enterTransition togetherWith exitTransition },
                                popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                entryProvider = entryProvider {
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
                                                            examBookingTotals = run {
                                                                // Seat totals per exam event, merged off the main thread;
                                                                // distinctUntilChanged keeps the instance stable when nothing changed.
                                                                val totalsFlow = remember(
                                                                    bookableExamsViewModel,
                                                                    bookedExamsViewModel
                                                                ) {
                                                                    combine(
                                                                        bookableExamsViewModel.examCalls,
                                                                        bookedExamsViewModel.bookings,
                                                                        bookedExamsViewModel.callTotals,
                                                                    ) { calls, bookings, lazyTotals ->
                                                                        buildMap {
                                                                            calls.valueOrNull()
                                                                                .orEmpty()
                                                                                .forEach { call ->
                                                                                    call.enrolledNumber?.let {
                                                                                        put(
                                                                                            examCalendarEventId(
                                                                                                call.key
                                                                                            ),
                                                                                            it
                                                                                        )
                                                                                    }
                                                                                }
                                                                            // The booking's persisted numIscritti wins over the bookable list's count…
                                                                            bookings.valueOrNull()
                                                                                .orEmpty()
                                                                                .forEach { booking ->
                                                                                    booking.totalBookings?.let {
                                                                                        put(
                                                                                            examCalendarEventId(
                                                                                                booking.key
                                                                                            ),
                                                                                            it
                                                                                        )
                                                                                    }
                                                                                }
                                                                            // …and a fresh lazy fetch wins over both.
                                                                            lazyTotals.forEach { (key, total) ->
                                                                                put(
                                                                                    examCalendarEventId(
                                                                                        key
                                                                                    ), total
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                        .distinctUntilChanged()
                                                                        .flowOn(Dispatchers.Default)
                                                                }
                                                                val totals by totalsFlow.collectAsStateWithLifecycle(
                                                                    initialValue = emptyMap(),
                                                                )
                                                                totals
                                                            },
                                                            onExamEventShown = { examEvent ->
                                                                bookedExamsViewModel.bookings.value.valueOrNull()
                                                                    .orEmpty()
                                                                    .firstOrNull {
                                                                        examCalendarEventId(
                                                                            it.key
                                                                        ) == examEvent.id
                                                                    }
                                                                    ?.let(bookedExamsViewModel::loadTotalBookings)
                                                            },
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
                                                                backStack.add(
                                                                    AppRoute.CourseDetail(
                                                                        courseId.value
                                                                    )
                                                                )
                                                            },
                                                            onOpenAssignment = { assignmentId, courseId ->
                                                                backStack.add(
                                                                    SheetRoute.AssignmentDetail(
                                                                        assignId = assignmentId,
                                                                        courseId = courseId,
                                                                    )
                                                                )
                                                            },
                                                            onOpenReservation = { event ->
                                                                calendarReservationEvent = event
                                                            },
                                                            bottomNavBarPadding = innerPadding,
                                                        )

                                                        ShellTab.Elearning -> ElearningScreen(
                                                            viewModel = elearningViewModel,
                                                            isActive = isActive,
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                            onOpenCourse = { courseId ->
                                                                backStack.add(
                                                                    AppRoute.CourseDetail(
                                                                        courseId.value
                                                                    )
                                                                )
                                                            },
                                                            onOpenAssignment = { courseId, assignmentId ->
                                                                backStack.add(
                                                                    SheetRoute.AssignmentDetail(
                                                                        assignId = assignmentId.value,
                                                                        courseId = courseId.value,
                                                                    )
                                                                )
                                                            },
                                                            onOpenQuiz = { courseId, quizId ->
                                                                backStack.add(
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
                                                                backStack.add(SheetRoute.Appelli)
                                                            },
                                                            onOpenTaxes = {
                                                                backStack.add(SheetRoute.Taxes)
                                                            },
                                                            onOpenIsee = {
                                                                backStack.add(SheetRoute.Isee)
                                                            },
                                                            onOpenRefunds = {
                                                                backStack.add(SheetRoute.Refunds)
                                                            },
                                                            onOpenExamResults = {
                                                                backStack.add(SheetRoute.ExamResults)
                                                            },
                                                            onOpenStudyPlan = {
                                                                backStack.add(SheetRoute.StudyPlan)
                                                            },
                                                            onOpenQuestionnaires = {
                                                                backStack.add(SheetRoute.Questionnaires)
                                                            },
                                                            onOpenAppointments = {
                                                                backStack.add(SheetRoute.Appointments)
                                                            },
                                                            onOpenLibrary = {
                                                                backStack.add(SheetRoute.Library)
                                                            },
                                                            onOpenAttendance = {
                                                                backStack.add(SheetRoute.Attendance)
                                                            },
                                                            onOpenEnrollments = {
                                                                backStack.add(SheetRoute.Enrollments)
                                                            },
                                                            onOpenTitles = {
                                                                backStack.add(SheetRoute.Titles)
                                                            },
                                                            onOpenCertificates = {
                                                                backStack.add(SheetRoute.Certificates)
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
                                            ProfileScreen(
                                                viewModel = profileViewModel,
                                                onOpenAppelli = { courseKey ->
                                                    bookableExamsViewModel.requestFocus(courseKey)
                                                    scope.launch {
                                                        pagerState.scrollToPage(ShellTab.Registry.ordinal)
                                                    }
                                                    backStack.add(SheetRoute.Appelli)
                                                },
                                            )
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
                                                    backStack.add(
                                                        SheetRoute.AssignmentDetail(
                                                            assignId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenQuiz = { id ->
                                                    backStack.add(
                                                        SheetRoute.QuizDetail(
                                                            quizId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenForum = { id ->
                                                    backStack.add(
                                                        SheetRoute.Forum(
                                                            forumId = id.value,
                                                            courseId = key.courseId,
                                                        )
                                                    )
                                                },
                                                onOpenDiscussion = { forumId, discussionId ->
                                                    backStack.add(
                                                        SheetRoute.Forum(
                                                            forumId = forumId.value,
                                                            courseId = key.courseId,
                                                            initialDiscussionId = discussionId.value,
                                                        )
                                                    )
                                                },
                                                onOpenVideo = { cmId, title ->
                                                    backStack.add(
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

                                        /**
                                         * True when the viewer was opened from a modal sheet and sits directly
                                         * above the sheet's entries. Predictive back cannot scrub into an
                                         * overlay scene (the sheet is its own window), so in that layering back
                                         * commits a plain pop instead — gesture and button both return cleanly
                                         * to the sheet.
                                         */
                                        val overSheet by remember {
                                            derivedStateOf {
                                                backStack.getOrNull(backStack.lastIndex - 1) is SheetRoute
                                            }
                                        }
                                        BackHandler(enabled = overSheet) { backStack.removeLastOrNull() }
                                        SubPage(topInset, immersive = true) {
                                            FileViewerScreen(
                                                onOpenFile = openFile,
                                                onClose = { backStack.removeLastOrNull() },
                                                viewModel = vm,
                                            )
                                        }
                                    }

                                    entry<AppRoute.VideoPlayback> { key ->
                                        val vm =
                                            hiltViewModel<VideoPlayerViewModel, VideoPlayerViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset, immersive = true) {
                                            VideoPlayerScreen(
                                                courseId = key.courseId,
                                                cmId = key.cmId,
                                                onBack = { backStack.removeLastOrNull() },
                                                viewModel = vm,
                                            )
                                        }
                                    }
                                    entry<AppRoute.TeacherDetail> { key ->
                                        SubPage(topInset) { TeacherDetailScreen(teacherCode = key.teacherCode) }
                                    }

                                    entry<SheetRoute.Enrollments>(
                                        metadata = sheetEntry("enrollments") {
                                            val history by enrollmentsViewModel.history
                                                .collectAsStateWithLifecycle()
                                            SheetHeaderSpec(
                                                title = stringResource(R.string.registry_enrollments),
                                                subtitle = history.valueOrNull()
                                                    ?.let(::enrollmentsHeaderSubtitle),
                                            )
                                        },
                                    ) {
                                        EnrollmentsTimelinePage(
                                            viewModel = enrollmentsViewModel,
                                            onOpenDetail = { id ->
                                                backStack.add(SheetRoute.EnrollmentDetail(id.value))
                                            },
                                        )
                                    }
                                    entry<SheetRoute.EnrollmentDetail>(
                                        metadata = sheetEntry("enrollments") {
                                            val top =
                                                backStack.lastOrNull() as? SheetRoute.EnrollmentDetail
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
                                            }
                                        },
                                    ) { key ->
                                        val history by enrollmentsViewModel.history
                                            .collectAsStateWithLifecycle()
                                        val enrollment = history.valueOrNull()?.years
                                            ?.firstOrNull { it.id.value == key.enrollmentId }
                                        LaunchedEffect(enrollment == null) {
                                            if (enrollment == null) backStack.removeLastOrNull()
                                        }
                                        if (enrollment != null) {
                                            EnrollmentDetailPage(enrollment = enrollment)
                                        }
                                    }
                                    entry<SheetRoute.Titles>(
                                        metadata = sheetEntry("titles") {
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
                                                backStack.add(
                                                    SheetRoute.TitleDetail(
                                                        id
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.TitleDetail>(
                                        metadata = sheetEntry("titles") {
                                            val top =
                                                backStack.lastOrNull() as? SheetRoute.TitleDetail
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
                                            }
                                        },
                                    ) { key ->
                                        val titles by titlesViewModel.titles
                                            .collectAsStateWithLifecycle()
                                        val title = titles.valueOrNull()
                                            ?.firstOrNull { it.id == key.titleId }
                                        LaunchedEffect(title == null) {
                                            if (title == null) backStack.removeLastOrNull()
                                        }
                                        if (title != null) TitleDetailPage(title = title)
                                    }
                                    entry<SheetRoute.Certificates>(
                                        metadata = sheetEntry("certificates"),
                                    ) {
                                        CertificatesPage(viewModel = certificatesViewModel)
                                    }
                                    entry<SheetRoute.Refunds>(
                                        metadata = sheetEntry("refunds") {
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
                                                backStack.add(
                                                    SheetRoute.RefundDetail(
                                                        key
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.RefundDetail>(
                                        metadata = sheetEntry("refunds") {
                                            val top =
                                                backStack.lastOrNull() as? SheetRoute.RefundDetail
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
                                            }
                                        },
                                    ) { key ->
                                        val refunds by refundsViewModel.refunds
                                            .collectAsStateWithLifecycle()
                                        val refund = refunds.valueOrNull()
                                            ?.firstOrNull { it.refundKey() == key.refundKey }
                                        LaunchedEffect(refund == null) {
                                            if (refund == null) backStack.removeLastOrNull()
                                        }
                                        if (refund != null) RefundDetailPage(refund = refund)
                                    }
                                    entry<SheetRoute.Isee>(
                                        metadata = sheetEntry("isee") {
                                            val state by taxesViewModel.isee
                                                .collectAsStateWithLifecycle()
                                            val declarations = state.valueOrNull()
                                                ?.filter { it.isee != null && it.academicYearEnrollmentId != null }
                                            SheetHeaderSpec(
                                                title = stringResource(R.string.registry_isee),
                                                subtitle = declarations?.let(::iseeHeaderSubtitle),
                                            )
                                        },
                                    ) {
                                        IseeDeclarationsPage(
                                            viewModel = taxesViewModel,
                                            onOpenDetail = { year ->
                                                backStack.add(
                                                    SheetRoute.IseeDetail(
                                                        year
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.IseeDetail>(
                                        metadata = sheetEntry("isee") {
                                            val top =
                                                backStack.lastOrNull() as? SheetRoute.IseeDetail
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
                                            }
                                        },
                                    ) { key ->
                                        val state by taxesViewModel.isee
                                            .collectAsStateWithLifecycle()
                                        val declaration = state.valueOrNull()
                                            ?.firstOrNull { it.academicYearEnrollmentId == key.year }
                                        LaunchedEffect(declaration == null) {
                                            if (declaration == null) backStack.removeLastOrNull()
                                        }
                                        if (declaration != null) IseeDetailPage(declaration = declaration)
                                    }
                                    entry<SheetRoute.ExamResults>(
                                        metadata = sheetEntry("examResults"),
                                    ) {
                                        ExamResultsPage(viewModel = examResultsViewModel)
                                    }
                                    entry<SheetRoute.Taxes>(
                                        metadata = sheetEntry("taxes") {
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
                                    entry<SheetRoute.QuizDetail>(
                                        metadata = sheetEntry("quiz"),
                                    ) { key ->
                                        QuizDetailPage(
                                            quizId = key.quizId,
                                            courseId = key.courseId,
                                        )
                                    }
                                    entry<SheetRoute.Forum>(
                                        metadata = sheetEntry("forum"),
                                    ) { key ->
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
                                    entry<SheetRoute.AssignmentDetail>(
                                        metadata = sheetEntry("assignment"),
                                    ) { key ->
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
                                    entry<SheetRoute.Attendance>(
                                        metadata = sheetEntry("attendance"),
                                    ) {
                                        AttendancePage(viewModel = attendanceViewModel)
                                    }
                                    entry<SheetRoute.Appelli>(
                                        metadata = sheetEntry("appelli"),
                                    ) {
                                        AppelliPage(
                                            bookableViewModel = bookableExamsViewModel,
                                            viewModel = bookedExamsViewModel,
                                        )
                                    }
                                    entry<SheetRoute.StudyPlan>(
                                        metadata = sheetEntry("studyPlan"),
                                    ) {
                                        StudyPlanPage(viewModel = studyPlanViewModel)
                                    }
                                    entry<SheetRoute.Questionnaires>(
                                        metadata = sheetEntry("questionnaires"),
                                    ) {
                                        QuestionnairesPage(viewModel = questionnairesViewModel)
                                    }
                                    entry<SheetRoute.Appointments>(
                                        metadata = sheetEntry("appointments"),
                                    ) {
                                        AppointmentsPage(
                                            viewModel = appointmentsViewModel,
                                            onOpenPdf = { path, name ->
                                                backStack.add(
                                                    AppRoute.FileViewer(
                                                        fileName = name,
                                                        localPath = path,
                                                        mimeType = "application/pdf",
                                                    )
                                                )
                                            },
                                        )
                                    }
                                    entry<SheetRoute.Library>(
                                        metadata = sheetEntry("library"),
                                    ) {
                                        LibraryPage(viewModel = libraryViewModel)
                                    }
                                    entry<SheetRoute.FileOpenChooser>(
                                        metadata = sheetEntry(chooserHostGroup ?: "fileChooser"),
                                    ) { key ->
                                        val kind = remember(key) {
                                            FileKind.classify(key.file.fileName, key.file.mimeType)
                                        }
                                        FileOpenChooserContent(
                                            fileName = key.file.fileName,
                                            sizeBytes = key.file.sizeBytes,
                                            kind = kind,
                                            onChoose = { choice, rememberChoice ->
                                                kind.preferenceKey
                                                    ?.takeIf { rememberChoice }
                                                    ?.let { fileOpenViewModel.remember(it, choice) }
                                                backStack.removeLastOrNull()
                                                when (choice) {
                                                    FileOpenChoice.InApp -> backStack.add(key.file)
                                                    FileOpenChoice.External -> externalFile =
                                                        key.file
                                                }
                                            },
                                        )
                                    }
                                },
                            )
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
                                    step is SearchNavStep.SwitchTab && step.tab == tab && backStack.size == 1
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
                                                while (backStack.size > 1) backStack.removeAt(
                                                    backStack.lastIndex
                                                )
                                                pagerState.scrollToPage(step.tab.ordinal)
                                            }

                                            is SearchNavStep.PushPage -> backStack.add(step.route)
                                            is SearchNavStep.PushSheet -> backStack.add(step.route)

                                            SearchNavStep.OpenAccountSwitcher ->
                                                showAccountSwitcher = true

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

            if (showAccountSwitcher) {
                AccountSwitcherSheet(
                    onDismiss = { showAccountSwitcher = false },
                    onOpenProfile = { backStack.add(AppRoute.Profile) },
                    onOpenSettings = { backStack.add(AppRoute.Settings) },
                    viewModel = accountViewModel,
                )
            }

            officeFile?.let { (app, route) ->
                OfficeOpenSheet(
                    app = app,
                    route = route,
                    onDismiss = { officeFile = null },
                )
            }

            externalFile?.let { route ->
                ExternalFileLauncher(
                    route = route,
                    onFinished = { externalFile = null },
                )
            }

            calendarReservationEvent?.let { event ->
                val dismiss = { calendarReservationEvent = null }

                /**
                 * Container contract of the nested reservation-management modal, mirroring
                 * [BottomSheetSceneStrategy]: the page drives gesture locking / dismiss vetoes
                 * through LocalSheetDismissControl, exactly as it does when hosted as a shell sheet.
                 */
                val control = remember { SheetDismissControl(dismiss = dismiss) }
                PredictiveModalBottomSheet(
                    onDismiss = dismiss,
                    gesturesEnabled = control.gesturesEnabled,
                    confirmDismiss = { control.confirmDismiss() },
                ) { _, _ ->
                    CompositionLocalProvider(LocalSheetDismissControl provides control) {
                        when (event) {
                            is CalendarEvent.Exam -> AppelliPage(
                                bookableViewModel = bookableExamsViewModel,
                                viewModel = bookedExamsViewModel,
                            )

                            is CalendarEvent.Appointment -> AppointmentsPage(
                                viewModel = appointmentsViewModel,
                                onOpenPdf = { path, name ->
                                    backStack.add(
                                        AppRoute.FileViewer(
                                            fileName = name,
                                            localPath = path,
                                            mimeType = "application/pdf",
                                        )
                                    )
                                },
                            )

                            is CalendarEvent.LibraryReservation -> LibraryPage(
                                viewModel = libraryViewModel,
                            )

                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sheet group of a back-stack key, mirroring the `sheetEntry(...)` literals in the entryProvider —
 * keep the two in sync when adding a sheet entry. Used by the file-open chooser to join the sheet
 * it was opened from.
 */
private fun sheetGroupOf(key: Any?): String? = when (key) {
    SheetRoute.Enrollments, is SheetRoute.EnrollmentDetail -> "enrollments"
    SheetRoute.Titles, is SheetRoute.TitleDetail -> "titles"
    SheetRoute.Certificates -> "certificates"
    SheetRoute.Refunds, is SheetRoute.RefundDetail -> "refunds"
    SheetRoute.Isee, is SheetRoute.IseeDetail -> "isee"
    SheetRoute.ExamResults -> "examResults"
    SheetRoute.Taxes -> "taxes"
    is SheetRoute.QuizDetail -> "quiz"
    is SheetRoute.Forum -> "forum"
    is SheetRoute.AssignmentDetail -> "assignment"
    SheetRoute.Attendance -> "attendance"
    SheetRoute.Appelli -> "appelli"
    SheetRoute.StudyPlan -> "studyPlan"
    SheetRoute.Questionnaires -> "questionnaires"
    SheetRoute.Appointments -> "appointments"
    SheetRoute.Library -> "library"
    else -> null
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
