package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
@Composable
fun ImageViewerContent(localPath: String, fileName: String, modifier: Modifier = Modifier) {
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
    ZoomableAsyncImage(
        model = File(localPath),
        contentDescription = fileName,
        imageLoader = imageLoader,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}
