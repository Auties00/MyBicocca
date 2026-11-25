package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaHubTray
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaArt
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishArtwork
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaDishDrawings
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/** How far the tray rises above the card. */
private val TrayRise = 46.dp
private val CardHeight = 176.dp
private val CardCorner = 28.dp
private val TrayHeight = 142.dp

/** The tray's long side; most of it runs past the card's right and bottom edges, cut off by them. */
private val TrayLength = 280.dp
private val TrayHandle = 10.dp
private val TrayCorner = 18.dp
private val TrayShadow = 4.dp

/** Where the tray's top-left corner sits: across the card's width, and down from the top. */
private const val TRAY_LEFT = 0.52f
private val TrayTop = 16.dp
private const val TRAY_TILT = 21f

// A terracotta canteen tray, the same in every palette.
private val TrayRim = Color(0xFFA35143)
private val TrayWell = Color(0xFF7A3634)
private val TrayEdge = Color(0xFFB8634F)
private val TrayBevel = Color(0xFF8E4640)

private val TrayPlate = 84.dp

/**
 * The menu card on the hub without a booking: a tilted canteen tray, with today's primo and
 * secondo, a bread roll, an apple and the cutlery, rising over the card's top and cut off by its
 * right and bottom edges. It slides out of the card when the sheet opens.
 *
 * @param playIntro whether the tray slides out now; false shows it in place, as when coming back
 * to the hub from a page inside the sheet.
 */
@Composable
internal fun MensaMenuTrayCard(
    tray: MensaHubTray,
    playIntro: Boolean,
    onIntroPlayed: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val press = remember { MutableInteractionSource() }
    val slide = remember { Animatable(if (playIntro) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (slide.value < 1f) {
            onIntroPlayed()
            // Once the sheet has come up.
            delay(250)
            slide.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessVeryLow))
        }
    }
    val open = { haptic.tap(); onClick() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TrayRise + CardHeight)
            .testTag(MensaTestTags.HUB_MENU)
            .clickable(interactionSource = press, indication = null, onClick = open),
    ) {
        val trayLeft = maxWidth * TRAY_LEFT
        val cardWidth = maxWidth
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = TrayRise),
            shape = RoundedCornerShape(CardCorner),
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
        ) {
            Box(modifier = Modifier.fillMaxSize().indication(press, ripple())) {
                // The tray's left edge slants inwards going down, so each line stops a little sooner.
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = stringResource(R.string.mensa_hub_menu_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = trayLeft - 56.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.mensa_hub_menu_body),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = trayLeft - 72.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = open,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.onPrimaryContainer,
                            contentColor = scheme.primaryContainer,
                        ),
                        contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.mensa_hub_menu_browse),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().clipToCardEnd()) {
            MensaTray(
                tray = tray,
                modifier = Modifier
                    .offset(x = trayLeft - TrayHandle, y = TrayTop)
                    .size(width = TrayHandle + TrayLength, height = TrayHeight + TrayShadow)
                    .graphicsLayer {
                        // Turned about the tray's top-left corner, which stays where it is placed.
                        transformOrigin = TransformOrigin(TrayHandle.toPx() / size.width, 0f)
                        rotationZ = TRAY_TILT
                        // Slides in along its own length, from past the card's right edge.
                        val away = (1f - slide.value) * cardWidth.toPx()
                        val angle = Math.toRadians(TRAY_TILT.toDouble())
                        translationX = away * cos(angle).toFloat()
                        translationY = away * sin(angle).toFloat()
                    },
            )
        }
    }
}

/**
 * Clips to the card's right and bottom edges, with its rounded bottom-right corner; what is drawn
 * above the card or before it stays visible.
 */
private fun Modifier.clipToCardEnd(): Modifier = drawWithContent {
    val corner = CornerRadius(CardCorner.toPx())
    val bounds = Path().apply {
        addRoundRect(
            RoundRect(
                left = -size.width,
                top = -size.height,
                right = size.width,
                bottom = size.height,
                bottomRightCornerRadius = corner,
            ),
        )
    }
    clipPath(bounds) { this@drawWithContent.drawContent() }
}

