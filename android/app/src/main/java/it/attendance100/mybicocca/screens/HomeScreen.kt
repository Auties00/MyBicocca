package it.attendance100.mybicocca.screens

import androidx.activity.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import coil.compose.*
import com.google.accompanist.pager.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.composables.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*

@OptIn(ExperimentalPagerApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomePage(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  drawerState: DrawerState,
) {
  val pagerState = rememberPagerState()
  val coroutineScope = rememberCoroutineScope()
  val currentPage = pagerState.currentPage
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight
  val primaryColor = MaterialTheme.colorScheme.primary

  // Handle status bar color changes based on drawer state
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val preferencesManager = rememberPreferencesManager()

  // Determine if we're in dark mode based on preferences
  val isDarkMode = preferencesManager.isDarkMode

  val density = LocalDensity.current
  val drawerWidthDp = 280.dp
  val drawerWidthPx = with(density) { drawerWidthDp.toPx() }
  val animationProgress = remember(drawerState.offset.value) {
    // Ensure we don't divide by zero and clamp the value
    if (drawerWidthPx == 0f) {
      if (drawerState.isOpen) 1f else 0f
    } else {
      (1f - (drawerState.offset.value / -drawerWidthPx)).coerceIn(0f, 1f)
    }
  }
  val avatarSize = 44.dp
  val startX = 13.dp // TopAppBar padding
  val startY = (60.dp - avatarSize) / 2 // Centered in 60.dp TopAppBar
  val endX = drawerWidthDp - 14.5.dp - avatarSize // Drawer padding and size
  val endY = 13.75.dp

  val animatedX = lerp(startX, endX, animationProgress)
  val animatedY = lerp(startY, endY, animationProgress)


  LaunchedEffect(animationProgress, isDarkMode) {
    activity?.let {
      // Interpolate scrim alpha based on drawer animation progress (0.0 to 0.25)
      val scrimAlpha = animationProgress * (if (!isDarkMode) 0.25f else 0.125f)

      // Get the current background color based on theme
      val backgroundColor = if (isDarkMode) BackgroundColor else BackgroundColorLight

      // Blend black overlay over the background color (lerp from background to black based on alpha)
      val blendedColor = lerp(backgroundColor, Color.Black, scrimAlpha)
      val scrimColor = blendedColor.toArgb()

      // Use appropriate SystemBarStyle based on theme mode
      val statusBarStyle = if (isDarkMode)
        SystemBarStyle.dark(scrimColor)
      else
        SystemBarStyle.light(
          scrim = scrimColor,
          darkScrim = scrimColor
        )

      it.enableEdgeToEdge(statusBarStyle = statusBarStyle)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = true,
      modifier = Modifier.fillMaxSize(),
      drawerContent = {
        ModalDrawerSheet(
          drawerContainerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.width(drawerWidthDp)
        ) {
          Column(
            modifier = Modifier
                .fillMaxSize()
          ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Login Manager Item
            StyledNavigationDrawerItem(
              icon = Icons.Default.Key,
              label = stringResource(R.string.login_manager),
              selected = false,
              onClick = {
                navController.navigate(Screen.LoginManager.route) // Switched order on purpose to make the animation feel snappier
                coroutineScope.launch {
                  drawerState.close()
                }
              }
            )

            // Settings Item
            StyledNavigationDrawerItem(
              icon = Icons.Outlined.Settings,
              label = stringResource(R.string.settings),
              selected = false,
              onClick = {
                coroutineScope.launch {
                  drawerState.close()
                }
                navController.navigate(Screen.Settings.route)
              }
            )

            // App Info Item
            StyledNavigationDrawerItem(
              icon = Icons.Outlined.Info,
              label = stringResource(R.string.app_info),
              selected = false,
              onClick = {
                coroutineScope.launch {
                  drawerState.close()
                }
                navController.navigate(Screen.AppInfo.route)
              }
            )
          }
        }
      },
    ) {
      Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
          TopAppBar(
            navController = navController,
            drawerState = drawerState,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
          )
        },
        bottomBar = {
          BottomNavBar(
            currentIndex = currentPage,
            onPageSelected = { index ->
              coroutineScope.launch {
                pagerState.animateScrollToPage(index)
              }
            }
          )
        }
      ) { paddingValues ->
        HorizontalPager(
          count = 4,
          state = pagerState,
          modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
        ) { page ->
          PageContent(page)
        }
      }
    }
    with(sharedTransitionScope) {
      SubcomposeAsyncImage(
        model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
        contentDescription = stringResource(R.string.homescreen_profile),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            // Apply the animated position for the drawer
            .offset(x = animatedX, y = animatedY)
            .size(avatarSize)
            .clip(CircleShape)
            .background(color = PrimaryColor.copy(alpha = 0.777f))
            .clickable {
              coroutineScope.launch {
                if (drawerState.isOpen) {
                  navController.navigate(Screen.LoginManager.route)
                  drawerState.close()
                } else {
                  drawerState.open()
                }
              }
            }
            .sharedElement(
              state = rememberSharedContentState(key = "avatar"),
              animatedVisibilityScope = animatedContentScope,
              boundsTransform = { _, _ -> tween(durationMillis = 400) },
              clipInOverlayDuringTransition = OverlayClip(CircleShape)
            )
      ) {
        val state = painter.state
        Crossfade(targetState = state) { currentState ->
          when (currentState) {
            is AsyncImagePainter.State.Loading -> {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(23.dp),
                  strokeWidth = 2.dp,
                  color = primaryColor
                )
              }
            }

            is AsyncImagePainter.State.Error -> {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = stringResource(R.string.error_loading_image),
                  tint = grayColor
                )
              }
            }

            else -> {
              SubcomposeAsyncImageContent()
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TopAppBar(
  navController: NavHostController,
  drawerState: DrawerState,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight
  val scope = rememberCoroutineScope()

  Surface(
    modifier = Modifier
        .fillMaxWidth()
        .height(60.dp),
    color = MaterialTheme.colorScheme.background
  ) {
    Row(
      modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 13.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Dropdown Menu
      Box {
        Box(
          modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .clickable {

                // The hoisted avatar will open the drawer
                scope.launch {
                  drawerState.open()
                }
              }
        )
      }

      // App Title
      AppTitle()

      // Search Icon
      IconButton(onClick = { /* Search action */ }) {
        Icon(
          imageVector = Icons.Outlined.Search,
          contentDescription = stringResource(R.string.search),
          tint = grayColor,
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

@Composable
fun BottomNavBar(currentIndex: Int, onPageSelected: (Int) -> Unit) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val backgroundColor = MaterialTheme.colorScheme.background
  val grayColor = if (backgroundColor == BackgroundColor) GrayColor else GrayColorLight

  NavigationBar(
    containerColor = backgroundColor,
    contentColor = primaryColor
  ) {
    val items = listOf(
      BottomNavItem(stringResource(R.string.bottom_navbar_calendario), Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
      BottomNavItem(stringResource(R.string.bottom_navbar_elearning), Icons.Outlined.School, Icons.Filled.School),
      BottomNavItem(stringResource(R.string.bottom_navbar_segreterie), Icons.Outlined.ContactPage, Icons.Filled.ContactPage),
      BottomNavItem(stringResource(R.string.bottom_navbar_carriera), Icons.Outlined.Badge, Icons.Filled.Badge)
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
        onClick = { onPageSelected(index) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = primaryColor,
          selectedTextColor = primaryColor,
          unselectedIconColor = grayColor,
          unselectedTextColor = grayColor,
          indicatorColor = backgroundColor,
        )
      )
    }
  }
}

@Composable
fun PageContent(page: Int) {
  Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Page ${page + 1}",
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 24.sp
    )
  }
}

data class BottomNavItem(
  val label: String,
  val icon: ImageVector,
  val selectedIcon: ImageVector,
)
