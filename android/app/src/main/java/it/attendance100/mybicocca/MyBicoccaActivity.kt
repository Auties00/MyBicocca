package it.attendance100.mybicocca

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.screens.EsamiScreen
import it.attendance100.mybicocca.ui.screen.login.LoginWebViewScreen
import it.attendance100.mybicocca.ui.screen.main.HomePage
import it.attendance100.mybicocca.ui.screen.main.profile.ProfiloScreen
import it.attendance100.mybicocca.ui.screen.main.settings.AppearanceSettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.BehaviourSettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.DeveloperSettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.GeneralSettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.SecuritySettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.SettingsScreen
import it.attendance100.mybicocca.ui.screen.main.settings.info.AppInfoScreen
import it.attendance100.mybicocca.ui.screen.main.settings.manager.LoginManagerScreen
import it.attendance100.mybicocca.ui.screen.splash.SplashScreen
import it.attendance100.mybicocca.ui.theme.MyBicoccaTheme
import it.attendance100.mybicocca.ui.theme.OnPrimaryColor
import it.attendance100.mybicocca.manager.NetworkManager
import it.attendance100.mybicocca.manager.StorageManager
import it.attendance100.mybicocca.manager.ProvideHapticManager
import it.attendance100.mybicocca.manager.rememberPreferencesManager
import javax.inject.Inject
import kotlin.math.roundToInt

// Navigation routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("/")
    object Profile : Screen("/profile")
    object Esami : Screen("/profile/esami")
    object Settings : Screen("/settings")
    object SettingsAppearance : Screen("/settings/settings_appearance")
    object SettingsGeneral : Screen("/settings/settings_general")
    object SettingsBehaviour : Screen("/settings/settings_behaviour")
    object SettingsSecurity : Screen("/settings/settings_security")
    object SettingsDeveloper : Screen("/settings/settings_developer")
    object LoginManager : Screen("/settings/login_manager")
    object Login : Screen("/settings/login_manager/login_webview")
    object AppInfo : Screen("/settings/app_info")
}


@AndroidEntryPoint
class MyBicoccaActivity : ComponentActivity() {
    @Inject
    lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val storageManager = StorageManager(this)
        storageManager.applyTheme() // Ensures theme is applied immediately

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val preferencesManager = rememberPreferencesManager() // Reinstantiation with context
            val systemInDarkTheme = isSystemInDarkTheme()

            var currentThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }

            // Determine theme based on mode
            val isDarkMode by remember(currentThemeMode, systemInDarkTheme) {
                derivedStateOf {
                    when (currentThemeMode) {
                        StorageManager.THEME_DARK -> true
                        StorageManager.THEME_LIGHT -> false
                        else -> systemInDarkTheme
                    }
                }
            }

            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            // Update edge-to-edge when theme changes
            LaunchedEffect(isDarkMode) { styleStatusBar(isDarkMode) }

            MyBicoccaTheme(darkTheme = isDarkMode) {
                ProvideHapticManager {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box {
                            AppNavigation(
                                onThemeChange = { isDarkModeInner ->
                                    // Update the state to trigger recomposition
                                    currentThemeMode = preferencesManager.themeMode
                                    styleStatusBar(isDarkModeInner)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        networkManager.refresh()
    }

    fun styleStatusBar(isDarkMode: Boolean) {
        enableEdgeToEdge(
            statusBarStyle =
                if (isDarkMode)
                    SystemBarStyle.dark(
                        Transparent.toArgb()
                    )
                else
                    SystemBarStyle.light(
                        Transparent.toArgb(),
                        darkScrim = Transparent.toArgb(),
                    ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = OnPrimaryColor.toArgb(),
                darkScrim = OnPrimaryColor.toArgb(),
            )
        )
    }
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(onThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    with(LocalDensity.current) { 35.dp.toPx().roundToInt() }
    with(LocalDensity.current) { 30.dp.toPx().roundToInt() }

    val kDefaultPopExitTransition = scaleOut(
        targetScale = 0.9f,
        transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
    ) + fadeOut(
        targetAlpha = 0.1f,
        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
    ) + slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
    )

    val kDefaultPopEnterTransition = slideInHorizontally(
        initialOffsetX = { -it / 2 },
        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
    )

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            popExitTransition = {
                kDefaultPopExitTransition
            },
            popEnterTransition = {
                kDefaultPopEnterTransition
            },
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }
            composable(
                route = "/?page={page}&tab={tab}",
                arguments = listOf(
                    navArgument("page") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                    navArgument("tab") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 0
                val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                HomePage(
                    navController,
                    this@SharedTransitionLayout,
                    this,
                    initialPage = page,
                    initialTab = tab
                )
            }
            composable(
                route = Screen.Profile.route,
                enterTransition = {
                    scaleIn(
                        initialScale = 0.0f,
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.06f,
                            pivotFractionY = 0.05f
                        ),
                    ) + fadeIn(
                        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
                    )
                },
                popExitTransition = {
                    scaleOut(
                        targetScale = 0.0f,
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.06f,
                            pivotFractionY = 0.05f
                        ),
                        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
                    ) + fadeOut(
                        animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
                    )
                },
                popEnterTransition = {
                    kDefaultPopEnterTransition
                }
            ) { _ ->
                ProfiloScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.Esami.route) {
                EsamiScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.LoginManager.route) { _ ->
                LoginManagerScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.Settings.route) { _ ->
                SettingsScreen(navController, this@SharedTransitionLayout, this, onThemeChange)
            }
            composable(Screen.SettingsAppearance.route) { _ ->
                AppearanceSettingsScreen(
                    navController,
                    this@SharedTransitionLayout,
                    this,
                    onThemeChange
                )
            }
            composable(Screen.SettingsGeneral.route) { _ ->
                GeneralSettingsScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.SettingsBehaviour.route) { _ ->
                BehaviourSettingsScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.SettingsSecurity.route) { _ ->
                SecuritySettingsScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.SettingsDeveloper.route) { _ ->
                DeveloperSettingsScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.AppInfo.route) { _ ->
                AppInfoScreen(navController, this@SharedTransitionLayout, this)
            }
            composable(Screen.Login.route) {
                LoginWebViewScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}