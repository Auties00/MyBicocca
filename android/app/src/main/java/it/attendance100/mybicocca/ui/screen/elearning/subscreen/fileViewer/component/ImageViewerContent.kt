package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import java.io.File

// telephoto's ZoomableAsyncImage sub-samples huge bitmaps (scanned slides won't OOM)
// and gives pinch/double-tap zoom. The dedicated loader adds gif + svg decoding on top
// of the app default.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageViewerContent(
    localPath: String,
    fileName: String,
    onShare: () -> Unit,
    onSaveToGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .build()
    }

    // true = Fit (letterboxed, full image visible); false = Crop (fills screen, may be clipped).
    // Toggling resets the zoom state via key() so the user starts fresh in each mode.
    var fitToScreen by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        key(fitToScreen) {
            ZoomableAsyncImage(
                model = File(localPath),
                contentDescription = fileName,
                imageLoader = imageLoader,
                contentScale = if (fitToScreen) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
        }
        ViewerBottomBar(
            primary = ViewerAction(
                icon = Icons.Outlined.Download,
                label = "Salva in galleria",
                onClick = onSaveToGallery,
            ),
            ViewerAction(
                icon = Icons.Outlined.Share,
                label = "Condividi",
                onClick = onShare,
            ),
            ViewerAction(
                // Icon shows the mode you'll switch TO, so the user knows what tapping does.
                icon = if (fitToScreen) Icons.Outlined.Fullscreen else Icons.Outlined.FitScreen,
                label = if (fitToScreen) "Riempi schermo" else "Adatta immagine",
                onClick = { fitToScreen = !fitToScreen },
            ),
        )
    }
}
