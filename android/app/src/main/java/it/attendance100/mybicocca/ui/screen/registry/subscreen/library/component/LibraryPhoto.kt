package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

private val DesaturatedColorFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(0f) },
)

/**
 * Library photo showing a plain surface while a remote photo loads and the bookshelf drawing only
 * when no URL is present or loading fails. Participates in the card-to-detail shared-element
 * transition via [libraryPhotoSharedKey]. When [dimmedAndDesaturated] is true (closed library
 * card), the photo or fallback is rendered in grayscale with a soft dimming veil.
 */
@Composable
internal fun LibraryPhoto(
    libraryId: String,
    pictureUrl: String?,
    modifier: Modifier = Modifier,
    clipShape: Shape? = null,
    overlayClipShape: Shape = clipShape ?: MaterialTheme.shapes.extraLarge,
    dimmedAndDesaturated: Boolean = false,
) {
    var failed by remember(pictureUrl) { mutableStateOf(false) }
    val showRemote = !pictureUrl.isNullOrBlank() && !failed
    val colorFilter = if (dimmedAndDesaturated) DesaturatedColorFilter else null

    Box(
        modifier = Modifier
            .librarySharedElement(
                key = libraryPhotoSharedKey(libraryId),
                clipShape = overlayClipShape,
            )
            .then(modifier)
            .then(if (clipShape != null) Modifier.clip(clipShape) else Modifier)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (showRemote) {
            AsyncImage(
                model = pictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter,
                onError = { failed = true },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.library_art_bookshelf),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
            )
        }
        if (dimmedAndDesaturated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
        }
    }
}

@Preview(name = "LibraryPhoto Open · Light", showBackground = true)
@Composable
private fun LibraryPhotoOpenLightPreview() {
    LibraryPreview(dark = false) {
        LibraryPhoto(
            libraryId = LibraryCatalog.CENTRALE_ID,
            pictureUrl = null,
            clipShape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        )
    }
}

@Preview(
    name = "LibraryPhoto Open · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryPhotoOpenDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryPhoto(
            libraryId = LibraryCatalog.CENTRALE_ID,
            pictureUrl = null,
            clipShape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        )
    }
}

@Preview(name = "LibraryPhoto Closed · Light", showBackground = true)
@Composable
private fun LibraryPhotoClosedLightPreview() {
    LibraryPreview(dark = false) {
        LibraryPhoto(
            libraryId = LibraryCatalog.MEDICINA_ID,
            pictureUrl = null,
            clipShape = MaterialTheme.shapes.extraLarge,
            dimmedAndDesaturated = true,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        )
    }
}

@Preview(
    name = "LibraryPhoto Closed · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LibraryPhotoClosedDarkPreview() {
    LibraryPreview(dark = true) {
        LibraryPhoto(
            libraryId = LibraryCatalog.MEDICINA_ID,
            pictureUrl = null,
            clipShape = MaterialTheme.shapes.extraLarge,
            dimmedAndDesaturated = true,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        )
    }
}
