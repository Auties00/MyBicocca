package it.attendance100.mybicocca.ui.screen.main.profile

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.Screen
import it.attendance100.mybicocca.domain.model.GradePoint
import it.attendance100.mybicocca.ui.component.DialogOpenerSettingItem
import it.attendance100.mybicocca.ui.component.badge.BadgeBack
import it.attendance100.mybicocca.ui.component.badge.BadgeFront
import it.attendance100.mybicocca.ui.component.badge.CreditCard
import it.attendance100.mybicocca.ui.component.card.ProgressStatCard
import it.attendance100.mybicocca.ui.component.card.StatCard
import it.attendance100.mybicocca.ui.screen.main.career.CareerViewModel
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.GrayColor
import it.attendance100.mybicocca.ui.theme.MyBicoccaDarkColorScheme
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import it.attendance100.mybicocca.util.rememberPreferencesManager
import java.util.Locale


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfiloScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel: CareerViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val grayColor = GrayColor()

    var showDialog by remember { mutableStateOf(false) }

    val mediaAritmetica = stats?.mediaAritmetica ?: 0f
    val mediaPonderata = stats?.mediaPonderata ?: 0f
    val esamiSostenuti = stats?.esamiSostenuti ?: 0
    val cfuAcquisiti = stats?.cfuAcquisiti ?: 0
    val cfuTotali = stats?.cfuTotali ?: 0

    val preferencesManager = rememberPreferencesManager()

    val progressBarToggle = preferencesManager.progressBarToggle

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
                )

                // Weighted Mean
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.career_media_ponderata),
                    value = String.format(Locale.getDefault(), "%.2f", mediaPonderata),
                    textColor = textColor,
                )
            }

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Esams Done
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.career_esami_sostenuti),
                    value = esamiSostenuti.toString(),
                    textColor = textColor,
                )

                // Crediti Acquired
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.career_cfu_acquisiti),
                    current = cfuAcquisiti,
                    total = cfuTotali,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    backgroundProgressBar = progressBarToggle,
                    progressbar = !progressBarToggle
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_screen)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.arrow_back),
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            item {
                userDataSection()
                Spacer(modifier = Modifier.height(16.dp))
                statisticsSection()
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
                Column {
                    DialogOpenerSettingItem(
                        title = stringResource(R.string.profile_esami),
                        subtitle = null,
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = { navController.navigate(Screen.Esami.route) },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.career_grafico_voti),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        GradesChart(grades, primaryColor)
                    }
                }
            }
        }
    }


    if (showDialog) {
        HypotheticalGradeDialog(

            onDismiss = {
                @Suppress("AssignedValueIsNeverRead")
                showDialog = false
            },
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
fun GradesChart(grades: List<GradePoint>, primaryColor: Color) {
    if (grades.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.career_no_data_available),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    val values = grades.map { it.value }
    val minGrade = values.minOrNull() ?: 18f
    val maxGrade = values.maxOrNull() ?: 30f

    val yAxisMin = minGrade.toDouble()
    val yAxisMax = maxGrade.toDouble()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(grades) {
        modelProducer.runTransaction {
            lineSeries { series(values) }
        }
    }

    val textColor = MaterialTheme.colorScheme.onSurface
    val zoom = Zoom.fixed(0.1f)

    CartesianChartHost(
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = zoom
        ),
        scrollState = rememberVicoScrollState(),
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                component = shapeComponent(
                                    shape = CorneredShape.Pill,
                                    fill = fill(primaryColor)
                                ),
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
    val newMediaPonderata =
        if (votoValue != null && votoValue >= 18 && cfuValue != null && cfuValue > 0) {
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
                        placeholder = { Text(stringResource(R.string.career_dialog_voto_placeholder)) },
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
                            text = stringResource(R.string.career_arrow_symbol),
                            color = grayColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = newValue?.let { String.format(Locale.getDefault(), "%.2f", it) }
                                ?: "",
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun GradesChartDarkPreview() {
    val grades = listOf(
        GradePoint(
            cfu = "8.0",
            value = 30f,
            date = "2023-10-25",
            name = "Test Grade 1",
            isLode = false,
        ),
    )
    Box(
        modifier = Modifier.size(400.dp, 350.dp),
    ) {
        MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
            GradesChart(grades, MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun GradesChartLightPreview() {
    val grades = UserMockData.careerStats.grades
    Box(
        modifier = Modifier.size(400.dp, 350.dp),
    ) {
        MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
            GradesChart(grades, MaterialTheme.colorScheme.primary)
        }
    }
}
