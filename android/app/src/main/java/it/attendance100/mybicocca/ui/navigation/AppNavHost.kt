package it.attendance100.mybicocca.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.appbar.AppTopBar
import it.attendance100.mybicocca.ui.screen.calendar.CalendarRoute
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.login.LoginScreen
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.map.Room360Screen
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.segreterie.SegreterieScreen
import it.attendance100.mybicocca.ui.screen.segreterie.attendance.AttendanceScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.BookedScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.BookingScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.ExamSessionDetailScreen
import it.attendance100.mybicocca.ui.screen.segreterie.certificates.SelfCertificatesScreen
import it.attendance100.mybicocca.ui.screen.segreterie.exams.ExamResultsScreen
import it.attendance100.mybicocca.ui.screen.segreterie.internships.StageScreen
import it.attendance100.mybicocca.ui.screen.segreterie.isee.IseeScreen
import it.attendance100.mybicocca.ui.screen.segreterie.questionnaires.QuestionnairesScreen
import it.attendance100.mybicocca.ui.screen.segreterie.reservations.ReservationsScreen
import it.attendance100.mybicocca.ui.screen.segreterie.studyplan.PianoCarrieraScreen
import it.attendance100.mybicocca.ui.screen.segreterie.taxes.TaxDetailScreen
import it.attendance100.mybicocca.ui.screen.segreterie.taxes.TaxesScreen
import it.attendance100.mybicocca.ui.screen.splash.SplashScreen
import it.attendance100.mybicocca.ui.theme.PrimaryColor
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

private enum class Tab(val label: String, val icon: ImageVector) {
    Calendar("Calendario", Icons.Default.CalendarMonth),
    Elearning("E-Learning", Icons.Default.School),
    Map("Luoghi", Icons.Default.Map),
    Segreterie("Segreterie", Icons.Default.Work),
}

private fun resolveSubPageTitle(
    entry: androidx.navigation.NavBackStackEntry?,
): String? {
    val dest = entry?.destination ?: return null
    return when {
        dest.hasRoute<AppRoutes.Profile>() -> "Profilo"
        dest.hasRoute<AppRoutes.Room360View>() -> entry.toRoute<AppRoutes.Room360View>().roomName
        dest.hasRoute<AppRoutes.Booking>() -> "Prenotazione Esami"
        dest.hasRoute<AppRoutes.BookingDetail>() -> "Dettaglio Appello"
        dest.hasRoute<AppRoutes.Booked>() -> "Esami Prenotati"
        dest.hasRoute<AppRoutes.Taxes>() -> "Tasse"
        dest.hasRoute<AppRoutes.TaxDetail>() -> "Dettaglio Tassa"
        dest.hasRoute<AppRoutes.StudyPlan>() -> "Piano di Studi"
        dest.hasRoute<AppRoutes.Isee>() -> "ISEE"
        dest.hasRoute<AppRoutes.SelfCertificates>() -> "Autocertificazioni"
        dest.hasRoute<AppRoutes.ExamResults>() -> "Bacheca Esiti"
        dest.hasRoute<AppRoutes.Attendance>() -> "Presenze"
        dest.hasRoute<AppRoutes.Questionnaires>() -> "Questionari"
        dest.hasRoute<AppRoutes.Reservations>() -> "Prenotazioni"
        dest.hasRoute<AppRoutes.Internships>() -> "Stage"
        else -> null
    }
}

@Composable
fun MyBicoccaNavHost(viewModel: AppNavHostViewModel = hiltViewModel()) {
    val rootNavController = rememberNavController()
    val rootEntry by rootNavController.currentBackStackEntryAsState()
    val profilePic by viewModel.profilePic.collectAsStateWithLifecycle(initialValue = null)

    val isInApp = rootEntry?.destination?.route?.let { route ->
        route != AppRoutes.Splash::class.qualifiedName && route != AppRoutes.Login::class.qualifiedName
    } ?: false

    if (isInApp) {
        MainShell(profilePic = profilePic)
    } else {
        NavHost(navController = rootNavController, startDestination = AppRoutes.Splash) {
            composable<AppRoutes.Splash> {
                SplashScreen(
                    onNavigateToLogin = {
                        rootNavController.navigate(AppRoutes.Login) {
                            popUpTo(AppRoutes.Splash) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        rootNavController.navigate(AppRoutes.Calendar) {
                            popUpTo(AppRoutes.Splash) { inclusive = true }
                        }
                    },
                )
            }
            composable<AppRoutes.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        rootNavController.navigate(AppRoutes.Calendar) {
                            popUpTo(AppRoutes.Login) { inclusive = true }
                        }
                    },
                )
            }
            composable<AppRoutes.Calendar> {}
        }
    }
}

