package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.NetworkStatusBar
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonProfileContent
import it.attendance100.mybicocca.ui.component.DashboardTile
import it.attendance100.mybicocca.ui.component.profile.CreditCard
import it.attendance100.mybicocca.ui.component.profile.ProgressStatCard
import it.attendance100.mybicocca.ui.component.profile.StatCard
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import it.attendance100.mybicocca.util.rememberPreferencesManager
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
	viewModel: ProfileViewModel = hiltViewModel(
		checkNotNull<ViewModelStoreOwner>(
			LocalViewModelStoreOwner.current
		) {
			"No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
		}, null
	),
) {
	val user by viewModel.user.collectAsStateWithLifecycle()
	val career by viewModel.activeCareer.collectAsStateWithLifecycle()
	val stats by viewModel.stats.collectAsStateWithLifecycle()
	val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
	val error by viewModel.error.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

	val primaryColor = MaterialTheme.colorScheme.primary
	val textColor = MaterialTheme.colorScheme.onBackground
	val grayColor = GrayColor()

	var showDialog by remember { mutableStateOf(false) }
	var showWeightedDialog by remember { mutableStateOf(false) }

	val arithmeticAverage = stats?.arithmeticAverage ?: 0f
	val weightedAverage = stats?.weightedAverage ?: 0f
	val passedExamCount = stats?.passedExamCount ?: 0
	val passedCredits = stats?.passedCredits?.toInt() ?: 0
	val totalCredits = stats?.totalCredits?.toInt() ?: 0

	val preferencesManager = rememberPreferencesManager()
	val progressBarToggle = preferencesManager.progressBarToggle

	val userDataSection: @Composable () -> Unit = @Composable {
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text(
				text = stringResource(R.string.profile_dati_personali),
				color = primaryColor,
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
			)

			CreditCard(
				accentColor = primaryColor,
				isChromatic = true,
				frontContent = { x, y, whiteBadge, _ ->
					BadgeFront(
						user,
						career = career,
						textColor = if (whiteBadge) BadgeWhiteDrawableColor else OnBackgroundColor,
						touchX = x,
						touchY = y,
						whiteBadge = whiteBadge,
					)
				},
				backContent = { x, y, whiteBadge, hazeState ->
					BadgeBack(
						user,
						career = career,
						textColor = if (whiteBadge) BadgeWhiteDrawableColor else OnBackgroundColor,
						touchX = x,
						touchY = y,
						whiteBadge = whiteBadge,
						hazeState = hazeState,
					)
				},
			)
		}
	}

	val statisticsSection: @Composable () -> Unit = @Composable {
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text(
				text = stringResource(R.string.profile_statistiche),
				color = primaryColor,
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
			)

			// Averages
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				StatCard(
					modifier = Modifier.weight(1f),
					title = stringResource(R.string.profile_media_aritmetica),
					value = String.format(Locale.getDefault(), "%.2f", arithmeticAverage),
					textColor = textColor,
					isLoading = stats == null,
					icon = { modifier ->
						Icon(
							Icons.Filled.Calculate,
							contentDescription = stringResource(R.string.profile_calcola_media),
							tint = primaryColor,
							modifier = modifier,
						)
					},
					iconOnClick = {
						showDialog = true
					},
				)

				StatCard(
					modifier = Modifier.weight(1f),
					title = stringResource(R.string.profile_media_ponderata),
					value = String.format(Locale.getDefault(), "%.2f", weightedAverage),
					textColor = textColor,
					isLoading = stats == null,
					icon = { modifier ->
						Icon(
							Icons.Filled.Calculate,
							contentDescription = stringResource(R.string.profile_calcola_media),
							tint = primaryColor,
							modifier = modifier,
						)
					},
					iconOnClick = {
						showWeightedDialog = true
					},
				)
			}

			// Progress
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				ProgressStatCard(
					modifier = Modifier.weight(1f),
					title = stringResource(R.string.profile_libretto_esami_sostenuti),
					current = passedExamCount,
					total = passedExamCount, // TODO: derive total exam count from study plan
					textColor = textColor,
					backgroundProgressBar = progressBarToggle,
					progressbar = !progressBarToggle,
					isLoading = stats == null,
				)

				ProgressStatCard(
					modifier = Modifier.weight(1f),
					title = stringResource(R.string.profile_cfu_acquisiti),
					current = passedCredits,
					total = totalCredits,
					primaryColor = primaryColor,
					textColor = textColor,
					backgroundProgressBar = progressBarToggle,
					progressbar = !progressBarToggle,
					isLoading = stats == null,
				)
			}
		}
	}

	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = { viewModel.refresh() },
		indicator = {},
		modifier = Modifier.fillMaxSize(),
	) {
		when {
			isRefreshing -> {
				Column {
					NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
					SkeletonProfileContent()
				}
			}

			else -> {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(16.dp),
				) {
					item {
						NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
					}

					item {
						userDataSection()
						Spacer(modifier = Modifier.height(16.dp))
						statisticsSection()
					}

					item {
						Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(12.dp),
							) {
								DashboardTile(
									title = stringResource(R.string.profile_esami),
									icon = Icons.AutoMirrored.Filled.LibraryBooks,
									isWide = true,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO: navigate to transcript */ },
								)

								DashboardTile(
									title = stringResource(R.string.profile_piano_studi),
									icon = Icons.Filled.Book,
									isWide = false,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO: navigate to study plan */ },
								)
							}

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(12.dp),
							) {
								DashboardTile(
									title = stringResource(R.string.profile_corsi),
									icon = Icons.Filled.School,
									isWide = false,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO: navigate to elearning tab */ },
								)

								DashboardTile(
									title = stringResource(R.string.profile_prenotazioni),
									icon = Icons.Filled.CalendarMonth,
									isWide = true,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO: navigate to booking */ },
								)
							}

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(12.dp),
							) {
								DashboardTile(
									title = "Mails",
									icon = Icons.Filled.Mail,
									isWide = false,
									height = 80.dp,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO */ },
								)

								DashboardTile(
									title = "Map",
									icon = Icons.Filled.Map,
									isWide = false,
									height = 80.dp,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO: navigate to map tab */ },
								)

								DashboardTile(
									title = "Mensa",
									icon = Icons.Filled.Restaurant,
									isWide = false,
									height = 80.dp,
									primaryColor = primaryColor,
									textColor = textColor,
									onClick = { /* TODO */ },
								)
							}
						}

						Spacer(modifier = Modifier.height(16.dp))
					}
				}
			}
		}
	}

	if (showDialog || showWeightedDialog) {
		ModalBottomSheet(
			onDismissRequest = {
				showDialog = false
				showWeightedDialog = false
			},
			sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
			containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
		) {
			HypotheticalGradeSheet(
				isWeighted = showWeightedDialog,
				onDismiss = {
					showDialog = false
					showWeightedDialog = false
				},
				currentArithmeticAverage = arithmeticAverage,
				currentWeightedAverage = weightedAverage,
				currentPassedExamCount = passedExamCount,
				currentPassedCredits = passedCredits,
				primaryColor = primaryColor,
				textColor = MaterialTheme.colorScheme.onBackground,
				grayColor = grayColor,
			)
		}
	}
}


