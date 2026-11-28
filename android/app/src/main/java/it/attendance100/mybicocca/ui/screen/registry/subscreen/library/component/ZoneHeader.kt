package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/** Decorative zone illustration participating in the Zones ↔ DateTime ↔ Seats shared element. */
@Composable
internal fun LibraryZoneArt(
    zone: LibraryZone,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(zone.color.artDrawable()),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .librarySharedElement(key = libraryZoneArtSharedKey(zone.resourceTypeId))
            .then(modifier),
    )
}

/** Compact selected-zone summary banner shown at the top of DateTime and Seats. */
@Composable
internal fun ZoneHeader(
    zone: LibraryZone,
    libraryName: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LibraryZoneArt(
                zone = zone,
                modifier = Modifier.size(width = 64.dp, height = 48.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (libraryName.isNotBlank()) {
                    Text(
                        text = libraryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(name = "ZoneHeader · Light", showBackground = true)
@Composable
private fun ZoneHeaderLightPreview() {
    LibraryPreview(dark = false) {
        ZoneHeader(
            zone = LibraryPreviewDefaults.Zones.first(),
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "ZoneHeader · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun ZoneHeaderDarkPreview() {
    LibraryPreview(dark = true) {
        ZoneHeader(
            zone = LibraryPreviewDefaults.Zones[4],
            libraryName = LibraryPreviewDefaults.Libraries.first().name,
            modifier = Modifier.padding(16.dp),
        )
    }
}
