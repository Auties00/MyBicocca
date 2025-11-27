package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.*
import com.patrykandpatrick.vico.core.cartesian.*
import com.patrykandpatrick.vico.core.cartesian.axis.*
import com.patrykandpatrick.vico.core.cartesian.data.*
import com.patrykandpatrick.vico.core.cartesian.layer.*
import com.patrykandpatrick.vico.core.common.*
import com.patrykandpatrick.vico.core.common.shape.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.uni_badge.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import kotlinx.coroutines.*
import java.util.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CareerScreen(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  viewModel: CareerViewModel = hiltViewModel(),
) {
  val pagerState = rememberPagerState(pageCount = { 4 })
  val coroutineScope = rememberCoroutineScope()
  val selectedTabIndex = pagerState.currentPage

  val primaryColor = MaterialTheme.colorScheme.primary
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  val user by viewModel.user.collectAsState()
  val stats by viewModel.stats.collectAsState()

  Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
  ) {
    // Tab Row
    PrimaryTabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = primaryColor,
    ) {
      listOf(
        stringResource(R.string.career_tab_profilo),
        stringResource(R.string.career_tab_piano),
        stringResource(R.string.career_tab_esami),
        stringResource(R.string.career_tab_luoghi)
      ).forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = {
            coroutineScope.launch {
              pagerState.animateScrollToPage(index)
            }
          },
          text = {
            Text(
              text = title,
              color = if (selectedTabIndex == index) primaryColor else grayColor,
              fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 14.sp
            )
          }
        )
      }
    }

    // Tab Content
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize()
    ) { page ->
      when (page) {
        0 -> ProfiloTab(sharedTransitionScope, animatedContentScope, user, stats)
        1 -> PlaceholderTab(stringResource(R.string.career_tab_piano))
        2 -> PlaceholderTab(stringResource(R.string.career_tab_esami))
        3 -> PlaceholderTab(stringResource(R.string.career_tab_luoghi))
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfiloTab(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  user: User?,
  stats: CareerStats?,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  var showDialog by remember { mutableStateOf(false) }

  val mediaAritmetica = stats?.mediaAritmetica ?: 0f
  val mediaPonderata = stats?.mediaPonderata ?: 0f
  val esamiSostenuti = stats?.esamiSostenuti ?: 0
  val esamiTotali = stats?.esamiTotali ?: 0
  val cfuAcquisiti = stats?.cfuAcquisiti ?: 0
  val cfuTotali = stats?.cfuTotali ?: 0

  val grades = stats?.grades ?: emptyList()

  val userDataSection: @Composable () -> Unit = @Composable {
    // Data Section
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Section Title
      Text(
        text = stringResource(R.string.career_dati),
        color = primaryColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )

      // Badge
      CreditCard(
        accentColor = primaryColor,
        isChromatic = true,
        frontContent = { x, y, whiteBadge, _ ->
          BadgeFront(
            user,
            textColor = if (whiteBadge) BadgeWhiteDrawableColor else OnBackgroundColor,
            touchX = x,
            touchY = y,
            whiteBadge = whiteBadge,
          )
        },
        backContent = { x, y, whiteBadge, hazeState ->
          BadgeBack(
            user,
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

  val statisticsSection: @Composable () -> Unit = @Composable { // Statistics Section
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Section Title
      Text(
        text = stringResource(R.string.career_statistiche),
        color = primaryColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      // Medie
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Arithmetic Mean
        StatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_media_aritmetica),
          value = String.format(Locale.getDefault(), "%.2f", mediaAritmetica),
          textColor = textColor,
          grayColor = grayColor
        )

        // Weighted Mean
        StatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_media_ponderata),
          value = String.format(Locale.getDefault(), "%.2f", mediaPonderata),
          textColor = textColor,
          grayColor = grayColor
        )
      }

      // Progress
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Esams Done
        ProgressStatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_esami_sostenuti),
          current = esamiSostenuti,
          total = esamiTotali,
          primaryColor = primaryColor,
          textColor = textColor,
          grayColor = grayColor
        )

        // Crediti Acquired
        ProgressStatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_cfu_acquisiti),
          current = cfuAcquisiti,
          total = cfuTotali,
          primaryColor = primaryColor,
          textColor = textColor,
          grayColor = grayColor
        )
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {

    item {
      if (isTablet()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Inside a Row, we might need weights or specific widths,
          // but for now, we just call the functions.
          Box(modifier = Modifier.weight(1f)) { userDataSection() }
          Spacer(modifier = Modifier.width(16.dp))
          Box(modifier = Modifier.weight(1f)) { statisticsSection() }
        }
      } else {
        userDataSection()
        Spacer(modifier = Modifier.height(16.dp))
        statisticsSection()
      }
    }

    // Calculate Average Button
    item {
      Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = primaryColor
        )
      ) {
        Text(
          text = stringResource(R.string.career_calcola_media),
          fontSize = 16.sp
        )
      }
    }

    // Grades Chart
    item {
      Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = stringResource(R.string.career_grafico_voti),
            color = primaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          GradesChart(grades, primaryColor)
        }
      }
    }
  }


  if (showDialog) {
    HypotheticalGradeDialog(
      onDismiss = { showDialog = false },
      currentMediaAritmetica = mediaAritmetica,
      currentMediaPonderata = mediaPonderata,
      currentEsamiSostenuti = esamiSostenuti,
      currentCfuAcquisiti = cfuAcquisiti,
      primaryColor = primaryColor,
      textColor = MaterialTheme.colorScheme.onBackground,
      grayColor = grayColor
    )
  }
}


