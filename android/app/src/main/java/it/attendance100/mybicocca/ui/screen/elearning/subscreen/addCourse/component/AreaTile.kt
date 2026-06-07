package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

sealed interface AreaTileVisual {
    data class Default(val accent: Color) : AreaTileVisual
    data class CustomColor(val color: Color) : AreaTileVisual
    data class CustomImage(val drawableRes: Int) : AreaTileVisual
}

// Colour-coded entry tile for a top-level catalog area. The accent identity carries through the
// rest of the browse flow; here it's the whole card, softened by a vertical sheen and a single
// decorative orb so the grid reads as expressive rather than flat blocks.
@Composable
fun AreaTile(
    label: String,
    visual: AreaTileVisual, // Changed to allow visual
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.65f)
            .clip(RoundedCornerShape(24.dp))
            .then(
                when (visual) {
                    is AreaTileVisual.Default -> Modifier.background(visual.accent)
                    is AreaTileVisual.CustomColor -> Modifier.background(visual.color)
                    is AreaTileVisual.CustomImage -> Modifier.background(Color.DarkGray)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { haptic.tap(); onClick() },
            ),
    ) {
        when (visual) {
            is AreaTileVisual.Default -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .offset(x = 44.dp, y = (-34).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.TopEnd)
                )
            }
            is AreaTileVisual.CustomColor -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 26.dp, y = (-26).dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo_simple),
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier
                            .size(64.dp)
                            .rotate(-45f)
                    )
                }
            }
            is AreaTileVisual.CustomImage -> {
                Image(
                    painter = painterResource(id = visual.drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
            }

            // For long names that actually begin with "Area", break "Area" onto its own line.
            // Match on the real prefix (case-insensitive) and strip exactly that prefix, preserving
            // its original casing, so we never duplicate or mangle the word.
            val areaPrefix = "Area"
            val splitArea = label.length > 30 && label.startsWith(areaPrefix, ignoreCase = true)
            val displayLabel = if (splitArea) {
                label.substring(0, areaPrefix.length) + "\n" + label.substring(areaPrefix.length).trimStart()
            } else {
                label
            }

            Text(
                text = displayLabel,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                letterSpacing = (-0.2).sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(y = (6).dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 220, name = "Light Mode")
@Preview(showBackground = true, widthDp = 220, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun AreaTilePreview() {
    BicoccaTheme(dark = isSystemInDarkTheme()) {
        ProvideHapticManager {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AreaTile(
                    label = "Area Economia e Statistica",
                    visual = AreaTileVisual.CustomColor(Color(0xFFF3C513)),
                    onClick = {},
                )
                AreaTile(
                    label = "Bicocca Academy",
                    visual = AreaTileVisual.CustomImage(R.drawable.elearning_bicoccaacademy),
                    onClick = {},
                )
                AreaTile(
                    label = "Area Default",
                    visual = AreaTileVisual.Default(MaterialTheme.colorScheme.primary),
                    onClick = {},
                )
            }
        }
    }
}