@Composable
private fun MainShell(profilePic: ByteArray?) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Calendar) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterToggle by remember { mutableStateOf<(() -> Unit)?>(null) }
    var filterActive by remember { mutableStateOf(false) }

    val searchPlaceholder = when (selectedTab) {
        Tab.Calendar -> stringResource(R.string.calendar_global_search_hint)
        Tab.Map -> stringResource(R.string.map_search_hint)
        else -> stringResource(R.string.search)
    }

    val subNavController = rememberNavController()
    val subEntry by subNavController.currentBackStackEntryAsState()
    val isOnSubPage = subEntry?.destination?.hasRoute<AppRoutes.TabRoot>() == false
    val subPageTitle = if (isOnSubPage) resolveSubPageTitle(subEntry) else null

    val topBarProgress = remember { Animatable(0f) }

    // Swipe gesture state (leftward → profile, rightward → search)
    val haptic = rememberHapticManager()
    val prefs = rememberPreferencesManager()
    val swipeProfileEnabled = prefs.swipeProfileEnabled
    val swipeSearchEnabled = prefs.swipeSearchEnabled
    val swipeThreshold = 200f

    var profileDragDist by remember { mutableFloatStateOf(0f) }
    var profileTriggered by remember { mutableStateOf(false) }
    val profileBounce = remember { Animatable(1f) }
    var profileTriggerTime by remember { mutableStateOf<LocalDateTime?>(null) }

    var searchDragDist by remember { mutableFloatStateOf(0f) }
    var searchTriggered by remember { mutableStateOf(false) }
    val searchBounce = remember { Animatable(1f) }
    var searchTriggerTime by remember { mutableStateOf<LocalDateTime?>(null) }

    LaunchedEffect(profileTriggered) {
        if (profileTriggered) {
            profileBounce.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            profileBounce.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh,
                ),
            )
        } else {
            profileBounce.animateTo(1.0f)
        }
    }

    LaunchedEffect(searchTriggered) {
        if (searchTriggered) {
            searchBounce.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            searchBounce.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh,
                ),
            )
        } else {
            searchBounce.animateTo(1.0f)
        }
    }

    val currentAvatarScale by remember {
        derivedStateOf {
            val perc = profileDragDist.coerceAtMost(swipeThreshold) / 100f
            val baseSize = 44f * (1f - perc) + 46f * perc
            (baseSize / 44f) * profileBounce.value
        }
    }

    val currentSearchScale by remember {
        derivedStateOf {
            val perc = searchDragDist.coerceAtMost(swipeThreshold) / 100f
            val baseSize = 44f * (1f - perc) + 46f * perc
            (baseSize / 44f) * searchBounce.value
        }
    }

    val currentScrimAlpha by remember {
        derivedStateOf {
            val profilePerc = profileDragDist.coerceAtMost(swipeThreshold) / 100f
            val searchPerc = searchDragDist.coerceAtMost(swipeThreshold) / 100f
            (maxOf(profilePerc, searchPerc) * 0.3f).coerceIn(0f, 0.3f)
        }
    }

    fun navigate(route: AppRoutes) {
        subNavController.navigate(route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isOnSubPage, searchActive, swipeProfileEnabled, swipeSearchEnabled) {
                if (isOnSubPage || searchActive) return@pointerInput
                if (!swipeProfileEnabled && !swipeSearchEnabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        profileDragDist = 0f
                        searchDragDist = 0f
                        profileTriggered = false
                        searchTriggered = false
                    },
                    onDragEnd = {
                        profileDragDist = 0f
                        searchDragDist = 0f
                        if (swipeProfileEnabled && profileTriggered) {
                            subNavController.navigate(AppRoutes.Profile)
                            profileTriggered = false
                            if (profileTriggerTime == null || LocalDateTime.now()
                                    .minusNanos(800_000).isAfter(profileTriggerTime)
                            )
                                haptic.tap()
                        }
                        if (swipeSearchEnabled && searchTriggered) {
                            searchActive = true
                            searchTriggered = false
                            if (searchTriggerTime == null || LocalDateTime.now().minusNanos(800_000)
                                    .isAfter(searchTriggerTime)
                            )
                                haptic.tap()
                        }
                    },
                    onDragCancel = {
                        profileDragDist = 0f
                        searchDragDist = 0f
                        profileTriggered = false
                        searchTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (change.isConsumed) return@detectHorizontalDragGestures

                        change.consume()

                        // Leftward swipe (negative dragAmount) → profile
                        if (swipeProfileEnabled) {
                            profileDragDist = (profileDragDist - dragAmount).coerceAtLeast(0f)

                            if (profileDragDist > swipeThreshold && !profileTriggered) {
                                profileTriggered = true
                                haptic.spring(0.3f)
                                profileTriggerTime = LocalDateTime.now()
                            } else if (profileDragDist <= swipeThreshold && profileTriggered) {
                                profileTriggered = false
                                haptic.feather()
                            }
                        }

                        // Rightward swipe (positive dragAmount) → search
                        if (swipeSearchEnabled) {
                            searchDragDist = (searchDragDist + dragAmount).coerceAtLeast(0f)

                            if (searchDragDist > swipeThreshold && !searchTriggered) {
                                searchTriggered = true
                                haptic.spring(0.3f)
                                searchTriggerTime = LocalDateTime.now()
                            } else if (searchDragDist <= swipeThreshold && searchTriggered) {
                                searchTriggered = false
                                haptic.feather()
                            }
                        }
                    },
                )
            },
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                AppTopBar(
                    profilePic = profilePic,
                    canNavigateBack = isOnSubPage,
                    subPageTitle = subPageTitle,
                    searchQuery = searchQuery,
                    searchActive = searchActive,
                    searchPlaceholder = searchPlaceholder,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchActiveChange = { searchActive = it },
                    onFilterToggle = filterToggle,
                    filterActive = filterActive,
                    externalProgress = topBarProgress,
                    onNavigateBack = { subNavController.popBackStack() },
                    onProfileClick = { navigate(AppRoutes.Profile) },
                    avatarScale = currentAvatarScale,
                    searchIconScale = currentSearchScale,
                )
            },
        ) { innerPadding ->
            val p = topBarProgress.value
            val bottomBarHeight = lerp(80.dp, 0.dp, p)
            val contentModifier = Modifier
                .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                .padding(bottom = bottomBarHeight)

            Box(modifier = Modifier.fillMaxSize()) {
                // Tab root content (behind NavHost)
                Box(modifier = contentModifier.fillMaxSize()) {
                    when (selectedTab) {
                        Tab.Calendar -> CalendarRoute(
                            searchQuery = searchQuery,
                            onProvideFilterToggle = { filterToggle = it },
                            onFilterActiveChanged = { filterActive = it },
                        )

                        Tab.Elearning -> ElearningScreen()
                        Tab.Map -> MapScreen(
                            onNavigateTo360 = { url, name ->
                                navigate(AppRoutes.Room360View(url, name))
                            },
                            searchQuery = searchQuery,
                        )

                        Tab.Segreterie -> SegreterieScreen(
                            onNavigateToBooking = { navigate(AppRoutes.Booking) },
                            onNavigateToBooked = { navigate(AppRoutes.Booked) },
                            onNavigateToTaxes = { navigate(AppRoutes.Taxes) },
                            onNavigateToIsee = { navigate(AppRoutes.Isee) },
                            onNavigateToSelfCertificates = { navigate(AppRoutes.SelfCertificates) },
                            onNavigateToExamResults = { navigate(AppRoutes.ExamResults) },
                            onNavigateToPianoCarriera = { navigate(AppRoutes.StudyPlan) },
                            onNavigateToQuestionnaires = { navigate(AppRoutes.Questionnaires) },
                            onNavigateToReservations = { navigate(AppRoutes.Reservations) },
                            onNavigateToAttendance = { navigate(AppRoutes.Attendance) },
                            onNavigateToStage = { navigate(AppRoutes.Internships) },
                        )
                    }
                }

                // Sub-page NavHost (on top, with fade transitions)
                NavHost(
                    navController = subNavController,
                    startDestination = AppRoutes.TabRoot,
                    modifier = contentModifier.fillMaxSize(),
                    enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
                ) {
                    composable<AppRoutes.TabRoot> {}

                    // First-level sub-pages (gesture drives top bar + bottom bar + content)
                    composable<AppRoutes.Profile> {
                        FirstLevelSubPage(topBarProgress, subNavController) {
                            ProfileScreen()
                        }
                    }
                    composable<AppRoutes.Room360View> { entry ->
                        val route = entry.toRoute<AppRoutes.Room360View>()
                        FirstLevelSubPage(topBarProgress, subNavController) {
                            Room360Screen(url = route.url)
                        }
                    }
                    composable<AppRoutes.Booking> {
                        FirstLevelSubPage(topBarProgress, subNavController) {
                            BookingScreen(
                                onNavigateToDetail = { id -> navigate(AppRoutes.BookingDetail(id)) },
                            )
                        }
                    }
                    composable<AppRoutes.Booked> {
                        FirstLevelSubPage(topBarProgress, subNavController) { BookedScreen() }
                    }
                    composable<AppRoutes.Taxes> {
                        FirstLevelSubPage(topBarProgress, subNavController) {
                            TaxesScreen(
                                onNavigateToDetail = { id -> navigate(AppRoutes.TaxDetail(id)) },
                            )
                        }
                    }
                    composable<AppRoutes.StudyPlan> {
                        FirstLevelSubPage(
                            topBarProgress,
                            subNavController
                        ) { PianoCarrieraScreen() }
                    }
                    composable<AppRoutes.Isee> {
                        FirstLevelSubPage(topBarProgress, subNavController) { IseeScreen() }
                    }
                    composable<AppRoutes.SelfCertificates> {
                        FirstLevelSubPage(
                            topBarProgress,
                            subNavController
                        ) { SelfCertificatesScreen() }
                    }
                    composable<AppRoutes.ExamResults> {
                        FirstLevelSubPage(topBarProgress, subNavController) { ExamResultsScreen() }
                    }
                    composable<AppRoutes.Attendance> {
                        FirstLevelSubPage(topBarProgress, subNavController) { AttendanceScreen() }
                    }
                    composable<AppRoutes.Questionnaires> {
                        FirstLevelSubPage(
                            topBarProgress,
                            subNavController
                        ) { QuestionnairesScreen() }
                    }
                    composable<AppRoutes.Reservations> {
                        FirstLevelSubPage(topBarProgress, subNavController) { ReservationsScreen() }
                    }
                    composable<AppRoutes.Internships> {
                        FirstLevelSubPage(topBarProgress, subNavController) { StageScreen() }
                    }

                    // Deeper sub-pages (NavHost handles back, bars stay in SUB_PAGE mode)
                    composable<AppRoutes.BookingDetail> { entry ->
                        val route = entry.toRoute<AppRoutes.BookingDetail>()
                        SubPageBackground { ExamSessionDetailScreen(sessionId = route.sessionId) }
                    }
                    composable<AppRoutes.TaxDetail> {
                        SubPageBackground { TaxDetailScreen() }
                    }
                }

                // Bottom bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = p * 300f
                        },
                ) {
                    NavigationBar {
                        val navBarColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryColor,
                            selectedTextColor = PrimaryColor,
                            indicatorColor = PrimaryColor.copy(alpha = 0.12f),
                        )
                        Tab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = tab == selectedTab,
                                colors = navBarColors,
                                onClick = {
                                    if (tab == selectedTab) {
                                        subNavController.popBackStack(
                                            AppRoutes.TabRoot, inclusive = false,
                                        )
                                    } else {
                                        subNavController.popBackStack(
                                            AppRoutes.TabRoot, inclusive = false,
                                        )
                                        searchActive = false
                                        searchQuery = ""
                                        filterToggle = null
                                        filterActive = false
                                        selectedTab = tab
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            }

            // Scrim overlay for swipe- gesture
            if (currentScrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = currentScrimAlpha)),
                )
            }
        }
    }
}

@Composable
private fun FirstLevelSubPage(
    topBarProgress: Animatable<Float, *>,
    navController: androidx.navigation.NavController,
    content: @Composable () -> Unit,
) {
    var gestureActive by remember { mutableStateOf(false) }

    PredictiveBackHandler(enabled = true) { backProgress ->
        gestureActive = true
        try {
            backProgress.collect { event ->
                topBarProgress.snapTo(
                    1f - (event.progress / 0.9f).coerceIn(0f, 1f)
                )
            }
            topBarProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            navController.popBackStack()
        } catch (_: CancellationException) {
            topBarProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
            gestureActive = false
        }
    }

    val alpha = if (gestureActive) topBarProgress.value else 1f
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha },
        color = MaterialTheme.colorScheme.background,
    ) {
        content()
    }
}

@Composable
private fun SubPageBackground(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        content()
    }
}
