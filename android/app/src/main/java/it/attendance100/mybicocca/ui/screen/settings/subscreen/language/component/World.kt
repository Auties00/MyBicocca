package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

val FlagGray = Color(0xFF898989)

/**
 * Neutral "follow the system" tile for the language picker: a globe glyph on gray where a
 * specific country flag would otherwise sit.
 */
@Composable
fun WorldFlag(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(FlagGray)) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(widthDp = 180)
@Composable
private fun WorldFlagPreview() = FlagFrame { WorldFlag(Modifier.fillMaxSize()) }