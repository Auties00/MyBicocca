package it.attendance100.mybicocca.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.ui.component.bar.BottomBarItem
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaBottomBar
import it.attendance100.mybicocca.ui.component.bar.MyBicoccaTopBar
import it.attendance100.mybicocca.ui.component.bar.TopBarSearchState
import it.attendance100.mybicocca.ui.component.feedback.AppSnackbarHost
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.component.feedback.rememberAppSnackbarController
import it.attendance100.mybicocca.ui.screen.account.AccountManagementScreen
import it.attendance100.mybicocca.ui.screen.account.AccountViewModel
import it.attendance100.mybicocca.ui.screen.account.subscreen.accountSwitcher.AccountSwitcherPopup
import it.attendance100.mybicocca.ui.screen.calendar.CalendarScreen
import it.attendance100.mybicocca.ui.screen.calendar.CalendarViewModel
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.teacherDetail.TeacherDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.elearning.ElearningViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.AssignmentDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.ConversationDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailActions
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.CourseDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.DiscussionDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.ForumDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.MessagingScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.QuizDetailScreen
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.VideoPlayerScreen
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.map.MapViewModel
import it.attendance100.mybicocca.ui.screen.map.subscreen.room360.Room360Screen
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.registry.RegistryScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.AttendanceScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.degreeAward.DegreeAwardScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.ExamResultsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.InternshipsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.QuestionnairesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.reservations.ReservationsScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.selfCertificates.SelfCertificatesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.StudyPlanScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxDetail.TaxDetailScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.TranscriptScreen
import it.attendance100.mybicocca.ui.screen.settings.SettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.AppInfoScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.loginManager.LoginManagerScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearanceScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsBehaviour.SettingsBehaviourScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsDeveloper.SettingsDeveloperScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsGeneral.SettingsGeneralScreen
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.SettingsSecurityScreen

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = hiltViewModel(),
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    var tab by rememberSaveable { mutableStateOf(ShellTab.Calendar) }
    val photo by accountViewModel.userPhoto.collectAsStateWithLifecycle()

    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val elearningViewModel: ElearningViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()

    val subNavController = rememberNavController()
    val subEntry by subNavController.currentBackStackEntryAsState()

    val currentRoute by remember { derivedStateOf { subEntry?.toAppRoute() } }
    val isOnSubPage = currentRoute?.isSubPage == true
    val subPageTitle = (currentRoute?.appTitle as? AppTitle.SubPage)?.title

    // Drives bottom-bar slide-off, global-top-bar fade, and the local-top-bar handoff.
    // The Animatable's lifecycle is owned here so PredictiveBackHandler instances inside
    // FirstLevelSubPage can read/snap the same value across destination recompositions.
    val topBarProgress = remember { Animatable(0f) }

    // Hand off rendering from the global top bar (shell scaffold) to the per-screen local
    // top bar once the morph is complete and we're on a sub-page. Both bars are always
    // composed; only their alpha animates — see SubPageWrappers.LocalAppTopBar.
    var useLocalTopBar by remember { mutableStateOf(false) }
    LaunchedEffect(topBarProgress.value, isOnSubPage) {
        useLocalTopBar = topBarProgress.value >= 0.99f &&
            !topBarProgress.isRunning &&
            isOnSubPage
    }

    var showAccountSwitcher by remember { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterToggle by remember { mutableStateOf<(() -> Unit)?>(null) }
    var filterActive by remember { mutableStateOf(false) }
    // Null = use the route's static title; non-null = sub-page is driving it at runtime.
    var subPageTitleOverride by remember { mutableStateOf<String?>(null) }

    // Reset search/filter only when the tab actually changes after first composition. Doing
    // this in the bottom-bar onClick wouldn't survive process restore — and resetting on
    // every recomposition would clobber valid restored state on cold start.
    var prevTab by remember { mutableStateOf(tab) }
    LaunchedEffect(tab) {
        if (prevTab != tab) {
            searchActive = false
            searchQuery = ""
            filterToggle = null
            filterActive = false
            subPageTitleOverride = null
            prevTab = tab
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

    val motion = MaterialTheme.motionScheme
    val enterTransition = remember(motion) { defaultEnterTransition(motion) }
    val exitTransition = remember(motion) { defaultExitTransition(motion) }
    val popEnterTransition = remember(motion) { defaultPopEnterTransition(motion) }
    val popExitTransition = remember(motion) { defaultPopExitTransition(motion) }

    CompositionLocalProvider(LocalAppSnackbarController provides snackbarController) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MyBicoccaTopBar(
                progress = topBarProgress,
                canNavigateBack = isOnSubPage,
                subPageTitle = subPageTitleOverride ?: subPageTitle,
                searchState = searchState,
                onProfileClick = { showAccountSwitcher = true },
                onNavigateBack = { subNavController.popBackStack() },
                photo = photo,
                globalAlpha = if (useLocalTopBar) 0f else 1f,
                onFilterToggle = filterToggle,
                filterActive = filterActive,
            )
        },
        bottomBar = {
            MyBicoccaBottomBar(
                items = bottomBarItems,
                selected = tab,
                onSelect = { selected ->
                    // Always pop the sub-stack first — switching tabs (or re-tapping the
                    // current tab) should land you at TabRoot, never deep on a sub-page.
                    subNavController.popBackStack(AppRoute.TabRoot, inclusive = false)
                    tab = selected
                },
                translationY = topBarProgress.value * 300f,
            )
        },
        snackbarHost = { AppSnackbarHost(controller = snackbarController) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
                // Tab content sits behind the NavHost and is padded to avoid the bars.
                // The NavHost itself fills the entire content slot (no innerPadding) so a
                // sub-page extends edge-to-edge: once the bars fade / translate off after
                // the morph, the sub-page covers the bands they vacated instead of
                // exposing them. M3 Scaffold lays content edge-to-edge with bars overlaid
                // on top, so an unpadded child renders behind the visible bars cleanly.
                val onProvideFilterToggle: ((() -> Unit)?) -> Unit = { filterToggle = it }
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    when (tab) {
                        ShellTab.Calendar -> CalendarScreen(
                            viewModel = calendarViewModel,
                            searchQuery = searchQuery,
                            onProvideFilterToggle = onProvideFilterToggle,
                            bottomNavBarPadding = innerPadding
                        )
                        ShellTab.Elearning -> ElearningScreen(
                            viewModel = elearningViewModel,
                            searchQuery = searchQuery,
                            onProvideFilterToggle = onProvideFilterToggle,
                            onOpenCourse = { courseId ->
                                subNavController.navigate(AppRoute.CourseDetail(courseId.value))
                            },
                            onOpenAssignment = { courseId, assignmentId ->
                                subNavController.navigate(
                                    AppRoute.AssignmentDetail(assignId = assignmentId.value, courseId = courseId.value),
                                )
                            },
                            onOpenQuiz = { courseId, quizId ->
                                subNavController.navigate(
                                    AppRoute.QuizDetail(quizId = quizId.value, courseId = courseId.value),
                                )
                            },
                        )
                        ShellTab.Map -> MapScreen(
                            viewModel = mapViewModel,
                            searchQuery = searchQuery,
                            onProvideFilterToggle = onProvideFilterToggle,
                            onOpenRoom360 = { url, roomName ->
                                subNavController.navigate(AppRoute.Room360View(url = url, roomName = roomName))
                            },
                        )
                        ShellTab.Registry -> RegistryScreen(
                            searchQuery = searchQuery,
                            onProvideFilterToggle = onProvideFilterToggle,
                        )
                    }
                }

                SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(
                            navController = subNavController,
                            startDestination = AppRoute.TabRoot,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { enterTransition },
                            exitTransition = { exitTransition },
                            popEnterTransition = { popEnterTransition },
                            popExitTransition = { popExitTransition },
                        ) {
                            // Transparent destination — tab content shows through.
                            composable<AppRoute.TabRoot> {}

                            // First-level sub-pages: predictive back drives topBarProgress.
                            firstLevel<AppRoute.Profile>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) {
                                ProfileScreen(
                                    onOpenTranscript = { careerId ->
                                        subNavController.navigate(AppRoute.Transcript(careerId = careerId))
                                    },
                                )
                            }
                            firstLevel<AppRoute.AccountManagement>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { entry ->
                                val args = entry.toRoute<AppRoute.AccountManagement>()
                                AccountManagementScreen(
                                    accountId = AccountId(args.accountId),
                                    onSignedOut = { subNavController.popBackStack() },
                                )
                            }
                            firstLevel<AppRoute.Settings>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { SettingsScreen() }
                            firstLevel<AppRoute.Taxes>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { TaxesScreen() }
                            firstLevel<AppRoute.StudyPlan>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { StudyPlanScreen() }
                            firstLevel<AppRoute.Isee>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { IseeScreen() }
                            firstLevel<AppRoute.SelfCertificates>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { SelfCertificatesScreen() }
                            firstLevel<AppRoute.ExamResults>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { ExamResultsScreen() }
                            firstLevel<AppRoute.Attendance>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { AttendanceScreen() }
                            firstLevel<AppRoute.Questionnaires>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { QuestionnairesScreen() }
                            firstLevel<AppRoute.Reservations>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { ReservationsScreen() }
                            firstLevel<AppRoute.Internships>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { InternshipsScreen() }
                            firstLevel<AppRoute.DegreeAward>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { DegreeAwardScreen() }
                            firstLevel<AppRoute.AppInfo>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { AppInfoScreen() }
                            firstLevel<AppRoute.LoginManager>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { LoginManagerScreen() }
                            firstLevel<AppRoute.Messaging>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { MessagingScreen() }

                            // First-level with arguments.
                            firstLevel<AppRoute.Room360View>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { entry ->
                                val args = entry.toRoute<AppRoute.Room360View>()
                                Room360Screen(url = args.url, roomName = args.roomName)
                            }
                            firstLevel<AppRoute.Transcript>(subNavController, topBarProgress, useLocalTopBarProvider = { useLocalTopBar }) { entry ->
                                val args = entry.toRoute<AppRoute.Transcript>()
                                TranscriptScreen(careerId = args.careerId)
                            }
                            firstLevel<AppRoute.CourseDetail>(
                                subNavController,
                                topBarProgress,
                                useLocalTopBarProvider = { useLocalTopBar },
                                titleOverrideProvider = { subPageTitleOverride },
                                actions = { CourseDetailActions() },
                            ) { entry ->
                                val args = entry.toRoute<AppRoute.CourseDetail>()
                                CourseDetailScreen(
                                    courseId = args.courseId,
                                    onProvideTitle = { subPageTitleOverride = it },
                                    onOpenAssignment = { id ->
                                        subNavController.navigate(
                                            AppRoute.AssignmentDetail(assignId = id.value, courseId = args.courseId),
                                        )
                                    },
                                    onOpenQuiz = { id ->
                                        subNavController.navigate(
                                            AppRoute.QuizDetail(quizId = id.value, courseId = args.courseId),
                                        )
                                    },
                                    onOpenForum = { id ->
                                        subNavController.navigate(
                                            AppRoute.ForumDetail(forumId = id.value, courseId = args.courseId),
                                        )
                                    },
                                    onOpenVideo = { cmId, title ->
                                        subNavController.navigate(
                                            AppRoute.VideoPlayback(
                                                courseId = args.courseId,
                                                cmId = cmId,
                                                title = title,
                                            ),
                                        )
                                    },
                                )
                            }

                            // Deeper sub-pages — always-on local top bar; no progress drive.
                            deepLevel<AppRoute.TaxDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.TaxDetail>()
                                TaxDetailScreen(chargeId = args.chargeId)
                            }
                            deepLevel<AppRoute.StudyPlanEdit>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.StudyPlanEdit>()
                                StudyPlanEditScreen(
                                    studentId = args.studentId,
                                    choiceRegulationId = args.choiceRegulationId,
                                    schemaId = args.schemaId,
                                    planId = args.planId,
                                )
                            }
                            deepLevel<AppRoute.QuizDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.QuizDetail>()
                                QuizDetailScreen(quizId = args.quizId, courseId = args.courseId)
                            }
                            deepLevel<AppRoute.AssignmentDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.AssignmentDetail>()
                                AssignmentDetailScreen(assignId = args.assignId, courseId = args.courseId)
                            }
                            deepLevel<AppRoute.ForumDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.ForumDetail>()
                                ForumDetailScreen(forumId = args.forumId, courseId = args.courseId)
                            }
                            deepLevel<AppRoute.DiscussionDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.DiscussionDetail>()
                                DiscussionDetailScreen(discussionId = args.discussionId)
                            }
                            deepLevel<AppRoute.ConversationDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.ConversationDetail>()
                                ConversationDetailScreen(conversationId = args.conversationId)
                            }
                            deepLevel<AppRoute.VideoPlayback>(
                                subNavController,
                                showTopBar = false,
                            ) { entry ->
                                val args = entry.toRoute<AppRoute.VideoPlayback>()
                                VideoPlayerScreen(
                                    courseId = args.courseId,
                                    cmId = args.cmId,
                                    onBack = { subNavController.popBackStack() },
                                )
                            }
                            deepLevel<AppRoute.TeacherDetail>(subNavController) { entry ->
                                val args = entry.toRoute<AppRoute.TeacherDetail>()
                                TeacherDetailScreen(teacherCode = args.teacherCode)
                            }

                            // Settings sub-pages: deeper since Settings itself is first-level.
                            deepLevel<AppRoute.SettingsAppearance>(subNavController) { SettingsAppearanceScreen() }
                            deepLevel<AppRoute.SettingsGeneral>(subNavController) { SettingsGeneralScreen() }
                            deepLevel<AppRoute.SettingsBehaviour>(subNavController) { SettingsBehaviourScreen() }
                            deepLevel<AppRoute.SettingsSecurity>(subNavController) { SettingsSecurityScreen() }
                            deepLevel<AppRoute.SettingsDeveloper>(subNavController) { SettingsDeveloperScreen() }
                        }
                    }
                }
            }
        }

        if (showAccountSwitcher) {
            AccountSwitcherPopup(
                onDismiss = { showAccountSwitcher = false },
                onAddAccount = { returnTo -> rootViewModel.requestAddAccount(returnTo) },
                onManageAccount = { account ->
                    subNavController.navigate(AppRoute.AccountManagement(account.id.value))
                },
                onOpenProfile = {
                    subNavController.navigate(AppRoute.Profile)
                },
            )
        }
    }
    }
}
