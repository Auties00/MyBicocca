package it.unimib.indoornav.lab.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import it.unimib.indoornav.lab.BuildConfig
import it.unimib.indoornav.lab.LabApp
import it.unimib.indoornav.lab.MainActivity
import it.unimib.indoornav.lab.R
import it.unimib.indoornav.lab.capture.AccelerometerCapture
import it.unimib.indoornav.lab.capture.AccelerometerRawCapture
import it.unimib.indoornav.lab.capture.BarometerCapture
import it.unimib.indoornav.lab.capture.BleCapture
import it.unimib.indoornav.lab.capture.EventsCapture
import it.unimib.indoornav.lab.capture.GameRotationCapture
import it.unimib.indoornav.lab.capture.GnssCapture
import it.unimib.indoornav.lab.capture.GyroscopeCapture
import it.unimib.indoornav.lab.capture.GyroscopeRawCapture
import it.unimib.indoornav.lab.capture.MagnetometerCapture
import it.unimib.indoornav.lab.capture.MagnetometerRawCapture
import it.unimib.indoornav.lab.capture.MarksCapture
import it.unimib.indoornav.lab.capture.RotationCapture
import it.unimib.indoornav.lab.capture.SensorAccuracyCapture
import it.unimib.indoornav.lab.capture.SessionWriter
import it.unimib.indoornav.lab.capture.StepsCapture
import it.unimib.indoornav.lab.capture.StreamCapture
import it.unimib.indoornav.lab.capture.WifiCapture
import it.unimib.indoornav.lab.capture.WifiRttCapture
import it.unimib.indoornav.lab.data.DeviceCapabilities
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.format.ClockInfo
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.RouteCheckpoints
import it.unimib.indoornav.lab.format.SessionManifest
import it.unimib.indoornav.lab.format.SessionNaming
import it.unimib.indoornav.lab.format.SetupManifestInfo
import it.unimib.indoornav.lab.format.StreamStats
import it.unimib.indoornav.lab.format.TestIdCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class RecordingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val fileIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fileIoMutex = Mutex()
    private var lastStartId: Int = 0

    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var writer: SessionWriter? = null
    private var eventsCapture: EventsCapture? = null
    private var marksCapture: MarksCapture? = null
    private var prompter: VoicePrompter? = null
    private var protocol: GuidedProtocolController? = null

    private val captures = mutableListOf<StreamCapture>()
    private val activeStreamKeys = linkedSetOf<String>()
    private val markStack = ArrayDeque<MarkHistoryItem>()

    private var currentSessionDir: File? = null
    private var initialManifest: SessionManifest? = null
    private var currentTemplate: ExperimentTemplate = ExperimentTemplate.E5
    private var currentConfig: SetupConfig = SetupConfig()
    private var startElapsedMs: Long = 0L
    private var tickerJob: Job? = null
    private var protocolObserverJob: Job? = null
    private var isStopping = false
    private var isRunning = false
    private var nextCheckpointIndex = 0
    private var currentRouteName = ""
    private var warned20Min = false

    private val app: LabApp
        get() = application as LabApp

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId
        when (intent?.action) {
            ACTION_START -> if (!isRunning) beginSession()
            ACTION_STOP -> requestFinish()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (isRunning && !isStopping) {
            finalizeSession(stoppedCleanly = false)
        }
        app.recordingCoordinator.activeService = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun beginSession() {
        isRunning = true
        isStopping = false
        warned20Min = false
        nextCheckpointIndex = 0
        markStack.clear()

        val rawConfig = app.preferences.config.value
        val tmpl = ExperimentTemplate.fromCode(rawConfig.templateCode) ?: ExperimentTemplate.E5
        currentTemplate = tmpl
        val effectiveRecordingMode = if (rawConfig.recordingMode in tmpl.allowedRecordingModes) {
            rawConfig.recordingMode
        } else {
            tmpl.defaultRecordingMode
        }
        val effectiveFields = rawConfig.templateFields.toMutableMap()
        val effectiveCarryMode = if (tmpl.hasModeField()) {
            val resolved = effectiveFields["mode"]?.trim()?.takeIf { it.isNotEmpty() }
                ?: rawConfig.carryMode.takeIf { it.isNotBlank() }
                ?: tmpl.defaultCarryMode
            effectiveFields["mode"] = resolved
            resolved
        } else {
            rawConfig.carryMode.ifBlank { tmpl.defaultCarryMode }
        }
        val config = rawConfig.copy(
            carryMode = effectiveCarryMode,
            recordingMode = effectiveRecordingMode,
            templateFields = effectiveFields,
        )
        currentConfig = config
        currentRouteName = effectiveFields["route"]?.trim().orEmpty()

        val wallMs = System.currentTimeMillis()
        val elapsedNs = SystemClock.elapsedRealtimeNanos()
        startElapsedMs = SystemClock.elapsedRealtime()

        val testId = TestIdCodec.build(currentTemplate, config.templateFields)
        val folderName = SessionNaming.buildFolderName(wallMs, config.phoneLabel, testId)
        val sessionDir = File(app.sessionRepository.sessionsRootDir(), folderName)
        currentSessionDir = sessionDir

        app.recordingCoordinator.activeService = this

        val notif = buildNotification("Starting $testId", "00:00")
        startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lab:RecordingWakeLock").apply {
            acquire(6 * 60 * 60 * 1000L)
        }

        val thread = HandlerThread("LabSensorThread").apply { start() }
        sensorThread = thread
        val handler = Handler(thread.looper)
        sensorHandler = handler

        val sessionWriter = SessionWriter(sessionDir)
        writer = sessionWriter
        app.recordingCoordinator.activeWriter = sessionWriter

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val capSnapshot = DeviceCapabilities.inspectCapabilities(this)

        val events = EventsCapture(this, handler, sessionWriter)
        eventsCapture = events
        val marks = MarksCapture(sessionWriter)
        marksCapture = marks

        val voicePrompter = app.voicePrompter
        prompter = voicePrompter

        val controller = GuidedProtocolController(
            scope = serviceScope,
            prompter = voicePrompter,
            logPhase = { phase -> events.logEvent("phase", phase) },
            onStopSession = {
                finalizeSession(stoppedCleanly = true)
            },
        )
        protocol = controller

        captures.clear()
        activeStreamKeys.clear()

        if (config.recordingMode == "marker_only") {
            val accOnly = AccelerometerCapture(
                sensorManager = sensorManager,
                sensorHandler = handler,
                writer = sessionWriter,
                accuracyCapture = null,
                onSample = { _, _, _, _, norm -> controller.onAccelerometerSample(norm) },
            )
            captures.addAll(listOf(events, marks, accOnly))
        } else {
            val accuracyCapture = SensorAccuracyCapture(sessionWriter)
            val accWithAccuracy = AccelerometerCapture(
                sensorManager = sensorManager,
                sensorHandler = handler,
                writer = sessionWriter,
                accuracyCapture = accuracyCapture,
                onSample = { _, _, _, _, norm -> controller.onAccelerometerSample(norm) },
            )
            val rttCapture = WifiRttCapture(this, handler, sessionWriter)
            val wifiCapture = WifiCapture(
                context = this,
                handler = handler,
                writer = sessionWriter,
                eventsCapture = events,
                universitySsids = config.universitySsids.toSet(),
                onRespondersUpdated = { responders -> rttCapture.updateResponders(responders) },
            )
            val bleCapture = BleCapture(
                context = this,
                handler = handler,
                writer = sessionWriter,
                eventsCapture = events,
                inventoryMode = config.bleInventoryMode,
                storeEverything = config.bleStoreEverything,
            )

            captures.addAll(
                listOf(
                    events,
                    marks,
                    accuracyCapture,
                    accWithAccuracy,
                    AccelerometerRawCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    GyroscopeCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    GyroscopeRawCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    MagnetometerCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    MagnetometerRawCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    BarometerCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    GameRotationCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    RotationCapture(sensorManager, handler, sessionWriter, accuracyCapture),
                    StepsCapture(this, sensorManager, handler, sessionWriter, accuracyCapture),
                    bleCapture,
                    wifiCapture,
                    rttCapture,
                    GnssCapture(this, thread.looper, sessionWriter),
                ),
            )
        }

        for (capture in captures) {
            if (capture.start()) {
                activeStreamKeys.add(capture.streamKey)
            }
        }

        val filteredSensors = if (config.recordingMode == "marker_only") {
            capSnapshot.toManifestSensors().filter { it.type == "accelerometer" }
        } else {
            capSnapshot.toManifestSensors()
        }

        val manifest = SessionManifest(
            app_version = BuildConfig.VERSION_NAME,
            phone = DeviceCapabilities.phoneInfo(config.phoneLabel),
            sensors = filteredSensors,
            capabilities = capSnapshot.capabilities,
            setup = SetupManifestInfo(
                operator = config.operator,
                marker = config.marker,
                building = config.building,
                floor = config.floor,
                test_id = testId,
                carry_mode = config.carryMode,
                recording_mode = config.recordingMode,
                template_fields = config.templateFields,
                notes = config.notes,
            ),
            clock = ClockInfo(wall_ms = wallMs, elapsed_ns = elapsedNs),
            university_ssids = config.universitySsids,
            streams = emptyMap(),
            errors = emptyList(),
            stopped_cleanly = false,
        )
        initialManifest = manifest
        fileIoScope.launch {
            fileIoMutex.withLock {
                writeManifest(sessionDir, manifest)
            }
        }

        val defaultTouchLock = config.recordingMode == "follower" ||
            config.carryMode == "pocket" ||
            config.carryMode == "bag" ||
            config.carryMode == "body"

        app.recordingCoordinator.updateState {
            RecordingUiState(
                isRecording = true,
                recordingMode = config.recordingMode,
                carryMode = config.carryMode,
                templateCode = currentTemplate.code,
                testId = testId,
                sessionFolderName = folderName,
                elapsedSec = 0L,
                twentyMinuteWarning = false,
                streamCounters = sessionWriter.snapshotCounters(activeStreamKeys),
                routeName = currentRouteName,
                nextCheckpointIndex = 0,
                lastMark = null,
                touchLocked = defaultTouchLock,
                allowScreenOff = config.allowScreenOff,
                inventoryMode = config.bleInventoryMode,
                pendingAfterWalk = null,
            )
        }

        protocolObserverJob = serviceScope.launch {
            controller.status.collectLatest { pStatus ->
                app.recordingCoordinator.updateState { it.copy(protocolStatus = pStatus) }
            }
        }

        when (config.recordingMode) {
            "guided", "marker_only" -> controller.startGuided()
            "follower" -> controller.startFollower()
            "free" -> controller.startFree(isStandMode = isStandSession())
            else -> controller.startGuided()
        }

        tickerJob = serviceScope.launch {
            while (isRunning) {
                delay(500L)
                val elapsedSec = (SystemClock.elapsedRealtime() - startElapsedMs) / 1_000L
                val warn20 = elapsedSec >= 20 * 60L
                if (warn20 && !warned20Min) {
                    warned20Min = true
                    voicePrompter.doubleBeep()
                }
                val counters = sessionWriter.snapshotCounters(activeStreamKeys)
                app.recordingCoordinator.updateState {
                    it.copy(
                        elapsedSec = elapsedSec,
                        twentyMinuteWarning = warn20,
                        streamCounters = counters,
                    )
                }
                updateNotification(testId, formatElapsed(elapsedSec), warn20)
            }
        }
    }

    private fun isStandSession(): Boolean =
        currentConfig.carryMode == "stand" || currentTemplate.code in ExperimentTemplate.STAND_TEMPLATE_CODES

    fun requestFinish() {
        if (!isRunning || isStopping) return
        val ctrl = protocol ?: return
        when (currentConfig.recordingMode) {
            "guided", "marker_only" -> ctrl.finishGuided()
            "free" -> ctrl.finishImmediate(isFreeStand = isStandSession())
            else -> ctrl.finishImmediate(isFreeStand = false)
        }
    }

    fun requestImmediateStop() {
        if (!isRunning || isStopping) return
        protocol?.cancel()
        if (currentConfig.recordingMode == "free" && isStandSession()) {
            eventsCapture?.logEvent("phase", "back")
        }
        eventsCapture?.logEvent("phase", "stop")
        val cleanly = currentConfig.recordingMode != "guided" && currentConfig.recordingMode != "marker_only"
        finalizeSession(stoppedCleanly = cleanly)
    }

    fun recordCheckpointMark(source: String) {
        if (!isRunning) return
        // A mark past the route's last checkpoint has no position, and would confuse the ground truth.
        if (RouteCheckpoints.isComplete(currentRouteName, nextCheckpointIndex)) return
        val label = RouteCheckpoints.labelAt(currentRouteName, nextCheckpointIndex)
        marksCapture?.recordMark("checkpoint", label, source)
        val item = MarkHistoryItem("checkpoint", label, source)
        markStack.addLast(item)
        nextCheckpointIndex++
        app.recordingCoordinator.updateState {
            it.copy(nextCheckpointIndex = nextCheckpointIndex, lastMark = item)
        }
    }

    fun recordCustomMark(kind: String, label: String, source: String) {
        if (!isRunning) return
        marksCapture?.recordMark(kind, label, source)
        val item = MarkHistoryItem(kind, label, source)
        markStack.addLast(item)
        app.recordingCoordinator.updateState { it.copy(lastMark = item) }
    }

    fun undoLastMark() {
        if (!isRunning || markStack.isEmpty()) return
        val undone = markStack.removeLast()
        marksCapture?.recordMark("undo", undone.label.ifBlank { undone.kind }, "button")
        if (undone.kind == "checkpoint" && nextCheckpointIndex > 0) {
            nextCheckpointIndex--
        }
        app.recordingCoordinator.updateState {
            it.copy(
                nextCheckpointIndex = nextCheckpointIndex,
                lastMark = markStack.lastOrNull(),
            )
        }
    }

    fun logAppForegroundState(inForeground: Boolean) {
        if (!isRunning) return
        eventsCapture?.logEvent(if (inForeground) "app_foreground" else "app_background")
    }

    private fun finalizeSession(stoppedCleanly: Boolean) {
        if (isStopping) return
        isStopping = true
        isRunning = false

        tickerJob?.cancel()
        protocolObserverJob?.cancel()
        protocol?.cancel()

        for (capture in captures.asReversed()) {
            runCatching { capture.stop() }
        }
        captures.clear()

        prompter?.stopSpeaking()
        prompter = null

        sensorThread?.quitSafely()
        sensorThread = null
        sensorHandler = null

        runCatching {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        }
        wakeLock = null

        val sessionWriter = writer
        writer = null
        val dir = currentSessionDir
        currentSessionDir = null
        val manifest = initialManifest
        initialManifest = null
        val startId = lastStartId

        val hasAfterWalk = currentTemplate.fields.any { it.afterWalk }
        val pendingPrompt = if (stoppedCleanly && hasAfterWalk && dir != null &&
            (currentConfig.recordingMode == "guided" || currentConfig.recordingMode == "marker_only")
        ) {
            PendingAfterWalkPrompt(dir, currentTemplate, currentConfig.templateFields)
        } else {
            null
        }

        app.recordingCoordinator.activeService = null
        app.recordingCoordinator.updateState {
            RecordingUiState(isRecording = false)
        }

        fileIoScope.launch {
            fileIoMutex.withLock {
                val stats: Map<String, StreamStats> = sessionWriter?.close() ?: emptyMap()
                if (app.recordingCoordinator.activeWriter === sessionWriter) {
                    app.recordingCoordinator.activeWriter = null
                }
                val errors = sessionWriter?.getErrors() ?: emptyList()
                if (dir != null && manifest != null) {
                    val finalManifest = manifest.copy(
                        streams = stats,
                        errors = errors,
                        stopped_cleanly = stoppedCleanly,
                    )
                    writeManifest(dir, finalManifest)
                }
            }
            withContext(Dispatchers.Main) {
                if (!isRunning) {
                    if (pendingPrompt != null) {
                        app.recordingCoordinator.updateState {
                            it.copy(pendingAfterWalk = pendingPrompt)
                        }
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelfResult(startId)
                }
            }
        }
    }

    private fun writeManifest(sessionDir: File, manifest: SessionManifest) {
        runCatching {
            sessionDir.mkdirs()
            File(sessionDir, "manifest.json").writeText(manifest.toJsonString())
        }
    }

    private fun ensureNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.recording_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.recording_channel_desc)
        }
        nm.createNotificationChannel(channel)
    }

    private fun updateNotification(testId: String, elapsed: String, warn20: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = if (warn20) "Recording (>20m!): $testId" else "Recording: $testId"
        nm.notify(NOTIFICATION_ID, buildNotification(title, elapsed))
    }

    private fun buildNotification(title: String, elapsed: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RecordingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lab_notification)
            .setContentTitle(title)
            .setContentText("Elapsed: $elapsed")
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun formatElapsed(sec: Long): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    companion object {
        const val ACTION_START = "it.unimib.indoornav.lab.action.START"
        const val ACTION_STOP = "it.unimib.indoornav.lab.action.STOP"
        private const val CHANNEL_ID = "lab_recording_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
