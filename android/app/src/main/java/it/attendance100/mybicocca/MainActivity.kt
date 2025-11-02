package it.attendance100.mybicocca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import it.attendance100.mybicocca.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyBicoccaTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          HomePage()
        }
      }
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomePage() {
  val pagerState = rememberPagerState()
  val coroutineScope = rememberCoroutineScope()
  val currentPage = pagerState.currentPage

  Scaffold(
    containerColor = BackgroundColor,
    topBar = {
      TopAppBar()
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

@Composable
fun TopAppBar() {
  Surface(
    modifier = Modifier
        .fillMaxWidth()
        .height(60.dp),
    color = BackgroundColor
  ) {
    Row(
      modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 13.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar
      SubcomposeAsyncImage(
        model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no", // Replace with actual image URL from elearning api
        contentDescription = stringResource(R.string.homescreen_profile),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable { /* Profile navigation */ }
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
                  color = PrimaryColor
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
                  tint = GrayColor
                )
              }
            }

            else -> {
              SubcomposeAsyncImageContent()
            }
          }
        }
      }


      // App Title
      Text(
        text = buildAnnotatedString {
          withStyle(
            style = SpanStyle(
              color = Color.White,
              fontWeight = FontWeight.Normal,
              fontSize = 20.sp
            )
          ) {
            append(stringResource(R.string.homescreen_app_prefix))
          }
          withStyle(
            style = SpanStyle(
              color = PrimaryColor,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          ) {
            append(stringResource(R.string.homescreen_app_suffix))
          }
        }
      )

      // Search Icon
      IconButton(onClick = { /* Search action */ }) {
        Icon(
          imageVector = Icons.Outlined.Search,
          contentDescription = stringResource(R.string.search),
          tint = GrayColor,
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

@Composable
fun BottomNavBar(currentIndex: Int, onPageSelected: (Int) -> Unit) {
  NavigationBar(
    containerColor = BackgroundColor,
    contentColor = PrimaryColor
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
          selectedIconColor = PrimaryColor,
          selectedTextColor = PrimaryColor,
          unselectedIconColor = GrayColor,
          unselectedTextColor = GrayColor,
          indicatorColor = BackgroundColor,
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
        .background(BackgroundColor),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Page ${page + 1}",
      color = Color.White,
      fontSize = 24.sp
    )
  }
}

data class BottomNavItem(
  val label: String,
  val icon: ImageVector,
  val selectedIcon: ImageVector,
)
