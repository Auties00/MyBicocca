package it.attendance100.mybicocca.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.attendance100.mybicocca.ui.component.appbar.AppTopBar
import it.attendance100.mybicocca.ui.screen.calendar.CalendarRoute
import it.attendance100.mybicocca.ui.screen.elearning.ElearningScreen
import it.attendance100.mybicocca.ui.screen.login.LoginScreen
import it.attendance100.mybicocca.ui.screen.map.MapScreen
import it.attendance100.mybicocca.ui.screen.segreterie.SegreterieScreen
import it.attendance100.mybicocca.ui.screen.segreterie.attendance.AttendanceScreen
import it.attendance100.mybicocca.ui.screen.segreterie.certificates.SelfCertificatesScreen
import it.attendance100.mybicocca.ui.screen.segreterie.exams.ExamResultsScreen
import it.attendance100.mybicocca.ui.screen.segreterie.internships.StageScreen
import it.attendance100.mybicocca.ui.screen.segreterie.isee.IseeScreen
import it.attendance100.mybicocca.ui.screen.segreterie.questionnaires.QuestionnairesScreen
import it.attendance100.mybicocca.ui.screen.segreterie.reservations.ReservationsScreen
import it.attendance100.mybicocca.ui.screen.segreterie.studyplan.PianoCarrieraScreen
import it.attendance100.mybicocca.ui.screen.segreterie.taxes.TaxDetailScreen
import it.attendance100.mybicocca.ui.screen.segreterie.taxes.TaxesScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.BookedScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.BookingScreen
import it.attendance100.mybicocca.ui.screen.segreterie.booking.ExamSessionDetailScreen
import it.attendance100.mybicocca.ui.screen.profile.ProfileScreen
import it.attendance100.mybicocca.ui.screen.splash.SplashScreen
import kotlin.coroutines.cancellation.CancellationException

private enum class Tab(val label: String, val icon: ImageVector) {
    Calendar("Calendario", Icons.Default.CalendarMonth),
    Elearning("E-Learning", Icons.Default.School),
    Map("Luoghi", Icons.Default.Map),
    Segreterie("Segreterie", Icons.Default.Work),
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

private sealed interface SubPage {
    val title: String

    data object Booking : SubPage { override val title = "Prenotazione Esami" }
    data class BookingDetail(val sessionId: Long) : SubPage { override val title = "Dettaglio Appello" }
    data object Booked : SubPage { override val title = "Esami Prenotati" }
    data object Taxes : SubPage { override val title = "Tasse" }
    data class TaxDetail(val chargeId: Long) : SubPage { override val title = "Dettaglio Tassa" }
    data object StudyPlan : SubPage { override val title = "Piano di Studi" }
    data object Isee : SubPage { override val title = "ISEE" }
    data object SelfCertificates : SubPage { override val title = "Autocertificazioni" }
    data object ExamResults : SubPage { override val title = "Bacheca Esiti" }
    data object Attendance : SubPage { override val title = "Presenze" }
    data object Questionnaires : SubPage { override val title = "Questionari" }
    data object Reservations : SubPage { override val title = "Prenotazioni" }
    data object Stage : SubPage { override val title = "Stage" }

