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
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.valentinilk.shimmer.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.card.DitheredTexture
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.util.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt


private fun toPercentage(value: Float): String =
	"${(value * 100).roundToInt()}%"

private fun Modifier.blendMode(blendMode: BlendMode, alpha: Float): Modifier =
	graphicsLayer {
		this.alpha = alpha
		compositingStrategy = CompositingStrategy.Offscreen
	}


@OptIn(ExperimentalTextApi::class)
@Composable
fun ProgressStatCard(
	modifier: Modifier = Modifier,
	title: String,
	current: Int,
	total: Int,
	primaryColor: Color? = null,
	textColor: Color,
	secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
	progressbar: Boolean = false,
	backgroundProgressBar: Boolean = false,
	isLoading: Boolean = false,
) {
	var primaryColor by remember { mutableStateOf(primaryColor) }
	if (primaryColor == null) {
		val preferencesManager = rememberPreferencesManager()
		primaryColor = if (preferencesManager.isDarkMode ?: isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
	}

	val targetProgress = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
	val progress by animateFloatAsState(
		targetValue = if (isLoading) 0f else targetProgress,
		animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
		label = "Progress Animation",
	)
	val haptic = rememberHapticManager()
	val scope = rememberCoroutineScope()
	val scale = remember { Animatable(1f) }
	val rotation = remember { Animatable(-22f) }
	val rotationDef = -22f
	val rotDelta = -4f

	val outerRadius = 16


	@Composable
	fun Dither(
		modifier: Modifier = Modifier,
		alpha: Float,
		offsetX: Dp = (-60).dp,
		offsetY: Dp = 70.dp,
		color: Color = MaterialTheme.colorScheme.outlineVariant,
	) {
		DitheredTexture(
			modifier = modifier
			  .fillMaxSize()
			  .offset(x = offsetX, y = offsetY)
			  .rotate(13f),
			color = color,
			spacing = 10f,
			dotSize = 5f,
			globalRotation = 35f,
			dotRotation = 80f,
			fadeStart = 0f,
			fadeEnd = 1f,
			alpha = alpha,
		)
	}

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
					rotation.animateTo(rotationDef - rotDelta, tween(100))
					rotation.animateTo(rotationDef + rotDelta, tween(100))
					rotation.animateTo(rotationDef, spring(dampingRatio = 0.4f, stiffness = 500f))
				}
			}
		},
		shape = RoundedCornerShape(outerRadius.dp),
	) {
		Box {
			Box(
				modifier = Modifier
				  .align(Alignment.CenterStart),
			) {
				Dither(alpha = 0.4f)
			}
			Box(modifier = Modifier.fillMaxSize()) {
				val padding = 6
				val innerRadius = outerRadius - padding
				if (backgroundProgressBar) {
					Box(
						modifier = Modifier
						  .matchParentSize()
						  .padding(padding.dp)
						  .clip(RoundedCornerShape(innerRadius.dp)),
					) {
						if (!isLoading) {
							Box(
								modifier = Modifier
								  .fillMaxHeight()
								  .fillMaxWidth(progress)
								  .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
								  .background(primaryColor!!),
							) {
								Dither(
									modifier = Modifier
									  .scale(scale.value)
									  .rotate(rotation.value),
									alpha = 0.8f,
									offsetY = (0).dp,
									color = MaterialTheme.colorScheme.inversePrimary,
								)
							}
						}
					}
				}

				Box(
					modifier = Modifier
					  .align(Alignment.CenterEnd),
				) {
					Text(
						text = toPercentage(progress),
						modifier = Modifier
						  .wrapContentWidth(unbounded = true)
						  .offset(x = (13).dp, y = (15 + (if (progressbar) -5 else -5)).dp)
						  .scale(scale.value)
						  .rotate(rotation.value)
						  .blendMode(BlendMode.Screen, 0.2f),
						color = Color.Gray,
						fontSize = 80.sp,
						softWrap = false,
						maxLines = 1,
						overflow = TextOverflow.Visible,
						fontFamily = FontFamily(
							Font(
								R.font.bebas_neue_regular,
								variationSettings = FontVariation.Settings(
									FontVariation.weight(900),
								),
							),
						),
					)
				}

				Column(
					modifier = Modifier
					  .padding(outerRadius.dp)
					  .padding(horizontal = 8.dp)
					  .fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					Text(
						text = title,
						color = secondaryColor,
						fontSize = 12.sp,
						maxLines = 2,
					)
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
							text = "$current/$total",
							color = textColor,
							fontSize = 24.sp,
							fontWeight = FontWeight.Bold,
						)
					}

					// Progress bar
					if (progressbar) Box(
						modifier = Modifier
						  .fillMaxWidth()
						  .height(8.dp)
						  .clip(RoundedCornerShape(4.dp))
						  .background(secondaryColor.copy(alpha = 0.2f)),
					) {
						if (!isLoading) {
							Box(
								modifier = Modifier
								  .fillMaxWidth(progress)
								  .fillMaxHeight()
								  .clip(RoundedCornerShape(4.dp))
								  .background(primaryColor!!),
							)
						}
					}
				}
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardDarkPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 8,
					total = 10,
					textColor = textColor,
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBarDarkPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 125.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 8,
					total = 10,
					textColor = textColor,
					progressbar = true,
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBGBarDarkPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 7,
					total = 10,
					textColor = textColor,
					backgroundProgressBar = true,
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardLightPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 8,
					total = 10,
					textColor = textColor,
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBarLightPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 125.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 8,
					total = 10,
					textColor = textColor,
					progressbar = true,
				)
			}
		}
	}
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBGBarLightPreview() {
	ProvideHapticManager {
		MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
			val textColor = MaterialTheme.colorScheme.onBackground
			Box(
				modifier = Modifier
				  .size(200.dp, 110.dp)
				  .padding(8.dp),
			) {
				ProgressStatCard(
					title = "Media aritmetica",
					current = 8,
					total = 10,
					textColor = textColor,
					backgroundProgressBar = true,
				)
			}
		}
	}
}
