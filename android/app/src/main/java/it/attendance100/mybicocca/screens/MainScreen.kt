package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.screens.career.*
import it.attendance100.mybicocca.screens.luoghi.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import java.time.*

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
  val isSessionExpired by mainViewModel.isSessionExpired.collectAsState()

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
                if (LocalDateTime.now().minusNanos(800_000).isAfter(lastTriggerTime)) haptic.tap() // Avoid double tap
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
            isOffline = isOffline,
            isSessionExpired = isSessionExpired
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


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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
      BottomNavItem(stringResource(R.string.bottom_navbar_calendario), Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
      BottomNavItem(stringResource(R.string.bottom_navbar_mappa), Icons.Outlined.LocationOn, Icons.Filled.LocationOn),
      BottomNavItem(stringResource(R.string.bottom_navbar_elearning), Icons.Outlined.School, Icons.Filled.School),
      BottomNavItem(stringResource(R.string.bottom_navbar_segreterie), Icons.Outlined.Badge, Icons.Filled.Badge)
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
      CalendarScreen()
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