@Composable
fun HypotheticalGradeSheet(
	onDismiss: () -> Unit,
	currentArithmeticAverage: Float,
	currentWeightedAverage: Float,
	currentPassedExamCount: Int,
	currentPassedCredits: Int,
	primaryColor: Color,
	textColor: Color,
	grayColor: Color,
	isWeighted: Boolean,
) {
	var voto by remember { mutableStateOf("") }
	var cfu by remember { mutableStateOf("") }

	val votoValue = voto.toFloatOrNull()
	val cfuValue = cfu.toIntOrNull()
	val MIN_PASSING_GRADE = 18f

	val newArithmeticAverage = if (votoValue != null && votoValue >= MIN_PASSING_GRADE) {
		val currentSum = currentArithmeticAverage * currentPassedExamCount
		(currentSum + votoValue) / (currentPassedExamCount + 1)
	} else null

	val newWeightedAverage = if (votoValue != null && votoValue >= MIN_PASSING_GRADE && cfuValue != null && cfuValue > 0) {
		val currentWeightedSum = currentWeightedAverage * currentPassedCredits
		(currentWeightedSum + votoValue * cfuValue) / (currentPassedCredits + cfuValue)
	} else null

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
		verticalArrangement = Arrangement.spacedBy(0.dp),
	) {
		Row {
			Icon(
				Icons.Filled.Calculate,
				contentDescription = null,
				tint = primaryColor,
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = stringResource(R.string.profile_dialog_title),
				color = textColor,
				fontSize = 22.sp,
				fontWeight = FontWeight.SemiBold,
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			if (!isWeighted) Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				HypotheticalStatCard(
					title = stringResource(R.string.profile_media_aritmetica),
					currentValue = currentArithmeticAverage,
					newValue = newArithmeticAverage,
					textColor = textColor,
					grayColor = grayColor,
					primaryColor = primaryColor,
				)
				val diffArithmetic = newArithmeticAverage?.let { it - currentArithmeticAverage }
				DifferenceIndicator(difference = diffArithmetic)
			}
			else Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
				HypotheticalStatCard(
					title = stringResource(R.string.profile_media_ponderata),
					currentValue = currentWeightedAverage,
					newValue = newWeightedAverage,
					textColor = textColor,
					grayColor = grayColor,
					primaryColor = primaryColor,
				)
				val diffWeighted = newWeightedAverage?.let { it - currentWeightedAverage }
				DifferenceIndicator(difference = diffWeighted)
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			OutlinedTextField(
				value = voto,
				onValueChange = { newValue ->
					if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
						voto = newValue
					}
				},
				label = { Text(stringResource(R.string.profile_dialog_voto)) },
				placeholder = { Text(stringResource(R.string.profile_dialog_voto_placeholder), color = grayColor) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				singleLine = true,
				isError = votoValue != null && votoValue < MIN_PASSING_GRADE,
				supportingText =
					if (votoValue != null && votoValue < MIN_PASSING_GRADE) {
						{ Text(stringResource(R.string.profile_dialog_voto_error)) }
					} else if (votoValue != null && votoValue >= 32) {
						if (votoValue == 67f || votoValue == 69f || votoValue == 420f) {
							{ Text(stringResource(R.string.profile_dialog_voto_warning_meme)) }
						} else {
							{ Text(stringResource(R.string.profile_dialog_voto_warning)) }
						}
					} else null,
				modifier = Modifier.weight(1f),
				shape = RoundedCornerShape(12.dp),
			)

			if (isWeighted) OutlinedTextField(
				value = cfu,
				onValueChange = { newValue ->
					if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
						if (newValue.isEmpty() || newValue.toInt() !in 1..<20) {
							cfu = ""
						} else {
							cfu = newValue
						}
					}
				},
				isError = cfuValue != null && (cfuValue <= 0),
				supportingText = if (cfuValue != null && cfuValue <= 0) {
					{ Text(stringResource(R.string.profile_dialog_cfu_error)) }
				} else {
					null
				},
				label = { Text(stringResource(R.string.profile_dialog_cfu)) },
				placeholder = { Text(stringResource(R.string.profile_dialog_cfu_optional), color = grayColor) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				singleLine = true,
				modifier = Modifier.weight(1f),
				shape = RoundedCornerShape(12.dp),
			)
		}

		Spacer(modifier = Modifier.height(16.dp))
	}
}


