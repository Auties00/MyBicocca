package it.unimib.indoornav.lab.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.ui.theme.BeaconHaloTokens
import it.unimib.indoornav.lab.ui.theme.LabTheme

enum class BeaconPose {
    CARD,
    DETAIL,
}

@Composable
fun BeaconModelView(
    label: String,
    subLabel: String,
    pose: BeaconPose,
    online: Boolean,
    modifier: Modifier = Modifier,
) {
    val modelAlpha by animateFloatAsState(
        targetValue = if (online) BeaconHaloTokens.ONLINE_MODEL_ALPHA else BeaconHaloTokens.OFFLINE_MODEL_ALPHA,
        animationSpec = tween(durationMillis = 300),
        label = "beaconModelAlpha",
    )
    val isDetail = pose == BeaconPose.DETAIL
    val shellShape = RoundedCornerShape(if (isDetail) 18.dp else 10.dp)
    val shellBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F6), Color(0xFFECE6E0)),
    )

    Box(
        modifier = modifier
            .alpha(modelAlpha)
            .graphicsLayer {
                if (isDetail) {
                    rotationZ = -10f
                    rotationX = 14f
                }
            }
            .clip(shellShape)
            .background(shellBrush, shellShape)
            .border(
                width = if (isDetail) 1.5.dp else 1.dp,
                color = Color(0xFFD0C7BE),
                shape = shellShape,
            )
            .padding(
                horizontal = if (isDetail) 10.dp else 4.dp,
                vertical = if (isDetail) 10.dp else 5.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(if (isDetail) 10.dp else 5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A2F32))
                    .border(1.dp, Color(0xFFB8AEA4), CircleShape),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isDetail) 4.dp else 2.dp),
                modifier = Modifier.padding(vertical = if (isDetail) 10.dp else 2.dp),
            ) {
                Text(
                    text = label.ifBlank { "—" },
                    color = Color(0xFF171518),
                    fontSize = if (isDetail) 22.sp else 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                if (isDetail && subLabel.isNotBlank()) {
                    Text(
                        text = subLabel,
                        color = Color(0xD9171518),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(if (isDetail) 10.dp else 5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A2F32))
                    .border(1.dp, Color(0xFFB8AEA4), CircleShape),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BeaconModelViewPreview() {
    LabTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BeaconModelView(
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.CARD,
                    online = true,
                    modifier = Modifier.size(width = 44.dp, height = 88.dp),
                )
                BeaconModelView(
                    label = "B16",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.CARD,
                    online = false,
                    modifier = Modifier.size(width = 44.dp, height = 88.dp),
                )
                BeaconModelView(
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.DETAIL,
                    online = true,
                    modifier = Modifier.size(width = 88.dp, height = 160.dp),
                )
            }
        }
    }
}
