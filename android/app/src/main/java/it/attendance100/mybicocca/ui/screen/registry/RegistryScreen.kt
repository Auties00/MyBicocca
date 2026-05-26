package it.attendance100.mybicocca.ui.screen.registry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.attendance100.mybicocca.ui.screen.registry.component.RegistryTabBar
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryTab
import it.attendance100.mybicocca.ui.screen.registry.state.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.exams.ExamsScreen
import kotlinx.coroutines.launch

@Composable
fun RegistryScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
) {
    LaunchedEffect(Unit) { onProvideFilterToggle(null) }

    val tabs = RegistryTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val selectedTab = tabs[pagerState.currentPage]

    Column(modifier = modifier.fillMaxSize()) {
        RegistryTabBar(
            selected = selectedTab,
            onSelect = { tab ->
                scope.launch { pagerState.animateScrollToPage(tabs.indexOf(tab)) }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                RegistryTab.Exams -> ExamsScreen()
                // Remaining pages wired later.
                else -> RegistryTabPage(tab = tabs[page])
            }
        }
    }
}

@Composable
private fun RegistryTabPage(tab: RegistryTab) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = tab.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
