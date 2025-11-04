package it.attendance100.mybicocca.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import coil.compose.*
import com.google.accompanist.pager.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.Screen
import it.attendance100.mybicocca.composables.AppTitle
import it.attendance100.mybicocca.ui.theme.*
import kotlinx.coroutines.*

@OptIn(ExperimentalPagerApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomePage(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val pagerState = rememberPagerState()
  val coroutineScope = rememberCoroutineScope()
  val currentPage = pagerState.currentPage

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(navController, sharedTransitionScope, animatedContentScope)
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TopAppBar(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  var showDropdownMenu by remember { mutableStateOf(false) }
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

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
        with(sharedTransitionScope) {
          SubcomposeAsyncImage(
            model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
            contentDescription = stringResource(R.string.homescreen_profile),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .sharedElement(
                  state = rememberSharedContentState(key = "avatar"),
                  animatedVisibilityScope = animatedContentScope,
                  boundsTransform = { _, _ ->
                    tween(durationMillis = 400)
                  },
                  clipInOverlayDuringTransition = OverlayClip(CircleShape)
                )
                .clickable { showDropdownMenu = true }
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

                // Fallback icon on error
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

        // Dropdown Menu
        DropdownMenu(
          expanded = showDropdownMenu,
          onDismissRequest = { showDropdownMenu = false },
          modifier = Modifier
              .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
              .width(250.dp),
          offset = DpOffset(x = 0.dp, y = (5).dp),

          ) {
          // Login Manager Item with Avatar
          DropdownMenuItem(
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = grayColor
              )
            },
            text = {
              Text(
                text = "Login Manager",
                color = textColor,
                fontSize = 16.sp
              )
            },
            onClick = {
              showDropdownMenu = false
              navController.navigate(Screen.LoginManager.route)
            }
          )

          Divider(color = grayColor.copy(alpha = 0.3f))

          // Settings Item
          DropdownMenuItem(
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = grayColor
              )
            },
            text = {
              Text(
                text = stringResource(R.string.settings),
                color = textColor,
                fontSize = 16.sp
              )
            },
            onClick = {
              showDropdownMenu = false
              navController.navigate(Screen.Settings.route)
            }
          )

          Divider(color = grayColor.copy(alpha = 0.3f))

          // App Info Item
          DropdownMenuItem(
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = grayColor
              )
            },
            text = {
              Text(
                text = "App Info",
                color = textColor,
                fontSize = 16.sp
              )
            },
            onClick = {
              showDropdownMenu = false
              navController.navigate(Screen.AppInfo.route)
            }
          )
        }
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

