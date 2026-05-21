package it.attendance100.mybicocca.ui.screen.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
) {
}
