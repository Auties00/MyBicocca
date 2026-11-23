package it.unimib.indoornav.lab.ui.components

import it.unimib.indoornav.lab.ui.theme.SignalBadgeStyle
import it.unimib.indoornav.lab.inventory.SlotState
import it.unimib.indoornav.lab.inventory.BeaconSlot
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.ui.theme.LabTheme
import it.unimib.indoornav.lab.ui.theme.SignalBadgeTokens

@Composable
fun SignalTypeBadge(
    frameType: BeaconFrameType,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val style = SignalBadgeTokens.forFrameType(frameType)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(style.containerColor)
            .padding(
                horizontal = if (compact) 6.dp else 7.dp,
                vertical = if (compact) 1.dp else 2.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(style.dotColor),
        )
        Text(
            text = if (compact) style.shortLabel else style.fullLabel,
            color = style.contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = TightLineStyle,
        )
    }
}

private val UrlStyle = SignalBadgeStyle(
    fullLabel = "URL",
    shortLabel = "URL",
    containerColor = Color(0xFF0B3B40),
    contentColor = Color(0xFF7BE3EC),
    dotColor = Color(0xFF22D3EE),
)

private fun styleOf(slot: BeaconSlot): SignalBadgeStyle = when (slot) {
    BeaconSlot.UID -> SignalBadgeTokens.forFrameType(BeaconFrameType.ED_UID)
    BeaconSlot.TLM -> SignalBadgeTokens.forFrameType(BeaconFrameType.ED_TLM)
    BeaconSlot.IBEACON -> SignalBadgeTokens.forFrameType(BeaconFrameType.IBEACON)
    BeaconSlot.URL -> UrlStyle
}

/**
 * One advertising slot as a pill: in its colour when set and heard (or just heard, for a beacon with
 * nothing set), grey when set but not heard, and pulsing red when heard but not set.
 */
@Composable
fun SlotBadge(slot: BeaconSlot, state: SlotState, compact: Boolean = false, modifier: Modifier = Modifier) {
    val style = styleOf(slot)
    val reduceMotion = rememberReduceMotion()
    val pulse = if (state == SlotState.UNEXPECTED && !reduceMotion) {
        rememberInfiniteTransition(label = "slotPulse").animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
            label = "slotPulseAlpha",
        ).value
    } else {
        1f
    }
    val (container, content, dot) = when (state) {
        SlotState.OK, SlotState.HEARD -> Triple(style.containerColor, style.contentColor, style.dotColor)
        SlotState.MISSING -> Triple(Color(0xFF2A2526), Color(0xFF8E8486), Color(0xFF5E5557))
        SlotState.UNEXPECTED -> Triple(Color(0xFFB3141E).copy(alpha = pulse), Color.White, Color.White)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(container)
            .padding(horizontal = if (compact) 6.dp else 7.dp, vertical = if (compact) 1.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dot),
        )
        Text(
            text = if (compact) style.shortLabel else style.fullLabel,
            color = content,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = TightLineStyle,
        )
    }
}

// Without this the text keeps its font padding and the pill ends up much taller than the label.
private val TightLineStyle = TextStyle(
    lineHeight = 13.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
)

@Preview(showBackground = true)
@Composable
private fun SignalTypeBadgePreview() {
    LabTheme {
        Surface {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BeaconFrameType.entries.forEach {
                        SignalTypeBadge(frameType = it, compact = false)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BeaconFrameType.entries.forEach {
                        SignalTypeBadge(frameType = it, compact = true)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SlotBadge(BeaconSlot.UID, SlotState.OK)
                    SlotBadge(BeaconSlot.TLM, SlotState.MISSING)
                    SlotBadge(BeaconSlot.IBEACON, SlotState.UNEXPECTED)
                    SlotBadge(BeaconSlot.URL, SlotState.MISSING)
                }
            }
        }
    }
}
