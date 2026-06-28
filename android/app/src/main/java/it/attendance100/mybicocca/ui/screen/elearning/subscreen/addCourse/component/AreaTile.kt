package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/**
 * How an area tile paints itself: a hashed palette accent, an official brand colour, or
 * full-bleed artwork.
 */
sealed interface AreaTileVisual {
    data class Default(val accent: Color) : AreaTileVisual
    data class CustomColor(val color: Color) : AreaTileVisual
    data class CustomImage(val drawableRes: Int) : AreaTileVisual
}

/**
 * Colour-coded entry tile for a top-level catalog area. The accent identity carries through the
 * rest of the browse flow; here it's the whole card, softened by a vertical sheen and a single
 * decorative orb so the grid reads as expressive rather than flat blocks. Brand-coloured areas
 * swap the orb for a white disc bearing the rotated university logo; artwork areas render the
 * image full-bleed under a darkening gradient that keeps the label legible. Long labels are
 * pre-broken onto two lines so titles wrap in balance, and taps give haptic feedback.
 */
@Composable
fun AreaTile(
    label: String,
    visual: AreaTileVisual,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val shape = RoundedCornerShape(24.dp)

    val surfaceColor = when (visual) {
        is AreaTileVisual.Default -> visual.accent
        is AreaTileVisual.CustomColor -> visual.color
        is AreaTileVisual.CustomImage -> Color.DarkGray
    }

    Surface(
        shape = shape,
        color = surfaceColor,
        interactionSource = remember { MutableInteractionSource() },
        onClick = { haptic.tap(); onClick() },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.65f)
    ) {
        Box(Modifier.fillMaxSize()) {
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                    Image(
                        painter = painterResource(id = visual.drawableRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                )
                            )
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

                val formattedText = remember(label) {
                    insertNewlineAtTwoFifths(label)
                }

                Text(
                    text = formattedText,
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
}

/**
 * Pre-breaks a long tile label onto two lines: past 20 characters, the space nearest a target
 * point early in the string (about two-sevenths of the way in) is replaced with a newline, so
 * the first line stays shorter and the title reads balanced inside the tile. Short or
 * space-free labels pass through unchanged.
 */
fun insertNewlineAtTwoFifths(input: String): String {
    if (input.length <= 20) return input

    val targetIndex = (input.length * 2) / 7

    val leftSpaceIndex = input.lastIndexOf(' ', targetIndex)
    val rightSpaceIndex = input.indexOf(' ', targetIndex)

    val bestSpaceIndex = when {
        leftSpaceIndex == -1 && rightSpaceIndex == -1 -> -1
        leftSpaceIndex == -1 -> rightSpaceIndex
        rightSpaceIndex == -1 -> leftSpaceIndex
        (targetIndex - leftSpaceIndex) <= (rightSpaceIndex - targetIndex) -> leftSpaceIndex
        else -> rightSpaceIndex
    }

    if (bestSpaceIndex == -1) return input

    return input.substring(0, bestSpaceIndex) + "\n" + input.substring(bestSpaceIndex + 1)
}

@Preview(
    showBackground = true,
    widthDp = 440,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
private fun AreaTilePreview() {
    BicoccaTheme(dark = isSystemInDarkTheme()) {
        data class AreaData(val label: String, val visual: AreaTileVisual)

        val areas = listOf(
            AreaData(
                label = "Area Economia e Statistica",
                visual = AreaTileVisual.CustomColor(Color(0xFFF3C513))
            ),
            AreaData(
                label = "Area Giuridica",
                visual = AreaTileVisual.CustomColor(Color(0xff1340f3))
            ),
            AreaData(
                label = "Area Medica, Chirurgica e dei Servizi Clinici",
                visual = AreaTileVisual.CustomColor(Color(0xfff34f13))
            ),
            AreaData(
                label = "Area Psicologica",
                visual = AreaTileVisual.CustomColor(Color(0xff76054e))
            ),
            AreaData(
                label = "Area di Scienze della Formazione",
                visual = AreaTileVisual.CustomColor(Color(0xffc91087))
            ),
            AreaData(
                label = "Area di Scienze",
                visual = AreaTileVisual.CustomColor(Color(0xff006629))
            ),
            AreaData(
                label = "Area di Sociologia",
                visual = AreaTileVisual.CustomColor(Color(0xffff9100))
            ),
            AreaData(
                label = "Bicocca Academy",
                visual = AreaTileVisual.CustomImage(R.drawable.elearning_bicoccaaccademy)
            ),
            AreaData(
                label = "Area Default",
                visual = AreaTileVisual.Default(MaterialTheme.colorScheme.primary)
            )
        )

        ProvideHapticManager(enabled = true) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val chunkedAreas = areas.chunked(2)

                items(chunkedAreas.size) { index ->
                    val rowItems = chunkedAreas[index]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { data ->
                            AreaTile(
                                label = data.label,
                                visual = data.visual,
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}