package it.attendance100.mybicocca.ui.screen.settings.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark

/**
 * Two-pane calendar tab mock for the tablet/foldable mini-screen. The left pane mirrors the
 * phone layout: day/week/month segmented control, weekday strip with one day highlighted (a
 * leading empty slot stands in for the hour gutter), a dotted-line timeline with a
 * tertiary-tinted event card, and a primary FAB. The right pane mocks the day's agenda: "Oggi"
 * and date bars over a stack of event rows, one expanded with extra text lines and a two-button
 * action pair.
 */
@Composable
fun CalendarPreviewTablet() {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
    ) {
        Row {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 2.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                            .background(scheme.surfaceContainerHigh, SegmentedControlShape)
                            .padding(2.dp),
                    ) {
                        Row(
                            Modifier
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
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(Modifier.weight(1f))

                        repeat(7) { i ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(17.dp)
                                    .background(
                                        if (i == 2) scheme.primary else scheme.surfaceContainerHigh,
                                        CircleShape,
                                    ),
                            )
                        }
                    }

                    Row {
                        Box(
                            Modifier
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
                                    .padding(top = 62.dp, start = 6.dp)
                                    .fillMaxHeight(0.67f)
                                    .background(
                                        scheme.tertiary.copy(alpha = 0.30f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, scheme.tertiary, RoundedCornerShape(4.dp)),
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.BottomEnd)
                        .size(width = 25.dp, height = 12.dp)
                        .background(scheme.primary, FabShape),
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .padding(start = 2.dp, top = 20.dp)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(.2f)
                            .height(2.dp)
                            .background(scheme.onSecondary, RoundedCornerShape(50)),
                    )

                    Box(
                        Modifier
                            .fillMaxWidth(.4f)
                            .height(4.dp)
                            .background(scheme.onSecondary, RoundedCornerShape(50)),
                    )

                    repeat(4) { i ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(if (i == 1) 46.dp else 17.dp)
                                .background(scheme.surfaceContainerHigh, RoundedCornerShape(5.dp))
                                .padding(4.dp),
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(9.dp)
                                            .background(scheme.primaryContainer, CircleShape)
                                    )

                                    if (i != 1) {
                                        Box(
                                            Modifier
                                                .padding(top = 3.dp)
                                                .fillMaxWidth(fakeBarWidth(i))
                                                .height(3.dp)
                                                .background(
                                                    scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    bar()
                                                )
                                        )
                                    } else {
                                        Column(
                                            Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            repeat(4) { j ->
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth(1 - ((j + 1) / 11f) - (j % 2) * 0.4f)
                                                        .height(3.dp)
                                                        .background(
                                                            scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                            bar()
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                if (i == 1) {
                                    Spacer(Modifier.height(5.dp))

                                    Row(
                                        Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .fillMaxWidth()
                                    ) {
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .background(
                                                    scheme.surfaceContainerHighest,
                                                    RoundedCornerShape(3.dp)
                                                )
                                        )
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .background(
                                                    scheme.primaryContainer,
                                                    RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Two-pane e-learning tab mock for the tablet/foldable mini-screen. The left pane mirrors the
 * phone layout: filter chips, accented course cards and a primary FAB. The right pane mocks a
 * course detail page — decorative Material shapes bleeding in from the edges, a favourite-star
 * dot, a header block (year bar, two-tone course title, professor line), a video-player card
 * with play glyph, title/description bars and a "Guarda" button pair, a tab strip, and an
 * "Informazioni" section with a divider and two stat cards. The right pane keeps no bottom
 * padding so its content reads as scrolling off-screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ElearningPreviewTablet() {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp)
    ) {
        Row {
            val accents = CourseAccents

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(end = 2.dp, bottom = 4.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        Modifier
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

                    for (accent in accents) {
                        Box {
                            Box(
                                Modifier
                                    .height(7.dp)
                                    .width(26.dp)
                                    .padding(start = 3.dp)
                                    .background(
                                        accent[1],
                                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(27.dp)
                                    .padding(top = 7.dp)
                                    .background(accent.first(), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
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
                                            .background(
                                                scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                bar()
                                            )
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

                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(width = 35.dp, height = 12.dp)
                        .background(scheme.primary, FabShape),
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(start = 4.dp)
            ) {
                val accent = accents[1]
                val accent2 = accents[2]
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = 15.dp)
                        .size(60.dp)
                        .clip(MaterialShapes.Sunny.toShape())
                        .background(accent2[0])
                )
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(y = 50.dp, x = (-5).dp)
                        .size(30.dp)
                        .clip(MaterialShapes.Pentagon.toShape())
                        .background(accent[0])
                )

                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(scheme.surfaceContainerHighest, CircleShape)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.2f)
                                .height(2.dp)
                                .background(accent2[1], bar())
                        )

                        Spacer(Modifier.height(1.dp))

                        Box(
                            Modifier
                                .fillMaxWidth(0.8f)
                                .height(5.dp)
                                .background(scheme.onSurface, bar())
                        )

                        Box(
                            Modifier
                                .fillMaxWidth(0.5f)
                                .height(5.dp)
                                .background(accent[1], bar())
                        )

                        Spacer(Modifier.height(2.dp))

                        Box(
                            Modifier
                                .fillMaxWidth(0.35f)
                                .height(2.dp)
                                .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(87.dp)
                            .background(scheme.surfaceContainerHigh, RoundedCornerShape(13.dp)),
                    ) {
                        Box(
                            Modifier
                                .padding(5.dp)
                                .fillMaxWidth()
                                .height(45.dp)
                                .background(
                                    scheme.surfaceContainerHighest,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .align(Alignment.TopStart)
                                    .offset(y = 30.dp)
                                    .size(30.dp)
                                    .clip(MaterialShapes.Sunny.toShape())
                                    .background(accent2[0])
                            )
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(y = (-8).dp, x = 8.dp)
                                    .rotate(180f)
                                    .size(40.dp)
                                    .clip(MaterialShapes.Pentagon.toShape())
                                    .background(accent[0])
                            )

                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = scheme.onBackground
                            )
                        }

                        Column(
                            Modifier.padding(start = 6.dp, bottom = 6.dp, end = 6.dp, top = 3.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(0.3f)
                                    .height(3.dp)
                                    .background(scheme.onSurface.copy(alpha = 0.8f), bar())
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth(0.45f)
                                    .height(2.dp)
                                    .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                            )
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .background(accent[1], RoundedCornerShape(50))
                            )
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .background(scheme.surfaceContainerHighest, CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(1.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(5) { i ->
                            Box(
                                Modifier
                                    .weight(if (i == 0) 1.2f else 1f)
                                    .height(8.dp)
                                    .background(
                                        if (i == 0) accent[1] else scheme.surfaceContainerHigh,
                                        RoundedCornerShape(50)
                                    )
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.45f)
                                .height(4.dp)
                                .background(scheme.onSurface.copy(alpha = 0.8f), bar())
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(scheme.surfaceContainerHigh)
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(2) { _ ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(
                                        scheme.surfaceContainerHigh,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(7.dp)
                                            .background(scheme.onSurface, CircleShape)
                                    )

                                    Box(
                                        Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(2.dp)
                                            .background(
                                                scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                bar()
                                            )
                                    )
                                }

                                Spacer(Modifier.height(1.dp))

                                Box(
                                    Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(2.dp)
                                        .background(
                                            scheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            bar()
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Map tab mock for the tablet/foldable mini-screen: rotated grey street strips and primary-dot
 * building pins across the full canvas, with the "Edifici" list sheet peeking up on the right
 * half only.
 */
@Composable
fun MapsPreviewTablet() {
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
                .offset(x = 15.dp)
                .padding(start = 12.dp)
                .fillMaxHeight()
                .width(5.dp)
                .rotate(-7f)
                .background(scheme.surfaceContainerHigh),
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 120.dp, y = -(10).dp)
                .padding(start = 12.dp)
                .requiredHeight(400.dp)
                .width(5.dp)
                .rotate(30f)
                .background(scheme.surfaceContainerHigh),
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 317.dp, y = -(45).dp)
                .padding(start = 12.dp)
                .requiredHeight(400.dp)
                .width(5.dp)
                .rotate(80f)
                .background(scheme.surfaceContainerHigh)
        )

        MapPin(Alignment.TopCenter, PaddingValues(top = 8.dp, end = 6.dp))
        MapPin(Alignment.BottomStart, PaddingValues(bottom = 27.dp, start = 55.dp))
        MapPin(Alignment.TopStart, PaddingValues(start = 60.dp, top = 54.dp))
        MapPin(Alignment.Center, PaddingValues(end = 26.dp))
        MapPin(Alignment.CenterEnd, PaddingValues(end = 54.dp, top = 0.dp))

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.44f)
                    .padding(horizontal = 4.dp)
                    .background(
                        scheme.surfaceContainer,
                        SheetTopShape
                    )
                    .padding(horizontal = 7.dp)
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

                repeat(5) { i ->
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
}

/**
 * Two-pane registry tab mock for the tablet/foldable mini-screen. The left pane mirrors the
 * phone layout: a "Scadenze" banner and service directory cards with icon chips. The right pane
 * mocks the study plan: a primary-container "Piano standard" card with an "Approvato" pill,
 * stats bars and Material-shape flourishes, an "Anno 1" header, course rows with CFU chips, and
 * an extended FAB.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RegistryPreviewTablet() {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(end = 2.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                Modifier
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
                    Modifier
                        .fillMaxWidth()
                        .background(scheme.surfaceContainerHigh, RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        Modifier
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


                    repeat(5) { j ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(11.dp)
                                    .background(
                                        if (i == 0) scheme.primary else scheme.tertiaryContainer,
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth((1 - ((j + 1) % 3) / 5f) - (j % 2) * 0.1f - 0.4f)
                                        .height(4.dp)
                                        .background(scheme.onSurface.copy(alpha = 0.7f), bar())
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(3.dp)
                                        .background(
                                            scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            bar()
                                        )
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

        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 2.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(7.dp))
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .offset(x = 10.dp, y = (-9).dp)
                        .size(52.dp)
                        .clip(MaterialShapes.Sunny.toShape())
                        .background(scheme.onTertiaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {}

                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(MaterialShapes.Flower.toShape())
                        .background(scheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(scheme.primary, RoundedCornerShape(2.dp))
                    )
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        Modifier
                            .width(34.dp)
                            .height(8.dp)
                            .background(Color(0xFF9DF0BC), RoundedCornerShape(50))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.55f)
                                .height(7.dp)
                                .background(scheme.onPrimaryContainer, bar())
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(0.4f)
                                .height(4.dp)
                                .background(scheme.onPrimaryContainer.copy(alpha = 0.8f), bar())
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(0.45f)
                                .height(3.dp)
                                .background(scheme.onPrimaryContainer.copy(alpha = 0.5f), bar())
                        )
                    }
                }
            }

            Spacer(Modifier.height(0.5.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.3f)
                        .height(5.dp)
                        .background(scheme.onSurface.copy(alpha = 0.8f), bar())
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.45f)
                        .height(3.dp)
                        .background(scheme.onSurfaceVariant.copy(alpha = 0.5f), bar())
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    repeat(5) { i ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(scheme.surfaceContainerHigh, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(scheme.primaryContainer, RoundedCornerShape(4.dp))
                            )
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.7f - (i % 3) * 0.1f)
                                        .height(4.dp)
                                        .background(scheme.onSurface.copy(alpha = 0.7f), bar())
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.35f)
                                        .height(3.dp)
                                        .background(
                                            scheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            bar()
                                        )
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .height(16.dp)
                        .background(scheme.primary, FabShape)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                    )
                    Box(
                        Modifier
                            .width(28.dp)
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = TabletPreviewW,
    heightDp = TabletPreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun CalendarPreviewPreview() {
    BicoccaTheme(dark = true) {
        CalendarPreviewTablet()
    }
}

@Preview(
    showBackground = true,
    widthDp = TabletPreviewW,
    heightDp = TabletPreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun ElearningPreviewPreview() {
    BicoccaTheme(dark = true) {
        ElearningPreviewTablet()
    }
}


@Preview(
    showBackground = true,
    widthDp = TabletPreviewW,
    heightDp = TabletPreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun MapsPreviewPreview() {
    BicoccaTheme(dark = true) {
        MapsPreviewTablet()
    }
}

@Preview(
    showBackground = true,
    widthDp = TabletPreviewW,
    heightDp = TabletPreviewH,
    backgroundColor = PreviewBgDark
)
@Composable
private fun RegistryPreviewPreview() {
    BicoccaTheme(dark = true) {
        RegistryPreviewTablet()
    }
}