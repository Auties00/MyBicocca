package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuIdentity
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The artwork styles a menu card can wear; [MENSA_MENU_ART_STYLE] is the one the app uses. */
internal const val MENSA_MENU_ART_STYLES = 6

/** Style shown in the app: 0 is the drawn-from-the-brief one, 1..5 are the alternatives. */
internal const val MENSA_MENU_ART_STYLE = 0

private val Cream = Color(0xFFFFFFFF)
private val Accent = Color(0xFFE39B52)

private data class MenuPalette(val background: Color)

private fun MensaMenuIdentity.palette(): MenuPalette = when (kind) {
    MensaMenuKind.CANTEEN -> MenuPalette(Color(0xFFCE3B27))
    MensaMenuKind.GLUTEN_FREE -> MenuPalette(Color(0xFF6B2142))
    MensaMenuKind.BAR -> MenuPalette(
        when (building) {
            "U6" -> Color(0xFF4C7A34)
            "U12" -> Color(0xFF1A2A4A)
            else -> Color(0xFF6E1F1B)
        },
    )
}

/**
 * A menu drawn as a portrait card: flat colour, big white silhouettes of what is served, and the
 * name set over it in the same few words every time ("MENÙ / BAR / U6"). Everything is vector,
 * so it stays sharp at any size and takes the app's languages through the label strings.
 */
@Composable
internal fun MensaMenuCard(
    identity: MensaMenuIdentity,
    modifier: Modifier = Modifier,
    style: Int = MENSA_MENU_ART_STYLE,
    showLabels: Boolean = true,
    aspect: Float = 0.62f,
    corner: Dp = 20.dp,
) {
    val palette = identity.palette()
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(corner)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(palette.background)
            drawMenuArt(identity, style.coerceIn(0, MENSA_MENU_ART_STYLES - 1), palette.background)
        }
        if (showLabels) MenuLabels(identity, style, maxWidth.value)
    }
}

@Composable
private fun MenuLabels(identity: MensaMenuIdentity, style: Int, widthDp: Float) {
    val menu = stringResource(R.string.mensa_art_menu).uppercase()
    val kindWord = when (identity.kind) {
        MensaMenuKind.BAR -> stringResource(R.string.mensa_art_bar)
        else -> stringResource(R.string.mensa_art_canteen)
    }.uppercase()
    val last = when (identity.kind) {
        MensaMenuKind.BAR -> identity.building.orEmpty()
        MensaMenuKind.GLUTEN_FREE -> stringResource(R.string.mensa_art_gluten_free).uppercase()
        MensaMenuKind.CANTEEN -> "U6-U12"
    }
    val big = (widthDp * when (identity.kind) {
        MensaMenuKind.GLUTEN_FREE -> 0.13f
        MensaMenuKind.CANTEEN -> 0.19f
        MensaMenuKind.BAR -> 0.26f
    }).sp
    val mid = (widthDp * 0.12f).sp
    val title = (widthDp * 0.2f).sp

    val atTop = style == 1 || style == 3 || (style == 0 && identity.kind == MensaMenuKind.CANTEEN)
    val atBottom = style == 4 || style == 5
    val centred = style == 2 || (style == 0 && identity.kind != MensaMenuKind.BAR)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = (widthDp * 0.1f).dp, vertical = (widthDp * 0.14f).dp),
        verticalArrangement = if (atTop) Arrangement.Top else if (atBottom) Arrangement.Bottom else Arrangement.Center,
        horizontalAlignment = if (centred) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        val align = if (centred) TextAlign.Center else TextAlign.Start
        Text(menu, color = Cream, fontSize = title, fontWeight = FontWeight.Black, lineHeight = title, textAlign = align, letterSpacing = (-1).sp)
        Text(kindWord, color = Accent, fontSize = mid, fontWeight = FontWeight.Black, lineHeight = mid, textAlign = align, letterSpacing = 1.sp)
        Text(last, color = Cream, fontSize = big, fontWeight = FontWeight.Black, lineHeight = big, textAlign = align, letterSpacing = (-1).sp, maxLines = 2)
    }
}

// Drawing ----------------------------------------------------------------------------------

