package it.attendance100.mybicocca.ui.screen.main.career

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.util.rememberPageNestedScrollConnection
import kotlinx.coroutines.launch
import kotlin.math.abs


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
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTabIndex,
                        matchContentSize = false
                    ),
                    width = Dp.Unspecified
                )
            }
        ) {
            listOf(
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
                            if (change.pressed && !handled && pagerState.currentPage == 0 && abs(
                                    pagerState.currentPageOffsetFraction
                                ) < 0.01f
                            ) {
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
                else -> PlaceholderTab(" :)")
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

