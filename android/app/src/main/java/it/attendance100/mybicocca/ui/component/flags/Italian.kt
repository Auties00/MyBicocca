package it.attendance100.mybicocca.ui.component.flags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

// Define accurate standard colors
val ItalyGreen = Color(0xFF009246)
val ItalyWhite = Color(0xFFF1F2F1)
val ItalyRed = Color(0xFFCE2B37)

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