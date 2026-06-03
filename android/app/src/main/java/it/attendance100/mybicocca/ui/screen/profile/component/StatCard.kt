package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    textColor: Color,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isLoading: Boolean = false,
    icon: (@Composable (Modifier) -> Unit)? = null,
    iconOnClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val rotationDef = 0f
    val rotationDelta = -4f

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = {
            onClick?.invoke()
            scope.launch {
                launch {
                    scale.animateTo(1.1f, tween(150))
                    scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
                }
                launch {
                    rotation.animateTo(rotationDef - rotationDelta, tween(100))
                    rotation.animateTo(rotationDef + rotationDelta, tween(100))
                    rotation.animateTo(rotationDef, spring(dampingRatio = 0.4f, stiffness = 500f))
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 16.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            // Title pinned to the top, value + button pushed to the bottom so they line up with
            // the progress bar in the sibling ProgressStatCard (which sits 16dp from the bottom).
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                color = secondaryColor,
                fontSize = 12.sp,
                maxLines = 2,
            )
            // Value and the icon button share one Row so the calculator sits vertically centered
            // on the grade rather than bottom-aligned beside it.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer()
                            .background(textColor.copy(alpha = 0.2f)),
                    )
                } else {
                    Text(
                        text = value,
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (icon != null) {
                    Spacer(Modifier.weight(1f))
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmer()
                                .background(textColor.copy(alpha = 0.2f)),
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .height(34.dp)
                                .width(34.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            onClick = {
                                iconOnClick?.invoke()
                            },
                            shape = RoundedCornerShape(16),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                icon(
                                    Modifier
                                        .size(34.dp)
                                        .scale(scale.value)
                                        .rotate(rotation.value),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
