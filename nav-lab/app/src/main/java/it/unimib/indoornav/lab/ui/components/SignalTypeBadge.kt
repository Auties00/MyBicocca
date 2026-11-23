package it.unimib.indoornav.lab.ui.components

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
import androidx.compose.ui.text.font.FontWeight
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
                vertical = if (compact) 2.dp else 3.dp,
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
        )
    }
}

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
            }
        }
    }
}
