package it.attendance100.mybicocca.ui.screen.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.Screen
import it.attendance100.mybicocca.manager.rememberHapticManager
import it.attendance100.mybicocca.ui.component.appbar.AppTitle
import it.attendance100.mybicocca.ui.component.appbar.SharedAvatar
import it.attendance100.mybicocca.ui.component.appbar.StatusIndicator
import it.attendance100.mybicocca.ui.screen.main.calendar.CalendarRoute
import it.attendance100.mybicocca.ui.screen.main.career.CareerScreen
import it.attendance100.mybicocca.ui.screen.main.map.LuoghiScreen
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.ui.theme.GrayColorDark
import java.time.LocalDateTime

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomePage(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    mainViewModel: MainViewModel = hiltViewModel(),
    initialPage: Int = 0,
    initialTab: Int = 0,
) {
    var currentPage by remember { mutableIntStateOf(initialPage) }

    LaunchedEffect(initialPage) {
        currentPage = initialPage
    }

    val haptic = rememberHapticManager()

    val isOffline by mainViewModel.isOffline.collectAsState()

    val swipeThreshold = 200f
    var totalDragDistance by remember { mutableFloatStateOf(0f) }
    var hasTriggered by remember { mutableStateOf(false) }
    val bounceScale = remember { Animatable(1f) }
    var lastTriggerTime by remember { mutableStateOf<LocalDateTime?>(null) }


    LaunchedEffect(hasTriggered) {
        if (hasTriggered) {
            bounceScale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            bounceScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        } else {
            bounceScale.animateTo(1.0f)
        }
    }

    val currentAvatarSize by remember {
        derivedStateOf {
            val perc = totalDragDistance.coerceAtMost(swipeThreshold) / 100
            val baseSize = (44f * (1 - perc) + 46f * perc)

            (baseSize * bounceScale.value).dp
        }
    }

    val currentScrimAlpha by remember {
        derivedStateOf {
            val perc = totalDragDistance.coerceAtMost(swipeThreshold) / 100
            (perc * 0.3f).coerceIn(0f, 0.3f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragDistance = 0f
                        hasTriggered = false
                    },
                    onDragEnd = {
                        totalDragDistance = 0f
                        if (hasTriggered) {
                            navController.navigate(Screen.Profile.route)
                            hasTriggered = false
                            if (LocalDateTime.now().minusNanos(800_000)
                                    .isAfter(lastTriggerTime)
                            ) haptic.tap() // Avoid double tap
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()

                        totalDragDistance = (totalDragDistance + dragAmount).coerceAtLeast(0f)

                        if (totalDragDistance > swipeThreshold && !hasTriggered) {
                            hasTriggered = true
                            haptic.spring(0.3f)
                            lastTriggerTime = LocalDateTime.now()
                        } else if (totalDragDistance <= swipeThreshold && hasTriggered) {
                            hasTriggered = false
                            haptic.feather()
                        }
                    }
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(navController = navController)
                },
                bottomBar = {
                    BottomNavBar(
                        currentIndex = currentPage,
                        onPageSelected = { index ->
                            currentPage = index
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    StatusIndicator(
                        isOffline = isOffline
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        PageContent(currentPage, initialTab)
                    }
                }
            }
        }
        if (currentScrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = currentScrimAlpha))
                    .zIndex(1f)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 13.dp)
                .height(60.dp)
                .width(44.dp)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.requiredSize(currentAvatarSize),
                onClick = {
                    navController.navigate(Screen.Profile.route)
                    haptic.spring(0.3f)
                }
            ) {
                SharedAvatar(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    modifier = Modifier.fillMaxSize(),
                    size = currentAvatarSize
                )
            }
        }
    }
}


@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun TopAppBar(
    navController: NavHostController,
) {
    val grayColor = GrayColor()
    val haptic = rememberHapticManager()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.background)
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(88.dp))

            // App Title
            AppTitle(
                modifier = Modifier.offset(y = 6.dp)
            )

            Row {
                // AI
                IconButton(onClick = {
                    haptic.tap()
                    // coroutineScope.launch {
                    //   navController.navigate(Screen.Settings.route)
                    // }
                }) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = stringResource(R.string.settings),
                        tint = grayColor
                    )
                }
                IconButton(
                    onClick = {
                        haptic.tap()
                        navController.navigate(Screen.Settings.route)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(currentIndex: Int, onPageSelected: (Int) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
    val grayColor = GrayColorDark

    NavigationBar(
        containerColor = backgroundColor,
        contentColor = primaryColor
    ) {
        val haptics = rememberHapticManager()
        val items = listOf(
            BottomNavItem(
                stringResource(R.string.bottom_navbar_calendario),
                Icons.Outlined.CalendarMonth,
                Icons.Filled.CalendarMonth
            ),
            BottomNavItem(
                stringResource(R.string.bottom_navbar_mappa),
                Icons.Outlined.LocationOn,
                Icons.Filled.LocationOn
            ),
            BottomNavItem(
                stringResource(R.string.bottom_navbar_elearning),
                Icons.Outlined.School,
                Icons.Filled.School
            ),
            BottomNavItem(
                stringResource(R.string.bottom_navbar_segreterie),
                Icons.Outlined.Badge,
                Icons.Filled.Badge
            )
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentIndex == index) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                    )
                },
                selected = currentIndex == index,
                onClick = {
                    haptics.tap()
                    onPageSelected(index)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor,
                    unselectedIconColor = grayColor,
                    unselectedTextColor = grayColor,
                    indicatorColor = primaryContainerColor.copy(alpha = 0.15f),
                )
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PageContent(page: Int, initialTab: Int = 0) {
    when (page) {
        0 -> {
            // Calendar page
            CalendarRoute()
        }

        1 -> {
            // Luoghi page
            LuoghiScreen()
        }

        3 -> {
            // Career page
            CareerScreen(initialTab = initialTab)
        }

        else -> {
            // Placeholder for other pages
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 6.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Page ${page + 1}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)
