package it.attendance100.mybicocca.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.user.User
import it.attendance100.mybicocca.ui.component.StatusIndicator
import it.attendance100.mybicocca.ui.component.appbar.AccountSwitcherPopup
import it.attendance100.mybicocca.ui.component.appbar.AppTopBar
import it.attendance100.mybicocca.ui.component.appbar.ProfileAvatar
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
import it.attendance100.mybicocca.ui.screen.settings.AppInfoScreen
import it.attendance100.mybicocca.ui.screen.settings.AppearanceSettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.BehaviourSettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.DeveloperSettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.GeneralSettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.SecuritySettingsScreen
import it.attendance100.mybicocca.ui.screen.settings.SettingsScreen
import it.attendance100.mybicocca.ui.screen.splash.SplashScreen
import it.attendance100.mybicocca.ui.theme.PrimaryColor
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

private enum class Tab(val label: String, val icon: ImageVector) {
    Calendar("Calendario", Icons.Default.CalendarMonth),
    Elearning("E-Learning", Icons.Default.School),
    Map("Luoghi", Icons.Default.Map),
    Segreterie("Segreterie", Icons.Default.Work),
}

// CompositionLocals for shared element transitions
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedContentScope = compositionLocalOf<AnimatedContentScope> {
    error("No AnimatedContentScope found")
}

// Navigation transition specs
private val kNavEasing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f)

private val kDefaultPopExitTransition = scaleOut(
    targetScale = 0.9f,
    transformOrigin = TransformOrigin(0.5f, 0.5f),
    animationSpec = tween(300, easing = kNavEasing),
) + fadeOut(
    targetAlpha = 0.1f,
    animationSpec = tween(300, easing = kNavEasing),
) + slideOutHorizontally(
    targetOffsetX = { it / 4 },
    animationSpec = tween(300, easing = kNavEasing),
)

private val kProfilePopExitTransition = scaleOut(
    targetScale = 0.9f,
    transformOrigin = TransformOrigin(0.5f, 0.5f),
    animationSpec = tween(300, easing = kNavEasing),
) + fadeOut(
    targetAlpha = 0.01f,
    animationSpec = tween(200, easing = CubicBezierEasing(0f, 1f, 0f, 1f)),
) + slideOut(
    targetOffset = { IntOffset(-(it.height) / 10, -(it.width) / 4) },
    animationSpec = tween(300, easing = kNavEasing),
)

private val kDefaultPopEnterTransition = slideInHorizontally(
    initialOffsetX = { -it / 2 },
    animationSpec = tween(300, easing = kNavEasing),
)

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
        dest.hasRoute<AppRoutes.Settings>() -> "Impostazioni"
        dest.hasRoute<AppRoutes.SettingsAppearance>() -> "Aspetto"
        dest.hasRoute<AppRoutes.SettingsGeneral>() -> "Generale"
        dest.hasRoute<AppRoutes.SettingsBehaviour>() -> "Comportamento"
        dest.hasRoute<AppRoutes.SettingsSecurity>() -> "Sicurezza"
        dest.hasRoute<AppRoutes.SettingsDeveloper>() -> "Sviluppatore"
        dest.hasRoute<AppRoutes.AppInfo>() -> "Info App"
        dest.hasRoute<AppRoutes.LoginManager>() -> "Login Manager"
        else -> null
    }
}