@Composable
fun HypotheticalStatCard(
	modifier: Modifier = Modifier,
	title: String,
	currentValue: Float,
	newValue: Float?,
	textColor: Color,
	grayColor: Color,
	primaryColor: Color,
) {
	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
		),
		shape = RoundedCornerShape(12.dp),
	) {
		Column(
			modifier = Modifier
				.padding(12.dp)
				.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Text(
				text = title,
				color = grayColor,
				fontSize = 11.sp,
				maxLines = 1,
			)
			Row(
				verticalAlignment = Alignment.Bottom,
				horizontalArrangement = Arrangement.spacedBy(6.dp),
			) {
				Text(
					text = String.format(Locale.getDefault(), "%.2f", currentValue),
					color = textColor,
					fontSize = 18.sp,
					fontWeight = FontWeight.Bold,
				)
				AnimatedVisibility(
					visible = newValue != null,
					enter = fadeIn() + expandHorizontally(),
					exit = fadeOut() + shrinkHorizontally(),
				) {
					Row(
						verticalAlignment = Alignment.Bottom,
						horizontalArrangement = Arrangement.spacedBy(4.dp),
					) {
						Text(
							text = stringResource(R.string.profile_dialog_arrow_symbol),
							color = grayColor,
							fontSize = 14.sp,
						)
						Text(
							text = newValue?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "",
							color = primaryColor,
							fontSize = 18.sp,
							fontWeight = FontWeight.Bold,
						)
					}
				}
			}
		}
	}
}


@Composable
fun DifferenceIndicator(
	modifier: Modifier = Modifier,
	difference: Float?,
) {
	val isPositive = difference != null && difference >= 0

	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(20.dp),
		contentAlignment = Alignment.Center,
	) {
		AnimatedVisibility(
			visible = difference != null,
			enter = fadeIn(),
			exit = fadeOut(),
		) {
			val chipColor = if (isPositive) {
				Color(0xFF4CAF50).copy(alpha = 0.15f)
			} else {
				Color(0xFFF44336).copy(alpha = 0.15f)
			}
			val chipTextColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)

			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(6.dp))
					.background(chipColor)
					.padding(horizontal = 8.dp, vertical = 2.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					modifier = Modifier.offset(y = (-2.85).dp),
					text = difference?.let {
						String.format(
							Locale.getDefault(),
							"%s%.2f",
							if (it >= 0) "+" else "",
							it,
						)
					} ?: "",
					color = chipTextColor,
					fontSize = 11.sp,
					fontWeight = FontWeight.Medium,
				)
			}
		}
	}
}
