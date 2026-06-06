package it.attendance100.mybicocca.ui.component.flags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import it.attendance100.mybicocca.ui.component.shape.DiagonalSplitShape

@Composable
fun UkUsaFlag(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        UsaFlag(modifier = Modifier.fillMaxSize())
        UkFlag(
            modifier = Modifier
                .fillMaxSize()
                .clip(DiagonalSplitShape(splitTopLeftToBottomRight = false))
        )
    }
}

@Preview(widthDp = 180)
@Composable
private fun UkUsaFlagPreview() = FlagFrame { UkUsaFlag(Modifier.fillMaxSize()) }