@Composable
fun MyBicoccaNavHost(
    viewModel: AppNavHostViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    )
) {
    val rootNavController = rememberNavController()
    val rootEntry by rootNavController.currentBackStackEntryAsState()
    val profilePic by viewModel.profilePic.collectAsStateWithLifecycle(initialValue = null)
    val user by viewModel.user.collectAsStateWithLifecycle(initialValue = null)
    val activeCareer by viewModel.activeCareer.collectAsStateWithLifecycle(initialValue = null)
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val isInApp = rootEntry?.destination?.route?.let { route ->
        route != AppRoutes.Splash::class.qualifiedName && route != AppRoutes.Login::class.qualifiedName
    } ?: false

    if (isInApp) {
        MainShell(
            profilePic = profilePic,
            user = user,
            activeCareer = activeCareer,
            isOnline = isOnline
        )
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainShell(
    profilePic: ByteArray?,
    user: User?,
    activeCareer: Career?,
    isOnline: Boolean = true,
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Calendar) }
    var showAccountSwitcher by remember { mutableStateOf(false) }
    val popupProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
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

    // Lock swipe direction once established to prevent cancel-drag triggering opposite gesture
    // -1 = locked left (profile), 0 = undecided, 1 = locked right (search)
    var swipeLock by remember { mutableIntStateOf(0) }

    var avatarSourceCenter by remember { mutableStateOf(Offset.Zero) }
    var avatarTargetCenter by remember { mutableStateOf(Offset.Zero) }

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
    val baseS = 50f
    val baseS2 = baseS + 2f
    val currentAvatarScale by remember {
        derivedStateOf {
            val perc = profileDragDist.coerceAtMost(swipeThreshold) / 100f
            val baseSize = baseS * (1f - perc) + baseS2 * perc
            (baseSize / baseS) * profileBounce.value
        }
    }

    val currentSearchScale by remember {
        derivedStateOf {
            val perc = searchDragDist.coerceAtMost(swipeThreshold) / 100f
            val baseSize = baseS * (1f - perc) + baseS2 * perc
            (baseSize / baseS) * searchBounce.value
        }
    }

    val currentScrimAlpha by remember {
        derivedStateOf {
            val profilePerc = profileDragDist.coerceAtMost(swipeThreshold) / 100f
            val searchPerc = searchDragDist.coerceAtMost(swipeThreshold) / 100f
            (maxOf(profilePerc, searchPerc) * 0.3f).coerceIn(0f, 0.3f)
        }
    }

    // Account switcher popup animations
    LaunchedEffect(showAccountSwitcher) {
        if (showAccountSwitcher) {
            popupProgress.snapTo(0f)
            popupProgress.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    PredictiveBackHandler(enabled = showAccountSwitcher) { backProgress ->
        try {
            backProgress.collect { event ->
                popupProgress.snapTo(
                    1f - (event.progress / 0.9f).coerceIn(0f, 1f),
                )
            }
            popupProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            showAccountSwitcher = false
        } catch (_: CancellationException) {
            popupProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    fun navigate(route: AppRoutes) {
        subNavController.navigate(route)
    }

    fun dismissPopupAndRun(action: () -> Unit = {}) {
        scope.launch {
            popupProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            showAccountSwitcher = false
            action()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(
                isOnSubPage,
                searchActive,
                showAccountSwitcher,
                swipeProfileEnabled,
                swipeSearchEnabled
            ) {
                if (isOnSubPage || searchActive || showAccountSwitcher) return@pointerInput
                if (!swipeProfileEnabled && !swipeSearchEnabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        profileDragDist = 0f
                        searchDragDist = 0f
                        profileTriggered = false
                        searchTriggered = false
                        swipeLock = 0
                    },
                    onDragEnd = {
                        profileDragDist = 0f
                        searchDragDist = 0f
                        swipeLock = 0
                        if (swipeProfileEnabled && profileTriggered) {
                            showAccountSwitcher = true
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
                        swipeLock = 0
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (change.isConsumed) return@detectHorizontalDragGestures

                        change.consume()

                        // Lock direction on first meaningful drag
                        if (swipeLock == 0 && kotlin.math.abs(dragAmount) > 1f) {
                            swipeLock = if (dragAmount < 0) -1 else 1
                        }

                        // Leftward swipe (negative dragAmount) → profile
                        if (swipeProfileEnabled && swipeLock == -1) {
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
                        if (swipeSearchEnabled && swipeLock == 1) {
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
                    onProfileClick = { showAccountSwitcher = true },
                    onAvatarPositioned = { avatarSourceCenter = it },
                    searchIconScale = currentSearchScale,
                    popupProgress = popupProgress.value,
                )
            },
        ) { innerPadding ->
            // Offline / session expired indicator
            StatusIndicator(
                isOffline = !isOnline,
                isSessionExpired = false,
            )

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

                // Sub-page NavHost (on top, with shared element transitions)
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = subNavController,
                    startDestination = AppRoutes.TabRoot,
                    modifier = contentModifier.fillMaxSize(),
                    enterTransition = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) },
                    exitTransition = { fadeOut(tween(700)) },
                    popEnterTransition = { kDefaultPopEnterTransition },
                    popExitTransition = { kDefaultPopExitTransition },
                ) {
                    composable<AppRoutes.TabRoot> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {}
                    }

                    // First-level sub-pages (gesture drives top bar + bottom bar + content)
                    composable<AppRoutes.Profile>(
                        enterTransition = {
                            scaleIn(
                                initialScale = 0.0f,
                                transformOrigin = TransformOrigin(0.94f, 0.05f),
                            ) + fadeIn(tween(300, easing = kNavEasing))
                        },
                        exitTransition = { ExitTransition.None },
                        popExitTransition = { kProfilePopExitTransition },
                        popEnterTransition = { EnterTransition.None },
                    ) {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                ProfileScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Room360View> { entry ->
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            val route = entry.toRoute<AppRoutes.Room360View>()
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                Room360Screen(url = route.url)
                            }
                        }
                    }
                    composable<AppRoutes.Booking>(
                        enterTransition = { kDefaultPopEnterTransition + fadeIn() },
                    ) {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                BookingScreen(
                                    onNavigateToDetail = { id -> navigate(AppRoutes.BookingDetail(id)) },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.Booked> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) { BookedScreen() }
                        }
                    }
                    composable<AppRoutes.Taxes>(
                        enterTransition = { kDefaultPopEnterTransition + fadeIn() },
                    ) {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                TaxesScreen(
                                    onNavigateToDetail = { id -> navigate(AppRoutes.TaxDetail(id)) },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.StudyPlan> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                PianoCarrieraScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Isee> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) { IseeScreen() }
                        }
                    }
                    composable<AppRoutes.SelfCertificates> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                SelfCertificatesScreen()
                            }
                        }
                    }
                    composable<AppRoutes.ExamResults> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                ExamResultsScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Attendance> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                AttendanceScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Questionnaires> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                QuestionnairesScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Reservations> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                ReservationsScreen()
                            }
                        }
                    }
                    composable<AppRoutes.Internships> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) { StageScreen() }
                        }
                    }

                    // Deeper sub-pages (shared element transitions handle enter/exit)
                    composable<AppRoutes.BookingDetail>(
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) { entry ->
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            val route = entry.toRoute<AppRoutes.BookingDetail>()
                            SubPageBackground {
                                ExamSessionDetailScreen(sessionId = route.sessionId)
                            }
                        }
                    }
                    composable<AppRoutes.TaxDetail>(
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground { TaxDetailScreen() }
                        }
                    }

                    // Settings screens
                    composable<AppRoutes.Settings> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            FirstLevelSubPage(topBarProgress, subNavController) {
                                SettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                    onNavigateToAppearance = { navigate(AppRoutes.SettingsAppearance) },
                                    onNavigateToGeneral = { navigate(AppRoutes.SettingsGeneral) },
                                    onNavigateToBehaviour = { navigate(AppRoutes.SettingsBehaviour) },
                                    onNavigateToSecurity = { navigate(AppRoutes.SettingsSecurity) },
                                    onNavigateToDeveloper = { navigate(AppRoutes.SettingsDeveloper) },
                                    onNavigateToLoginManager = { navigate(AppRoutes.LoginManager) },
                                    onNavigateToAppInfo = { navigate(AppRoutes.AppInfo) },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.SettingsAppearance> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                AppearanceSettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.SettingsGeneral> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                GeneralSettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.SettingsBehaviour> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                BehaviourSettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.SettingsSecurity> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                SecuritySettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.SettingsDeveloper> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                DeveloperSettingsScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.AppInfo> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                AppInfoScreen(
                                    onNavigateBack = { subNavController.popBackStack() },
                                )
                            }
                        }
                    }
                    composable<AppRoutes.LoginManager> {
                        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                            SubPageBackground {
                                // TODO: LoginManagerScreen
                            }
                        }
                    }
                }
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

            // Scrim overlay for swipe gesture
            if (currentScrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = currentScrimAlpha)),
                )
            }

        }

        // Account switcher popup overlay
        AccountSwitcherPopup(
            progress = popupProgress.value,
            user = user,
            career = activeCareer,
            onAvatarTargetPositioned = { avatarTargetCenter = it },
            onDismiss = { dismissPopupAndRun() },
            onProfileClick = { dismissPopupAndRun { navigate(AppRoutes.Profile) } },
            onAddAccount = { dismissPopupAndRun() },
            onManageAccounts = { dismissPopupAndRun() },
            onSettingsClick = { dismissPopupAndRun { navigate(AppRoutes.Settings) } },
        )

        // Flying avatar overlay
        val density = LocalDensity.current
        val p = popupProgress.value
        val effectiveTarget =
            if (avatarTargetCenter == Offset.Zero) avatarSourceCenter else avatarTargetCenter
        val avatarSizeDp = lerp(37.dp, 72.dp, p)
        val avatarSizePx = with(density) { avatarSizeDp.toPx() }
        val avatarCenter = if (avatarSourceCenter != Offset.Zero) Offset(
            x = avatarSourceCenter.x + (effectiveTarget.x - avatarSourceCenter.x) * p,
            y = avatarSourceCenter.y + (effectiveTarget.y - avatarSourceCenter.y) * p,
        ) else Offset.Zero
        val flyingAvatarScale = if (p > 0f) 1f else currentAvatarScale
        val flyingAvatarAlpha = when {
            avatarSourceCenter == Offset.Zero -> 0f
            p > 0f -> 1f
            searchActive -> 0f
            else -> 1f - topBarProgress.value
        }

        ProfileAvatar(
            profilePic = profilePic,
            contentDescription = null,
            size = avatarSizeDp,
            modifier = Modifier
                .offset {
                    IntOffset(
                        (avatarCenter.x - avatarSizePx / 2f).toInt() + 10,
                        (avatarCenter.y - avatarSizePx / 2f).toInt(),
                    )
                }
                .graphicsLayer {
                    scaleX = flyingAvatarScale
                    scaleY = flyingAvatarScale
                    alpha = flyingAvatarAlpha
                },
        )
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
