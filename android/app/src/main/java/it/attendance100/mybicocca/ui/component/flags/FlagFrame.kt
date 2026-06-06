package it.attendance100.mybicocca.ui.component.flags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Shared geometry for every flag shown in the language picker: a 3:2 box with
 * rounded corners. Individual flags draw at [Modifier.fillMaxSize] inside this
 * frame, so the aspect ratio and corner radius live in exactly one place.
 */
@Composable
fun FlagFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(12.dp)),
        content = content,
    )
}
