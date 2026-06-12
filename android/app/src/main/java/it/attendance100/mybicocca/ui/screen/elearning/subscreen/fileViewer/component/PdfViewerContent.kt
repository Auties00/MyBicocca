package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer.component

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RuntimeShader
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.intellij.lang.annotations.Language
import java.io.Closeable
import java.io.File


/**
 * AGSL shader for dark-theme page rendering on API 33+: a perception-aware "smart invert" that
 * flips near-grayscale pixels (text, paper) to a dark palette while leaving saturated colors
 * (figures, highlights) untouched, blending smoothly between the two by saturation.
 */
@Language("AGSL")
const val SmartInvertShader = """
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        half4 color = composable.eval(fragCoord);
        
        // Don't apply to transparent padding added by ContentScale.Fit
        if (color.a < 0.5) return color;
        
        // Calculate color saturation (max - min)
        half cMin = min(color.r, min(color.g, color.b));
        half cMax = max(color.r, max(color.g, color.b));
        half sat = cMax - cMin;
        
        // Calculate the perception-aware inverted color
        half3 inverted;
        inverted.r = 0.902 - (0.808 * color.r);
        inverted.g = 0.863 - (0.761 * color.g);
        inverted.b = 0.784 - (0.667 * color.b);
        
        // Smoothly blend based on saturation.
        // Grayscale pixels (sat < 0.05) are fully inverted.
        // Colorful pixels (sat > 0.2) are untouched.
        half invertWeight = 1.0 - smoothstep(0.05, 0.2, sat);
        
        color.rgb = mix(color.rgb, inverted, invertWeight);
        
        return color;
    }
"""

/**
 * In-app PDF viewer: renders the document with the platform [PdfRenderer] into a vertical or
 * horizontal pager — one zoomable page per item — with a floating "current / total" pill on top
 * and the shared bottom action bar (download, share, theme cycle, pager-direction toggle,
 * picture-in-picture). Shows an inline message when the document cannot be opened.
 *
 * [themeMode] and [orientation] are persisted preferences owned by the ViewModel; the action bar
 * cycles/toggles them via the callbacks. Dark rendering keeps pages legible without re-rendering
 * the bitmaps: the smart-invert runtime shader on API 33+, an approximating inversion
 * ColorMatrix below. Pages render at 1.5x the viewport width (capped) so they stay sharp when
 * zoomed.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PdfViewerContent(
    localPath: String,
    themeMode: PdfThemeMode,
    orientation: PdfPagerOrientation,
    onThemeModeChange: (PdfThemeMode) -> Unit,
    onOrientationChange: (PdfPagerOrientation) -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onPip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val document = remember(localPath) { runCatching { PdfDocument(localPath) }.getOrNull() }
    DisposableEffect(document) {
        onDispose { document?.close() }
    }

    if (document == null || document.pageCount == 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.elearning_file_pdf_open_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { document.pageCount })

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val widthPx = with(density) { (maxWidth.toPx() * 1.5f).toInt() }.coerceIn(1, 2400)

        val systemDark = isSystemInDarkTheme()
        val isDarkTheme = when (themeMode) {
            PdfThemeMode.System -> systemDark
            PdfThemeMode.Light -> false
            PdfThemeMode.Dark -> true
        }

        if (orientation == PdfPagerOrientation.Vertical) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 8.dp,
            ) { index ->
                PdfPage(
                    document = document,
                    index = index,
                    widthPx = widthPx,
                    isDarkTheme = isDarkTheme
                )
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 8.dp,
            ) { index ->
                PdfPage(
                    document = document,
                    index = index,
                    widthPx = widthPx,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        if (document.pageCount > 1) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.elearning_file_pdf_page_indicator,
                        pagerState.currentPage + 1,
                        document.pageCount,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
        ViewerBottomBar(
            primary = ViewerAction(
                icon = Icons.Outlined.Download,
                label = stringResource(R.string.elearning_file_download),
                onClick = onDownload,
            ),
            ViewerAction(
                icon = Icons.Outlined.Share,
                label = stringResource(R.string.elearning_file_share),
                onClick = onShare,
            ),
            ViewerAction(
                icon = when (themeMode) {
                    PdfThemeMode.System -> Icons.Outlined.SettingsBrightness
                    PdfThemeMode.Light -> Icons.Outlined.LightMode
                    PdfThemeMode.Dark -> Icons.Outlined.DarkMode
                },
                label = stringResource(R.string.elearning_file_change_theme),
                onClick = {
                    onThemeModeChange(
                        when (themeMode) {
                            PdfThemeMode.System -> PdfThemeMode.Light
                            PdfThemeMode.Light -> PdfThemeMode.Dark
                            PdfThemeMode.Dark -> PdfThemeMode.System
                        }
                    )
                },
            ),
            ViewerAction(
                icon = if (orientation == PdfPagerOrientation.Vertical) Icons.Outlined.SwapVert else Icons.Outlined.SwapHoriz,
                label = stringResource(R.string.elearning_file_change_direction),
                onClick = {
                    onOrientationChange(
                        if (orientation == PdfPagerOrientation.Vertical)
                            PdfPagerOrientation.Horizontal
                        else
                            PdfPagerOrientation.Vertical
                    )
                },
            ),
            ViewerAction(
                icon = Icons.Outlined.PictureInPictureAlt,
                label = stringResource(R.string.elearning_file_picture_in_picture),
                onClick = onPip,
            ),
        )
    }
}


/**
 * One pager page: renders the bitmap lazily (spinner placeholder until ready), makes it
 * pinch/double-tap zoomable, and applies the dark-mode filter or shader when requested.
 */
@Composable
private fun PdfPage(document: PdfDocument, index: Int, widthPx: Int, isDarkTheme: Boolean) {
    var page by remember(index, widthPx) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(index, widthPx) {
        page = runCatching { document.renderPage(index, widthPx).asImageBitmap() }.getOrNull()
    }

    val bitmap = page
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            val zoomState = rememberZoomableState()

            val colorFilter = remember(isDarkTheme) {
                if (isDarkTheme && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    ColorFilter.colorMatrix(
                        ColorMatrix(
                            floatArrayOf(
                                -0.808f, 0f, 0f, 0f, 230f,
                                0f, -0.761f, 0f, 0f, 220f,
                                0f, 0f, -0.667f, 0f, 200f,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                    )
                } else null
            }

            val smartShader = remember(isDarkTheme) {
                if (isDarkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    RuntimeShader(SmartInvertShader)
                } else null
            }

            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.elearning_file_pdf_page, index + 1),
                contentScale = ContentScale.Fit,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState)
                    .then(
                        if (smartShader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Modifier.graphicsLayer {
                                renderEffect =
                                    android.graphics.RenderEffect.createRuntimeShaderEffect(
                                        smartShader, "composable"
                                    ).asComposeRenderEffect()
                            }
                        } else Modifier
                    )
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

/**
 * Closeable wrapper around [PdfRenderer]. Page rendering runs on the IO dispatcher and is
 * serialized through a mutex because the renderer is not thread-safe; pages are rasterized onto
 * a white background since PDF content assumes white paper.
 */
private class PdfDocument(path: String) : Closeable {
    private val descriptor =
        ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(descriptor)
    private val mutex = Mutex()

    val pageCount: Int = renderer.pageCount

    suspend fun renderPage(index: Int, widthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(index).use { page ->
                val heightPx = (widthPx * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
                val bitmap = createBitmap(widthPx, heightPx)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    override fun close() {
        runCatching { renderer.close() }
        runCatching { descriptor.close() }
    }
}