private fun DrawScope.drawMenuArt(identity: MensaMenuIdentity, style: Int, bg: Color) {
    val w = size.width
    val h = size.height
    when (style) {
        0 -> when (identity.kind) {
            MensaMenuKind.BAR -> {
                cup(w * 0.58f, h * 0.5f, w * 0.4f, h * 0.6f, bg)
                burger(w * -0.02f, h * 0.86f, w * 0.6f, h * 0.2f, bg)
            }
            MensaMenuKind.GLUTEN_FREE -> {
                spoon(w * 0.04f, h * 0.1f, h * 0.84f, w * 0.26f)
                fork(w * 0.95f, h * 0.05f, h * 0.86f, w * 0.2f)
            }
            MensaMenuKind.CANTEEN -> {
                drawRect(Accent, Offset(0f, h * 0.87f), Size(w, h * 0.13f))
                rotate(90f, Offset(w * 0.5f, h * 0.74f)) { fork(w * 0.5f, h * 0.74f - w * 0.62f, h * 0.74f + w * 0.62f, w * 0.34f) }
            }
        }
        // A cloche over the plate, steam rising.
        1 -> {
            cloche(w * 0.5f, h * 0.97f, w * 0.74f, bg)
            steam(w * 0.5f, h * 0.67f, w * 0.06f)
        }
        // Cutlery framing the card.
        2 -> {
            spoon(w * 0.04f, h * 0.06f, h * 0.9f, w * 0.3f)
            fork(w * 0.95f, h * 0.04f, h * 0.92f, w * 0.26f)
        }
        // A plate with the cutlery either side.
        3 -> {
            plate(w * 0.5f, h * 0.77f, w * 0.31f, bg)
            fork(w * 0.1f, h * 0.62f, h * 1.05f, w * 0.14f)
            spoon(w * 0.9f, h * 0.62f, h * 1.05f, w * 0.12f)
        }
        // Three tiles across the top: spoon, plate, fork.
        4 -> {
            val tile = w * 0.27f
            for (i in 0..2) {
                val x = w * 0.08f + i * (tile + w * 0.055f)
                val y = h * 0.06f
                drawRoundRect(Cream, Offset(x, y), Size(tile, tile), CornerRadius(w * 0.04f))
                val cx = x + tile / 2
                val cy = y + tile / 2
                when (i) {
                    0 -> rotate(35f, Offset(cx, cy)) { spoon(cx, y + tile * 0.12f, y + tile * 0.9f, tile * 0.3f, bg) }
                    1 -> plate(cx, cy, tile * 0.34f, bg, ink = bg)
                    else -> rotate(-35f, Offset(cx, cy)) { fork(cx, y + tile * 0.1f, y + tile * 0.9f, tile * 0.34f, bg) }
                }
            }
        }
        // A huge fork bleeding in from the top.
        else -> fork(w * 0.5f, -h * 0.04f, h * 0.5f, w * 0.5f)
    }
}

private fun DrawScope.fork(cx: Float, top: Float, bottom: Float, width: Float, color: Color = Cream) {
    val len = bottom - top
    val tineLen = len * 0.36f
    val t = width / 7f
    val left = cx - width / 2
    for (i in 0..3) {
        drawRoundRect(color, Offset(left + i * 2 * t, top), Size(t, tineLen + t), CornerRadius(t / 2))
    }
    val neckTop = top + tineLen
    val handleW = width * 0.26f
    val neck = Path().apply {
        moveTo(left, neckTop)
        lineTo(left + width, neckTop)
        cubicTo(left + width, neckTop + len * 0.14f, cx + handleW / 2, neckTop + len * 0.08f, cx + handleW / 2, neckTop + len * 0.2f)
        lineTo(cx - handleW / 2, neckTop + len * 0.2f)
        cubicTo(cx - handleW / 2, neckTop + len * 0.08f, left, neckTop + len * 0.14f, left, neckTop)
        close()
    }
    drawPath(neck, color)
    val handleTop = neckTop + len * 0.18f
    drawRoundRect(color, Offset(cx - handleW / 2, handleTop), Size(handleW, bottom - handleTop), CornerRadius(handleW / 2))
}

private fun DrawScope.spoon(cx: Float, top: Float, bottom: Float, width: Float, color: Color = Cream) {
    val bowlH = width * 1.4f
    drawOval(color, Offset(cx - width / 2, top), Size(width, bowlH))
    val handleW = width * 0.27f
    val neckTop = top + bowlH * 0.8f
    val neck = Path().apply {
        moveTo(cx - width * 0.2f, neckTop)
        lineTo(cx + width * 0.2f, neckTop)
        lineTo(cx + handleW / 2, top + bowlH + width * 0.25f)
        lineTo(cx - handleW / 2, top + bowlH + width * 0.25f)
        close()
    }
    drawPath(neck, color)
    val handleTop = top + bowlH + width * 0.2f
    drawRoundRect(color, Offset(cx - handleW / 2, handleTop), Size(handleW, (bottom - handleTop).coerceAtLeast(0f)), CornerRadius(handleW / 2))
}

/** A take-away cup, lid on, its sleeve a band of background colour; it bleeds off the bottom. */
private fun DrawScope.cup(left: Float, top: Float, width: Float, height: Float, bg: Color) {
    val lidH = height * 0.07f
    drawRoundRect(Cream, Offset(left, top), Size(width, lidH), CornerRadius(lidH / 2))
    val body = Path().apply {
        moveTo(left + width * 0.04f, top + lidH)
        lineTo(left + width * 0.96f, top + lidH)
        lineTo(left + width * 0.84f, top + height)
        lineTo(left + width * 0.16f, top + height)
        close()
    }
    drawPath(body, Cream)
    drawRect(bg, Offset(left, top + height * 0.4f), Size(width, height * 0.035f))
    drawRect(bg, Offset(left - width * 0.04f, top), Size(width * 0.04f + width * 0.001f, height))
}

