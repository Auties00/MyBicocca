package it.attendance100.mybicocca.ui.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// One fact wrapped in its own card: an icon chip leads, label over value. The shared section
// style across every booking/result detail sheet (esiti, appelli, segreterie, biblioteca) so
// they all read as the same object.
@Composable
fun DetailFactCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = scheme.surfaceContainerHighest,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface,
                    fontFamily = if (mono) FontFamily.Monospace else null,
                    fontWeight = if (mono) FontWeight.SemiBold else null,
                )
            }
        }
    }
}
