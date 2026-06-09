package it.attendance100.mybicocca.ui.screen.map.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.acos

// Renders the campus building pin to a Bitmap for use as a MapLibre symbol icon. MapLibre has no
// Composable-marker equivalent (unlike google-maps-compose's MarkerComposable), so the pin — a
// circular head whose sides run tangentially into a bottom tip, matching the old PinShape — is
// rasterized here with Canvas. Colors come from the active theme (ARGB ints). The tip sits at
// bottom-center to match the symbol's iconAnchor("bottom"); idle vs selected differ in size/colour.
object BuildingPin {

    private const val HEAD_IDLE_DP = 36f
    private const val HEAD_SELECTED_DP = 46f
    private const val TAIL_IDLE_DP = 9f
    private const val TAIL_SELECTED_DP = 12f
    private const val BORDER_DP = 2f
    private const val SHADOW_DP = 3f

    // Distance in px from the symbol's bottom anchor (the geographic point, where the tip sits) up
    // to the SELECTED head's center. Centering this — not the anchor — puts the visible head in the
    // middle of the map viewport. Geometry mirrors render(): height − headCenterFromTop =
    // (head + tail + 2·pad) − (pad + head/2) = head/2 + tail + pad, with pad = shadow + border.
    fun selectedHeadCenterOffset(density: Float): Float =
        (HEAD_SELECTED_DP / 2f + TAIL_SELECTED_DP + SHADOW_DP + BORDER_DP) * density

    fun render(
        density: Float,
        code: String?,
        containerColor: Int,
        borderColor: Int,
        textColor: Int,
        selected: Boolean,
    ): Bitmap {
        val headDp = if (selected) HEAD_SELECTED_DP else HEAD_IDLE_DP
        val tailDp = if (selected) TAIL_SELECTED_DP else TAIL_IDLE_DP
        val borderDp = BORDER_DP
        val shadowDp = SHADOW_DP

        val head = headDp * density
        val tail = tailDp * density
        val pad = (shadowDp + borderDp) * density
        val width = (head + pad * 2f)
        val height = (head + tail + pad * 2f)

        val bmp = Bitmap.createBitmap(width.toInt().coerceAtLeast(1), height.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val radius = head / 2f
        val cx = width / 2f
        val cy = pad + radius
        val tipY = pad + head + tail
        val tangent = Math.toDegrees(acos((radius / (tipY - cy)).toDouble())).toFloat()

        val path = Path().apply {
            arcTo(RectF(cx - radius, cy - radius, cx + radius, cy + radius), 90f + tangent, 360f - 2f * tangent)
            lineTo(cx, tipY)
            close()
        }

        // Canvas-on-Bitmap is software-rendered, so setShadowLayer draws the drop shadow directly.
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = containerColor
            setShadowLayer(shadowDp * density, 0f, 1.5f * density, 0x55000000)
        }
        canvas.drawPath(path, fill)
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = borderDp * density
            color = borderColor
        })

        if (code != null) {
            val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = (if (selected) 15f else 13f) * density
            }
            val fm = tp.fontMetrics
            canvas.drawText(code, cx, cy - (fm.ascent + fm.descent) / 2f, tp)
        } else {
            // Icon-less buildings: a simple filled square stands in for the apartment glyph.
            val s = radius * 0.55f
            canvas.drawRect(cx - s, cy - s, cx + s, cy + s, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = textColor })
        }
        return bmp
    }
}
