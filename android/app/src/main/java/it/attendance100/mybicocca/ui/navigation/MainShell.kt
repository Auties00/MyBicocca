package it.attendance100.mybicocca.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.ui.component.bar.BottomBarItem
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaBottomBar
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaTopBar
import it.attendance100.mybicocca.ui.component.bar.TopBarSearchState
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarHost
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.rememberAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.transitions.LocalAnimatedContentScope
import it.attendance100.mybicocca.ui.navigation.transitions.LocalSharedTransitionScope
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherSheet
import it.attendance100.mybicocca.ui.screen.calendar.CalendarScreen
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewModel
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.teacherDetail.TeacherDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.ConversationDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.ConversationDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.DiscussionDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.DiscussionDetailViewModel
import it.attendance100.mybicocca.data.local.settings.FileOpenChoice
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.ExternalFileLauncher
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileOpenPreferenceViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.FileViewerViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.FileKind
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.state.OfficeApp
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.officeOpen.OfficeOpenSheet
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.subscreen.openChooser.FileOpenChooserSheet
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.ForumDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.ForumDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.MessagingScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailPage
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerViewModel
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.map.MapViewModel
import it.attendance100.mybicocca.ui.screen.map.subscreen.room360.Room360Screen
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.profile.ProfileViewModel
import it.attendance100.mybicocca.ui.screen.registry.RegistryScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendancePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendanceViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.AppelliPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.certificates.CertificatesViewModel
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.navigation.scene.BottomSheetSceneStrategy
import it.attendance100.mybicocca.ui.navigation.scene.SheetHeaderSpec
import it.attendance100.mybicocca.ui.navigation.scene.sheetEntry
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.AppointmentsViewModel
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxesHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitleDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesListPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.TitlesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.headline
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.headlineSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.titlesHeaderSubtitle
import it.attendance100.mybicocca.ui.screen.search.SearchOverlay
import it.attendance100.mybicocca.ui.screen.search.SearchViewModel
import it.attendance100.mybicocca.ui.screen.settings.SettingsScreen
import kotlinx.coroutines.launch

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
    // One pager hosts all four tabs and keeps them composed (see beyondViewportPageCount below),
    // so switching is instant. The pager state (saveable) is the source of truth for the selected
    // tab; it lives here in the shell body (NOT inside the TabRoot entry) so it survives the
    // entry being disposed/recomposed when a sub-page is on top.
    val pagerState = rememberPagerState(
        initialPage = ShellTab.Calendar.ordinal,
        pageCount = { ShellTab.entries.size },
    )
    val scope = rememberCoroutineScope()
    val tab = ShellTab.entries[pagerState.currentPage]
    val photo by accountViewModel.userPhoto.collectAsStateWithLifecycle()

    // Warm Coil's cache for every stored account's avatar as soon as the shell loads, so the
    // account switcher renders photos with no placeholder flash.
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
    // Hoisted so the Segreterie landing can derive its status badges and the scadenzario
    // deadline spine from the exam outcomes, and the Esiti sub-page shares the same fetch.
    val examResultsViewModel: ExamResultsViewModel = hiltViewModel()
    // Hoisted here so the tax fetch starts on shell load and the list / detail / ISEE
    // destinations share one in-memory result (taxes are not cached to Room).
    val taxesViewModel: TaxesViewModel = hiltViewModel()
    // Hoisted so the compilation sub-page can refresh the questionnaire list after a
    // confirmed submission (questionnaires are not cached to Room).
    val questionnairesViewModel: QuestionnairesViewModel = hiltViewModel()
    // Hoisted so the whole Appuntamenti modal (reservations + booking wizard) shares one
    // owner; opened as a shell sheet rather than a back-stack route.
    val appointmentsViewModel: AppointmentsViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    // Hoisted so the sheet entries share one VM that outlives the sheet, like the other
    // shell-scoped sheet ViewModels above.
    val enrollmentsViewModel: EnrollmentsViewModel = hiltViewModel()
    val titlesViewModel: TitlesViewModel = hiltViewModel()
    val certificatesViewModel: CertificatesViewModel = hiltViewModel()
    val refundsViewModel: RefundsViewModel = hiltViewModel()
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val studyPlanViewModel: StudyPlanViewModel = hiltViewModel()
    // Hoisted so the transcript refresh (kicked off in the VM's init) starts on shell load,
    // not when the Profile sub-page is first opened — the stats/badge are then already warm.
    val profileViewModel: ProfileViewModel = hiltViewModel()

    // Unified search: one ViewModel feeds both the bar's text field and the full-screen
    // overlay body, so it is hoisted at shell level like the tab ViewModels above.
    val searchViewModel: SearchViewModel = hiltViewModel()

    // Navigation 3 back stack. TabRoot is always the root; sub-pages are pushed on top of it.
    // The four tabs live in the pager hosted INSIDE the TabRoot entry, so a list ticket and a
    // detail ticket are both inside NavDisplay's AnimatedContent — that is what makes the
    // list -> detail shared-element morph seek with the predictive-back gesture.
    val backStack = rememberNavBackStack(AppRoute.TabRoot)
    val currentRoute = backStack.lastOrNull() as? AppRoute

    // Multi-page modal sheets ride this same back stack as overlay scenes (see SheetRoute /
    // BottomSheetSceneStrategy). pop(n) closes or steps a sheet by removing n trailing entries.
    val bottomSheetStrategy = remember {
        BottomSheetSceneStrategy<NavKey>(pop = { count -> repeat(count) { backStack.removeLastOrNull() } })
    }

    val presenceDeepLinkViewModel: PresenceDeepLinkViewModel = hiltViewModel()
    val pendingPresenceScan by presenceDeepLinkViewModel.pending.collectAsStateWithLifecycle()

    val isOnSubPage = currentRoute?.isSubPage == true
    val subPageTitle = (currentRoute?.appTitle as? AppTitle.SubPage)?.title
    // Video playback and the file viewer are immersive: the global chrome is hidden and the
    // page goes edge to edge (the file viewer draws its own Custom-Tab-style top bar).
    val immersive = currentRoute is AppRoute.VideoPlayback || currentRoute is AppRoute.FileViewer

    val motion = MaterialTheme.motionScheme
    val enterTransition = remember(motion) { defaultEnterTransition(motion) }
    val exitTransition = remember(motion) { defaultExitTransition(motion) }
    val popEnterTransition = remember(motion) { defaultPopEnterTransition(motion) }
    val popExitTransition = remember(motion) { defaultPopExitTransition(motion) }

    // Two independent drivers feed the chrome morph; the bar / bottom bar combine them as max().
    //  - navProgress: how far a sub-page covers the tab root (0 = on a tab, 1 = sub-page on top).
    //    It is driven by the NavDisplay's OWN TabRoot<->sub-page transition (published from the
    //    TabRoot entry below via animateFloat on that entry's transition), so the bar expand and
    //    the bottom-bar slide-off seek in lockstep with the page slide — including while the
    //    predictive-back gesture is scrubbing it, which a commit-time spring could never track.
    //  - searchProgress: the search field open/close, scrubbed by the bar's own predictive-back
    //    handler. Search is page-only, so the two never both drive the morph at the same time.
    // Seeded from the restored back stack: after an activity recreation (process death, or
    // a config change not declared in the manifest, e.g. fontScale/density) the stack can
    // come back with a sub-page already on top and NO transition — the TabRoot entry
    // (which publishes this fraction) never composes, so a 0f initial would leave the bar
    // collapsed on a sub-page.
    val navProgress = remember { mutableFloatStateOf(if (isOnSubPage) 1f else 0f) }
    val searchProgress = remember { Animatable(0f) }

    var showAccountSwitcher by remember { mutableStateOf(false) }
    // The Office install prompt — only shown when the matching Microsoft app is missing.
    var officeFile by remember { mutableStateOf<Pair<OfficeApp, AppRoute.FileViewer>?>(null) }
    // External hand-off (download + ACTION_VIEW): PDFs go to the default reader, Office to the
    // installed app, and any file the user chose to open externally.
    var externalFile by remember { mutableStateOf<AppRoute.FileViewer?>(null) }
    // The in-app/external chooser for a file type with no remembered choice (or a long-press).
    var chooserFile by remember { mutableStateOf<AppRoute.FileViewer?>(null) }

    // An Affluences confirm/cancel email link opened in the app: open the Biblioteca sheet so its
    // snackbar shows the result of the action the ViewModel is running.
    LaunchedEffect(Unit) {
        libraryViewModel.openSheetRequests.collect {
            if (backStack.lastOrNull() != SheetRoute.Library) backStack.add(SheetRoute.Library)
        }
    }
    // A mod_attendance QR scanned outside the app lands here: surface the presenze
    // sheet so its ViewModel can run the marking flow on the pending link.
    LaunchedEffect(pendingPresenceScan) {
        if (pendingPresenceScan != null && backStack.lastOrNull() != SheetRoute.Attendance) {
            backStack.add(SheetRoute.Attendance)
        }
    }

    val fileOpenViewModel: FileOpenPreferenceViewModel = hiltViewModel()
    val fileOpenChoices by fileOpenViewModel.choices.collectAsStateWithLifecycle()

    // Decides how a tapped file opens. In-app-capable kinds honour a remembered choice or, when
    // none (or on a long-press), show the chooser. Unknown has no in-app viewer so it hands off;
    // Office opens in its app when installed, else shows the install prompt.
    val openFile: (AppRoute.FileViewer, Boolean) -> Unit = { route, forceChooser ->
        when (val kind = FileKind.classify(route.fileName, route.mimeType)) {
            // Office has no in-app viewer and ACTION_VIEW doesn't reliably open it, so always
            // show the hand-off sheet: it opens the file directly in the Microsoft app via the
            // documented ms-*:ofv protocol (and offers install / another app as fallbacks).
            is FileKind.Office -> officeFile = kind.app to route
            FileKind.Unknown -> externalFile = route
            else -> {
                val remembered = kind.preferenceKey?.let { fileOpenChoices[it] }
                when {
                    forceChooser || remembered == null -> chooserFile = route
                    remembered == FileOpenChoice.InApp -> backStack.add(route)
                    remembered == FileOpenChoice.External -> externalFile = route
                }
            }
        }
    }
    // Non-long-press adapter for callers that don't carry a chooser flag (nested zip viewer).
    val openFileSimple: (AppRoute.FileViewer) -> Unit = { openFile(it, false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    // Query and dictation live in the SearchViewModel (the query is SavedStateHandle-backed
    // there); the shell only owns the open/closed flag that drives the bar morph.
    val searchQuery by searchViewModel.query.collectAsStateWithLifecycle()
    val searchDictating by searchViewModel.dictating.collectAsStateWithLifecycle()
    var filterToggle by remember { mutableStateOf<(() -> Unit)?>(null) }
    var filterActive by remember { mutableStateOf(false) }
    // Null = use the route's static title; non-null = sub-page is driving it at runtime.
    var subPageTitleOverride by remember { mutableStateOf<String?>(null) }
    // The active sub-page's trailing action, hoisted so the global top bar can render it. The
    // lambda is published by the screen and captures the screen's own ViewModel, so it stays
    // correctly scoped even when invoked from the shell-level bar.
    var subPageActions by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    // Reset search/filter only when the settled tab actually changes after first composition.
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

    // Dictation starts on mic tap once RECORD_AUDIO is granted; the system prompt fires on
    // first use and starts listening immediately on grant.
    val recordAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) searchViewModel.startDictation() }

    val shellKeyboardController = LocalSoftwareKeyboardController.current
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
            // Voice replaces typing: the IME would just sit under the dictation dialog.
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

    val bottomBarItems = remember {
        ShellTab.entries.map { BottomBarItem(key = it, label = it.label, icon = it.icon) }
    }

    val snackbarController = rememberAppSnackbarController()

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
                        // Pages that draw behind the bar (course detail) keep it see-through
                        // until they publish a runtime title — i.e. until their own hero
                        // headline has scrolled past and the bar must take over as the header.
                        transparentBackground = currentRoute?.extendsBehindTopBar == true &&
                                subPageTitleOverride == null,
                    )
                },
                bottomBar = {
                    MyBicoccaBottomBar(
                        items = bottomBarItems,
                        selected = tab,
                        onSelect = { selected ->
                            // Always pop the sub-stack first — switching tabs (or re-tapping the
                            // current tab) should land you at TabRoot, never deep on a sub-page.
                            while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                            // Instant jump (no scroll-through of intermediate pages); the pages are
                            // already composed, so this is a cheap show/hide, not a rebuild.
                            scope.launch { pagerState.scrollToPage(selected.ordinal) }
                        },
                        translationY = maxOf(navProgress.floatValue, searchProgress.value) * 300f,
                    )
                },
                snackbarHost = { AppSnackbarHost(controller = snackbarController) },
            ) { innerPadding ->
                val topInset = innerPadding.calculateTopPadding()
                Box(modifier = Modifier.fillMaxSize()) {
                    // SharedTransitionLayout wraps the NavDisplay so shared elements can morph between
                    // NavEntry instances. The tab pager is hosted inside the TabRoot entry (below) so its
                    // list tickets share NavDisplay's AnimatedContent scope with the detail entry.
                    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            NavDisplay(
                                backStack = backStack,
                                onBack = { backStack.removeLastOrNull() },
                                modifier = Modifier.fillMaxSize(),
                                sceneStrategies = listOf(bottomSheetStrategy, SinglePaneSceneStrategy()),
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                ),
                                transitionSpec = { enterTransition togetherWith exitTransition },
                                popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
                                entryProvider = entryProvider {
                                    // Root: the four-tab pager. Bridges NavDisplay's AnimatedContentScope into
                                    // the app's LocalAnimatedContentScope so list tickets can be true shared
                                    // elements that seek into the detail entry.
                                    entry<AppRoute.TabRoot> {
                                        val tabRootScope = LocalNavAnimatedContentScope.current
                                        // Publish the bar/bottom-bar morph fraction off THIS entry's enter/exit.
                                        // animateFloat rides the same (seekable) transition that slides the page
                                        // and seeks the shared elements, so the chrome tracks the predictive-back
                                        // gesture frame-for-frame. presence is 1 when TabRoot fully covers the
                                        // screen and 0 once a sub-page has fully replaced it.
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
                                            // All four tabs stay composed (beyondViewportPageCount = size - 1)
                                            // so switching is instant. User swipe is disabled: Registry hosts
                                            // its own pager and the map pans horizontally — the bottom bar
                                            // drives page changes.
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
                                                // The map renders behind the floating top bar; other tabs
                                                // inset under both bars.
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
                                                            // The calendar's popup-window chrome must hide
                                                            // for BOTH covers: sub-page push and search open.
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
                                                            onOpenRoom360 = { url, roomName ->
                                                                backStack.add(
                                                                    AppRoute.Room360View(
                                                                        url = url,
                                                                        roomName = roomName
                                                                    )
                                                                )
                                                            },
                                                        )

                                                        ShellTab.Registry -> RegistryScreen(
                                                            bookedExamsViewModel = bookedExamsViewModel,
                                                            bookableExamsViewModel = bookableExamsViewModel,
                                                            taxesViewModel = taxesViewModel,
                                                            examResultsViewModel = examResultsViewModel,
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

                                    // First-level sub-pages (no arguments).
                                    entry<AppRoute.Profile> {
                                        SubPage(topInset) {
                                            ProfileScreen(
                                                viewModel = profileViewModel,
                                                // Deep-link from a libretto course to its bookable
                                                // appelli: arm the focus on the shell-scoped bookable
                                                // VM, land on the Servizi tab behind, then open the
                                                // Appelli sheet, which enters its booking flow on the
                                                // pending focus, scrolls to the exam section and lets
                                                // the user pick an appello.
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
                                    entry<AppRoute.Messaging> { SubPage(topInset) { MessagingScreen() } }

                                    // First-level with arguments.
                                    entry<AppRoute.Room360View> { key ->
                                        SubPage(topInset) {
                                            Room360Screen(
                                                url = key.url,
                                                roomName = key.roomName
                                            )
                                        }
                                    }
                                    entry<AppRoute.CourseDetail> { key ->
                                        val vm =
                                            hiltViewModel<CourseDetailViewModel, CourseDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset, extendBehindBar = key.extendsBehindTopBar) {
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
                                                        AppRoute.ForumDetail(
                                                            forumId = id.value,
                                                            courseId = key.courseId
                                                        )
                                                    )
                                                },
                                                onOpenDiscussion = { id ->
                                                    backStack.add(
                                                        AppRoute.DiscussionDetail(discussionId = id.value)
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
                                        SubPage(topInset, immersive = true) {
                                            FileViewerScreen(
                                                // Zip entries re-dispatch through a nested viewer
                                                // (or the office/external hand-off, via the shell).
                                                onOpenFile = openFileSimple,
                                                onClose = { backStack.removeLastOrNull() },
                                                viewModel = vm,
                                            )
                                        }
                                    }

                                    // Deeper sub-pages.
                                    entry<AppRoute.QuizDetail> { key ->
                                        val vm =
                                            hiltViewModel<QuizDetailViewModel, QuizDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            QuizDetailScreen(
                                                quizId = key.quizId,
                                                courseId = key.courseId,
                                                onExit = { backStack.removeLastOrNull() },
                                                viewModel = vm
                                            )
                                        }
                                    }
                                    entry<AppRoute.ForumDetail> { key ->
                                        val vm =
                                            hiltViewModel<ForumDetailViewModel, ForumDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            ForumDetailScreen(
                                                forumId = key.forumId,
                                                courseId = key.courseId,
                                                onOpenDiscussion = { id ->
                                                    backStack.add(
                                                        AppRoute.DiscussionDetail(discussionId = id.value)
                                                    )
                                                },
                                                viewModel = vm
                                            )
                                        }
                                    }
                                    entry<AppRoute.DiscussionDetail> { key ->
                                        val vm =
                                            hiltViewModel<DiscussionDetailViewModel, DiscussionDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            DiscussionDetailScreen(
                                                discussionId = key.discussionId,
                                                viewModel = vm
                                            )
                                        }
                                    }
                                    entry<AppRoute.ConversationDetail> { key ->
                                        val vm =
                                            hiltViewModel<ConversationDetailViewModel, ConversationDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            ConversationDetailScreen(
                                                conversationId = key.conversationId,
                                                viewModel = vm
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

                                    // --- Modal bottom-sheet pages: rendered as overlay sheets over the
                                    // current tab by BottomSheetSceneStrategy, grouped by metadata "group". ---
                                    entry<SheetRoute.Enrollments>(
                                        metadata = sheetEntry("enrollments") {
                                            val history by enrollmentsViewModel.history
                                                .collectAsStateWithLifecycle()
                                            SheetHeaderSpec(
                                                title = "Iscrizioni",
                                                subtitle = history.valueOrNull()?.let(::enrollmentsHeaderSubtitle),
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
                                            // Resolved from live VM history against the top key, so an evicted
                                            // year (e.g. a career switch) gracefully shows no header.
                                            val top = backStack.lastOrNull() as? SheetRoute.EnrollmentDetail
                                            val history by enrollmentsViewModel.history
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                history.valueOrNull()?.years
                                                    ?.firstOrNull { it.id.value == k.enrollmentId }
                                            }?.let { enrollment ->
                                                SheetHeaderSpec(
                                                    title = "Iscrizione ${enrollment.academicYearLabel()}",
                                                    subtitle = "${enrollment.courseYearLabel()} · ${enrollment.statusLabel()}",
                                                )
                                            }
                                        },
                                    ) { key ->
                                        val history by enrollmentsViewModel.history
                                            .collectAsStateWithLifecycle()
                                        val enrollment = history.valueOrNull()?.years
                                            ?.firstOrNull { it.id.value == key.enrollmentId }
                                        // The year can be evicted under an open detail: fall back to the
                                        // timeline instead of rendering a stale snapshot.
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
                                                title = "Titoli",
                                                subtitle = titles.valueOrNull()?.let(::titlesHeaderSubtitle),
                                            )
                                        },
                                    ) {
                                        TitlesListPage(
                                            viewModel = titlesViewModel,
                                            onOpenDetail = { id -> backStack.add(SheetRoute.TitleDetail(id)) },
                                        )
                                    }
                                    entry<SheetRoute.TitleDetail>(
                                        metadata = sheetEntry("titles") {
                                            val top = backStack.lastOrNull() as? SheetRoute.TitleDetail
                                            val titles by titlesViewModel.titles
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                titles.valueOrNull()?.firstOrNull { it.id == k.titleId }
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
                                                title = "Rimborsi",
                                                subtitle = refunds.valueOrNull()?.let(::refundsHeaderSubtitle),
                                            )
                                        },
                                    ) {
                                        RefundsListPage(
                                            viewModel = refundsViewModel,
                                            onOpenDetail = { key -> backStack.add(SheetRoute.RefundDetail(key)) },
                                        )
                                    }
                                    entry<SheetRoute.RefundDetail>(
                                        metadata = sheetEntry("refunds") {
                                            val top = backStack.lastOrNull() as? SheetRoute.RefundDetail
                                            val refunds by refundsViewModel.refunds
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                refunds.valueOrNull()?.firstOrNull { it.refundKey() == k.refundKey }
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
                                                title = "ISEE",
                                                subtitle = declarations?.let(::iseeHeaderSubtitle),
                                            )
                                        },
                                    ) {
                                        IseeDeclarationsPage(
                                            viewModel = taxesViewModel,
                                            onOpenDetail = { year -> backStack.add(SheetRoute.IseeDetail(year)) },
                                        )
                                    }
                                    entry<SheetRoute.IseeDetail>(
                                        metadata = sheetEntry("isee") {
                                            val top = backStack.lastOrNull() as? SheetRoute.IseeDetail
                                            val state by taxesViewModel.isee
                                                .collectAsStateWithLifecycle()
                                            top?.let { k ->
                                                state.valueOrNull()?.firstOrNull { it.academicYearEnrollmentId == k.year }
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
                                                title = "Tasse",
                                                subtitle = state.valueOrNull()?.let(::taxesHeaderSubtitle),
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
                                            onStartAttempt = {
                                                backStack.removeLastOrNull()
                                                backStack.add(
                                                    AppRoute.QuizDetail(
                                                        quizId = key.quizId,
                                                        courseId = key.courseId,
                                                        startNew = true,
                                                    )
                                                )
                                            },
                                            onResumeAttempt = { attemptId ->
                                                backStack.removeLastOrNull()
                                                backStack.add(
                                                    AppRoute.QuizDetail(
                                                        quizId = key.quizId,
                                                        courseId = key.courseId,
                                                        resumeAttemptId = attemptId,
                                                    )
                                                )
                                            },
                                            onReviewAttempt = { attemptId ->
                                                backStack.removeLastOrNull()
                                                backStack.add(
                                                    AppRoute.QuizDetail(
                                                        quizId = key.quizId,
                                                        courseId = key.courseId,
                                                        reviewAttemptId = attemptId,
                                                    )
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
                                            onOpenPage = { url ->
                                                runCatching {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_VIEW, url.toUri())
                                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                                    )
                                                }
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
                                },
                            )
                        }
                    }

                    // Full-screen search body (M3 search view). Drawn AFTER the NavDisplay so it
                    // covers the active tab, but still under the Scaffold's top bar — the bar's
                    // search field stays interactive above it. It rides searchProgress, so the
                    // predictive-back gesture that scrubs the bar collapse scrubs it in lockstep.
                    // Composed only while open (or animating) to keep the idle tabs untouched.
                    if (searchActive || searchProgress.value > 0f) {
                        val keyboardController = LocalSoftwareKeyboardController.current
                        fun closeSearch() {
                            keyboardController?.hide()
                            searchActive = false
                            searchViewModel.reset()
                        }

                        // Tab landings close search (the destination IS the page you asked
                        // for); sub-page pushes keep it alive underneath, so popping back
                        // returns to the search view with query, results and scroll intact.
                        fun openSubPage(route: AppRoute) {
                            keyboardController?.hide()
                            backStack.add(route)
                        }

                        fun openDestination(destination: SearchDestination) {
                            when (val landing = destination.toLanding()) {
                                is SearchLanding.Tab -> {
                                    scope.launch { pagerState.scrollToPage(landing.tab.ordinal) }
                                    closeSearch()
                                }

                                is SearchLanding.SubPage -> openSubPage(landing.route)

                                // Sheets land on their owning tab like a tab hit, then
                                // animate in over it.
                                is SearchLanding.Sheet -> {
                                    scope.launch { pagerState.scrollToPage(landing.hostTab.ordinal) }
                                    closeSearch()
                                    when (landing.sheet) {
                                        ShellSheet.StudyPlan -> backStack.add(SheetRoute.StudyPlan)
                                        ShellSheet.Attendance -> backStack.add(SheetRoute.Attendance)
                                        ShellSheet.Enrollments -> backStack.add(SheetRoute.Enrollments)
                                        ShellSheet.Titles -> backStack.add(SheetRoute.Titles)
                                        ShellSheet.Certificates -> backStack.add(SheetRoute.Certificates)
                                        ShellSheet.BookedExams -> backStack.add(SheetRoute.Appelli)
                                        ShellSheet.ExamResults -> backStack.add(SheetRoute.ExamResults)
                                        ShellSheet.Questionnaires -> backStack.add(SheetRoute.Questionnaires)
                                        ShellSheet.Appointments -> backStack.add(SheetRoute.Appointments)
                                        ShellSheet.Taxes -> backStack.add(SheetRoute.Taxes)
                                    }
                                }
                            }
                        }

                        SearchOverlay(
                            viewModel = searchViewModel,
                            progress = searchProgress.value,
                            subPageProgress = navProgress.floatValue,
                            topInset = topInset,
                            onOpenResult = { result ->
                                // Opening a hit is a successful search — persist the query.
                                searchViewModel.commitToHistory()
                                when (result) {
                                    is SearchResult.Destination -> openDestination(result.destination)

                                    is SearchResult.Course ->
                                        openSubPage(AppRoute.CourseDetail(result.courseId.value))

                                    // Land on the calendar with the hit's day selected and its
                                    // detail sheet open.
                                    is SearchResult.CalendarEntry -> {
                                        calendarViewModel.selectDay(result.date)
                                        calendarViewModel.openEventDetail(result.eventId)
                                        scope.launch { pagerState.scrollToPage(ShellTab.Calendar.ordinal) }
                                        closeSearch()
                                    }

                                    is SearchResult.Building -> {
                                        mapViewModel.selectBuilding(result.code)
                                        scope.launch { pagerState.scrollToPage(ShellTab.Map.ordinal) }
                                        closeSearch()
                                    }

                                    // Libretto exam hits land on the Profile page, which renders the libretto.
                                    is SearchResult.TranscriptEntry -> openSubPage(AppRoute.Profile)
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

            chooserFile?.let { route ->
                val kind = remember(route) { FileKind.classify(route.fileName, route.mimeType) }
                FileOpenChooserSheet(
                    fileName = route.fileName,
                    sizeBytes = route.sizeBytes,
                    kind = kind,
                    onChoose = { choice, remember ->
                        kind.preferenceKey?.takeIf { remember }?.let { fileOpenViewModel.remember(it, choice) }
                        chooserFile = null
                        when (choice) {
                            FileOpenChoice.InApp -> backStack.add(route)
                            FileOpenChoice.External -> externalFile = route
                        }
                    },
                    onDismiss = { chooserFile = null },
                )
            }












        }
    }
}

// Sub-page container: opaque surface that covers the tab pager, inset below the global top bar
// (the bottom bar slides off on sub-pages). Immersive pages (video) go fully edge to edge;
// extendBehindBar pages keep the opaque background but skip the top inset, scrolling their
// content behind the see-through bar (they handle the inset themselves via contentPadding).
// Bridges NavDisplay's AnimatedContentScope into LocalAnimatedContentScope so shared elements in
// the page (e.g. the tax detail ticket) seek with the page transition.
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
