package it.attendance100.mybicocca.ui.navigation

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
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
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.ConversationDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.ConversationDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.DiscussionDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.DiscussionDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.ForumDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.ForumDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.MessagingScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerViewModel
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.map.MapViewModel
import it.attendance100.mybicocca.ui.screen.map.subscreen.room360.Room360Screen
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.profile.ProfileViewModel
import it.attendance100.mybicocca.ui.screen.registry.Registry
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendanceScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamDetailScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams.BookedExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.DegreeAwardScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.InternshipsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.reservations.ReservationsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.selfCertificates.SelfCertificatesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxDetail.TaxDetailScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.TranscriptScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.TranscriptViewModel
import it.attendance100.mybicocca.ui.screen.settings.SettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.loginManager.LoginManagerScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearanceScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsBehaviour.SettingsBehaviourScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsDeveloper.SettingsDeveloperScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsGeneral.SettingsGeneralScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecurityScreen
import kotlinx.coroutines.launch

@Suppress("AssignedValueIsNeverRead")
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
    // Hoisted here so the tax fetch starts on shell load and the list / detail / ISEE
    // destinations share one in-memory result (taxes are not cached to Room).
    val taxesViewModel: TaxesViewModel = hiltViewModel()
    // Hoisted so the transcript refresh (kicked off in the VM's init) starts on shell load,
    // not when the Profile sub-page is first opened — the stats/badge are then already warm.
    val profileViewModel: ProfileViewModel = hiltViewModel()

    // Navigation 3 back stack. TabRoot is always the root; sub-pages are pushed on top of it.
    // The four tabs live in the pager hosted INSIDE the TabRoot entry, so a list ticket and a
    // detail ticket are both inside NavDisplay's AnimatedContent — that is what makes the
    // list -> detail shared-element morph seek with the predictive-back gesture.
    val backStack = rememberNavBackStack(AppRoute.TabRoot)
    val currentRoute = backStack.lastOrNull() as? AppRoute
    val isOnSubPage = currentRoute?.isSubPage == true
    val subPageTitle = (currentRoute?.appTitle as? AppTitle.SubPage)?.title
    // Video playback is immersive: the chrome is fully hidden and the page goes edge to edge.
    val immersive = currentRoute is AppRoute.VideoPlayback

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
    val navProgress = remember { mutableFloatStateOf(0f) }
    val searchProgress = remember { Animatable(0f) }

    var showAccountSwitcher by remember { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
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
            searchQuery = ""
            filterActive = false
            subPageTitleOverride = null
            subPageActions = null
            prevPage = pagerState.settledPage
        }
    }

    val searchState = TopBarSearchState(
        query = searchQuery,
        active = searchActive,
        placeholder = tab.searchPlaceholder,
        onQueryChange = { searchQuery = it },
        onActiveChange = { searchActive = it },
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
                                                val pageQuery = if (isActive) searchQuery else ""
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
                                                            navProgress = navProgress,
                                                            searchQuery = pageQuery,
                                                            onProvideFilterToggle = onProvideFilterToggle,
                                                            bottomNavBarPadding = innerPadding,
                                                        )

                                                        ShellTab.Elearning -> ElearningScreen(
                                                            viewModel = elearningViewModel,
                                                            isActive = isActive,
                                                            searchQuery = pageQuery,
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
                                                                    AppRoute.AssignmentDetail(
                                                                        assignId = assignmentId.value,
                                                                        courseId = courseId.value
                                                                    ),
                                                                )
                                                            },
                                                            onOpenQuiz = { courseId, quizId ->
                                                                backStack.add(
                                                                    AppRoute.QuizDetail(
                                                                        quizId = quizId.value,
                                                                        courseId = courseId.value
                                                                    ),
                                                                )
                                                            },
                                                        )

                                                        ShellTab.Map -> MapScreen(
                                                            viewModel = mapViewModel,
                                                            isActive = isActive,
                                                            searchQuery = pageQuery,
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

                                                        ShellTab.Registry ->  Registry(
                                                            bookedExamsViewModel = bookedExamsViewModel,
                                                            bookableExamsViewModel = bookableExamsViewModel,
                                                            taxesViewModel = taxesViewModel,
                                                            isActive = isActive,
                                                            onOpenBookedExams = {
                                                                backStack.add(
                                                                    AppRoute.BookedExams
                                                                )
                                                            },
                                                            onOpenTaxes = { backStack.add(AppRoute.Taxes) },
                                                            onOpenExamResults = {
                                                                backStack.add(
                                                                    AppRoute.ExamResults
                                                                )
                                                            },
                                                            onOpenStudyPlan = {
                                                                backStack.add(
                                                                    AppRoute.StudyPlan
                                                                )
                                                            },
                                                            onOpenQuestionnaires = {
                                                                backStack.add(
                                                                    AppRoute.Questionnaires
                                                                )
                                                            },
                                                            onOpenReservations = {
                                                                backStack.add(
                                                                    AppRoute.Reservations
                                                                )
                                                            },
                                                            onOpenAttendance = {
                                                                backStack.add(
                                                                    AppRoute.Attendance
                                                                )
                                                            },
                                                            onOpenInternships = {
                                                                backStack.add(
                                                                    AppRoute.Internships
                                                                )
                                                            },
                                                            onOpenSelfCertificates = {
                                                                backStack.add(
                                                                    AppRoute.SelfCertificates
                                                                )
                                                            },
                                                            onOpenDegreeAward = {
                                                                backStack.add(
                                                                    AppRoute.DegreeAward
                                                                )
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
                                                viewModel = profileViewModel
                                            )
                                        }
                                    }
                                    // Esami and Tasse are launched from the Registry dashboard cards.
                                    // Hosted here (not inside the Registry pager) so their list -> detail
                                    // shared-element morphs seek with predictive back like every other sub-page.
                                    entry<AppRoute.BookedExams> {
                                        SubPage(topInset) {
                                            BookedExamsScreen(
                                                bookedViewModel = bookedExamsViewModel,
                                                bookableViewModel = bookableExamsViewModel,
                                                onOpenExam = { courseOfStudyId, activityId, callId ->
                                                    backStack.add(
                                                        AppRoute.BookedExamDetail(
                                                            courseOfStudyId = courseOfStudyId,
                                                            activityId = activityId,
                                                            callId = callId
                                                        )
                                                    )
                                                },
                                            )
                                        }
                                    }
                                    entry<AppRoute.Taxes> {
                                        SubPage(topInset) {
                                            TaxesScreen(
                                                viewModel = taxesViewModel,
                                                onOpenDetail = { chargeId ->
                                                    backStack.add(AppRoute.TaxDetail(chargeId = chargeId))
                                                },
                                            )
                                        }
                                    }
                                    entry<AppRoute.Settings> {
                                        SubPage(topInset) {
                                            SettingsScreen(
                                                onOpenSecurity = { backStack.add(AppRoute.SettingsSecurity) },
                                            )
                                        }
                                    }
                                    entry<AppRoute.StudyPlan> { SubPage(topInset) { StudyPlanScreen() } }
                                    entry<AppRoute.SelfCertificates> { SubPage(topInset) { SelfCertificatesScreen() } }
                                    entry<AppRoute.ExamResults> { SubPage(topInset) { ExamResultsScreen() } }
                                    entry<AppRoute.Attendance> { SubPage(topInset) { AttendanceScreen() } }
                                    entry<AppRoute.Questionnaires> { SubPage(topInset) { QuestionnairesScreen() } }
                                    entry<AppRoute.Reservations> { SubPage(topInset) { ReservationsScreen() } }
                                    entry<AppRoute.Internships> { SubPage(topInset) { InternshipsScreen() } }
                                    entry<AppRoute.DegreeAward> { SubPage(topInset) { DegreeAwardScreen() } }
                                    entry<AppRoute.AppInfo> { SubPage(topInset) { AppInfoScreen() } }
                                    entry<AppRoute.LoginManager> { SubPage(topInset) { LoginManagerScreen() } }
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
                                    entry<AppRoute.BookedExamDetail> { key ->
                                        SubPage(topInset) {
                                            BookedExamDetailScreen(
                                                courseOfStudyId = key.courseOfStudyId,
                                                activityId = key.activityId,
                                                callId = key.callId,
                                                viewModel = bookedExamsViewModel,
                                                onBack = { backStack.removeLastOrNull() },
                                            )
                                        }
                                    }
                                    entry<AppRoute.Transcript> { key ->
                                        val vm =
                                            hiltViewModel<TranscriptViewModel, TranscriptViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            TranscriptScreen(
                                                careerId = key.careerId,
                                                viewModel = vm
                                            )
                                        }
                                    }
                                    entry<AppRoute.CourseDetail> { key ->
                                        val vm =
                                            hiltViewModel<CourseDetailViewModel, CourseDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            CourseDetailScreen(
                                                courseId = key.courseId,
                                                viewModel = vm,
                                                onProvideTitle = { subPageTitleOverride = it },
                                                onProvideActions = { subPageActions = it },
                                                onOpenAssignment = { id ->
                                                    backStack.add(
                                                        AppRoute.AssignmentDetail(
                                                            assignId = id.value,
                                                            courseId = key.courseId
                                                        )
                                                    )
                                                },
                                                onOpenQuiz = { id ->
                                                    backStack.add(
                                                        AppRoute.QuizDetail(
                                                            quizId = id.value,
                                                            courseId = key.courseId
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
                                                onOpenVideo = { cmId, title ->
                                                    backStack.add(
                                                        AppRoute.VideoPlayback(
                                                            courseId = key.courseId,
                                                            cmId = cmId,
                                                            title = title
                                                        )
                                                    )
                                                },
                                            )
                                        }
                                    }
                                    entry<AppRoute.TaxDetail> { key ->
                                        SubPage(topInset) {
                                            TaxDetailScreen(
                                                chargeId = key.chargeId,
                                                viewModel = taxesViewModel
                                            )
                                        }
                                    }

                                    // Deeper sub-pages.
                                    entry<AppRoute.StudyPlanEdit> { key ->
                                        SubPage(topInset) {
                                            StudyPlanEditScreen(
                                                studentId = key.studentId,
                                                choiceRegulationId = key.choiceRegulationId,
                                                schemaId = key.schemaId,
                                                planId = key.planId,
                                            )
                                        }
                                    }
                                    entry<AppRoute.QuizDetail> { key ->
                                        val vm =
                                            hiltViewModel<QuizDetailViewModel, QuizDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            QuizDetailScreen(
                                                quizId = key.quizId,
                                                courseId = key.courseId,
                                                viewModel = vm
                                            )
                                        }
                                    }
                                    entry<AppRoute.AssignmentDetail> { key ->
                                        val vm =
                                            hiltViewModel<AssignmentDetailViewModel, AssignmentDetailViewModel.Factory>(
                                                creationCallback = { it.create(key) },
                                            )
                                        SubPage(topInset) {
                                            AssignmentDetailScreen(
                                                assignId = key.assignId,
                                                courseId = key.courseId,
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

                                    // Settings sub-pages.
                                    entry<AppRoute.SettingsAppearance> { SubPage(topInset) { SettingsAppearanceScreen() } }
                                    entry<AppRoute.SettingsGeneral> { SubPage(topInset) { SettingsGeneralScreen() } }
                                    entry<AppRoute.SettingsBehaviour> { SubPage(topInset) { SettingsBehaviourScreen() } }
                                    entry<AppRoute.SettingsSecurity> { SubPage(topInset) { SettingsSecurityScreen() } }
                                    entry<AppRoute.SettingsDeveloper> { SubPage(topInset) { SettingsDeveloperScreen() } }
                                },
                            )
                        }
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
        }
    }
}

// Sub-page container: opaque surface that covers the tab pager, inset below the global top bar
// (the bottom bar slides off on sub-pages). Immersive pages (video) go fully edge to edge.
// Bridges NavDisplay's AnimatedContentScope into LocalAnimatedContentScope so shared elements in
// the page (e.g. the tax detail ticket) seek with the page transition.
@Composable
private fun SubPage(
    topInset: Dp,
    immersive: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAnimatedContentScope provides LocalNavAnimatedContentScope.current,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (immersive) Modifier else Modifier.background(MaterialTheme.colorScheme.surface))
                .padding(top = if (immersive) 0.dp else topInset),
        ) {
            content()
        }
    }
}
