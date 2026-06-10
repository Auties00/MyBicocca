package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Calendar tab mock for the phone mini-screen: a day/week/month segmented control, a weekday
 * strip with one day highlighted (a leading empty slot stands in for the hour gutter), and a
 * timeline — vertical dotted hour line, a tertiary-tinted event card, and a primary FAB in the
 * bottom-end corner.
 */
@Composable
fun CalendarPreviewPhone() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .background(scheme.surfaceContainerHigh, SegmentedControlShape)
                .padding(2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (i == 0) scheme.primary else scheme.surfaceContainerHigh,
                                SegmentedControlShape,
                            ),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(Modifier.weight(1f))

            repeat(7) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(12.dp)
                        .background(
                            if (i == 2) scheme.primary else scheme.surfaceContainerHigh,
                            CircleShape,
                        ),
                )
            }
        }

        Row {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .padding(start = 2.dp)
            ) {
                val pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f),
                    0f
                )
                Canvas(Modifier.fillMaxSize()) {
                    drawLine(
                        color = scheme.outlineVariant,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 3f,
                        pathEffect = pathEffect
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, start = 6.dp)
                        .fillMaxHeight(0.5f)
                        .background(scheme.tertiary.copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                        .border(1.dp, scheme.tertiary, RoundedCornerShape(4.dp)),
                )

                Box(
                    Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.BottomEnd)
                        .size(width = 22.dp, height = 12.dp)
                        .background(scheme.primary, FabShape),
                )
            }
        }
    }
}

/**
 * E-learning tab mock for the phone mini-screen: a strip of filter chips, then course cards —
 * each with a distinct year-badge accent, placeholder title bars and a favourite-star dot.
 */
@Composable
fun ElearningPreviewPhone() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 1.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (i == 1) scheme.primary else scheme.surfaceContainerHigh,
                            chipShape(i, 4),
                        ),
                )
            }
        }

        for (accent in CourseAccents) {
            Box {
                Box(
                    Modifier
                        .height(7.dp)
                        .width(26.dp)
                        .padding(start = 3.dp)
                        .background(accent[1], RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(27.dp)
                        .padding(top = 7.dp)
                        .background(accent.first(), RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.7f)
                                .height(4.dp)
                                .background(scheme.onSurface.copy(alpha = 0.7f), bar())
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(0.4f)
                                .height(3.dp)
                                .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                        )
                    }
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(scheme.tertiary, CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * Map tab mock for the phone mini-screen: streets as grey strips (one rotated), primary-dot
 * building pins scattered over the canvas, and the "Edifici" list sheet — drag handle plus
 * icon-and-bar rows — peeking up from the bottom edge.
 */
@Composable
fun MapsPreviewPhone() {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(scheme.background)
            .clip(RectangleShape)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .fillMaxHeight()
                .width(5.dp)
                .background(scheme.surfaceContainerHigh)
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 50.dp, y = -(10).dp)
                .padding(start = 12.dp)
                .fillMaxHeight()
                .width(5.dp)
                .rotate(30f)
                .background(scheme.surfaceContainerHigh)
        )

        MapPin(Alignment.TopCenter, PaddingValues(top = 8.dp, end = 6.dp))
        MapPin(Alignment.TopStart, PaddingValues(start = 30.dp, top = 24.dp))
        MapPin(Alignment.Center, PaddingValues(end = 26.dp))
        MapPin(Alignment.CenterEnd, PaddingValues(end = 14.dp, top = 6.dp))

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.34f)
                .background(
                    scheme.surfaceContainer,
                    SheetTopShape
                )
                .padding(horizontal = 9.dp)
                .padding(top = 7.dp)
                .background(
                    scheme.surfaceContainerHigh,
                    SheetTopShape
                )
                .padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-4).dp)
                    .width(16.dp)
                    .height(2.dp)
                    .background(scheme.outlineVariant, CircleShape),
            )

            repeat(3) { i ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .background(scheme.primaryContainer, CircleShape)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(fakeBarWidth(i))
                            .height(3.dp)
                            .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                    )
                }
            }
        }
    }
}

/**
 * Registry tab mock for the phone mini-screen: a "Scadenze" banner on primary-container, then
 * service directory cards — a header row followed by rows of primary icon chips, placeholder
 * text bars and a trailing chevron dot.
 */
@Composable
fun RegistryPreviewPhone() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(scheme.primaryContainer, RoundedCornerShape(6.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(
                        scheme.onPrimaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(3.dp)
                    )
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.4f)
                        .height(4.dp)
                        .background(scheme.onPrimaryContainer, bar())
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(3.dp)
                        .background(scheme.onPrimaryContainer.copy(alpha = 0.6f), bar())
                )
            }
            Box(
                Modifier
                    .size(8.dp)
                    .background(
                        scheme.onPrimaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(30.dp)
                    )
            )
        }

        repeat(2) { i ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((4 - (i * 2)) * 15.dp)
                    .background(scheme.surfaceContainerHigh, RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.3f)
                                .height(4.dp)
                                .background(scheme.onSurface.copy(alpha = 0.7f), bar())
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(0.5f)
                                .height(3.dp)
                                .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                        )
                    }
                }


                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            Modifier
                                .size(11.dp)
                                .background(scheme.primary, RoundedCornerShape(3.dp))
                        )
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(4.dp)
                                    .background(scheme.onSurface.copy(alpha = 0.7f), bar())
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(3.dp)
                                    .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                            )
                        }
                        Box(
                            Modifier
                                .size(4.dp)
                                .background(
                                    scheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun CalendarPreviewPreview() {
    BicoccaTheme(dark = true) {
        CalendarPreviewPhone()
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun ElearningPreviewPreview() {
    BicoccaTheme(dark = true) {
        ElearningPreviewPhone()
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun MapsPreviewPreview() {
    BicoccaTheme(dark = true) {
        MapsPreviewPhone()
    }
}

@Preview(
    showBackground = true,
    widthDp = PhonePreviewW,
    heightDp = PhonePreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun RegistryPreviewPreview() {
    BicoccaTheme(dark = true) {
        RegistryPreviewPhone()
    }
}