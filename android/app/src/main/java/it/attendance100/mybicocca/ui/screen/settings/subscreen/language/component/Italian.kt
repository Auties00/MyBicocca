package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

val ItalyGreen = Color(0xFF009246)
val ItalyWhite = Color(0xFFF1F2F1)
val ItalyRed = Color(0xFFCE2B37)

/**
 * Italian tricolore for the language picker: three equal vertical bands in the officially
 * specified green/white/red. Draws edge to edge — callers provide the frame (see FlagFrame).
 */
@Composable
fun ItalyFlag(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(ItalyGreen))
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(ItalyWhite))
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(ItalyRed))
    }
}

@Preview(widthDp = 180)
@Composable
private fun ItalyFlagPreview() = FlagFrame { ItalyFlag(Modifier.fillMaxSize()) }