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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
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

/** How far the tray rises above the card. */
private val TrayRise = 46.dp
private val CardHeight = 150.dp
private val TrayHeight = 140.dp

/** Room above the tray for its raised left end once tilted. */
private val TrayTop = 18.dp
private val TrayHandle = 10.dp
private val TrayCorner = 18.dp

/** The tray's right end, past the card's edge and cut off by it. */
private val TrayHidden = 44.dp

// A terracotta canteen tray, the same in every palette.
private val TrayRim = Color(0xFFA35143)
private val TrayWell = Color(0xFF7A3634)
private val TrayEdge = Color(0xFFB8634F)
private val TrayBevel = Color(0xFF8E4640)

/** The share of the card the tray takes on the right; the text has the rest. */
private const val TRAY_SHARE = 0.56f
private const val TRAY_TILT = 14f
private val TrayPlate = 76.dp

/**
 * The menu card on the hub without a booking: a canteen tray, with today's primo and secondo, a
 * bread roll, an apple and the cutlery, sticking out of the card's right side and over its top. It slides out of
 * the card when the sheet opens.
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TrayRise + CardHeight)
            .testTag(MensaTestTags.HUB_MENU)
            .clickable(interactionSource = press, indication = null) { haptic.tap(); onClick() },
    ) {
        val trayVisible = maxWidth * TRAY_SHARE
        val textWidth = maxWidth - trayVisible
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = TrayRise),
            shape = RoundedCornerShape(28.dp),
            color = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
        ) {
            Box(modifier = Modifier.fillMaxSize().indication(press, ripple())) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(textWidth)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = stringResource(R.string.mensa_hub_menu_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.mensa_hub_menu_body),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        // Cut only at the card's right edge, so the tray can rise above the card.
        Box(modifier = Modifier.fillMaxSize().clipAtEnd()) {
            MensaTray(
                tray = tray,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = TrayHidden, y = TrayTop)
                    .size(width = trayVisible + TrayHidden, height = TrayHeight)
                    .graphicsLayer {
                        translationX = (1f - slide.value) * size.width
                        rotationZ = TRAY_TILT
                    },
            )
        }
    }
}

/** Clips at this element's end edge only; what it draws above, below or before stays visible. */
private fun Modifier.clipAtEnd(): Modifier = drawWithContent {
    clipRect(left = -size.width, top = -size.height, right = size.width, bottom = size.height * 2) {
        this@drawWithContent.drawContent()
    }
}

/** The tray and what is on it, its left end showing a handle; the right end is cut off by the card. */
@Composable
private fun MensaTray(tray: MensaHubTray, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clearAndSetSemantics { }) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawTray() }
        val left = TrayHandle + 10.dp
        Image(
            painter = painterResource(R.drawable.mensa_art_tray_cutlery),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = left)
                .height(TrayHeight - 20.dp)
                .aspectRatio(48f / 108f)
                .rotate(-4f),
        )
        TrayPlate(
            drawable = tray.primo.trayDrawable(MensaArt.PASTA_TOMATO),
            modifier = Modifier.offset(x = left + 46.dp, y = 52.dp),
        )
        TrayPlate(
            drawable = tray.secondo.trayDrawable(MensaArt.MEAT),
            modifier = Modifier.offset(x = left + 46.dp + TrayPlate - 12.dp, y = 44.dp),
        )
        TrayItem(R.drawable.mensa_art_tray_bread, size = 58.dp, modifier = Modifier.offset(x = left + 50.dp, y = 2.dp).rotate(-12f))
        TrayItem(R.drawable.mensa_art_tray_apple, size = 50.dp, modifier = Modifier.offset(x = left + 124.dp, y = 2.dp))
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
    val bodyHeight = size.height - 4.dp.toPx()
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
