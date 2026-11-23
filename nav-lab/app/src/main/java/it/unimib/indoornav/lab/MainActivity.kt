package it.unimib.indoornav.lab

import android.os.Bundle
import android.widget.Toast
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.unimib.indoornav.lab.data.DeviceCapabilities
import it.unimib.indoornav.lab.data.WifiThrottleCheckState
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.inventory.BeaconGrouping
import it.unimib.indoornav.lab.ui.InventoryScreen
import it.unimib.indoornav.lab.ui.RecordingSummarySheet
import it.unimib.indoornav.lab.ui.LiveRecordingScreen
import it.unimib.indoornav.lab.ui.PermissionsHelper
import it.unimib.indoornav.lab.ui.SessionsScreen
import it.unimib.indoornav.lab.ui.SetupScreen
import it.unimib.indoornav.lab.ui.SurveyScreen
import it.unimib.indoornav.lab.ui.theme.LabTheme

class MainActivity : ComponentActivity() {

    private val app: LabApp
        get() = application as LabApp

    private var throttleState by mutableStateOf(WifiThrottleCheckState.UNTESTED)
    private var throttleDetail by mutableStateOf("Tap Test to run 5-scan check")
    private var readinessTrigger by mutableStateOf(0)
    private var volUpDownTimeMs: Long = 0L
    private var volUpHandledLongPress: Boolean = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        readinessTrigger++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF0B0808.toInt()),
            navigationBarStyle = SystemBarStyle.dark(0xFF140A0C.toInt()),
        )

        setContent {
            LabTheme {
                val navController = rememberNavController()
                val config by app.preferences.config.collectAsState()
                val recordingState by app.recordingCoordinator.uiState.collectAsState()
                val surveyState by app.surveyManager.state.collectAsState()
                val capabilities = remember { DeviceCapabilities.inspectCapabilities(this) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                readinessTrigger++
                                app.recordingCoordinator.onAppForegroundChanged(true)
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                app.recordingCoordinator.onAppForegroundChanged(false)
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                val keepScreenOn = (recordingState.isRecording &&
                    (!recordingState.allowScreenOff || recordingState.inventoryMode)) ||
                    surveyState.beaconScanning
                val dimScreen = recordingState.isRecording && !recordingState.allowScreenOff

                LaunchedEffect(keepScreenOn, dimScreen) {
                    if (keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    val attrs = window.attributes
                    attrs.screenBrightness = if (dimScreen) 0.01f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    window.attributes = attrs
                }

                val readiness = remember(readinessTrigger, throttleState, throttleDetail) {
                    val missing = PermissionsHelper.missingPermissions(this)
                    DeviceCapabilities.readReadiness(this, missing, throttleState, throttleDetail)
                }

                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route ?: "record"

                LaunchedEffect(currentRoute) {
                    if (currentRoute != "survey" && currentRoute != "inventory") {
                        app.surveyManager.release()
                    }
                }

                LaunchedEffect(surveyState.allBeacons) {
                    if (surveyState.allBeacons.isNotEmpty()) {
                        val grouped = BeaconGrouping.groupByPhysicalBeacon(surveyState.allBeacons)
                        app.beaconInventoryRepository.updateLiveTelemetry(grouped)
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (!recordingState.isRecording) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "record",
                                    onClick = {
                                        navController.navigate("record") {
                                            popUpTo("record") { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.FiberManualRecord, contentDescription = null) },
                                    label = { Text("Record") },
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "survey",
                                    onClick = {
                                        navController.navigate("survey") {
                                            popUpTo("record")
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Radar, contentDescription = null) },
                                    label = { Text("Survey") },
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "inventory",
                                    onClick = {
                                        navController.navigate("inventory") {
                                            popUpTo("record")
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                                    label = { Text("Inventory") },
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "sessions",
                                    onClick = {
                                        navController.navigate("sessions") {
                                            popUpTo("record")
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                    label = { Text("Sessions") },
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        if (recordingState.isRecording) {
                            LiveRecordingScreen(
                                state = recordingState,
                                onCheckpoint = { app.recordingCoordinator.markCheckpoint("button") },
                                onUndoMark = { app.recordingCoordinator.undoLastMark() },
                                onEventMark = { kind, label ->
                                    app.recordingCoordinator.markEvent(kind, label, "button")
                                },
                                onFinish = { app.recordingCoordinator.requestFinish() },
                                onToggleTouchLock = { app.recordingCoordinator.setTouchLocked(it) },
                                onToggleAllowScreenOff = { app.recordingCoordinator.setAllowScreenOff(it) },
                            )
                        } else {
                            NavHost(
                                navController = navController,
                                startDestination = "record",
                            ) {
                                composable("record") {
                                    SetupScreen(
                                        config = config,
                                        readiness = readiness,
                                        capabilities = capabilities,
                                        onUpdateConfig = { app.preferences.update(it) },
                                        onRequestPermissions = {
                                            val missing = PermissionsHelper.missingPermissions(this@MainActivity)
                                            if (missing.isNotEmpty()) {
                                                permissionLauncher.launch(missing.toTypedArray())
                                            }
                                        },
                                        onCheckWifiThrottle = {
                                            app.surveyManager.checkWifiThrottling { s, d ->
                                                throttleState = s
                                                throttleDetail = d
                                            }
                                        },
                                        onRefreshReadiness = { readinessTrigger++ },
                                        onStartRecording = {
                                            app.surveyManager.release()
                                            app.recordingCoordinator.startRecording(config)
                                        },
                                    )
                                }
                                composable("survey") {
                                    SurveyScreen(
                                        surveyState = surveyState,
                                        config = config,
                                        surveyManager = app.surveyManager,
                                        inventoryRepository = app.beaconInventoryRepository,
                                        onUpdateConfig = { app.preferences.update(it) },
                                    )
                                }
                                composable("inventory") {
                                    InventoryScreen(
                                        surveyState = surveyState,
                                        config = config,
                                        isRecording = recordingState.isRecording,
                                        surveyManager = app.surveyManager,
                                        inventoryRepository = app.beaconInventoryRepository,
                                        sessionRepository = app.sessionRepository,
                                    )
                                }
                                composable("sessions") {
                                    SessionsScreen(repository = app.sessionRepository)
                                }
                            }
                        }
                    }

                    recordingState.pendingSummary?.let { summary ->
                        val showResult: (String) -> Unit = { msg ->
                            runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show() }
                        }
                        RecordingSummarySheet(
                            summary = summary,
                            onSave = { edits -> app.recordingCoordinator.applySummaryEdits(edits, showResult) },
                            onDelete = { app.recordingCoordinator.deleteSummarySession(showResult) },
                            onDismiss = { app.recordingCoordinator.dismissSummary() },
                        )
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val recState = app.recordingCoordinator.uiState.value
        if (recState.isRecording && !recState.touchLocked && recState.recordingMode != "follower") {
            val isStandFree = recState.recordingMode == "free" &&
                recState.templateCode in ExperimentTemplate.STAND_TEMPLATE_CODES
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && !isStandFree) {
                if (event?.repeatCount == 0) {
                    app.recordingCoordinator.markCheckpoint("volume_key")
                }
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (event?.repeatCount == 0) {
                    volUpDownTimeMs = android.os.SystemClock.elapsedRealtime()
                    volUpHandledLongPress = false
                } else if (!volUpHandledLongPress &&
                    volUpDownTimeMs > 0L &&
                    android.os.SystemClock.elapsedRealtime() - volUpDownTimeMs >= 2_000L
                ) {
                    volUpHandledLongPress = true
                    app.recordingCoordinator.requestFinish()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val recState = app.recordingCoordinator.uiState.value
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val handle = recState.isRecording && !recState.touchLocked && recState.recordingMode != "follower"
            volUpDownTimeMs = 0L
            volUpHandledLongPress = false
            if (handle) return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        if (isFinishing) {
            app.surveyManager.release()
        }
        super.onDestroy()
    }
}
