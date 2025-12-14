package it.attendance100.mybicocca.screens.career

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import kotlinx.coroutines.*
import kotlin.math.*


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CareerScreen(
  viewModel: CareerViewModel = hiltViewModel(),
  initialTab: Int = 0,
) {
  val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 4 })
  val coroutineScope = rememberCoroutineScope()
  val selectedTabIndex = pagerState.currentPage

  val nestedScrollConnection = rememberPageNestedScrollConnection(state = pagerState)

  val primaryColor = MaterialTheme.colorScheme.primary
  val grayColor = GrayColor()

  val user by viewModel.user.collectAsState()
  val stats by viewModel.stats.collectAsState()

  var userScrollEnabled by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
  ) {
    // Tab Row
    PrimaryTabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = primaryColor,
      indicator = {
        TabRowDefaults.PrimaryIndicator(
          modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false),
          width = Dp.Unspecified
        )
      }
    ) {
      listOf(
        stringResource(R.string.career_tab_profilo),
        stringResource(R.string.career_tab_esami),
        stringResource(R.string.career_tab_piano),
        stringResource(R.string.career_tab_segreterie)
      ).forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = {
            coroutineScope.launch {
              pagerState.animateScrollToPage(index)
            }
          },
          text = {
            Text(
              text = title,
              color = if (selectedTabIndex == index) primaryColor else grayColor,
              fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 14.sp,
              maxLines = 1,
              softWrap = false,
              overflow = TextOverflow.Visible
            )
          }
        )
      }
    }

    // Tab Content
    HorizontalPager(
      state = pagerState,
      userScrollEnabled = userScrollEnabled,
      modifier = Modifier
          .fillMaxSize()
          .nestedScroll(nestedScrollConnection)
          .pointerInput(pagerState) {
            awaitEachGesture {
              awaitFirstDown(pass = PointerEventPass.Initial)
              userScrollEnabled = true
              var handled = false
              do {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: break
                if (change.pressed && !handled && pagerState.currentPage == 0 && abs(pagerState.currentPageOffsetFraction) < 0.01f) {
                  val delta = change.position.x - change.previousPosition.x
                  if (delta > 0) {
                    userScrollEnabled = false
                    handled = true
                  } else if (delta < 0) {
                    handled = true
                  }
                }
              } while (event.changes.any { it.pressed })
              userScrollEnabled = true
            }
          },
      beyondViewportPageCount = 1,
      flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
    ) { page ->
      when (page) {
        0 -> PlaceholderTab("TODO")

        1 -> ExamsTab(
          passedExams = stats?.passedExams ?: emptyList(),
          pendingExams = stats?.remainingExams ?: emptyList(),
        )

        2 -> PianoTab(
          user,
          stats,
        )

        3 -> PlaceholderTab(stringResource(R.string.career_tab_segreterie))
      }
    }
  }
}

@Composable
fun PlaceholderTab(tabName: String) {
  Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(R.string.career_coming_soon_format, tabName),
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 18.sp
    )
  }
}

