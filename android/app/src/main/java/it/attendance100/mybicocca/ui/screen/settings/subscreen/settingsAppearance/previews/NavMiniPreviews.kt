package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PhoneBottomNavBar(index: Int = 0) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val validIndex = index.coerceIn(0, 3)
                repeat(4) { tabIndex ->
                    Box(
                        modifier = Modifier
                            .size(17.dp, 11.dp)
                            .background(
                                color = if (tabIndex == validIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            )
                            .padding(3.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun FoldableBottomNavBar(index: Int = 0) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val validIndex = index.coerceIn(0, 3)
                    repeat(4) { tabIndex ->
                        Box(
                            modifier = Modifier
                                .size(25.dp, 11.dp)
                                .background(
                                    color = if (tabIndex == validIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                )
                                .padding(3.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabletNavRail(index: Int = 0) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .width(20.dp)
                .padding(end = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 12.dp)
                    .height(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {

                Icon(
                    Icons.Default.Menu,
                    modifier = Modifier.size(16.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(6.dp))

                val validIndex = index.coerceIn(0, 3)
                repeat(4) { tabIndex ->
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .size(17.dp, 11.dp)
                            .padding(
                                top = if (tabIndex == validIndex) 0.dp else 3.25.dp,
                                bottom = if (tabIndex == validIndex) 0.dp else 3.25.dp
                            )
                            .background(
                                color = if (tabIndex == validIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            )
                            .padding(3.dp)
                    )
                }
            }
        }
    }
}