    // Global (accessible from any tab)
    data object Profile : SubPage { override val title = "Profile" }
}

@Composable
private fun MainShell(profilePic: ByteArray?) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Calendar) }

    val tabStacks = remember {
        Tab.entries.associateWith { mutableStateListOf<SubPage>() }
    }
    val currentStack = tabStacks[selectedTab]!!
    val currentSubPage = currentStack.lastOrNull()
    val canNavigateBack = currentStack.isNotEmpty()
    val subPageTitle = currentSubPage?.title

    val topBarProgress = remember { Animatable(0f) }

    fun navigate(subPage: SubPage) {
        currentStack.add(subPage)
    }

    fun popBack() {
        val size = currentStack.size
        if (size != 0) currentStack.removeAt(size - 1)
    }

    PredictiveBackHandler(enabled = canNavigateBack) { backProgress ->
        val returnsToRoot = currentStack.size <= 1
        try {
            if (returnsToRoot) {
                backProgress.collect { event ->
                    topBarProgress.snapTo(
                        1f - (event.progress / 0.9f).coerceIn(0f, 1f)
                    )
                }
                topBarProgress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            } else {
                backProgress.collect { }
            }
            popBack()
        } catch (_: CancellationException) {
            if (returnsToRoot) {
                topBarProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar(
                profilePic = profilePic,
                canNavigateBack = canNavigateBack,
                subPageTitle = subPageTitle,
                externalProgress = topBarProgress,
                onNavigateBack = { popBack() },
                onProfileClick = { navigate(SubPage.Profile) },
            )
        },
    ) { innerPadding ->
        val p = topBarProgress.value
        val bottomBarHeight = lerp(80.dp, 0.dp, p)
        val contentModifier = Modifier
            .padding(top = innerPadding.calculateTopPadding() + 8.dp)
            .padding(bottom = bottomBarHeight)

        Box(modifier = Modifier.fillMaxSize()) {
            // Tab content with sub-page overlay
            Box(modifier = contentModifier.fillMaxSize()) {
                if (currentSubPage is SubPage.Profile) {
                    ProfileScreen()
                } else {
                    when (selectedTab) {
                        Tab.Calendar -> CalendarRoute()
                        Tab.Elearning -> ElearningScreen()
                        Tab.Map -> MapScreen()
                        Tab.Segreterie -> SegreterieTabContent(
                            currentSubPage = currentSubPage,
                            onNavigate = { navigate(it) },
                        )
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
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTab,
                            onClick = {
                                if (tab == selectedTab) {
                                    currentStack.clear()
                                } else {
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
    }
}

@Composable
private fun SegreterieTabContent(
    currentSubPage: SubPage?,
    onNavigate: (SubPage) -> Unit,
) {
    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "segreterieContent",
    ) { subPage ->
        when (subPage) {
            null -> SegreterieScreen(
                onNavigateToBooking = { onNavigate(SubPage.Booking) },
                onNavigateToBooked = { onNavigate(SubPage.Booked) },
                onNavigateToTaxes = { onNavigate(SubPage.Taxes) },
                onNavigateToIsee = { onNavigate(SubPage.Isee) },
                onNavigateToSelfCertificates = { onNavigate(SubPage.SelfCertificates) },
                onNavigateToExamResults = { onNavigate(SubPage.ExamResults) },
                onNavigateToPianoCarriera = { onNavigate(SubPage.StudyPlan) },
                onNavigateToQuestionnaires = { onNavigate(SubPage.Questionnaires) },
                onNavigateToReservations = { onNavigate(SubPage.Reservations) },
                onNavigateToAttendance = { onNavigate(SubPage.Attendance) },
                onNavigateToStage = { onNavigate(SubPage.Stage) },
            )
            is SubPage.Booking -> BookingScreen(
                onNavigateToDetail = { id -> onNavigate(SubPage.BookingDetail(id)) },
            )
            is SubPage.BookingDetail -> ExamSessionDetailScreen(sessionId = subPage.sessionId)
            is SubPage.Booked -> BookedScreen()
            is SubPage.Taxes -> TaxesScreen(
                onNavigateToDetail = { id -> onNavigate(SubPage.TaxDetail(id)) },
            )
            is SubPage.TaxDetail -> TaxDetailScreen()
            is SubPage.StudyPlan -> PianoCarrieraScreen()
            is SubPage.Isee -> IseeScreen()
            is SubPage.SelfCertificates -> SelfCertificatesScreen()
            is SubPage.ExamResults -> ExamResultsScreen()
            is SubPage.Attendance -> AttendanceScreen()
            is SubPage.Questionnaires -> QuestionnairesScreen()
            is SubPage.Reservations -> ReservationsScreen()
            is SubPage.Stage -> StageScreen()
            else -> throw InternalError("Unexpected page")
        }
    }
}
