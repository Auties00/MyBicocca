package it.unimib.indoornav.lab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.inventory.BatteryLevel
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlin.math.roundToInt

object BatteryColors {
    fun of(level: BatteryLevel): Color = when (level) {
        BatteryLevel.NEW -> Color(0xFF4ADE80)
        BatteryLevel.GREAT -> Color(0xFFA3E635)
        BatteryLevel.GOOD -> Color(0xFFFACC15)
        BatteryLevel.LOW -> Color(0xFFFB923C)
        BatteryLevel.VERY_LOW -> Color(0xFFF87171)
        BatteryLevel.DYING -> Color(0xFFEF4444)
    }

    fun icon(level: BatteryLevel): ImageVector = when (level) {
        BatteryLevel.NEW -> Icons.Default.BatteryFull
        BatteryLevel.GREAT -> Icons.Default.Battery6Bar
        BatteryLevel.GOOD -> Icons.Default.Battery5Bar
        BatteryLevel.LOW -> Icons.Default.Battery3Bar
        BatteryLevel.VERY_LOW -> Icons.Default.Battery2Bar
        BatteryLevel.DYING -> Icons.Default.Battery0Bar
    }
}

/** The battery's estimated level as a colour-coded pill, e.g. "Great 62%". */
@Composable
fun BatteryBadge(
    remainingFraction: Double,
    modifier: Modifier = Modifier,
) {
    val level = BatteryLevel.of(remainingFraction)
    val color = BatteryColors.of(level)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(start = 6.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(BatteryColors.icon(level), contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(
            text = "${level.label} ${(remainingFraction * 100).roundToInt()}%",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BatteryBadgePreview() {
    LabTheme {
        Surface {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.95, 0.62, 0.40, 0.25, 0.10, 0.02).forEach { BatteryBadge(it) }
            }
        }
    }
}
