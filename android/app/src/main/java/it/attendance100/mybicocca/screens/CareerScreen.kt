package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import it.attendance100.mybicocca.ui.theme.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CareerScreen(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val primaryColor = MaterialTheme.colorScheme.primary
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
  ) {
    // Tab Row
    SecondaryScrollableTabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = primaryColor,
      edgePadding = 0.dp,
      indicator = {
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
          color = primaryColor
        )
      }
    ) {
      listOf(
        stringResource(R.string.career_tab_profilo),
        stringResource(R.string.career_tab_piano),
        stringResource(R.string.career_tab_esami),
        stringResource(R.string.career_tab_luoghi)
      ).forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = { selectedTabIndex = index },
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
    when (selectedTabIndex) {
      0 -> ProfiloTab(sharedTransitionScope, animatedContentScope)
      1 -> PlaceholderTab(stringResource(R.string.career_tab_piano))
      2 -> PlaceholderTab(stringResource(R.string.career_tab_esami))
      3 -> PlaceholderTab(stringResource(R.string.career_tab_luoghi))
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfiloTab(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  var showDialog by remember { mutableStateOf(false) }

  // Sample data - replace with actual data
  val name = "Mario"
  val surname = "Rossi"
  val matricola = "123456"
  val corso = "Informatica"
  val anno = "3"
  val email = "m.rossi@campus.unimib.it"

  val mediaAritmetica = 27.5f
  val mediaPonderata = 28.2f
  val esamiSostenuti = 18
  val esamiTotali = 24
  val cfuAcquisiti = 144
  val cfuTotali = 180

  // Sample grades for chart
  val grades = listOf(28f, 30f, 26f, 29f, 27f, 30f, 28f, 25f, 29f, 30f, 27f, 28f, 30f, 26f, 29f, 27f, 28f, 30f, 31f, 19f)

  Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // Dati Section
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = stringResource(R.string.career_dati),
        color = primaryColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          InfoRow(stringResource(R.string.career_name), name, textColor, grayColor)
          InfoRow(stringResource(R.string.career_surname), surname, textColor, grayColor)
          InfoRow(stringResource(R.string.career_matricola), matricola, textColor, grayColor)
          InfoRow(stringResource(R.string.career_corso), corso, textColor, grayColor)
          InfoRow(stringResource(R.string.career_anno), anno, textColor, grayColor)
          InfoRow(stringResource(R.string.career_email), email, textColor, grayColor)
        }
      }
    }

    // Statistiche Section
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = stringResource(R.string.career_statistiche),
        color = primaryColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Media Aritmetica
        StatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_media_aritmetica),
          value = String.format(java.util.Locale.getDefault(), "%.2f", mediaAritmetica),
          textColor = textColor,
          grayColor = grayColor
        )

        // Media Ponderata
        StatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_media_ponderata),
          value = String.format(java.util.Locale.getDefault(), "%.2f", mediaPonderata),
          textColor = MaterialTheme.colorScheme.onBackground,
          grayColor = grayColor
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Esami Sostenuti
        ProgressStatCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.career_esami_sostenuti),
          current = esamiSostenuti,
          total = esamiTotali,
          primaryColor = primaryColor,
          textColor = textColor,
          grayColor = grayColor
        )

        // CFU Acquisiti
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

    // Calcola Media Button
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

    // Grades Chart
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

  if (showDialog) {
    HypotheticalGradeDialog(
      onDismiss = { showDialog = false },
      onCalculate = { _, _ ->
        // Calculate new averages
        showDialog = false
      },
      primaryColor = primaryColor,
      textColor = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
fun InfoRow(
  label: String,
  value: String,
  textColor: Color,
  grayColor: Color,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      color = grayColor,
      fontSize = 14.sp
    )
    Text(
      text = value,
      color = textColor,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium
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
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
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

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
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

@Composable
fun HypotheticalGradeDialog(
  onDismiss: () -> Unit,
  onCalculate: (Int, Int) -> Unit,
  primaryColor: Color,
  textColor: Color,
) {
  var voto by remember { mutableStateOf("") }
  var cfu by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    title = {
      Text(
        text = stringResource(R.string.career_dialog_title),
        color = textColor
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = voto,
          onValueChange = { voto = it },
          label = { Text(stringResource(R.string.career_dialog_voto)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = cfu,
          onValueChange = { cfu = it },
          label = { Text(stringResource(R.string.career_dialog_cfu)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val votoInt = voto.toIntOrNull()
          val cfuInt = cfu.toIntOrNull()
          if (votoInt != null && cfuInt != null) {
            onCalculate(votoInt, cfuInt)
          }
        }
      ) {
        Text(stringResource(R.string.career_dialog_calcola), color = primaryColor)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.career_dialog_annulla), color = textColor)
      }
    }
  )
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
