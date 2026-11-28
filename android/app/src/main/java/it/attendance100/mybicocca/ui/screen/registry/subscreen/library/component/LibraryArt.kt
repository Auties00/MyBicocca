package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import kotlinx.coroutines.delay

private const val PAGE_TURN_LOOP_MS = 3_600L

/**
 * Plays an animated vector drawable in a loop by recreating its painter every [loopMillis] while
 * parsing the [AnimatedImageVector] once. Honors reduced motion by freezing on the first frame
 * when the system animator duration scale is 0.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
internal fun LibraryLoopingArt(
    @DrawableRes drawable: Int,
    loopMillis: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val animationsEnabled = remember(context, inspection) {
        if (inspection) {
            false
        } else {
            runCatching {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f,
                ) > 0f
            }.getOrDefault(true)
        }
    }

    val vector = AnimatedImageVector.animatedVectorResource(drawable)

    if (!animationsEnabled) {
        Image(
            painter = rememberAnimatedVectorPainter(vector, atEnd = false),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
        return
    }

    var round by remember(drawable) { mutableIntStateOf(0) }
    LaunchedEffect(drawable, loopMillis) {
        while (true) {
            delay(loopMillis)
            round++
        }
    }
    key(round) {
        var atEnd by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { atEnd = true }
        Image(
            painter = rememberAnimatedVectorPainter(vector, atEnd),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    }
}

/** Library loading state using the page-turn illustration with a polite live region for accessibility. */
@Composable
internal fun LibraryLoading(
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LibraryLoopingArt(
            drawable = R.drawable.library_art_page_turn,
            loopMillis = PAGE_TURN_LOOP_MS,
            modifier = Modifier.size(width = 160.dp, height = 120.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** A chair fills less than half its drawing's width, so it can be drawn larger than a desk or carrel. */
internal val LibraryZoneColor.isChair: Boolean
    get() = this != LibraryZoneColor.Carrels && this != LibraryZoneColor.Other

/** Maps a seating zone color/kind to its decorative vector illustration. */
@DrawableRes
internal fun LibraryZoneColor.artDrawable(): Int = when (this) {
    LibraryZoneColor.Rossa -> R.drawable.library_art_zone_chair_red
    LibraryZoneColor.Gialla -> R.drawable.library_art_zone_chair_yellow
    LibraryZoneColor.Verde -> R.drawable.library_art_zone_chair_green
    LibraryZoneColor.Lilla -> R.drawable.library_art_zone_chair_lilac
    LibraryZoneColor.Blu -> R.drawable.library_art_zone_chair_blue
    LibraryZoneColor.Arancio -> R.drawable.library_art_zone_chair_orange
    LibraryZoneColor.Carrels -> R.drawable.library_art_carrel
    LibraryZoneColor.Other -> R.drawable.library_art_desk
}

@Preview(name = "LibraryLoading · Light", showBackground = true)
@Composable
private fun LibraryLoadingLightPreview() {
    LibraryPreview(dark = false) {
        LibraryLoading(label = "Caricamento biblioteche…")
    }
}

@Preview(
    name = "LibraryLoading · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryLoadingDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryLoading(label = "Caricamento biblioteche…")
    }
}