/** The tray and what is on it, its left end showing a handle; the rest runs past the card. */
@Composable
private fun MensaTray(tray: MensaHubTray, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clearAndSetSemantics { }) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawTray() }
        // Laid out from the tray's left end; only about its first 190dp shows inside the card.
        val body = TrayHandle
        Image(
            painter = painterResource(R.drawable.mensa_art_tray_cutlery),
            contentDescription = null,
            modifier = Modifier
                .offset(x = body + 10.dp, y = 12.dp)
                .height(TrayHeight - 24.dp)
                .aspectRatio(48f / 108f)
                .rotate(-4f),
        )
        TrayPlate(
            drawable = tray.primo.trayDrawable(MensaArt.PASTA_TOMATO),
            modifier = Modifier.offset(x = body + 64.dp, y = 50.dp),
        )
        TrayPlate(
            drawable = tray.secondo.trayDrawable(MensaArt.MEAT),
            modifier = Modifier.offset(x = body + 64.dp + TrayPlate - 10.dp, y = 44.dp),
        )
        TrayItem(R.drawable.mensa_art_tray_bread, size = 60.dp, modifier = Modifier.offset(x = body + 68.dp, y = 6.dp).rotate(-12f))
        TrayItem(R.drawable.mensa_art_tray_apple, size = 54.dp, modifier = Modifier.offset(x = body + 136.dp, y = 4.dp))
    }
}

@Composable
private fun TrayItem(@DrawableRes drawable: Int, size: Dp, modifier: Modifier = Modifier) {
    Image(painter = painterResource(drawable), contentDescription = null, modifier = modifier.size(size))
}

@Composable
private fun TrayPlate(@DrawableRes drawable: Int, modifier: Modifier = Modifier) {
    // Today's dish replaces the stand-in once the menu has loaded.
    Crossfade(targetState = drawable, modifier = modifier, label = "tray_plate") { id ->
        Image(painter = painterResource(id), contentDescription = null, modifier = Modifier.size(TrayPlate))
    }
}

@DrawableRes
private fun MensaDish?.trayDrawable(standIn: MensaArt): Int =
    this?.let { MensaDishDrawings.of(it.id) ?: MensaDishArtwork.forDish(it).drawable } ?: standIn.drawable

/** A canteen tray seen from above: a raised rim around a sunken well, a long handle on the left end. */
private fun DrawScope.drawTray() {
    val handle = TrayHandle.toPx()
    val corner = CornerRadius(TrayCorner.toPx())
    val bodyHeight = size.height - TrayShadow.toPx()
    // Shadow on the card
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.22f),
        topLeft = Offset(handle + 2.dp.toPx(), 5.dp.toPx()),
        size = Size(size.width - handle, bodyHeight),
        cornerRadius = corner,
    )
    // The handle: a tab along half the left side, tapering into the rim at both ends.
    val handleTop = bodyHeight * 0.24f
    val handleBottom = bodyHeight * 0.76f
    val taper = 14.dp.toPx()
    val tab = Path().apply {
        moveTo(handle + 2.dp.toPx(), handleTop - 4.dp.toPx())
        quadraticTo(handle * 0.3f, handleTop, handle * 0.15f, handleTop + taper)
        lineTo(handle * 0.15f, handleBottom - taper)
        quadraticTo(handle * 0.3f, handleBottom, handle + 2.dp.toPx(), handleBottom + 4.dp.toPx())
        close()
    }
    drawPath(tab, color = TrayRim)
    // Rim, its lit edge, and the well inside with a bevel along its edge
    drawRoundRect(color = TrayRim, topLeft = Offset(handle, 0f), size = Size(size.width - handle, bodyHeight), cornerRadius = corner)
    drawRoundRect(
        color = TrayEdge,
        topLeft = Offset(handle + 1.dp.toPx(), 1.dp.toPx()),
        size = Size(size.width - handle - 2.dp.toPx(), bodyHeight - 2.dp.toPx()),
        cornerRadius = corner,
        style = Stroke(width = 1.5.dp.toPx()),
    )
    val inset = 8.dp.toPx()
    drawRoundRect(
        color = TrayBevel,
        topLeft = Offset(handle + inset - 1.5.dp.toPx(), inset - 1.5.dp.toPx()),
        size = Size(size.width - handle - 2 * inset + 3.dp.toPx(), bodyHeight - 2 * inset + 3.dp.toPx()),
        cornerRadius = CornerRadius(13.dp.toPx()),
    )
    drawRoundRect(
        color = TrayWell,
        topLeft = Offset(handle + inset, inset),
        size = Size(size.width - handle - 2 * inset, bodyHeight - 2 * inset),
        cornerRadius = CornerRadius(12.dp.toPx()),
    )
}

@Preview(name = "Menu tray card", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 230)
@Composable
private fun MenuTrayCardPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaMenuTrayCard(
            tray = MensaHubTray(),
            playIntro = false,
            onIntroPlayed = {},
            onClick = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}
