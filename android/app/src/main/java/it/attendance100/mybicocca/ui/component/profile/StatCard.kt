package it.attendance100.mybicocca.ui.component.profile

import android.content.res.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.valentinilk.shimmer.*
import it.attendance100.mybicocca.ui.component.card.DitheredTexture
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.util.*
import kotlinx.coroutines.*


@OptIn(ExperimentalTextApi::class)
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
) {
	val haptic = rememberHapticManager()
	val scope = rememberCoroutineScope()
	val scale = remember { Animatable(1f) }
	val rotation = remember { Animatable(0f) }
	val rotationDef = 0f
	val rotationDelta = -4f

	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
		),
		onClick = {
			haptic.tap()
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
		Box {
			Box(
				modifier = Modifier
				  .align(Alignment.CenterStart),
			) {
				DitheredTexture(
					modifier = Modifier
					  .fillMaxSize()
					  .offset(x = (-60).dp, y = (80).dp)
					  .rotate(13f),
					color = MaterialTheme.colorScheme.outlineVariant,
					spacing = 10f,
					dotSize = 5f,
					globalRotation = 35f,
					dotRotation = 80f,
					fadeStart = 0f,
					fadeEnd = 1f,
					alpha = 0.5f,
				)
			}

			Column(
				modifier = Modifier
				  .padding(horizontal = 18.dp)
				  .padding(top = 12.dp, bottom = 10.dp)
				  .fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(4.dp),
			) {
				Text(
					text = title,
					color = secondaryColor,
					fontSize = 12.sp,
					maxLines = 2,
				)
				Box(
					contentAlignment = Alignment.BottomStart,
				) {
					if (isLoading) {
						Row(
							modifier = Modifier
							  .fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
						) {
							Box(
								modifier = Modifier
								  .width(60.dp)
								  .height(24.dp)
								  .clip(RoundedCornerShape(8.dp))
								  .shimmer()
								  .background(textColor.copy(alpha = 0.2f)),
							)
							Box(
								modifier = Modifier
								  .width(24.dp)
								  .height(24.dp)
								  .clip(RoundedCornerShape(8.dp))
								  .shimmer()
								  .background(textColor.copy(alpha = 0.2f)),
							)
						}
					} else {
						Text(
							text = value,
							color = textColor,
							fontSize = 24.sp,
							fontWeight = FontWeight.Bold,
						)
					}

					if (icon != null) Box(
						modifier.fillMaxWidth(),
						contentAlignment = Alignment.BottomEnd,
					) {
						Card(
							modifier = Modifier
							  .height(34.dp)
							  .width(34.dp)
							  .offset(y = (-4).dp),
							colors = CardDefaults.cardColors(
								containerColor = Color.Transparent,
							),
							onClick = {
								haptic.spring()
								if (iconOnClick != null) iconOnClick()
							},
							shape = RoundedCornerShape(16),
						) {
							Box(
								modifier = Modifier
								  .fillMaxSize(),
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


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true, device = "spec:width=1080px,height=2340px,dpi=240")
@Composable
fun StatCardDarkPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				StatCard(
					title = "Media aritmetica",
					value = "8.5",
					textColor = textColor,
					secondaryColor = GrayColor(),
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true, device = "spec:width=1080px,height=2340px,dpi=240")
@Composable
fun StatCardLightPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				StatCard(
					title = "Media aritmetica",
					value = "8.5",
					textColor = textColor,
					secondaryColor = GrayColor(),
				)
			}
		}
	}
}