private fun DrawScope.burger(left: Float, top: Float, width: Float, height: Float, bg: Color) {
    val layer = height / 5f
    val bun = Path().apply {
        moveTo(left, top + layer * 1.3f)
        cubicTo(left, top - layer * 0.6f, left + width, top - layer * 0.6f, left + width, top + layer * 1.3f)
        close()
    }
    drawPath(bun, Cream)
    val lettuce = Path().apply {
        val y = top + layer * 1.6f
        moveTo(left, y)
        val steps = 6
        for (i in 0 until steps) {
            val x1 = left + width * (i + 0.5f) / steps
            val x2 = left + width * (i + 1f) / steps
            quadraticTo(x1, y + layer * 0.6f * (if (i % 2 == 0) 1f else -0.2f), x2, y)
        }
        lineTo(left + width, y + layer * 0.5f)
        lineTo(left, y + layer * 0.5f)
        close()
    }
    drawPath(lettuce, Cream)
    drawRoundRect(Cream, Offset(left, top + layer * 2.4f), Size(width, layer * 1.2f), CornerRadius(layer * 0.5f))
    drawRoundRect(Cream, Offset(left, top + layer * 3.8f), Size(width, layer * 1.3f), CornerRadius(layer * 0.6f))
    drawRect(bg, Offset(left, top + layer * 3.62f), Size(width, layer * 0.14f))
}

private fun DrawScope.cloche(cx: Float, baseY: Float, width: Float, bg: Color) {
    val domeH = width * 0.55f
    val left = cx - width / 2
    val dome = Path().apply {
        moveTo(left, baseY - width * 0.1f)
        cubicTo(left, baseY - width * 0.1f - domeH * 1.3f, left + width, baseY - width * 0.1f - domeH * 1.3f, left + width, baseY - width * 0.1f)
        close()
    }
    drawPath(dome, Cream)
    drawCircle(Cream, width * 0.045f, Offset(cx, baseY - width * 0.1f - domeH * 0.98f))
    drawRoundRect(Cream, Offset(left - width * 0.06f, baseY - width * 0.06f), Size(width * 1.12f, width * 0.1f), CornerRadius(width * 0.05f))
    drawRect(bg, Offset(left, baseY - width * 0.1f), Size(width, width * 0.035f))
}

private fun DrawScope.steam(cx: Float, y: Float, amp: Float) {
    val stroke = Stroke(width = amp * 0.45f, cap = StrokeCap.Round)
    for (i in -1..1) {
        val x = cx + i * amp * 2.2f
        val p = Path().apply {
            moveTo(x, y)
            cubicTo(x + amp, y - amp * 1.2f, x - amp, y - amp * 2.4f, x, y - amp * 3.6f)
        }
        drawPath(p, Cream, style = stroke)
    }
}

private fun DrawScope.plate(cx: Float, cy: Float, radius: Float, bg: Color, ink: Color = Cream) {
    drawCircle(ink, radius, Offset(cx, cy))
    drawCircle(if (ink == bg) Cream else bg, radius * 0.78f, Offset(cx, cy))
    drawCircle(ink, radius * 0.6f, Offset(cx, cy))
    drawCircle(if (ink == bg) Cream else bg, radius * 0.46f, Offset(cx - radius * 0.05f, cy - radius * 0.05f))
    val a = 25.0 * PI / 180
    drawCircle(ink, radius * 0.07f, Offset(cx + (radius * 0.9f * cos(a)).toFloat(), cy - (radius * 0.9f * sin(a)).toFloat()))
}

// Previews ---------------------------------------------------------------------------------

private val PreviewMenus = listOf(
    MensaMenuIdentity(MensaMenuKind.BAR, "Bar U6", "U6"),
    MensaMenuIdentity(MensaMenuKind.BAR, "Bar U12", "U12"),
    MensaMenuIdentity(MensaMenuKind.GLUTEN_FREE, "Senza glutine", null),
    MensaMenuIdentity(MensaMenuKind.BAR, "Bar U3", "U3"),
    MensaMenuIdentity(MensaMenuKind.CANTEEN, "Menù Bicocca", null),
)

/** Every style of every menu, one row per style, so the one to ship can be picked by eye. */
@Preview(name = "Menu card styles", widthDp = 1000, heightDp = 1500)
@Composable
private fun MenuCardStylesPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (style in 0 until MENSA_MENU_ART_STYLES) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PreviewMenus.forEach { MensaMenuCard(it, Modifier.width(180.dp), style = style) }
                }
            }
        }
    }
}

@Preview(name = "Menu card", widthDp = 220, heightDp = 360)
@Composable
private fun MenuCardPreview() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) { MensaMenuCard(PreviewMenus[4], Modifier.width(180.dp)) }
    }
}
