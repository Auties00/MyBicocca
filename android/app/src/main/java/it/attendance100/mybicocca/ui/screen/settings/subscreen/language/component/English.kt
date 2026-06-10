package it.attendance100.mybicocca.ui.screen.settings.subscreen.language.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview

/**
 * Combined UK/USA tile representing English in the language picker: the UK flag clipped to
 * the bottom-right diagonal half, layered over the full USA flag.
 */
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