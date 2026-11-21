package it.unimib.indoornav.lab.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import it.unimib.indoornav.lab.capture.LiveStreamCounter
import it.unimib.indoornav.lab.capture.SessionWriter
import it.unimib.indoornav.lab.data.SetupConfig
import it.unimib.indoornav.lab.format.ExperimentTemplate
import it.unimib.indoornav.lab.format.RouteCheckpoints
import it.unimib.indoornav.lab.format.SessionManifest
import it.unimib.indoornav.lab.format.TestIdCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class MarkHistoryItem(
    val kind: String,
    val label: String,
    val source: String,
)

data class PendingAfterWalkPrompt(
    val sessionDir: File,
    val template: ExperimentTemplate,
    val currentFields: Map<String, String>,
)

data class RecordingUiState(
    val isRecording: Boolean = false,
    val recordingMode: String = "guided",
    val carryMode: String = "hand-up",
    val templateCode: String = "",
    val testId: String = "",
    val sessionFolderName: String = "",
    val elapsedSec: Long = 0L,
    val twentyMinuteWarning: Boolean = false,
    val protocolStatus: ProtocolStatus = ProtocolStatus(),
    val streamCounters: List<LiveStreamCounter> = emptyList(),
    val routeName: String = "",
    val nextCheckpointIndex: Int = 0,
    val lastMark: MarkHistoryItem? = null,
    val touchLocked: Boolean = false,
    val allowScreenOff: Boolean = false,
    val inventoryMode: Boolean = false,
    val pendingAfterWalk: PendingAfterWalkPrompt? = null,
) {
    val nextCheckpointLabel: String
        get() = RouteCheckpoints.labelAt(routeName, nextCheckpointIndex)

    val routeComplete: Boolean
        get() = RouteCheckpoints.isComplete(routeName, nextCheckpointIndex)
}

class RecordingCoordinator(private val context: Context) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    @Volatile
    internal var activeService: RecordingService? = null

    @Volatile
    internal var activeWriter: SessionWriter? = null

    fun emergencyFlushActiveWriter() {
        activeWriter?.emergencyFlush()
    }

    fun startRecording(config: SetupConfig) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun requestFinish() {
        activeService?.requestFinish()
    }

    fun requestImmediateStop() {
        activeService?.requestImmediateStop()
    }

    fun markCheckpoint(source: String) {
        activeService?.recordCheckpointMark(source)
    }

    fun undoLastMark() {
        activeService?.undoLastMark()
    }

    fun markEvent(kind: String, label: String = "", source: String = "button") {
        activeService?.recordCustomMark(kind, label, source)
    }

    fun setTouchLocked(locked: Boolean) {
        _uiState.value = _uiState.value.copy(touchLocked = locked)
    }

    fun setAllowScreenOff(allow: Boolean) {
        _uiState.value = _uiState.value.copy(allowScreenOff = allow)
    }

    fun onAppForegroundChanged(inForeground: Boolean) {
        activeService?.logAppForegroundState(inForeground)
    }

    fun updateState(transform: (RecordingUiState) -> RecordingUiState) {
        _uiState.value = transform(_uiState.value)
    }

    fun applyAfterWalkFields(values: Map<String, String>) {
        val pending = _uiState.value.pendingAfterWalk ?: return
        _uiState.value = _uiState.value.copy(pendingAfterWalk = null)
        val manifestFile = File(pending.sessionDir, "manifest.json")
        ioScope.launch {
            if (manifestFile.exists()) {
                runCatching {
                    val old = SessionManifest.fromJsonString(manifestFile.readText())
                    val mergedFields = old.setup.template_fields.toMutableMap().apply {
                        putAll(values.filterValues { it.isNotBlank() })
                    }
                    val newTestId = TestIdCodec.build(pending.template, mergedFields)
                    val updated = old.copy(
                        setup = old.setup.copy(
                            test_id = newTestId,
                            template_fields = mergedFields,
                        ),
                    )
                    manifestFile.writeText(updated.toJsonString())
                }
            }
        }
    }

    fun dismissAfterWalkPrompt() {
        _uiState.value = _uiState.value.copy(pendingAfterWalk = null)
    }
}