@Composable
fun StatCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  textColor: Color,
  grayColor: Color,
) {
  val haptic = rememberHapticManager()
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    onClick = {
      haptic.tap()
    },
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = title,
        color = grayColor,
        fontSize = 12.sp,
        maxLines = 2
      )
      Text(
        text = value,
        color = textColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ProgressStatCard(
  modifier: Modifier = Modifier,
  title: String,
  current: Int,
  total: Int,
  primaryColor: Color,
  textColor: Color,
  grayColor: Color,
) {
  val progress = current.toFloat() / total.toFloat()
  val haptic = rememberHapticManager()

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    onClick = {
      haptic.spring()
    },
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = title,
        color = grayColor,
        fontSize = 12.sp,
        maxLines = 2
      )
      Text(
        text = "$current/$total",
        color = textColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )

      // Progress bar
      Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(grayColor.copy(alpha = 0.2f))
      ) {
        Box(
          modifier = Modifier
              .fillMaxWidth(progress)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(primaryColor)
        )
      }
    }
  }
}

@Composable
fun GradesChart(grades: List<Float>, primaryColor: Color) {
  if (grades.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No data available",
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )
    }
    return
  }

  val minGrade = grades.minOrNull() ?: 18f
  val maxGrade = grades.maxOrNull() ?: 30f

  val yAxisMin = minGrade.toDouble()
  val yAxisMax = maxGrade.toDouble()

  val modelProducer = remember { CartesianChartModelProducer() }

  LaunchedEffect(grades) {
    modelProducer.runTransaction {
      lineSeries { series(grades) }
    }
  }

  val textColor = MaterialTheme.colorScheme.onSurface
  val zoom = Zoom.fixed(0.95f)
  val maxZoom = Zoom.fixed(2f)

  CartesianChartHost(
    zoomState = rememberVicoZoomState(
      initialZoom = zoom,
      maxZoom = maxZoom,
    ),
    chart = rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
            pointProvider = LineCartesianLayer.PointProvider.single(
              LineCartesianLayer.Point(
                component = shapeComponent(shape = CorneredShape.Pill, fill = fill(primaryColor)),
                sizeDp = 6f
              )
            )
          )
        ),
        rangeProvider = CartesianLayerRangeProvider.fixed(
          minY = yAxisMin,
          maxY = yAxisMax
        ),
        verticalAxisPosition = null
      ),
      endAxis = VerticalAxis.rememberEnd(
        label = rememberAxisLabelComponent(
          color = textColor,
          margins = Insets(horizontalDp = 8f, verticalDp = 4f)
        ),
        valueFormatter = { _, value, _ ->
          val gradeValue = value.toInt()
          if (gradeValue == 31) "30L" else gradeValue.toString()
        }
      ),
      bottomAxis = HorizontalAxis.rememberBottom(
        label = null
      )
    ),
    modelProducer = modelProducer,
    modifier = Modifier.fillMaxSize()
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HypotheticalGradeDialog(
  onDismiss: () -> Unit,
  currentMediaAritmetica: Float,
  currentMediaPonderata: Float,
  currentEsamiSostenuti: Int,
  currentCfuAcquisiti: Int,
  primaryColor: Color,
  textColor: Color,
  grayColor: Color,
) {
  var voto by remember { mutableStateOf("") }
  var cfu by remember { mutableStateOf("") }

  // Calculate new averages
  val votoValue = voto.toFloatOrNull()
  val cfuValue = cfu.toIntOrNull()

  // New arithmetic average: (sum of all grades + new grade) / (count + 1)
  val newMediaAritmetica = if (votoValue != null && votoValue >= 18) {
    val currentSum = currentMediaAritmetica * currentEsamiSostenuti
    (currentSum + votoValue) / (currentEsamiSostenuti + 1)
  } else null

  // New weighted average: (sum of (grade * cfu) + new grade * new cfu) / (total cfu + new cfu)
  val newMediaPonderata = if (votoValue != null && votoValue >= 18 && cfuValue != null && cfuValue > 0) {
    val currentWeightedSum = currentMediaPonderata * currentCfuAcquisiti
    (currentWeightedSum + votoValue * cfuValue) / (currentCfuAcquisiti + cfuValue)
  } else null

  BasicAlertDialog(
    onDismissRequest = onDismiss,
  ) {
    Card(
      modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      )
    ) {
      Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Title
        Text(
          text = stringResource(R.string.career_dialog_title),
          color = textColor,
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold
        )

        // Averages Section - both current and new in same cards
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Arithmetic Mean Card with difference
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            HypotheticalStatCard(
              title = stringResource(R.string.career_media_aritmetica),
              currentValue = currentMediaAritmetica,
              newValue = newMediaAritmetica,
              textColor = textColor,
              grayColor = grayColor,
              primaryColor = primaryColor
            )
            // Difference underneath
            val diffAritmetica = newMediaAritmetica?.let { it - currentMediaAritmetica }
            DifferenceIndicator(
              difference = diffAritmetica
            )
          }

          // Weighted Mean Card with difference
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            HypotheticalStatCard(
              title = stringResource(R.string.career_media_ponderata),
              currentValue = currentMediaPonderata,
              newValue = newMediaPonderata,
              textColor = textColor,
              grayColor = grayColor,
              primaryColor = primaryColor
            )
            // Difference underneath
            val diffPonderata = newMediaPonderata?.let { it - currentMediaPonderata }
            DifferenceIndicator(
              difference = diffPonderata
            )
          }
        }

        HorizontalDivider(color = grayColor.copy(alpha = 0.2f))

        // Input Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = voto,
            onValueChange = { newValue ->
              if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                voto = newValue
              }
            },
            label = { Text(stringResource(R.string.career_dialog_voto)) },
            placeholder = { Text(">17") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = votoValue != null && votoValue < 18,
            supportingText = if (votoValue != null && votoValue < 18) {
              { Text(stringResource(R.string.career_dialog_voto_error)) }
            } else null,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = cfu,
            onValueChange = { newValue ->
              if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                cfu = newValue
              }
            },
            label = { Text(stringResource(R.string.career_dialog_cfu)) },
            placeholder = { Text(stringResource(R.string.career_dialog_cfu_optional)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text(
              text = stringResource(R.string.career_dialog_chiudi),
              color = textColor
            )
          }
        }
      }
    }
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
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
          .padding(12.dp)
          .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = title,
        color = grayColor,
        fontSize = 11.sp,
        maxLines = 1
      )
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Current value
        Text(
          text = String.format(Locale.getDefault(), "%.2f", currentValue),
          color = textColor,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        // New value
        AnimatedVisibility(
          visible = newValue != null,
          enter = fadeIn() + expandHorizontally(),
          exit = fadeOut() + shrinkHorizontally(),
        ) {
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "→",
              color = grayColor,
              fontSize = 14.sp
            )
            Text(
              text = newValue?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "",
              color = primaryColor,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
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
    contentAlignment = Alignment.Center
  ) {
    AnimatedVisibility(
      visible = difference != null,
      enter = fadeIn(),
      exit = fadeOut()
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
        contentAlignment = Alignment.Center
      ) {
        Text(
          modifier = Modifier.offset(y = (-2.85).dp),
          text = difference?.let {
            String.format(
              Locale.getDefault(),
              "%s%.2f",
              if (it >= 0) "+" else "",
              it
            )
          } ?: "",
          color = chipTextColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun PlaceholderTab(tabName: String) {
  Box(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "$tabName - Coming soon",
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 18.sp
    )
  }
}
