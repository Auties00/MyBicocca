package it.attendance100.mybicocca.ui.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.coroutines.cancellation.CancellationException

// Wraps a first-level destination (one step deep from a tab). Owns predictive-back gesture
// driving the shared topBarProgress so the global top bar fades, the bottom bar slides off,
// and the sub-page content fades during the gesture.
@Composable
fun FirstLevelSubPage(
    topBarProgress: Animatable<Float, *>,
    navController: NavController,
    useLocalTopBar: Boolean,
    title: String,
    actions: @Composable () -> Unit = { Spacer(Modifier.size(40.dp)) },
    content: @Composable () -> Unit,
) {
    var gestureActive by remember { mutableStateOf(false) }
    val motion = MaterialTheme.motionScheme
    val settleSpec = remember(motion) { motion.defaultSpatialSpec<Float>() }

    PredictiveBackHandler(enabled = true) { backProgress ->
        gestureActive = true
        try {
            backProgress.collect { event ->
                topBarProgress.snapTo(1f - (event.progress / 0.9f).coerceIn(0f, 1f))
            }
            topBarProgress.animateTo(0f, settleSpec)
            navController.popBackStack()
        } catch (_: CancellationException) {
            topBarProgress.animateTo(1f, settleSpec)
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
        Column(modifier = Modifier.fillMaxSize()) {
            LocalAppTopBar(
                title = title,
                visible = useLocalTopBar,
                onNavigateBack = { navController.popBackStack() },
                actions = actions,
            )
            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
fun SubPageBackground(
    navController: NavController,
    title: String,
    actions: @Composable () -> Unit = { Spacer(Modifier.size(40.dp)) },
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LocalAppTopBar(
                title = title,
                visible = true,
                onNavigateBack = { navController.popBackStack() },
                actions = actions,
            )
            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

// Always composed; only its alpha animates. Gating composition on `visible` would cause a
// one-frame flicker as the global top bar fades out and the local bar hasn't measured yet.
@Composable
private fun LocalAppTopBar(
    title: String,
    visible: Boolean,
    onNavigateBack: () -> Unit,
    actions: @Composable () -> Unit = { Spacer(Modifier.size(40.dp)) },
) {
    val statusBarHeight = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (visible) 1f else 0f },
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            // Metrics mirror MyBicoccaTopBar at progress=1 (SUB_PAGE mode) so the alpha
            // handoff at end-of-morph isn't visible: same icon-button size, same row
            // arrangement, same total height (statusBar + 8 + 56 — no trailing spacer
            // inside the Surface).
            Column {
                Spacer(Modifier.height(statusBarHeight + 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Indietro",
                        )
                    }
                    // Font/letter-spacing/start-inset match SubPageTitleContent.
                    Crossfade(
                        targetState = title,
                        label = "local-top-bar-title",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                    ) { t ->
                        Text(
                            text = t,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    actions()
                }
            }
        }
    }
}

// Helpers for wiring NavHost graphs. Both read their title directly from the typed
// route's `appTitle` (no central dispatcher needed inside the wrapper because T is
// known), and expose the entry to the content lambda so routes with arguments can
// extract them.
inline fun <reified T : AppRoute> NavGraphBuilder.firstLevel(
    navController: NavController,
    topBarProgress: Animatable<Float, *>,
    crossinline useLocalTopBarProvider: () -> Boolean = { false },
    crossinline titleOverrideProvider: () -> String? = { null },
    noinline actions: (@Composable (NavBackStackEntry) -> Unit)? = null,
    crossinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable<T> { entry ->
        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
            FirstLevelSubPage(
                topBarProgress = topBarProgress,
                navController = navController,
                useLocalTopBar = useLocalTopBarProvider(),
                title = titleOverrideProvider() ?: entry.toRoute<T>().subPageTitleOrEmpty(),
                actions = if (actions != null) {
                    { actions(entry) }
                } else {
                    { Spacer(Modifier.size(40.dp)) }
                },
            ) { content(entry) }
        }
    }
}

inline fun <reified T : AppRoute> NavGraphBuilder.deepLevel(
    navController: NavController,
    showTopBar: Boolean = true,
    crossinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable<T> { entry ->
        CompositionLocalProvider(LocalAnimatedContentScope provides this) {
            if (showTopBar) {
                SubPageBackground(
                    navController = navController,
                    title = entry.toRoute<T>().subPageTitleOrEmpty(),
                ) { content(entry) }
            } else {
                content(entry)
            }
        }
    }
}

fun AppRoute.subPageTitleOrEmpty(): String =
    (appTitle as? AppTitle.SubPage)?.title.orEmpty()

fun NavBackStackEntry.toAppRoute(): AppRoute? {
    val dest = destination
    return when {
        dest.hasRoute<AppRoute.TabRoot>() -> AppRoute.TabRoot
        dest.hasRoute<AppRoute.Profile>() -> AppRoute.Profile
        dest.hasRoute<AppRoute.AccountManagement>() -> toRoute<AppRoute.AccountManagement>()
        dest.hasRoute<AppRoute.Settings>() -> AppRoute.Settings
        dest.hasRoute<AppRoute.SettingsAppearance>() -> AppRoute.SettingsAppearance
        dest.hasRoute<AppRoute.SettingsGeneral>() -> AppRoute.SettingsGeneral
        dest.hasRoute<AppRoute.SettingsBehaviour>() -> AppRoute.SettingsBehaviour
        dest.hasRoute<AppRoute.SettingsSecurity>() -> AppRoute.SettingsSecurity
        dest.hasRoute<AppRoute.SettingsDeveloper>() -> AppRoute.SettingsDeveloper
        dest.hasRoute<AppRoute.AppInfo>() -> AppRoute.AppInfo
        dest.hasRoute<AppRoute.LoginManager>() -> AppRoute.LoginManager
        dest.hasRoute<AppRoute.Taxes>() -> AppRoute.Taxes
        dest.hasRoute<AppRoute.TaxDetail>() -> toRoute<AppRoute.TaxDetail>()
        dest.hasRoute<AppRoute.StudyPlan>() -> AppRoute.StudyPlan
        dest.hasRoute<AppRoute.StudyPlanEdit>() -> toRoute<AppRoute.StudyPlanEdit>()
        dest.hasRoute<AppRoute.Transcript>() -> toRoute<AppRoute.Transcript>()
        dest.hasRoute<AppRoute.ExamResults>() -> AppRoute.ExamResults
        dest.hasRoute<AppRoute.Attendance>() -> AppRoute.Attendance
        dest.hasRoute<AppRoute.Internships>() -> AppRoute.Internships
        dest.hasRoute<AppRoute.Questionnaires>() -> AppRoute.Questionnaires
        dest.hasRoute<AppRoute.DegreeAward>() -> AppRoute.DegreeAward
        dest.hasRoute<AppRoute.SelfCertificates>() -> AppRoute.SelfCertificates
        dest.hasRoute<AppRoute.Reservations>() -> AppRoute.Reservations
        dest.hasRoute<AppRoute.Isee>() -> AppRoute.Isee
        dest.hasRoute<AppRoute.CourseDetail>() -> toRoute<AppRoute.CourseDetail>()
        dest.hasRoute<AppRoute.QuizDetail>() -> toRoute<AppRoute.QuizDetail>()
        dest.hasRoute<AppRoute.AssignmentDetail>() -> toRoute<AppRoute.AssignmentDetail>()
        dest.hasRoute<AppRoute.ForumDetail>() -> toRoute<AppRoute.ForumDetail>()
        dest.hasRoute<AppRoute.DiscussionDetail>() -> toRoute<AppRoute.DiscussionDetail>()
        dest.hasRoute<AppRoute.Messaging>() -> AppRoute.Messaging
        dest.hasRoute<AppRoute.ConversationDetail>() -> toRoute<AppRoute.ConversationDetail>()
        dest.hasRoute<AppRoute.VideoPlayback>() -> toRoute<AppRoute.VideoPlayback>()
        dest.hasRoute<AppRoute.Room360View>() -> toRoute<AppRoute.Room360View>()
        dest.hasRoute<AppRoute.TeacherDetail>() -> toRoute<AppRoute.TeacherDetail>()
        else -> null
    }
}
